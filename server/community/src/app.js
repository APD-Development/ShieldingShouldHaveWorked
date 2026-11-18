// APD Community server: one Cloudflare Worker, D1 database.
// Everything is JSON over HTTPS. No private messages exist. The client is untrusted: every rule is enforced here.
//
// env.DB      D1 database (or the Node test adapter with the same prepare/bind/first/all/run API)
// env.MAILER  { send(to, subject, text) } for verification codes
// env.GOOGLE  { verify(idToken) -> {sub, email} | null } checks a Google ID token
// env.ADMIN_EMAILS  comma-separated emails that get the admin role (Chris)
// env.now     optional clock () => ms, for tests

import { newId, newCode, sha256, hashPassword, verifyPassword, randomHex, constantTimeEqual } from './crypto.js';
import { clean } from './filter.js';

const SESSION_MS = 30 * 24 * 3600 * 1000;
const CODE_MS = 15 * 60 * 1000;
const MAX_CODE_ATTEMPTS = 5;
const LIMITS = { name: 24, title: 80, body: 4000, comment: 2000, reason: 200, password: 8 };

class HttpError extends Error {
  constructor(status, code) { super(code); this.status = status; this.code = code; }
}
const fail = (status, code) => { throw new HttpError(status, code); };

function json(status, body) {
  return new Response(JSON.stringify(body), { status, headers: { 'content-type': 'application/json' } });
}

async function body(request) {
  try { return await request.json(); } catch { fail(400, 'bad_json'); }
}

function text(v, max, field) {
  if (typeof v !== 'string') fail(400, `missing_${field}`);
  const t = v.trim();
  if (!t) fail(400, `missing_${field}`);
  if (t.length > max) fail(400, `too_long_${field}`);
  return t;
}

function filtered(v, max, field) {
  const t = clean(text(v, max, field));
  if (t === null) fail(422, 'not_allowed_words');
  return t;
}

function isAdminEmail(env, email) {
  return !!email && String(env.ADMIN_EMAILS || '').split(',').map((s) => s.trim().toLowerCase()).includes(email);
}

async function startSession(env, accountId) {
  const token = randomHex(32);
  await env.DB.prepare('INSERT INTO sessions (token_hash, account_id, expires_at) VALUES (?, ?, ?)')
    .bind(await sha256(token), accountId, now(env) + SESSION_MS).run();
  return token;
}

const now = (env) => (env.now ? env.now() : Date.now());

async function currentAccount(env, request) {
  const auth = request.headers.get('authorization') || '';
  if (!auth.startsWith('Bearer ')) return null;
  const row = await env.DB.prepare(
    'SELECT a.* FROM sessions s JOIN accounts a ON a.id = s.account_id WHERE s.token_hash = ? AND s.expires_at > ?')
    .bind(await sha256(auth.slice(7)), now(env)).first();
  return row || null;
}

async function need(env, request) {
  const a = await currentAccount(env, request);
  if (!a) fail(401, 'signed_out');
  if (a.banned) fail(403, 'banned');
  return a;
}

// Posting, commenting, voting and reporting need a verified, 16+, unbanned account.
async function needMember(env, request) {
  const a = await need(env, request);
  if (!a.email_verified) fail(403, 'email_not_verified');
  if (!a.age_ok) fail(403, 'age_gate');
  return a;
}

async function needAdmin(env, request) {
  const a = await need(env, request);
  if (a.role !== 'admin') fail(403, 'not_admin');
  return a;
}

async function sendCode(env, account) {
  const code = newCode();
  await env.DB.prepare(
    'INSERT INTO verification_codes (account_id, code_hash, expires_at, attempts) VALUES (?, ?, ?, 0) ' +
    'ON CONFLICT(account_id) DO UPDATE SET code_hash = excluded.code_hash, expires_at = excluded.expires_at, attempts = 0')
    .bind(account.id, await sha256(code), now(env) + CODE_MS).run();
  await env.MAILER.send(account.email, 'Your ADHD Pixel Dungeon code',
    `Your ADHD Pixel Dungeon Community code is ${code}. It expires in 15 minutes.`);
}

