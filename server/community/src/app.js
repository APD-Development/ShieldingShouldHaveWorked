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
import { googleVerifier } from './google.js';
import { cloudflareMailer } from './mailer.js';
import { seeds, bracket } from './tournament.js';

const SESSION_MS = 30 * 24 * 3600 * 1000;
const CODE_MS = 15 * 60 * 1000;
const MAX_CODE_ATTEMPTS = 5;
// Anti-spam caps per account (Claude's call, Nov 18; Chris can change them): how many of each in a rolling window.
const RATE = {
  submissions: { table: 'submissions', col: 'author_id', max: 5, ms: 24 * 3600 * 1000 },
  comments: { table: 'comments', col: 'author_id', max: 10, ms: 10 * 60 * 1000 },
  championComments: { table: 'champion_comments', col: 'author_id', max: 10, ms: 10 * 60 * 1000 },
  reviewRequests: { table: 'review_requests', col: 'account_id', max: 3, ms: 24 * 3600 * 1000 },
  reports: { table: 'reports', col: 'reporter_id', max: 30, ms: 24 * 3600 * 1000 },
};

async function rateLimit(env, account, kind) {
  const r = RATE[kind];
  const row = await env.DB.prepare(`SELECT COUNT(*) AS n FROM ${r.table} WHERE ${r.col} = ? AND created_at > ?`)
    .bind(account.id, now(env) - r.ms).first();
  if (row.n >= r.max) fail(429, `too_many_${kind}`);
}

// Guessing and spam limits (Claude's call, Nov 18): failed sign-ins per email and per network address, sign-ups and
// code requests per network address, counted over a window.
const GUARD = {
  login_email: { max: 10, ms: 15 * 60 * 1000 },
  login_ip: { max: 30, ms: 15 * 60 * 1000 },
  register_ip: { max: 5, ms: 60 * 60 * 1000 },
  code_ip: { max: 10, ms: 60 * 60 * 1000 },
};

function ipOf(req) { return req.headers.get('cf-connecting-ip') || 'unknown'; }

async function guarded(env, kind, who) {
  const g = GUARD[kind];
  const row = await env.DB.prepare('SELECT COUNT(*) AS n FROM throttle WHERE key = ? AND at > ?').bind(`${kind}:${who}`, now(env) - g.ms).first();
  if (row.n >= g.max) fail(429, 'slow_down');
}

async function strike(env, kind, who) {
  await env.DB.prepare('INSERT INTO throttle (key, at) VALUES (?, ?)').bind(`${kind}:${who}`, now(env)).run();
  if (Math.random() < 0.02) await env.DB.prepare('DELETE FROM throttle WHERE at < ?').bind(now(env) - 24 * 3600 * 1000).run();
}

const LIMITS = { name: 24, title: 80, body: 4000, comment: 2000, reason: 200, password: 8 };

class HttpError extends Error {
  constructor(status, code) { super(code); this.status = status; this.code = code; }
}
const fail = (status, code) => { throw new HttpError(status, code); };

function json(status, body) {
  return new Response(JSON.stringify(body), { status, headers: { 'content-type': 'application/json' } });
}

