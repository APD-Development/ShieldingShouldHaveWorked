import { test } from 'node:test';
import assert from 'node:assert/strict';
import { handle } from '../src/app.js';
import { testDb } from './d1-node.js';

function setup() {
  const mail = [];
  let clock = 1_800_000_000_000;
  const env = {
    DB: testDb(),
    MAILER: { async send(to, subject, text) { mail.push({ to, subject, text }); } },
    GOOGLE: { async verify(t) { return t.startsWith('good:') ? { sub: t.slice(5), email: `${t.slice(5)}@gmail.com` } : null; } },
    ADMIN_EMAILS: 'chris@example.com',
    now: () => clock,
  };
  const call = async (method, path, body, token) => {
    const headers = { 'content-type': 'application/json' };
    if (token) headers.authorization = `Bearer ${token}`;
    const res = await handle(new Request(`https://x${path}`, { method, headers, body: body ? JSON.stringify(body) : undefined }), env);
    return { status: res.status, body: await res.json() };
  };
  const lastCode = (to) => [...mail].reverse().find((m) => m.to === to).text.match(/\d{6}/)[0];
  // A verified, 16+ member, ready to post.
  const member = async (email, name = email.split('@')[0]) => {
    await call('POST', '/v1/auth/register', { email, password: 'hunter2hunter2', displayName: name });
    const { body } = await call('POST', '/v1/auth/verify', { email, code: lastCode(email) });
    await call('POST', '/v1/account/age', { passed: true }, body.token);
    const me = await call('GET', '/v1/me', null, body.token);
    return { token: body.token, id: me.body.id };
  };
  return { env, mail, call, lastCode, member, tick: (ms) => { clock += ms; } };
}

test('register sends a code; the code verifies the email and signs in', async () => {
  const { call, mail, lastCode } = setup();
  const r = await call('POST', '/v1/auth/register', { email: 'A@Example.com', password: 'longenough', displayName: 'Ana' });
  assert.equal(r.status, 201);
  assert.equal(mail.length, 1);
  assert.equal(mail[0].to, 'a@example.com');
  const login = await call('POST', '/v1/auth/login', { email: 'a@example.com', password: 'longenough' });
  assert.deepEqual([login.status, login.body.error], [403, 'email_not_verified']);
  const v = await call('POST', '/v1/auth/verify', { email: 'a@example.com', code: lastCode('a@example.com') });
  assert.equal(v.status, 200);
  const me = await call('GET', '/v1/me', null, v.body.token);
  assert.deepEqual([me.body.displayName, me.body.emailVerified, me.body.ageOk, me.body.admin], ['Ana', true, false, false]);
  const again = await call('POST', '/v1/auth/login', { email: 'a@example.com', password: 'longenough' });
  assert.equal(again.status, 200);
});

test('wrong codes are limited, and codes expire', async () => {
  const { call, lastCode, tick } = setup();
  await call('POST', '/v1/auth/register', { email: 'b@x.com', password: 'longenough', displayName: 'B' });
  const good = lastCode('b@x.com');
  const wrong = good === '000000' ? '111111' : '000000';
  for (let i = 0; i < 5; i++) assert.equal((await call('POST', '/v1/auth/verify', { email: 'b@x.com', code: wrong })).body.error, 'bad_code');
  assert.equal((await call('POST', '/v1/auth/verify', { email: 'b@x.com', code: good })).body.error, 'code_expired');
  await call('POST', '/v1/auth/resend', { email: 'b@x.com' });
  tick(16 * 60 * 1000);
  assert.equal((await call('POST', '/v1/auth/verify', { email: 'b@x.com', code: lastCode('b@x.com') })).body.error, 'code_expired');
});

test('passwords are hashed, short ones refused, wrong ones rejected, verified emails cannot be re-registered', async () => {
  const { env, call, member } = setup();
  assert.equal((await call('POST', '/v1/auth/register', { email: 'c@x.com', password: 'short', displayName: 'C' })).body.error, 'weak_password');
  await member('c@x.com');
  const row = env.DB.raw.prepare('SELECT password_hash FROM accounts').get();
  assert.ok(row.password_hash.startsWith('pbkdf2$') && !row.password_hash.includes('hunter2'));
  assert.equal((await call('POST', '/v1/auth/login', { email: 'c@x.com', password: 'nope-nope-nope' })).status, 401);
  assert.equal((await call('POST', '/v1/auth/register', { email: 'c@x.com', password: 'longenough', displayName: 'C' })).body.error, 'email_taken');
});