function publicAccount(a) {
  return { id: a.id, displayName: a.display_name, emailVerified: !!a.email_verified, ageOk: !!a.age_ok, admin: a.role === 'admin' };
}

async function blockedIds(env, viewer) {
  if (!viewer) return new Set();
  const { results } = await env.DB.prepare('SELECT blocked_id FROM blocks WHERE blocker_id = ?').bind(viewer.id).all();
  return new Set(results.map((r) => r.blocked_id));
}

async function audit(env, admin, action, target) {
  await env.DB.prepare('INSERT INTO audit (admin_id, action, target, created_at) VALUES (?, ?, ?, ?)')
    .bind(admin.id, action, target, now(env)).run();
}

const routes = [];
const route = (method, pattern, fn) => routes.push({ method, re: new RegExp(`^${pattern}$`), fn });

// ---- accounts ----

route('POST', '/v1/auth/register', async (env, req) => {
  const b = await body(req);
  const email = text(b.email, 254, 'email').toLowerCase();
  if (!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(email)) fail(400, 'bad_email');
  if (typeof b.password !== 'string' || b.password.length < LIMITS.password) fail(400, 'weak_password');
  if (b.password.length > 200) fail(400, 'too_long_password');
  const name = filtered(b.displayName, LIMITS.name, 'displayName');
  const existing = await env.DB.prepare('SELECT * FROM accounts WHERE email = ?').bind(email).first();
  if (existing) {
    if (existing.email_verified) fail(409, 'email_taken');
    // Unverified: let the owner of the inbox start over (new password, new code).
    await env.DB.prepare('UPDATE accounts SET password_hash = ?, display_name = ? WHERE id = ?')
      .bind(await hashPassword(b.password), name, existing.id).run();
    await sendCode(env, existing);
    return json(201, { accountId: existing.id });
  }
  const a = { id: newId(), email };
  await env.DB.prepare(
    'INSERT INTO accounts (id, display_name, email, password_hash, role, created_at) VALUES (?, ?, ?, ?, ?, ?)')
    .bind(a.id, name, email, await hashPassword(b.password), isAdminEmail(env, email) ? 'admin' : 'player', now(env)).run();
  await sendCode(env, a);
  return json(201, { accountId: a.id });
});

route('POST', '/v1/auth/resend', async (env, req) => {
  const b = await body(req);
  const email = text(b.email, 254, 'email').toLowerCase();
  const a = await env.DB.prepare('SELECT * FROM accounts WHERE email = ? AND email_verified = 0').bind(email).first();
  if (a) await sendCode(env, a);
  return json(200, { ok: true }); // same answer either way: doesn't reveal which emails exist
});

route('POST', '/v1/auth/verify', async (env, req) => {
  const b = await body(req);
  const email = text(b.email, 254, 'email').toLowerCase();
  const code = text(b.code, 6, 'code');
  const a = await env.DB.prepare('SELECT * FROM accounts WHERE email = ?').bind(email).first();
  const v = a && await env.DB.prepare('SELECT * FROM verification_codes WHERE account_id = ?').bind(a.id).first();
  if (!v) fail(400, 'bad_code');
  if (v.expires_at < now(env) || v.attempts >= MAX_CODE_ATTEMPTS) fail(400, 'code_expired');
  if (!constantTimeEqual(await sha256(code), v.code_hash)) {
    await env.DB.prepare('UPDATE verification_codes SET attempts = attempts + 1 WHERE account_id = ?').bind(a.id).run();
    fail(400, 'bad_code');
  }
  await env.DB.prepare('UPDATE accounts SET email_verified = 1 WHERE id = ?').bind(a.id).run();
  await env.DB.prepare('DELETE FROM verification_codes WHERE account_id = ?').bind(a.id).run();
  return json(200, { token: await startSession(env, a.id) });
});