async function body(request) {
  let v;
  try { v = await request.json(); } catch { fail(400, 'bad_json'); }
  // Only a JSON object: null made every b.field a TypeError (500); numbers/strings/arrays carry no fields.
  if (v === null || typeof v !== 'object' || Array.isArray(v)) fail(400, 'bad_json');
  return v;
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

// Chris's account becomes admin whenever it signs in, even if it was made before his email was configured.
async function promoteIfAdmin(env, accountId) {
  const a = await env.DB.prepare('SELECT email, role FROM accounts WHERE id = ?').bind(accountId).first();
  if (a && a.role !== 'admin' && isAdminEmail(env, a.email)) {
    await env.DB.prepare("UPDATE accounts SET role = 'admin' WHERE id = ?").bind(accountId).run();
  }
}

async function startSession(env, accountId) {
  await promoteIfAdmin(env, accountId);
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

// Launch is Google sign-in only (Chris, Nov 18). Email/password stays in the code for a later Steam build, off unless
// env.EMAIL_SIGNIN is "on"; while off its routes answer 404 as if they did not exist.
const emailOn = (env) => String(env.EMAIL_SIGNIN || '').toLowerCase() === 'on';
const needEmail = (env) => { if (!emailOn(env)) fail(404, 'not_found'); };

const routes = [];
const route = (method, pattern, fn) => routes.push({ method, re: new RegExp(`^${pattern}$`), fn });

// ---- accounts ----

route('POST', '/v1/auth/register', async (env, req) => {
  needEmail(env);
  await guarded(env, 'register_ip', ipOf(req));
  await strike(env, 'register_ip', ipOf(req));
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
  needEmail(env);
  await guarded(env, 'code_ip', ipOf(req));
  await strike(env, 'code_ip', ipOf(req));
  const b = await body(req);
  const email = text(b.email, 254, 'email').toLowerCase();
  const a = await env.DB.prepare('SELECT * FROM accounts WHERE email = ? AND email_verified = 0').bind(email).first();
  if (a) await sendCode(env, a);
  return json(200, { ok: true }); // same answer either way: doesn't reveal which emails exist
});

route('POST', '/v1/auth/verify', async (env, req) => {
  needEmail(env);
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
  needEmail(env);
  const b = await body(req);
  const email = text(b.email, 254, 'email').toLowerCase();
  await guarded(env, 'login_email', email);
  await guarded(env, 'login_ip', ipOf(req));
  const a = await env.DB.prepare('SELECT * FROM accounts WHERE email = ?').bind(email).first();
  if (!a || !(await verifyPassword(String(b.password || ''), a.password_hash))) {
    await strike(env, 'login_email', email);
    await strike(env, 'login_ip', ipOf(req));
    fail(401, 'wrong_login');
  }
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
    // No email is stored for Google accounts: the Google id is enough, and the email is only read here for the admin check.
    await env.DB.prepare(
      'INSERT INTO accounts (id, display_name, email, google_sub, email_verified, role, created_at) VALUES (?, ?, NULL, ?, 1, ?, ?)')
      .bind(a.id, name, who.sub, isAdminEmail(env, email) ? 'admin' : 'player', now(env)).run();
    a = await env.DB.prepare('SELECT * FROM accounts WHERE id = ?').bind(a.id).first();
  }
  if (a.banned) fail(403, 'banned');
  if (a.role !== 'admin' && isAdminEmail(env, who.email ? String(who.email).toLowerCase() : null)) {
    await env.DB.prepare("UPDATE accounts SET role = 'admin' WHERE id = ?").bind(a.id).run();
  }
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
async function deleteAccount(env, a) {
  // My comments that others replied to stay as empty placeholders owned by [deleted], so their replies aren't lost.
  // Repeated until nothing changes: a placeholder counts as "someone else's", so a chain of my own comments above
  // another player's reply all stay (one pass would delete the top one and cascade the reply away).
  // Everything else I wrote, voted, reported or blocked goes with the account.
  for (const table of ['comments', 'champion_comments']) {
    for (;;) {
      const r = await env.DB.prepare(
        `UPDATE ${table} SET author_id = 'deleted', body = '', status = 'removed'
          WHERE author_id = ? AND id IN (SELECT parent_id FROM ${table} WHERE parent_id IS NOT NULL AND author_id != ?)`)
        .bind(a.id, a.id).run();
      if (!r.meta || !r.meta.changes) break;
    }
  }
  // A champion page is the game's development record and stays, but my name and my text come off it.
  await env.DB.prepare("UPDATE champions SET author_id = NULL, body = '' WHERE author_id = ?").bind(a.id).run();
  await env.DB.prepare('DELETE FROM accounts WHERE id = ?').bind(a.id).run();
}

route('DELETE', '/v1/account', async (env, req) => {
  await deleteAccount(env, await need(env, req));
  return json(200, { deleted: true });
});

// ---- seasons ----

const DAY = 24 * 3600 * 1000;

export function phaseOf(season, t) {
  if (!season || t < season.starts_at) return 'none';
  if (t < season.voting_at) return 'submissions';
  if (t < season.tournament_at) return 'voting';
  if (t < season.ends_at) return 'tournament';
  return 'finished';
}

// The season players see: the latest one that has started.
async function currentSeason(env) {
  return env.DB.prepare('SELECT * FROM seasons WHERE starts_at <= ? ORDER BY starts_at DESC LIMIT 1').bind(now(env)).first();
}

function publicSeason(s, t) {
  return s && { id: s.id, title: s.title, phase: phaseOf(s, t), startsAt: s.starts_at, votingAt: s.voting_at,
    tournamentAt: s.tournament_at, endsAt: s.ends_at };
}

route('GET', '/v1/season', async (env) => json(200, { season: publicSeason(await currentSeason(env), now(env)) }));

// Chris opens a season. Default: 30 days per phase from the given start. Phases may not overlap another season.
route('POST', '/v1/admin/seasons', async (env, req) => {
  const admin = await needAdmin(env, req);
  const b = await body(req);
  const title = filtered(b.title, LIMITS.title, 'title');
  const start = Number.isFinite(b.startsAt) ? b.startsAt : now(env);
  const len = (k, d) => (Number.isFinite(b[k]) && b[k] > 0 ? b[k] : d);
  const voting = start + len('submissionMs', 30 * DAY);
  const tournament = voting + len('votingMs', 30 * DAY);
  const ends = tournament + len('tournamentMs', 28 * DAY);
  const clash = await env.DB.prepare('SELECT id FROM seasons WHERE starts_at < ? AND ends_at > ?').bind(ends, start).first();
  if (clash) fail(409, 'season_overlaps');
  const id = newId();
  await env.DB.prepare('INSERT INTO seasons (id, title, starts_at, voting_at, tournament_at, ends_at, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)')
    .bind(id, title, start, voting, tournament, ends, now(env)).run();
  await audit(env, admin, 'season', id);
  return json(201, { id });
});

// ---- account deletion on the web (Google Play requires a link that works without the app) ----

const esc = (v) => String(v).replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[c]);
const clientIdOf = (env) => String(env.GOOGLE_CLIENT_ID || '').split(',')[0].trim();

const PAGE = (env, msg) => `<!doctype html><html lang="en"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Delete your ADHD Pixel Dungeon Community account</title>
<script src="https://accounts.google.com/gsi/client" async></script>
<body style="font-family:system-ui,sans-serif;max-width:32rem;margin:2rem auto;padding:0 1rem;line-height:1.5">
<h1 style="font-size:1.4rem">Delete your ADHD Pixel Dungeon Community account</h1>
${msg}
<p>Deleting removes your account, your ideas, comments, votes, reports and blocks. Comments other players replied to
are kept as empty "[deleted]" placeholders so their replies still make sense. This can't be undone. Your game saves
and run history stay on your phone and are not affected.</p>
<p>Sign in with the Google account you use for Community. Your account is deleted as soon as you sign in.</p>
<form id="del" method="post" action="/delete"><input type="hidden" name="credential" id="cred"></form>
<div id="g_id_onload" data-client_id="${esc(clientIdOf(env))}" data-callback="apdDelete" data-auto_prompt="false"></div>
<div class="g_id_signin" data-type="standard" data-text="continue_with"></div>
<script>function apdDelete(r){document.getElementById('cred').value=r.credential;document.getElementById('del').submit();}</script>
<p>You can also delete it in the game: Community, then Account, then Delete my account.</p>
</body></html>`;

const html = (status, body) => new Response(body, { status, headers: { 'content-type': 'text/html; charset=utf-8' } });

const PRIVACY = `<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>ADHD Pixel Dungeon Privacy Policy | CharltonBorn Studios</title>
<style>
body{font-family:system-ui,sans-serif;max-width:44rem;margin:2rem auto;padding:0 1rem;line-height:1.6;color:#1b1b1b;background:#fff}
h1{font-size:1.6rem;margin-bottom:.2rem}h2{font-size:1.15rem;margin-top:2rem}
.updated{color:#666;margin-top:0}a{color:#d35400}
</style>
</head>
<body>
<h1>ADHD Pixel Dungeon Privacy Policy</h1>
<p class="updated">CharltonBorn Studios. Last updated November 18, 2026.</p>

<h2>The game</h2>
<p>ADHD Pixel Dungeon shows no ads and does not sell your data. Your saves, settings, run history and progress stay on your device. Playing the game itself sends nothing about you anywhere.</p>

<h2>Community (optional, 16+)</h2>
<p>Community is an optional part of the game where players post ideas, comment and vote. If you choose to use it, we collect:</p>
<ul>
<li><b>Your Google account ID</b>, from Google sign-in. Sign-in is through Google only. We do not store your email address, and there is no password.</li>
<li><b>The display name you choose.</b></li>
<li><b>What you post:</b> ideas and comments, which other players can see under your display name.</li>
<li><b>What you do:</b> upvotes, tournament votes, reports and blocks.</li>
<li><b>Whether you passed the 16+ check</b>, as a single yes. Nothing else about your age is kept.</li>
<li><b>Basic technical data</b> such as IP address and timestamps, used only to limit spam and abuse.</li>
</ul>
<p>Your email address is passed to us by Google during sign-in only to check whether your account is the developer's admin account. It is not saved.</p>

<h2>Age check</h2>
<p>Community is for players 16 and older. The game uses Google Play's age signals when available. Otherwise it asks for your birth month and year once, on your device. Only the pass or fail result is kept, never your birth date.</p>

<h2>Who sees your data</h2>
<p>Cloudflare hosts the Community server for us. Google handles Google sign-in. We do not sell your data or use it for advertising. Your posts, comments and display name are visible to other Community players.</p>

<h2>Deleting your data</h2>
<p>You can delete your Community account and everything you posted at any time:</p>
<ul>
<li>In the game: Community, then Account, then Delete my account.</li>
<li>On the web, without the app: <a href="https://apd-community.charltonchris29.workers.dev/delete">apd-community.charltonchris29.workers.dev/delete</a>. Sign in with the same Google account and it is deleted.</li>
</ul>
<p>Deletion removes your account, ideas, comments, votes, reports and blocks right away. Comments that other players replied to are kept as empty "[deleted]" placeholders so their replies still make sense. Your game saves on your phone are not affected.</p>

<h2>Children</h2>
<p>Community is not available to anyone under 16, and we do not knowingly collect data from children. If you believe a child has used Community, contact us and we will delete the account.</p>

<h2>Security and changes</h2>
<p>All Community data is sent over encrypted connections. If this policy changes, the new version is posted here with an updated date.</p>

<h2>Contact</h2>
<p><a href="mailto:charltonchris29@gmail.com">charltonchris29@gmail.com</a></p>
</body>
</html>
`;
route('GET', '/privacy', async () => html(200, PRIVACY));

route('GET', '/delete', async (env) => html(200, PAGE(env, '')));

route('POST', '/delete', async (env, req) => {
  const form = await req.formData().catch(() => null);
  const credential = String(form?.get('credential') || '');
  try { await guarded(env, 'login_ip', ipOf(req)); } catch (e) {
    return html(429, PAGE(env, '<p style="color:#b00"><b>Too many tries. Wait a few minutes and try again.</b></p>'));
  }
  const who = credential ? await env.GOOGLE.verify(credential) : null;
  const a = who && who.sub && await env.DB.prepare('SELECT * FROM accounts WHERE google_sub = ?').bind(who.sub).first();
  if (!who || !who.sub) {
    await strike(env, 'login_ip', ipOf(req));
    return html(401, PAGE(env, '<p style="color:#b00"><b>Google sign-in didn\'t work. Try again.</b></p>'));
  }
  if (!a) return html(404, PAGE(env, '<p style="color:#b00"><b>That Google account has no Community account. Nothing was deleted.</b></p>'));
  await deleteAccount(env, a);
  return html(200, '<!doctype html><meta charset="utf-8"><body style="font-family:system-ui,sans-serif;max-width:32rem;margin:2rem auto"><h1>Your account is deleted.</h1><p>Everything you posted in ADHD Pixel Dungeon Community is gone.</p></body>');
});

// ---- submissions and comments ----

route('GET', '/v1/submissions', async (env, req) => {
  const viewer = await currentAccount(env, req);
  const blocked = await blockedIds(env, viewer);
  const { results } = await env.DB.prepare(
    `SELECT s.id, s.title, s.created_at, s.author_id, a.display_name,
            (SELECT COUNT(*) FROM comments c JOIN accounts ca ON ca.id = c.author_id
              WHERE c.submission_id = s.id AND c.status = 'visible' AND ca.banned = 0
                AND c.author_id NOT IN (SELECT blocked_id FROM blocks WHERE blocker_id = ?)) AS comments
       FROM submissions s JOIN accounts a ON a.id = s.author_id
      WHERE s.status = 'visible' AND a.banned = 0 AND s.season_id = ?
        AND s.author_id NOT IN (SELECT blocked_id FROM blocks WHERE blocker_id = ?) ORDER BY s.created_at DESC LIMIT 500`)
    // The count matches what the idea page shows readable: no banned players' or blocked players' comments.
    // Blocked authors are left out in SQL, before the LIMIT: 500 newer ideas by a blocked player must not empty the list.
    .bind(viewer ? viewer.id : '', ((await currentSeason(env)) || {}).id ?? '', viewer ? viewer.id : '').all();
  return json(200, { submissions: results.filter((s) => !blocked.has(s.author_id)).map((s) => ({
    id: s.id, title: s.title, author: { id: s.author_id, displayName: s.display_name }, createdAt: s.created_at, comments: s.comments,
  })) });
});

route('POST', '/v1/submissions', async (env, req) => {
  const a = await needMember(env, req);
  const b = await body(req);
  const season = await currentSeason(env);
  if (phaseOf(season, now(env)) !== 'submissions') fail(409, 'submissions_closed');
  await rateLimit(env, a, 'submissions');
  const id = newId();
  await env.DB.prepare('INSERT INTO submissions (id, author_id, title, body, season_id, created_at) VALUES (?, ?, ?, ?, ?, ?)')
    .bind(id, a.id, filtered(b.title, LIMITS.title, 'title'), filtered(b.body, LIMITS.body, 'body'), season.id, now(env)).run();
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
  // Same rule as reading it: a banned player's idea, or one from a player I blocked, can't be commented on by its id.
  const s = await env.DB.prepare(
    `SELECT s.id, s.author_id FROM submissions s JOIN accounts x ON x.id = s.author_id
      WHERE s.id = ? AND s.status = 'visible' AND x.banned = 0`).bind(id).first();
  if (!s || (await blockedIds(env, a)).has(s.author_id)) fail(404, 'not_found');
  await rateLimit(env, a, 'comments');
  let parent = null;
  if (b.parentId != null) {
    // Only a string id: String(['c']) would match comment c and bind as its id.
    if (typeof b.parentId !== 'string') fail(400, 'bad_parent');
    parent = await env.DB.prepare(
      `SELECT c.id, c.author_id FROM comments c JOIN accounts x ON x.id = c.author_id
        WHERE c.id = ? AND c.submission_id = ? AND c.status = 'visible' AND x.banned = 0`).bind(b.parentId, id).first();
    // A reply can't be aimed at a placeholder: a hidden/removed comment, a banned player's, or one by a player I blocked.
    if (!parent || (await blockedIds(env, a)).has(parent.author_id)) fail(400, 'bad_parent');
  }
  const cid = newId();
  await env.DB.prepare('INSERT INTO comments (id, submission_id, parent_id, author_id, body, created_at) VALUES (?, ?, ?, ?, ?, ?)')
    .bind(cid, id, parent ? parent.id : null, a.id, filtered(b.body, LIMITS.comment, 'body'), now(env)).run();
  return json(201, { id: cid });
});

// ---- voting and the tournament ----

function tournamentWeek(season, t) {
  const week = Math.floor((t - season.tournament_at) / ((season.ends_at - season.tournament_at) / 4)) + 1;
  return Math.max(1, Math.min(4, week));
}

// Everything the results depend on, read fresh. Only visible submissions by unbanned authors, and only votes by
// unbanned accounts, count.
async function seasonState(env, season) {
  const t = now(env);
  const { results: entries } = await env.DB.prepare(
    `SELECT s.id, s.title, s.created_at, s.author_id,
            (SELECT COUNT(*) FROM upvotes u JOIN accounts v ON v.id = u.account_id
              WHERE u.submission_id = s.id AND v.banned = 0) AS score
       FROM submissions s JOIN accounts a ON a.id = s.author_id
      WHERE s.season_id = ? AND s.status = 'visible' AND a.banned = 0`).bind(season.id).all();
  const phase = phaseOf(season, t);
  const state = { season, phase, entries, t };
  if (phase !== 'tournament' && phase !== 'finished') return state;
  state.week = phase === 'finished' ? 5 : tournamentWeek(season, t);
  const { results: tv } = await env.DB.prepare(
    `SELECT tv.week, tv.slot, tv.submission_id, COUNT(*) AS n FROM tournament_votes tv JOIN accounts v ON v.id = tv.account_id
      WHERE tv.season_id = ? AND v.banned = 0 GROUP BY tv.week, tv.slot, tv.submission_id`).bind(season.id).all();
  const byWeek = new Map();
  for (const r of tv) {
    if (!byWeek.has(r.week)) byWeek.set(r.week, new Map());
    const w = byWeek.get(r.week);
    if (!w.has(r.slot)) w.set(r.slot, new Map());
    w.get(r.slot).set(r.submission_id, r.n);
  }
  // The 16 seeds come straight from Month 2's upvotes; the bracket starts in week 1.
  state.seeds = seeds(entries);
  state.rounds = bracket(state.seeds, byWeek, Math.min(state.week, 4));
  if (phase === 'finished' && state.rounds.length) {
    const last = state.rounds[state.rounds.length - 1].matchups;
    if (last.length === 1) state.champion = last[0].winner;
  }
  return state;
}

function liveSeason(env, season) {
  if (!season) fail(409, 'no_season');
  return season;
}

route('POST', '/v1/submissions/([0-9a-f]+)/upvote', async (env, req, [id]) => {
  const a = await needMember(env, req);
  const season = liveSeason(env, await currentSeason(env));
  if (phaseOf(season, now(env)) !== 'voting') fail(409, 'voting_closed');
  // Same rule as reading it: a banned player's idea, or one from a player I blocked, can't be upvoted by its id.
  const s = await env.DB.prepare(
    `SELECT s.author_id FROM submissions s JOIN accounts x ON x.id = s.author_id
      WHERE s.id = ? AND s.season_id = ? AND s.status = 'visible' AND x.banned = 0`).bind(id, season.id).first();
  if (!s || (await blockedIds(env, a)).has(s.author_id)) fail(404, 'not_found');
  if (s.author_id === a.id) fail(403, 'own_submission');
  await env.DB.prepare('INSERT OR IGNORE INTO upvotes (account_id, submission_id, created_at) VALUES (?, ?, ?)').bind(a.id, id, now(env)).run();
  return json(200, { upvoted: true });
});

route('DELETE', '/v1/submissions/([0-9a-f]+)/upvote', async (env, req, [id]) => {
  const a = await needMember(env, req);
  const season = liveSeason(env, await currentSeason(env));
  if (phaseOf(season, now(env)) !== 'voting') fail(409, 'voting_closed');
  await env.DB.prepare('DELETE FROM upvotes WHERE account_id = ? AND submission_id = ?').bind(a.id, id).run();
  return json(200, { upvoted: false });
});

// Weeks 1-4: one vote per matchup of this week's round, changeable until the week ends.
route('POST', '/v1/tournament/vote', async (env, req) => {
  const a = await needMember(env, req);
  const season = liveSeason(env, await currentSeason(env));
  const st = await seasonState(env, season);
  if (st.phase !== 'tournament') fail(409, 'no_matchups_open');
  const b = await body(req);
  const round = st.rounds.find((r) => r.week === st.week);
  // Only a plain integer index: [7] or "length" must not reach matchups[...] or the INTEGER slot column.
  const m = round && Number.isInteger(b.matchup) && b.matchup >= 0 ? round.matchups[b.matchup] : undefined;
  if (!m) fail(400, 'bad_matchup');
  if (m.bye) fail(400, 'bye_no_vote');
  // Only a string id, like every other id field: String([id]) would otherwise match and count as a vote.
  const pick = typeof b.submissionId === 'string' ? b.submissionId : '';
  if (pick !== m.a && pick !== m.b) fail(400, 'not_in_matchup');
  const authorOf = new Map(st.entries.map((e) => [e.id, e.author_id]));
  if (authorOf.get(pick) === a.id) fail(403, 'own_submission');
  await env.DB.prepare(
    'INSERT INTO tournament_votes (season_id, week, slot, account_id, submission_id, created_at) VALUES (?, ?, ?, ?, ?, ?) ' +
    'ON CONFLICT(season_id, week, slot, account_id) DO UPDATE SET submission_id = excluded.submission_id, created_at = excluded.created_at')
    .bind(season.id, st.week, b.matchup, a.id, pick, now(env)).run();
  return json(200, { ok: true });
});

route('GET', '/v1/season/results', async (env, req) => {
  const season = await currentSeason(env);
  if (!season) return json(200, { season: null });
  const st = await seasonState(env, season);
  const viewer = await currentAccount(env, req);
  const title = new Map(st.entries.map((e) => [e.id, e.title]));
  const out = { season: publicSeason(season, st.t) };
  if (st.phase === 'voting' || st.phase === 'submissions') {
    out.standings = st.phase === 'voting'
      ? [...st.entries].sort((x, y) => y.score - x.score || x.created_at - y.created_at).map((e) => ({ id: e.id, title: e.title, upvotes: e.score }))
      : [];
    if (viewer) {
      const { results } = await env.DB.prepare('SELECT submission_id FROM upvotes WHERE account_id = ?').bind(viewer.id).all();
      out.myUpvotes = results.map((r) => r.submission_id);
    }
    return json(200, out);
  }
  out.week = Math.min(st.week, 4);
  out.seeds = st.seeds.map((e, i) => ({ seed: i + 1, id: e.id, title: e.title, upvotes: e.score }));
  out.rounds = st.rounds.map((r) => ({ week: r.week, final: r.week < st.week, matchups: r.matchups.map((m, i) => ({
    matchup: i, bye: !!m.bye, a: m.a && { id: m.a, title: title.get(m.a), votes: m.votesA },
    b: m.b && { id: m.b, title: title.get(m.b), votes: m.votesB },
    winner: r.week < st.week ? m.winner : null })) }));
  if (st.champion) out.champion = { id: st.champion, title: title.get(st.champion) };
  return json(200, out);
});

// ---- selected content: champions (Chris, Nov 18) ----
// When a season's bracket finishes, its champion gets a page: a status Chris sets (Planned -> In development ->
// Testing -> Released), his dev notes, and player comments. Every past champion stays in a browsable history.

const CHAMPION_STATUSES = ['planned', 'in_development', 'testing', 'released'];

// Record champions of finished seasons that don't have a page yet. Frozen once written.
async function recordChampions(env) {
  const { results } = await env.DB.prepare(
    `SELECT s.* FROM seasons s WHERE s.ends_at <= ? AND NOT EXISTS (SELECT 1 FROM champions c WHERE c.season_id = s.id)`)
    .bind(now(env)).all();
  for (const season of results) {
    const st = await seasonState(env, season);
    if (!st.champion) continue; // fewer than 2 entries: no bracket, no champion
    const e = await env.DB.prepare('SELECT id, author_id, title, body FROM submissions WHERE id = ?').bind(st.champion).first();
    if (!e) continue;
    await env.DB.prepare(
      'INSERT OR IGNORE INTO champions (season_id, submission_id, author_id, title, body, status, won_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)')
      .bind(season.id, e.id, e.author_id, e.title, e.body, 'planned', season.ends_at, now(env)).run();
  }
}

function publicChampion(c) {
  return { seasonId: c.season_id, seasonTitle: c.season_title, submissionId: c.submission_id, title: c.title,
    status: c.status, wonAt: c.won_at, updatedAt: c.updated_at,
    author: c.author_id ? { id: c.author_id, displayName: c.display_name } : null };
}

const CHAMPION_SELECT = `SELECT c.*, s.title AS season_title, a.display_name FROM champions c JOIN seasons s ON s.id = c.season_id
  LEFT JOIN accounts a ON a.id = c.author_id`;

route('GET', '/v1/champions', async (env) => {
  await recordChampions(env);
  const { results } = await env.DB.prepare(`${CHAMPION_SELECT} ORDER BY c.won_at DESC`).all();
  return json(200, { champions: results.map(publicChampion) });
});

async function championOr404(env, seasonId) {
  await recordChampions(env);
  const c = await env.DB.prepare(`${CHAMPION_SELECT} WHERE c.season_id = ?`).bind(seasonId).first();
  if (!c) fail(404, 'not_found');
  return c;
}

route('GET', '/v1/champions/([0-9a-z]+)', async (env, req, [sid]) => {
  const c = await championOr404(env, sid);
  const viewer = await currentAccount(env, req);
  const blocked = await blockedIds(env, viewer);
  const { results: notes } = await env.DB.prepare(
    'SELECT id, body, created_at FROM champion_notes WHERE season_id = ? ORDER BY created_at').bind(sid).all();
  const { results } = await env.DB.prepare(
    `SELECT k.id, k.parent_id, k.body, k.status, k.created_at, k.author_id, a.display_name, a.banned
       FROM champion_comments k JOIN accounts a ON a.id = k.author_id WHERE k.season_id = ? ORDER BY k.created_at`).bind(sid).all();
  const comments = results.map((k) => {
    const gone = k.status !== 'visible' || k.banned || blocked.has(k.author_id);
    return { id: k.id, parentId: k.parent_id, createdAt: k.created_at,
      body: gone ? null : k.body, author: gone ? null : { id: k.author_id, displayName: k.display_name } };
  });
  return json(200, { ...publicChampion(c), body: c.body,
    notes: notes.map((n) => ({ id: n.id, body: n.body, createdAt: n.created_at })), comments });
});

route('POST', '/v1/champions/([0-9a-z]+)/comments', async (env, req, [sid]) => {
  const a = await needMember(env, req);
  const b = await body(req);
  await championOr404(env, sid);
  await rateLimit(env, a, 'championComments');
  let parent = null;
  if (b.parentId != null) {
    // Only a string id: String(['c']) would match comment c and bind as its id.
    if (typeof b.parentId !== 'string') fail(400, 'bad_parent');
    // Same rule as idea comments: no reply aimed at a placeholder (hidden/removed, banned author, or blocked by me).
    parent = await env.DB.prepare(
      `SELECT k.id, k.author_id FROM champion_comments k JOIN accounts x ON x.id = k.author_id
        WHERE k.id = ? AND k.season_id = ? AND k.status = 'visible' AND x.banned = 0`).bind(b.parentId, sid).first();
    if (!parent || (await blockedIds(env, a)).has(parent.author_id)) fail(400, 'bad_parent');
  }
  const id = newId();
  await env.DB.prepare('INSERT INTO champion_comments (id, season_id, parent_id, author_id, body, created_at) VALUES (?, ?, ?, ?, ?, ?)')
    .bind(id, sid, parent ? parent.id : null, a.id, filtered(b.body, LIMITS.comment, 'body'), now(env)).run();
  return json(201, { id });
});

// Chris sets the status. Any order is allowed (a feature can go back to testing), but only the four known values.
route('POST', '/v1/admin/champions/([0-9a-z]+)/status', async (env, req, [sid]) => {
  const admin = await needAdmin(env, req);
  const b = await body(req);
  if (!CHAMPION_STATUSES.includes(b.status)) fail(400, 'bad_status');
  await championOr404(env, sid);
  await env.DB.prepare('UPDATE champions SET status = ?, updated_at = ? WHERE season_id = ?').bind(b.status, now(env), sid).run();
  await audit(env, admin, `champion_status:${b.status}`, sid);
  return json(200, { ok: true });
});

route('POST', '/v1/admin/champions/([0-9a-z]+)/notes', async (env, req, [sid]) => {
  const admin = await needAdmin(env, req);
  const b = await body(req);
  await championOr404(env, sid);
  const id = newId();
  await env.DB.prepare('INSERT INTO champion_notes (id, season_id, body, created_at) VALUES (?, ?, ?, ?)')
    .bind(id, sid, text(b.body, LIMITS.body, 'body'), now(env)).run();
  await env.DB.prepare('UPDATE champions SET updated_at = ? WHERE season_id = ?').bind(now(env), sid).run();
  await audit(env, admin, 'champion_note', `${sid}:${id}`);
  return json(201, { id });
});

route('DELETE', '/v1/admin/champions/([0-9a-z]+)/notes/([0-9a-f]+)', async (env, req, [sid, nid]) => {
  const admin = await needAdmin(env, req);
  const r = await env.DB.prepare('DELETE FROM champion_notes WHERE id = ? AND season_id = ?').bind(nid, sid).run();
  if (!r.meta.changes) fail(404, 'not_found');
  await audit(env, admin, 'champion_note_delete', `${sid}:${nid}`);
  return json(200, { ok: true });
});

// ---- run reviews (Chris's design, Nov 18; do not add to it) ----
// A player submits a finished run. Reviewers claim it, get its seed and setup (never shown publicly), play it once,
// and send how far they got plus a verdict: salvageable or not, and what the player should have done. Reaching the
// original death depth counts as surviving it; dying earlier is a failed review, which still counts. No retries.
// It closes at 5 finished reviews; the player sees "2/5" and the verdicts only once it closes. A reviewer sees how the
// original run ended only after sending their own result. Loot generation is unchanged ("luck drop" is Chris's open question).

const REVIEWS_NEEDED = 5;

async function needReviewer(env, req) {
  const a = await needMember(env, req);
  if (a.review_banned) fail(403, 'review_access_removed');
  return a;
}

async function reviewCounts(env, requestId) {
  return env.DB.prepare(
    `SELECT COUNT(*) AS done, COALESCE(SUM(survived), 0) AS survived FROM reviews WHERE request_id = ? AND finished_at IS NOT NULL`)
    .bind(requestId).first();
}

function jsonText(v, max, field) {
  if (v == null || typeof v !== 'object') fail(400, `missing_${field}`);
  const t = JSON.stringify(v);
  if (t.length > max) fail(400, `too_long_${field}`);
  return t;
}

const REVIEW_TYPES = ['roasts', 'advice', 'encouraging'];

route('POST', '/v1/reviews', async (env, req) => {
  const a = await needMember(env, req);
  const b = await body(req);
  const seed = text(b.seed, 64, 'seed');
  const depth = Number(b.deathDepth);
  if (!Number.isInteger(depth) || depth < 1 || depth > 100) fail(400, 'missing_deathDepth');
  // Review type replaces roasts on/off; an old app's `roasts` flag still maps onto it.
  const type = b.reviewType == null ? (b.roasts === true ? 'roasts' : 'advice') : b.reviewType;
  if (!REVIEW_TYPES.includes(type)) fail(400, 'bad_reviewType');
  await rateLimit(env, a, 'reviewRequests');
  const id = newId();
  await env.DB.prepare(
    'INSERT INTO review_requests (id, account_id, seed, setup, death_depth, ending, roasts, review_type, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)')
    .bind(id, a.id, seed, jsonText(b.setup, 4000, 'setup'), depth, jsonText(b.ending, 8000, 'ending'), type === 'roasts' ? 1 : 0, type, now(env)).run();
  return json(201, { id });
});

// Open runs I could review: not mine, not one I've already taken, from players I haven't blocked. No seed, no ending.
route('GET', '/v1/reviews/open', async (env, req) => {
  const a = await needReviewer(env, req);
  const { results } = await env.DB.prepare(
    `SELECT r.id, r.account_id, r.setup, r.roasts, r.review_type, r.created_at, x.display_name,
            (SELECT COUNT(*) FROM reviews v WHERE v.request_id = r.id AND v.finished_at IS NOT NULL) AS done
       FROM review_requests r JOIN accounts x ON x.id = r.account_id
      WHERE r.status = 'open' AND x.banned = 0 AND r.account_id != ?
        AND NOT EXISTS (SELECT 1 FROM reviews v WHERE v.request_id = r.id AND v.reviewer_id = ?)
        AND NOT EXISTS (SELECT 1 FROM blocks b WHERE b.blocker_id = ? AND b.blocked_id = r.account_id)
      ORDER BY r.created_at LIMIT 100`).bind(a.id, a.id, a.id).all();
  // Blocked players are left out in SQL, before the LIMIT: 100 old runs by blocked players must not empty the list.
  return json(200, { runs: results.map((r) => ({
    id: r.id, player: r.display_name, setup: JSON.parse(r.setup), roasts: !!r.roasts, reviewType: r.review_type, done: r.done, needed: REVIEWS_NEEDED })) });
});

// Take a run to review. Only now does the seed come down, so the game can start it; the app never displays it.
const RECLAIM_WINDOW_MS = 10 * 60 * 1000;
route('POST', '/v1/reviews/([0-9a-f]+)/claim', async (env, req, [id]) => {
  const a = await needReviewer(env, req);
  const r = await env.DB.prepare("SELECT * FROM review_requests WHERE id = ? AND status = 'open'").bind(id).first();
  if (!r) fail(404, 'not_found');
  if (r.account_id === a.id) fail(403, 'own_run');
  // Same rule as the open list: a banned player's run, or one from a player I blocked, can't be taken by its id either.
  const author = await env.DB.prepare('SELECT banned FROM accounts WHERE id = ?').bind(r.account_id).first();
  if (!author || author.banned || (await blockedIds(env, a)).has(r.account_id)) fail(404, 'not_found');
  const prior = await env.DB.prepare('SELECT finished_at, claimed_at FROM reviews WHERE request_id = ? AND reviewer_id = ?').bind(id, a.id).first();
  if (prior && prior.finished_at != null) fail(409, 'already_reviewed');
  // One attempt, no retry: asking again only works shortly after the first claim (its reply may have been lost).
  // Later the seed is not handed out again, so deleting the review save can't restart the run.
  if (prior && now(env) - prior.claimed_at > RECLAIM_WINDOW_MS) fail(409, 'already_claimed');
  if (!prior) {
    await env.DB.prepare('INSERT INTO reviews (request_id, reviewer_id, id, claimed_at) VALUES (?, ?, ?, ?)')
      .bind(id, a.id, newId(), now(env)).run();
  }
  return json(200, { id, seed: r.seed, setup: JSON.parse(r.setup), deathDepth: r.death_depth, roasts: !!r.roasts, reviewType: r.review_type });
});

// One result per reviewer, no retry. Then they may see how the original run ended.
route('POST', '/v1/reviews/([0-9a-f]+)/result', async (env, req, [id]) => {
  const a = await needReviewer(env, req);
  const b = await body(req);
  const r = await env.DB.prepare('SELECT * FROM review_requests WHERE id = ?').bind(id).first();
  if (!r || r.status === 'removed') fail(404, 'not_found');
  const mine = await env.DB.prepare('SELECT * FROM reviews WHERE request_id = ? AND reviewer_id = ?').bind(id, a.id).first();
  if (!mine) fail(409, 'not_claimed');
  if (mine.finished_at != null) fail(409, 'already_reviewed');
  if (r.status !== 'open') fail(409, 'review_closed');
  const reached = Number(b.reachedDepth);
  if (!Number.isInteger(reached) || reached < 0 || reached > 100) fail(400, 'missing_reachedDepth');
  if (typeof b.salvageable !== 'boolean') fail(400, 'missing_salvageable');
  const advice = filtered(b.advice, LIMITS.comment, 'advice');
  // Chris, Nov 19: the reviewer tags their own comment's tone; a tone that doesn't match what the player asked for
  // is held for a moderator (the vote still counts, the words aren't shown until a moderator restores them).
  // Build 925 (already sent to Play) has no tone picker: a missing tone is accepted untagged and shown as before.
  if (b.tone != null && !REVIEW_TYPES.includes(b.tone)) fail(400, 'bad_tone');
  const tone = b.tone == null ? null : b.tone;
  const held = tone != null && tone !== r.review_type;
  const survived = reached >= r.death_depth ? 1 : 0;
  const res = await env.DB.prepare(
    `UPDATE reviews SET finished_at = ?, reached_depth = ?, survived = ?, salvageable = ?, advice = ?, tone = ?, status = ?
      WHERE request_id = ? AND reviewer_id = ? AND finished_at IS NULL
        AND (SELECT COUNT(*) FROM reviews v WHERE v.request_id = ? AND v.finished_at IS NOT NULL) < ?`)
    .bind(now(env), reached, survived, b.salvageable ? 1 : 0, advice, tone, held ? 'held' : 'visible', id, a.id, id, REVIEWS_NEEDED).run();
  if (!res.meta.changes) fail(409, 'review_closed');
  if (held) {
    await env.DB.prepare(
      'INSERT OR IGNORE INTO reports (id, reporter_id, target_kind, target_id, reason, created_at) VALUES (?, ?, ?, ?, ?, ?)')
      .bind(newId(), a.id, 'review', mine.id, `Held automatically: tagged ${tone}, player asked for ${r.review_type}`, now(env)).run();
  }
  const c = await reviewCounts(env, id);
  if (c.done >= REVIEWS_NEEDED) {
    await env.DB.prepare("UPDATE review_requests SET status = 'closed', closed_at = ? WHERE id = ? AND status = 'open'").bind(now(env), id).run();
  }
  return json(200, { survived: !!survived, held, ending: JSON.parse(r.ending), done: c.done, needed: REVIEWS_NEEDED });
});

function verdicts(rows, blocked) {
  return rows.map((v) => {
    const gone = v.status !== 'visible' || v.banned || (blocked && blocked.has(v.reviewer_id));
    return { id: v.id, survived: !!v.survived, reachedDepth: v.reached_depth, salvageable: !!v.salvageable, tone: v.tone,
      advice: gone ? null : v.advice, reviewer: gone ? null : { id: v.reviewer_id, displayName: v.display_name } };
  });
}

// My own submitted runs: progress like 2/5 while open; the verdicts once closed.
route('GET', '/v1/reviews/mine', async (env, req) => {
  const a = await need(env, req);
  const blocked = await blockedIds(env, a);
  const { results } = await env.DB.prepare(
    "SELECT * FROM review_requests WHERE account_id = ? AND status != 'removed' ORDER BY created_at DESC").bind(a.id).all();
  const out = [];
  for (const r of results) {
    const c = await reviewCounts(env, r.id);
    const item = { id: r.id, setup: JSON.parse(r.setup), roasts: !!r.roasts, reviewType: r.review_type, status: r.status, done: c.done, needed: REVIEWS_NEEDED, createdAt: r.created_at };
    if (r.status === 'closed') {
      const { results: rows } = await env.DB.prepare(
        `SELECT v.*, x.display_name, x.banned FROM reviews v JOIN accounts x ON x.id = v.reviewer_id
          WHERE v.request_id = ? AND v.finished_at IS NOT NULL ORDER BY v.finished_at`).bind(r.id).all();
      item.reviews = verdicts(rows, blocked);
    }
    out.push(item);
  }
  return json(200, { runs: out });
});

// A moderator takes review access away from (or gives it back to) a reviewer.
route('POST', '/v1/admin/review-access', async (env, req) => {
  const admin = await needAdmin(env, req);
  const b = await body(req);
  const id = String(b.accountId || '');
  if (typeof b.allowed !== 'boolean') fail(400, 'missing_allowed');
  const r = await env.DB.prepare('UPDATE accounts SET review_banned = ? WHERE id = ?').bind(b.allowed ? 0 : 1, id).run();
  if (!r.meta.changes) fail(404, 'not_found');
  await audit(env, admin, b.allowed ? 'review_access_on' : 'review_access_off', `account:${id}`);
  return json(200, { ok: true });
});

// ---- hero mod workshop (Chris, Nov 19: data + art only; plan step 4e) ----
// The server checks sizes, the id, the PNG header and the shown words; the game re-validates every mod it downloads.

const MOD_JSON_MAX = 16 * 1024;
const MOD_SPRITE_MAX = 64 * 1024;
const MOD_UPLOADS = { max: 5, ms: 24 * 3600 * 1000 }; // uploads + updates per account per day (Claude's call)

function modSprite(b64) {
  if (typeof b64 !== 'string' || !b64 || b64.length > Math.ceil(MOD_SPRITE_MAX / 3) * 4 + 4) fail(400, 'bad_sprite');
  if (!/^[A-Za-z0-9+/]+={0,2}$/.test(b64)) fail(400, 'bad_sprite');
  let bytes;
  try { bytes = Uint8Array.from(atob(b64), (c) => c.charCodeAt(0)); } catch { fail(400, 'bad_sprite'); }
  if (bytes.length > MOD_SPRITE_MAX) fail(400, 'sprite_too_large');
  const sig = [0x89, 0x50, 0x4e, 0x47, 13, 10, 26, 10];
  if (bytes.length < 24 || sig.some((v, i) => bytes[i] !== v) || String.fromCharCode(...bytes.slice(12, 16)) !== 'IHDR') fail(400, 'bad_sprite');
  const dv = new DataView(bytes.buffer);
  if (dv.getUint32(16) !== 256 || dv.getUint32(20) !== 128) fail(400, 'sprite_wrong_size');
  return b64;
}

function modJson(t) {
  if (typeof t !== 'string' || !t.trim()) fail(400, 'missing_json');
  if (new TextEncoder().encode(t).length > MOD_JSON_MAX) fail(400, 'json_too_large');
  let m;
  try { m = JSON.parse(t); } catch { fail(400, 'bad_json_mod'); }
  if (!m || typeof m !== 'object' || Array.isArray(m)) fail(400, 'bad_json_mod');
  if (typeof m.id !== 'string' || m.id.length > 64 || !/^[a-z0-9][a-z0-9._-]*$/.test(m.id)) fail(400, 'bad_mod_id');
  // The game shows hero.json's own words, which the server can't mask, so any word the filter would mask or refuse
  // refuses the whole mod.
  const shown = (v, max, field) => { const t = text(v, max, field); if (clean(t) !== t) fail(422, 'not_allowed_words'); return t; };
  const name = shown(m.name, 40, 'name');
  if (m.author !== undefined) shown(m.author, 40, 'author');
  const description = typeof m.description === 'string' && m.description.trim() ? shown(m.description, 1000, 'description') : '';
  return { modId: m.id, name, description };
}

function publicMod(m, extra = {}) {
  return { id: m.id, modId: m.mod_id, name: m.name, description: m.description, version: m.version,
    author: { id: m.author_id, displayName: m.display_name }, subscribers: m.subscribers ?? 0, updatedAt: m.updated_at, ...extra };
}

const MOD_SELECT = `SELECT m.*, a.display_name,
  (SELECT COUNT(*) FROM mod_subscriptions s WHERE s.mod_id = m.id) AS subscribers
  FROM mods m JOIN accounts a ON a.id = m.author_id WHERE m.status = 'visible' AND a.banned = 0`;

route('POST', '/v1/mods', async (env, req) => {
  const a = await needMember(env, req);
  const b = await body(req);
  const meta = modJson(b.json);
  const sprite = modSprite(b.sprite);
  const recent = await env.DB.prepare('SELECT COUNT(*) AS n FROM mod_uploads WHERE account_id = ? AND created_at > ?')
    .bind(a.id, now(env) - MOD_UPLOADS.ms).first();
  if (recent.n >= MOD_UPLOADS.max) fail(429, 'too_many_mods');
  const old = await env.DB.prepare('SELECT id, author_id, status FROM mods WHERE mod_id = ?').bind(meta.modId).first();
  if (old && old.author_id !== a.id) fail(409, 'mod_id_taken');
  if (old && old.status === 'removed') fail(403, 'mod_removed');
  await env.DB.prepare('INSERT INTO mod_uploads (account_id, created_at) VALUES (?, ?)').bind(a.id, now(env)).run();
  if (old) {
    await env.DB.prepare('UPDATE mods SET name = ?, description = ?, hero_json = ?, sprite = ?, version = version + 1, updated_at = ? WHERE id = ?')
      .bind(meta.name, meta.description, b.json, sprite, now(env), old.id).run();
    return json(200, { id: old.id, updated: true });
  }
  const id = newId();
  await env.DB.prepare('INSERT INTO mods (id, author_id, mod_id, name, description, hero_json, sprite, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)')
    .bind(id, a.id, meta.modId, meta.name, meta.description, b.json, sprite, now(env), now(env)).run();
  return json(201, { id });
});

route('GET', '/v1/mods', async (env, req) => {
  const viewer = await currentAccount(env, req);
  const blocked = await blockedIds(env, viewer);
  // Blocked authors are left out in SQL, before the LIMIT: 500 newer mods by a blocked player must not empty the list.
  const { results } = await env.DB.prepare(
    `${MOD_SELECT} AND m.author_id NOT IN (SELECT blocked_id FROM blocks WHERE blocker_id = ?) ORDER BY m.updated_at DESC LIMIT 500`)
    .bind(viewer ? viewer.id : '').all();
  return json(200, { mods: results.filter((m) => !blocked.has(m.author_id)).map((m) => publicMod(m)) });
});

route('GET', '/v1/mods/subscribed', async (env, req) => {
  const a = await need(env, req);
  const { results } = await env.DB.prepare(
    `${MOD_SELECT} AND m.id IN (SELECT mod_id FROM mod_subscriptions WHERE account_id = ?) ORDER BY m.name`).bind(a.id).all();
  // Same rule as browse and download: a blocked author's mods don't show (download would 404 anyway).
  const blocked = await blockedIds(env, a);
  return json(200, { mods: results.filter((m) => !blocked.has(m.author_id)).map((m) => publicMod(m)) });
});

// Download: the exact hero.json and sprite, for the game to check and install.
route('GET', '/v1/mods/([0-9a-f]+)', async (env, req, [id]) => {
  const blocked = await blockedIds(env, await currentAccount(env, req));
  const m = await env.DB.prepare(`${MOD_SELECT} AND m.id = ?`).bind(id).first();
  if (!m || blocked.has(m.author_id)) fail(404, 'not_found');
  return json(200, publicMod(m, { json: m.hero_json, sprite: m.sprite }));
});

route('POST', '/v1/mods/([0-9a-f]+)/subscribe', async (env, req, [id]) => {
  const a = await needMember(env, req);
  const m = await env.DB.prepare(`${MOD_SELECT} AND m.id = ?`).bind(id).first();
  if (!m || (await blockedIds(env, a)).has(m.author_id)) fail(404, 'not_found');
  await env.DB.prepare('INSERT OR IGNORE INTO mod_subscriptions (mod_id, account_id, created_at) VALUES (?, ?, ?)').bind(id, a.id, now(env)).run();
  return json(200, { subscribed: true });
});

route('DELETE', '/v1/mods/([0-9a-f]+)/subscribe', async (env, req, [id]) => {
  const a = await need(env, req);
  await env.DB.prepare('DELETE FROM mod_subscriptions WHERE mod_id = ? AND account_id = ?').bind(id, a.id).run();
  return json(200, { subscribed: false });
});

// An author can take their own mod down (it goes for everyone, subscriptions too). Not one a moderator removed:
// deleting that row would free its mod id, and the same mod could then be uploaded again as a new visible mod.
route('DELETE', '/v1/mods/([0-9a-f]+)', async (env, req, [id]) => {
  const a = await need(env, req);
  const r = await env.DB.prepare("DELETE FROM mods WHERE id = ? AND author_id = ? AND status != 'removed'").bind(id, a.id).run();
  if (!r.meta.changes) fail(404, 'not_found');
  return json(200, { deleted: true });
});

// ---- reports and blocks ----

const KINDS = { submission: 'submissions', comment: 'comments', account: 'accounts', championComment: 'champion_comments', review: 'reviews', mod: 'mods' };

route('POST', '/v1/reports', async (env, req) => {
  const a = await needMember(env, req);
  const b = await body(req);
  // Own keys only: 'constructor'/'toString' would otherwise reach the SQL as a table name, and ['submission'] would match.
  const table = typeof b.kind === 'string' && Object.hasOwn(KINDS, b.kind) ? KINDS[b.kind] : null;
  if (!table) fail(400, 'bad_kind');
  await rateLimit(env, a, 'reports');
  const target = await env.DB.prepare(`SELECT id FROM ${table} WHERE id = ?`).bind(String(b.id || '')).first();
  // The 'deleted' placeholder isn't a player (same rule as blocking or banning it): a moderator could never act on it.
  if (!target || (table === 'accounts' && target.id === 'deleted')) fail(404, 'not_found');
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
  // The 'deleted' placeholder isn't a player: blocking it would hide every deleted comment and could never be undone
  // (DELETE /v1/blocks/<id> only takes real hex ids).
  if (!(await env.DB.prepare("SELECT id FROM accounts WHERE id = ? AND id != 'deleted'").bind(other).first())) fail(404, 'not_found');
  await env.DB.prepare('INSERT OR IGNORE INTO blocks (blocker_id, blocked_id, created_at) VALUES (?, ?, ?)').bind(a.id, other, now(env)).run();
  return json(201, { ok: true });
});

route('GET', '/v1/blocks', async (env, req) => {
  const a = await need(env, req);
  const { results } = await env.DB.prepare(
    'SELECT b.blocked_id AS id, x.display_name FROM blocks b JOIN accounts x ON x.id = b.blocked_id WHERE b.blocker_id = ? ORDER BY b.created_at DESC')
    .bind(a.id).all();
  return json(200, { blocked: results.map((r) => ({ id: r.id, displayName: r.display_name })) });
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
    `SELECT r.target_kind, r.target_id, COUNT(*) AS reports, GROUP_CONCAT(r.reason, ' | ') AS reasons, MIN(r.created_at) AS first,
            CASE r.target_kind WHEN 'account' THEN r.target_id
              WHEN 'submission' THEN (SELECT author_id FROM submissions WHERE id = r.target_id)
              WHEN 'championComment' THEN (SELECT author_id FROM champion_comments WHERE id = r.target_id)
              WHEN 'review' THEN (SELECT reviewer_id FROM reviews WHERE id = r.target_id)
              WHEN 'mod' THEN (SELECT author_id FROM mods WHERE id = r.target_id)
              ELSE (SELECT author_id FROM comments WHERE id = r.target_id) END AS author_id,
            CASE r.target_kind WHEN 'account' THEN (SELECT display_name FROM accounts WHERE id = r.target_id)
              WHEN 'submission' THEN (SELECT title FROM submissions WHERE id = r.target_id)
              WHEN 'championComment' THEN (SELECT body FROM champion_comments WHERE id = r.target_id)
              WHEN 'review' THEN (SELECT advice FROM reviews WHERE id = r.target_id)
              WHEN 'mod' THEN (SELECT name FROM mods WHERE id = r.target_id)
              ELSE (SELECT body FROM comments WHERE id = r.target_id) END AS preview
       FROM reports r WHERE r.resolved = 0 GROUP BY r.target_kind, r.target_id ORDER BY reports DESC, first`).all();
  return json(200, { reports: results });
});

// Find players by name (part of it) or exact email, banned ones included, so Chris can ban or unban anyone.
route('GET', '/v1/admin/accounts', async (env, req) => {
  await needAdmin(env, req);
  const q = (new URL(req.url).searchParams.get('q') || '').trim().toLowerCase();
  if (q.length < 2) fail(400, 'search_too_short');
  const like = `%${q.replace(/[\\%_]/g, (c) => '\\' + c)}%`;
  const { results } = await env.DB.prepare(
    `SELECT id, display_name, email, banned, role, created_at FROM accounts
      WHERE id != 'deleted' AND (lower(display_name) LIKE ? ESCAPE '\\' OR email = ?) ORDER BY created_at DESC LIMIT 50`)
    .bind(like, q).all();
  return json(200, { accounts: results.map((a) => ({ id: a.id, displayName: a.display_name, email: a.email, banned: !!a.banned, admin: a.role === 'admin' })) });
});

async function setStatus(env, req, status) {
  const admin = await needAdmin(env, req);
  const b = await body(req);
  if (b.kind !== 'submission' && b.kind !== 'comment' && b.kind !== 'championComment' && b.kind !== 'review' && b.kind !== 'mod') fail(400, 'bad_kind');
  // Only a string id: [id] used to String() into a match, change the status, then fail binding the reports update
  // (500, reports left open, no audit line).
  if (typeof b.id !== 'string') fail(400, 'bad_id');
  const r = await env.DB.prepare(`UPDATE ${KINDS[b.kind]} SET status = ? WHERE id = ?`).bind(status, b.id).run();
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
  // The 'deleted' placeholder isn't a player (same rule as blocking it): banning it would hide every deleted player's kept posts.
  if (id === 'deleted') fail(404, 'not_found');
  const r = await env.DB.prepare('UPDATE accounts SET banned = ? WHERE id = ?').bind(banned ? 1 : 0, id).run();
  if (!r.meta.changes) fail(404, 'not_found');
  if (banned) await env.DB.prepare('DELETE FROM sessions WHERE account_id = ?').bind(id).run();
  await env.DB.prepare("UPDATE reports SET resolved = 1 WHERE target_kind = 'account' AND target_id = ?").bind(id).run();
  await audit(env, admin, banned ? 'ban' : 'unban', `account:${id}`);
  return json(200, { ok: true });
}
route('POST', '/v1/admin/ban', (env, req) => setBan(env, req, true));
route('POST', '/v1/admin/unban', (env, req) => setBan(env, req, false));

// Chris, Nov 19 2026: Community is blocked until the player installs the update. Every /v1 request from an app older
// than MIN_APP_VERSION (or one that doesn't say its version, i.e. every build before this check) gets 426 update_required.
// Web pages (/privacy, /delete) stay open.
function tooOld(request, env, url) {
  const min = Number(env.MIN_APP_VERSION || 0);
  if (!min || !url.pathname.startsWith('/v1/')) return false;
  const v = Number(request.headers.get('x-apd-version'));
  return !Number.isInteger(v) || v < min;
}

export async function handle(request, env) {
  const url = new URL(request.url);
  if (tooOld(request, env, url)) return json(426, { error: 'update_required' });
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

// Real deployment: Google tokens are checked against GOOGLE_CLIENT_ID. The mail sender is a binding set up once the
// email provider is chosen; until then sign-up codes can't be delivered and registration answers 503.
let google = null;
export default {
  fetch(request, env) {
    if (!env.GOOGLE) {
      google = google || googleVerifier({ clientId: env.GOOGLE_CLIENT_ID });
      env = { ...env, GOOGLE: google };
    }
    if (!env.MAILER) env = { ...env, MAILER: cloudflareMailer(env) || { async send() { throw new HttpError(503, 'email_not_set_up'); } } };
    return handle(request, env);
  },
};