test('Google sign-in makes its own account and signs back into it', async () => {
  const { call } = setup();
  assert.equal((await call('POST', '/v1/auth/google', { idToken: 'bad' })).status, 401);
  const first = await call('POST', '/v1/auth/google', { idToken: 'good:123', displayName: 'Gee' });
  const second = await call('POST', '/v1/auth/google', { idToken: 'good:123' });
  const a = await call('GET', '/v1/me', null, first.body.token);
  const b = await call('GET', '/v1/me', null, second.body.token);
  assert.equal(a.body.id, b.body.id);
  assert.equal(a.body.emailVerified, true);
});

test('posting needs a verified, 16+ account; the server only ever records a pass', async () => {
  const { call, lastCode } = setup();
  await call('POST', '/v1/auth/register', { email: 'd@x.com', password: 'longenough', displayName: 'D' });
  const { body } = await call('POST', '/v1/auth/verify', { email: 'd@x.com', code: lastCode('d@x.com') });
  const post = () => call('POST', '/v1/submissions', { title: 'Idea', body: 'A new boss' }, body.token);
  assert.equal((await post()).body.error, 'age_gate');
  assert.equal((await call('POST', '/v1/account/age', { passed: false }, body.token)).status, 400);
  assert.equal((await post()).body.error, 'age_gate');
  await call('POST', '/v1/account/age', { passed: true }, body.token);
  assert.equal((await post()).status, 201);
  assert.equal((await call('POST', '/v1/submissions', { title: 'x', body: 'y' })).status, 401);
});

test('threaded comments: replies point at their parent; a removed comment stays as an empty placeholder', async () => {
  const { call, member } = setup();
  const ana = await member('chris@example.com', 'Chris');
  const bo = await member('bo@x.com', 'Bo');
  const s = (await call('POST', '/v1/submissions', { title: 'Shield rework', body: 'Shields should have worked' }, bo.token)).body.id;
  const top = (await call('POST', `/v1/submissions/${s}/comments`, { body: 'Love it' }, ana.token)).body.id;
  const reply = (await call('POST', `/v1/submissions/${s}/comments`, { body: 'Thanks', parentId: top }, bo.token)).body.id;
  assert.equal((await call('POST', `/v1/submissions/${s}/comments`, { body: 'x', parentId: 'ffff' }, bo.token)).body.error, 'bad_parent');
  let view = (await call('GET', `/v1/submissions/${s}`)).body;
  assert.deepEqual(view.comments.map((c) => [c.id, c.parentId]), [[top, null], [reply, top]]);
  await call('POST', '/v1/admin/remove', { kind: 'comment', id: top }, ana.token);
  view = (await call('GET', `/v1/submissions/${s}`)).body;
  assert.equal(view.comments[0].body, null);
  assert.equal(view.comments[1].body, 'Thanks');
  const list = (await call('GET', '/v1/submissions')).body.submissions;
  assert.deepEqual([list[0].title, list[0].comments], ['Shield rework', 1]);
});

test('filter: slurs are refused, swears are masked, harmless words pass', async () => {
  const { call, member } = setup();
  const m = await member('e@x.com');
  const post = (body) => call('POST', '/v1/submissions', { title: 'T', body }, m.token);
  assert.equal((await post('you f.a.g.g.0.t')).body.error, 'not_allowed_words');
  const id = (await post('this boss is fucking hard, like Sh1t')).body.id;
  assert.equal((await call('GET', `/v1/submissions/${id}`)).body.body, 'this boss is ****ing hard, like ****');
  const id2 = (await post('Cockatrice in Scunthorpe, a classic assassin')).body.id;
  assert.equal((await call('GET', `/v1/submissions/${id2}`)).body.body, 'Cockatrice in Scunthorpe, a classic assassin');
  assert.equal((await call('POST', '/v1/auth/register', { email: 'f@x.com', password: 'longenough', displayName: 'n1gger' })).body.error, 'not_allowed_words');
});