route('POST', '/v1/auth/login', async (env, req) => {
  const b = await body(req);
  const email = text(b.email, 254, 'email').toLowerCase();
  const a = await env.DB.prepare('SELECT * FROM accounts WHERE email = ?').bind(email).first();
  if (!a || !(await verifyPassword(String(b.password || ''), a.password_hash))) fail(401, 'wrong_login');
  if (a.banned) fail(403, 'banned');
  if (!a.email_verified) fail(403, 'email_not_verified');
  return json(200, { token: await startSession(env, a.id) });
});

route('POST', '/v1/auth/google', async (env, req) => {
  const b = await body(req);
  const who = await env.GOOGLE.verify(text(b.idToken, 4096, 'idToken'));
  if (!who || !who.sub) fail(401, 'bad_google_token');
  let a = await env.DB.prepare('SELECT * FROM accounts WHERE google_sub = ?').bind(who.sub).first();
  if (!a) {
    const name = clean(String(b.displayName || '').trim().slice(0, LIMITS.name)) || 'Adventurer';
    a = { id: newId() };
    const email = who.email ? String(who.email).toLowerCase() : null;
    const taken = email && await env.DB.prepare('SELECT id FROM accounts WHERE email = ?').bind(email).first();
    // Never merge into an existing email account silently: a Google account gets its own account.
    await env.DB.prepare(
      'INSERT INTO accounts (id, display_name, email, google_sub, email_verified, role, created_at) VALUES (?, ?, ?, ?, 1, ?, ?)')
      .bind(a.id, name, taken ? null : email, who.sub, isAdminEmail(env, email) ? 'admin' : 'player', now(env)).run();
    a = await env.DB.prepare('SELECT * FROM accounts WHERE id = ?').bind(a.id).first();
  }
  if (a.banned) fail(403, 'banned');
  return json(200, { token: await startSession(env, a.id) });
});

route('POST', '/v1/auth/logout', async (env, req) => {
  const auth = req.headers.get('authorization') || '';
  if (auth.startsWith('Bearer ')) {
    await env.DB.prepare('DELETE FROM sessions WHERE token_hash = ?').bind(await sha256(auth.slice(7))).run();
  }
  return json(200, { ok: true });
});

route('GET', '/v1/me', async (env, req) => json(200, publicAccount(await need(env, req))));

// The device decides the age gate (Play Age Signals, else birth month/year). The server stores only that it passed.
// A failed check is never sent: the device locks Community itself, and nothing about the age is stored here.
route('POST', '/v1/account/age', async (env, req) => {
  const a = await need(env, req);
  const b = await body(req);
  if (b.passed !== true) fail(400, 'only_pass_is_recorded');
  await env.DB.prepare('UPDATE accounts SET age_ok = 1 WHERE id = ?').bind(a.id).run();
  return json(200, { ok: true });
});

// Deletes the account and everything it posted, voted or reported. Replies by others under deleted comments go too.
route('DELETE', '/v1/account', async (env, req) => {
  const a = await need(env, req);
  await env.DB.prepare('DELETE FROM accounts WHERE id = ?').bind(a.id).run();
  return json(200, { deleted: true });
});

// ---- submissions and comments ----

route('GET', '/v1/submissions', async (env, req) => {
  const viewer = await currentAccount(env, req);
  const blocked = await blockedIds(env, viewer);
  const { results } = await env.DB.prepare(
    `SELECT s.id, s.title, s.created_at, s.author_id, a.display_name,
            (SELECT COUNT(*) FROM comments c WHERE c.submission_id = s.id AND c.status = 'visible') AS comments
       FROM submissions s JOIN accounts a ON a.id = s.author_id
      WHERE s.status = 'visible' AND a.banned = 0 ORDER BY s.created_at DESC LIMIT 200`).all();
  return json(200, { submissions: results.filter((s) => !blocked.has(s.author_id)).map((s) => ({
    id: s.id, title: s.title, author: { id: s.author_id, displayName: s.display_name }, createdAt: s.created_at, comments: s.comments,
  })) });
});

route('POST', '/v1/submissions', async (env, req) => {
  const a = await needMember(env, req);
  const b = await body(req);
  const id = newId();
  await env.DB.prepare('INSERT INTO submissions (id, author_id, title, body, created_at) VALUES (?, ?, ?, ?, ?)')
    .bind(id, a.id, filtered(b.title, LIMITS.title, 'title'), filtered(b.body, LIMITS.body, 'body'), now(env)).run();
  return json(201, { id });
});

route('GET', '/v1/submissions/([0-9a-f]+)', async (env, req, [id]) => {
  const viewer = await currentAccount(env, req);
  const blocked = await blockedIds(env, viewer);
  const s = await env.DB.prepare(
    `SELECT s.*, a.display_name FROM submissions s JOIN accounts a ON a.id = s.author_id
      WHERE s.id = ? AND s.status = 'visible' AND a.banned = 0`).bind(id).first();
  if (!s || blocked.has(s.author_id)) fail(404, 'not_found');
  const { results } = await env.DB.prepare(
    `SELECT c.id, c.parent_id, c.body, c.status, c.created_at, c.author_id, a.display_name, a.banned
       FROM comments c JOIN accounts a ON a.id = c.author_id WHERE c.submission_id = ? ORDER BY c.created_at`).bind(id).all();
  // Threaded: a hidden/removed/blocked comment keeps its place as a placeholder so replies under it still make sense.
  const comments = results.map((c) => {
    const gone = c.status !== 'visible' || c.banned || blocked.has(c.author_id);
    return { id: c.id, parentId: c.parent_id, createdAt: c.created_at,
      body: gone ? null : c.body, author: gone ? null : { id: c.author_id, displayName: c.display_name } };
  });
  return json(200, { id: s.id, title: s.title, body: s.body, createdAt: s.created_at,
    author: { id: s.author_id, displayName: s.display_name }, comments });
});

route('POST', '/v1/submissions/([0-9a-f]+)/comments', async (env, req, [id]) => {
  const a = await needMember(env, req);
  const b = await body(req);
  const s = await env.DB.prepare("SELECT id FROM submissions WHERE id = ? AND status = 'visible'").bind(id).first();
  if (!s) fail(404, 'not_found');
  let parent = null;
  if (b.parentId != null) {
    parent = await env.DB.prepare("SELECT id FROM comments WHERE id = ? AND submission_id = ?").bind(String(b.parentId), id).first();
    if (!parent) fail(400, 'bad_parent');
  }
  const cid = newId();
  await env.DB.prepare('INSERT INTO comments (id, submission_id, parent_id, author_id, body, created_at) VALUES (?, ?, ?, ?, ?, ?)')
    .bind(cid, id, parent ? parent.id : null, a.id, filtered(b.body, LIMITS.comment, 'body'), now(env)).run();
  return json(201, { id: cid });
});

// ---- reports and blocks ----

const KINDS = { submission: 'submissions', comment: 'comments', account: 'accounts' };

route('POST', '/v1/reports', async (env, req) => {
  const a = await needMember(env, req);
  const b = await body(req);
  const table = KINDS[b.kind];
  if (!table) fail(400, 'bad_kind');
  const target = await env.DB.prepare(`SELECT id FROM ${table} WHERE id = ?`).bind(String(b.id || '')).first();
  if (!target) fail(404, 'not_found');
  const reason = clean(text(b.reason, LIMITS.reason, 'reason')) ?? '[reason contained blocked words]';
  await env.DB.prepare(
    'INSERT OR IGNORE INTO reports (id, reporter_id, target_kind, target_id, reason, created_at) VALUES (?, ?, ?, ?, ?, ?)')
    .bind(newId(), a.id, b.kind, target.id, reason, now(env)).run();
  return json(201, { ok: true });
});