test('report and block: blocking hides that player from me; reports reach the admin queue once per reporter', async () => {
  const { call, member } = setup();
  const chris = await member('chris@example.com', 'Chris');
  const ana = await member('ana@x.com', 'Ana');
  const troll = await member('troll@x.com', 'Troll');
  const s = (await call('POST', '/v1/submissions', { title: 'bait', body: 'bait' }, troll.token)).body.id;
  await call('POST', `/v1/submissions/${s}/comments`, { body: 'hi' }, troll.token);
  for (let i = 0; i < 2; i++) await call('POST', '/v1/reports', { kind: 'submission', id: s, reason: 'spam' }, ana.token);
  await call('POST', '/v1/reports', { kind: 'submission', id: s, reason: 'rude' }, chris.token);
  assert.equal((await call('GET', '/v1/admin/reports', null, ana.token)).status, 403);
  const q = (await call('GET', '/v1/admin/reports', null, chris.token)).body.reports;
  assert.deepEqual([q.length, q[0].reports], [1, 2]);
  assert.equal((await call('POST', '/v1/blocks', { accountId: ana.id }, ana.token)).body.error, 'cannot_block_self');
  await call('POST', '/v1/blocks', { accountId: troll.id }, ana.token);
  assert.equal((await call('GET', '/v1/submissions', null, ana.token)).body.submissions.length, 0);
  assert.equal((await call('GET', `/v1/submissions/${s}`, null, ana.token)).status, 404);
  assert.equal((await call('GET', '/v1/submissions', null, chris.token)).body.submissions.length, 1);
  await call('DELETE', `/v1/blocks/${troll.id}`, null, ana.token);
  assert.equal((await call('GET', '/v1/submissions', null, ana.token)).body.submissions.length, 1);
});

test('admin: hide, remove and restore posts, ban and unban accounts; banned players are signed out and hidden', async () => {
  const { env, call, member } = setup();
  const chris = await member('chris@example.com', 'Chris');
  const bad = await member('bad@x.com', 'Bad');
  const s = (await call('POST', '/v1/submissions', { title: 't', body: 'b' }, bad.token)).body.id;
  assert.equal((await call('POST', '/v1/admin/hide', { kind: 'submission', id: s }, bad.token)).status, 403);
  await call('POST', '/v1/admin/hide', { kind: 'submission', id: s }, chris.token);
  assert.equal((await call('GET', '/v1/submissions')).body.submissions.length, 0);
  await call('POST', '/v1/admin/restore', { kind: 'submission', id: s }, chris.token);
  assert.equal((await call('GET', '/v1/submissions')).body.submissions.length, 1);
  await call('POST', '/v1/admin/ban', { accountId: bad.id }, chris.token);
  assert.equal((await call('GET', '/v1/me', null, bad.token)).status, 401);
  assert.equal((await call('POST', '/v1/auth/login', { email: 'bad@x.com', password: 'hunter2hunter2' })).body.error, 'banned');
  assert.equal((await call('GET', '/v1/submissions')).body.submissions.length, 0);
  assert.equal((await call('POST', '/v1/admin/ban', { accountId: chris.id }, chris.token)).body.error, 'cannot_ban_self');
  await call('POST', '/v1/admin/unban', { accountId: bad.id }, chris.token);
  assert.equal((await call('GET', '/v1/submissions')).body.submissions.length, 1);
  const log = env.DB.raw.prepare('SELECT action FROM audit ORDER BY id').all().map((r) => r.action);
  assert.deepEqual(log, ['hidden', 'visible', 'ban', 'unban']);
});

test('account deletion removes the account, its sessions, posts, comments, reports and blocks', async () => {
  const { env, call, member } = setup();
  const a = await member('gone@x.com');
  const b = await member('stay@x.com');
  const s = (await call('POST', '/v1/submissions', { title: 't', body: 'b' }, a.token)).body.id;
  await call('POST', `/v1/submissions/${s}/comments`, { body: 'c' }, a.token);
  const s2 = (await call('POST', '/v1/submissions', { title: 't2', body: 'b2' }, b.token)).body.id;
  await call('POST', `/v1/submissions/${s2}/comments`, { body: 'mine' }, a.token);
  await call('POST', '/v1/reports', { kind: 'submission', id: s2, reason: 'x' }, a.token);
  await call('POST', '/v1/blocks', { accountId: b.id }, a.token);
  assert.equal((await call('DELETE', '/v1/account', null, a.token)).status, 200);
  assert.equal((await call('GET', '/v1/me', null, a.token)).status, 401);
  const count = (t) => env.DB.raw.prepare(`SELECT COUNT(*) AS n FROM ${t}`).get().n;
  assert.deepEqual(['accounts', 'submissions', 'comments', 'reports', 'blocks'].map(count), [1, 1, 0, 0, 0]);
  assert.equal((await call('POST', '/v1/auth/login', { email: 'gone@x.com', password: 'hunter2hunter2' })).status, 401);
});

test('there is no private-message route', async () => {
  const { call, member } = setup();
  const m = await member('pm@x.com');
  for (const p of ['/v1/messages', '/v1/dm', '/v1/inbox']) assert.equal((await call('POST', p, { to: 'x', body: 'hi' }, m.token)).status, 404);
});