route('POST', '/v1/blocks', async (env, req) => {
  const a = await need(env, req);
  const b = await body(req);
  const other = String(b.accountId || '');
  if (other === a.id) fail(400, 'cannot_block_self');
  if (!(await env.DB.prepare('SELECT id FROM accounts WHERE id = ?').bind(other).first())) fail(404, 'not_found');
  await env.DB.prepare('INSERT OR IGNORE INTO blocks (blocker_id, blocked_id, created_at) VALUES (?, ?, ?)').bind(a.id, other, now(env)).run();
  return json(201, { ok: true });
});

route('DELETE', '/v1/blocks/([0-9a-f]+)', async (env, req, [other]) => {
  const a = await need(env, req);
  await env.DB.prepare('DELETE FROM blocks WHERE blocker_id = ? AND blocked_id = ?').bind(a.id, other).run();
  return json(200, { ok: true });
});

// ---- admin (Chris) ----

route('GET', '/v1/admin/reports', async (env, req) => {
  await needAdmin(env, req);
  const { results } = await env.DB.prepare(
    `SELECT target_kind, target_id, COUNT(*) AS reports, GROUP_CONCAT(reason, ' | ') AS reasons, MIN(created_at) AS first
       FROM reports WHERE resolved = 0 GROUP BY target_kind, target_id ORDER BY reports DESC, first`).all();
  return json(200, { reports: results });
});

async function setStatus(env, req, status) {
  const admin = await needAdmin(env, req);
  const b = await body(req);
  if (b.kind !== 'submission' && b.kind !== 'comment') fail(400, 'bad_kind');
  const r = await env.DB.prepare(`UPDATE ${KINDS[b.kind]} SET status = ? WHERE id = ?`).bind(status, String(b.id || '')).run();
  if (!r.meta.changes) fail(404, 'not_found');
  await env.DB.prepare('UPDATE reports SET resolved = 1 WHERE target_kind = ? AND target_id = ?').bind(b.kind, b.id).run();
  await audit(env, admin, status, `${b.kind}:${b.id}`);
  return json(200, { ok: true });
}
route('POST', '/v1/admin/hide', (env, req) => setStatus(env, req, 'hidden'));
route('POST', '/v1/admin/remove', (env, req) => setStatus(env, req, 'removed'));
route('POST', '/v1/admin/restore', (env, req) => setStatus(env, req, 'visible'));

async function setBan(env, req, banned) {
  const admin = await needAdmin(env, req);
  const b = await body(req);
  const id = String(b.accountId || '');
  if (id === admin.id) fail(400, 'cannot_ban_self');
  const r = await env.DB.prepare('UPDATE accounts SET banned = ? WHERE id = ?').bind(banned ? 1 : 0, id).run();
  if (!r.meta.changes) fail(404, 'not_found');
  if (banned) await env.DB.prepare('DELETE FROM sessions WHERE account_id = ?').bind(id).run();
  await env.DB.prepare("UPDATE reports SET resolved = 1 WHERE target_kind = 'account' AND target_id = ?").bind(id).run();
  await audit(env, admin, banned ? 'ban' : 'unban', `account:${id}`);
  return json(200, { ok: true });
}
route('POST', '/v1/admin/ban', (env, req) => setBan(env, req, true));
route('POST', '/v1/admin/unban', (env, req) => setBan(env, req, false));

export async function handle(request, env) {
  const url = new URL(request.url);
  for (const r of routes) {
    if (r.method !== request.method) continue;
    const m = url.pathname.match(r.re);
    if (!m) continue;
    try {
      return await r.fn(env, request, m.slice(1));
    } catch (e) {
      if (e instanceof HttpError) return json(e.status, { error: e.code });
      console.error(e);
      return json(500, { error: 'server_error' });
    }
  }
  return json(404, { error: 'no_route' });
}

export default { fetch: handle };
