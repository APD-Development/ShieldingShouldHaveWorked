import { test } from 'node:test';
import assert from 'node:assert/strict';
import { handle } from '../src/app.js';
import { testDb } from './d1-node.js';

function setup() {
  const mail = [];
  let clock = 1_800_000_000_000;
  const db = testDb();
  // An open season so posting works; season tests make their own.
  db.raw.prepare('INSERT INTO seasons VALUES (?, ?, ?, ?, ?, ?, ?)').run('s0', 'Test season', clock - 1, clock + 1e12, clock + 2e12, clock + 3e12, clock);
  const env = {
    DB: db,
    MAILER: { async send(to, subject, text) { mail.push({ to, subject, text }); } },
    GOOGLE: { async verify(t) { return t.startsWith('good:') ? { sub: t.slice(5), email: `${t.slice(5)}@gmail.com` } : null; } },
    ADMIN_EMAILS: 'chris@example.com',
    EMAIL_SIGNIN: 'on', // most tests use the (dormant) email path to make members quickly
    GOOGLE_CLIENT_ID: 'cid.apps.googleusercontent.com',
    now: () => clock,
  };
  let ipCounter = 0;
  // Each test request comes from its own address unless a test sets one (the sign-up limit is per address).
  const call = async (method, path, body, token, ip) => {
    const headers = { 'content-type': 'application/json', 'cf-connecting-ip': ip || `10.0.0.${++ipCounter}` };
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
  const row = env.DB.raw.prepare("SELECT password_hash FROM accounts WHERE id != 'deleted'").get();
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
  assert.deepEqual([q.length, q[0].reports, q[0].author_id, q[0].preview], [1, 2, troll.id, 'bait']);
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
  assert.deepEqual(['accounts', 'submissions', 'comments', 'reports', 'blocks'].map(count), [2, 1, 0, 0, 0]); // + the [deleted] placeholder
  assert.equal((await call('POST', '/v1/auth/login', { email: 'gone@x.com', password: 'hunter2hunter2' })).status, 401);
});

test('there is no private-message route', async () => {
  const { call, member } = setup();
  const m = await member('pm@x.com');
  for (const p of ['/v1/messages', '/v1/dm', '/v1/inbox']) assert.equal((await call('POST', p, { to: 'x', body: 'hi' }, m.token)).status, 404);
});

test('seasons: submissions only during Month 1, discussion continues, the list shows the current season', async () => {
  const { env, call, member, tick } = setup();
  env.DB.raw.prepare('DELETE FROM seasons').run();
  const chris = await member('chris@example.com', 'Chris');
  const ana = await member('ana@x.com', 'Ana');
  assert.equal((await call('GET', '/v1/season')).body.season, null);
  assert.equal((await call('POST', '/v1/submissions', { title: 't', body: 'b' }, ana.token)).body.error, 'submissions_closed');
  const min = 60 * 1000;
  assert.equal((await call('POST', '/v1/admin/seasons', { title: 'x' }, ana.token)).status, 403);
  const r = await call('POST', '/v1/admin/seasons', { title: 'Season 1', submissionMs: 10 * min, votingMs: 10 * min, tournamentMs: 20 * min }, chris.token);
  assert.equal(r.status, 201);
  assert.equal((await call('POST', '/v1/admin/seasons', { title: 'Clash' }, chris.token)).body.error, 'season_overlaps');
  assert.equal((await call('GET', '/v1/season')).body.season.phase, 'submissions');
  const s = (await call('POST', '/v1/submissions', { title: 'Idea', body: 'b' }, ana.token)).body.id;
  tick(10 * min);
  assert.equal((await call('GET', '/v1/season')).body.season.phase, 'voting');
  assert.equal((await call('POST', '/v1/submissions', { title: 'Late', body: 'b' }, ana.token)).body.error, 'submissions_closed');
  assert.equal((await call('POST', `/v1/submissions/${s}/comments`, { body: 'still talking' }, chris.token)).status, 201);
  tick(10 * min);
  assert.equal((await call('GET', '/v1/season')).body.season.phase, 'tournament');
  tick(20 * min);
  assert.equal((await call('GET', '/v1/season')).body.season.phase, 'finished');
  assert.equal((await call('GET', '/v1/submissions')).body.submissions.length, 1);
  await call('POST', '/v1/admin/seasons', { title: 'Season 2' }, chris.token);
  assert.equal((await call('GET', '/v1/season')).body.season.title, 'Season 2');
  assert.equal((await call('GET', '/v1/submissions')).body.submissions.length, 0);
});

test('full season on a fast clock: upvotes to qualify, a 16-seed bracket from week 1 to a champion', async () => {
  const { env, call, member, tick } = setup();
  env.DB.raw.prepare('DELETE FROM seasons').run();
  const min = 60 * 1000;
  const chris = await member('chris@example.com', 'Chris');
  const authors = [];
  for (let i = 0; i < 10; i++) authors.push(await member(`author${i}@x.com`, `A${i}`));
  const voters = [];
  for (let i = 0; i < 4; i++) voters.push(await member(`voter${i}@x.com`, `V${i}`));
  await call('POST', '/v1/admin/seasons', { title: 'S', submissionMs: 10 * min, votingMs: 10 * min, tournamentMs: 40 * min }, chris.token);
  const subs = [];
  for (let i = 0; i < 10; i++) { subs.push((await call('POST', '/v1/submissions', { title: `Idea ${i}`, body: 'b' }, authors[i].token)).body.id); tick(1); }
  assert.equal((await call('POST', `/v1/submissions/${subs[0]}/upvote`, null, voters[0].token)).body.error, 'voting_closed');
  tick(10 * min); // Month 2
  assert.equal((await call('POST', `/v1/submissions/${subs[0]}/upvote`, null, authors[0].token)).body.error, 'own_submission');
  // Idea 9 gets 4 upvotes, Idea 8 gets 3, ... ; one upvote taken back
  for (let i = 0; i < 4; i++) for (let k = 9; k >= 9 - i; k--) await call('POST', `/v1/submissions/${subs[k]}/upvote`, null, voters[i].token);
  await call('POST', `/v1/submissions/${subs[0]}/upvote`, null, voters[0].token);
  await call('DELETE', `/v1/submissions/${subs[0]}/upvote`, null, voters[0].token);
  let r = (await call('GET', '/v1/season/results', null, voters[0].token)).body;
  assert.equal(r.standings[0].title, 'Idea 9');
  assert.equal(r.standings[0].upvotes, 4);
  assert.ok(!r.myUpvotes.includes(subs[0]));
  // A banned voter's upvotes stop counting
  await call('POST', '/v1/admin/ban', { accountId: voters[3].id }, chris.token);
  r = (await call('GET', '/v1/season/results')).body;
  assert.equal(r.standings.find((x) => x.title === 'Idea 6').upvotes, 0);
  await call('POST', '/v1/admin/unban', { accountId: voters[3].id }, chris.token);
  voters[3].token = (await call('POST', '/v1/auth/login', { email: 'voter3@x.com', password: 'hunter2hunter2' })).body.token; // the ban signed them out
  tick(10 * min); // Tournament week 1: the bracket starts straight from Month 2's upvotes
  r = (await call('GET', '/v1/season/results')).body;
  assert.deepEqual([r.season.phase, r.week, r.seeds.length], ['tournament', 1, 10]);
  assert.deepEqual(r.seeds.slice(0, 4).map((s) => s.title), ['Idea 9', 'Idea 8', 'Idea 7', 'Idea 6']);
  assert.equal(r.rounds[0].matchups.length, 8);
  assert.equal((await call('POST', '/v1/tournament/picks', { submissionIds: [subs[0]] }, voters[0].token)).status, 404);
  const m0 = r.rounds[0].matchups[0]; // seed 1 vs seed 16: only 10 entries, so a bye
  assert.ok(m0.bye);
  assert.equal((await call('POST', '/v1/tournament/vote', { matchup: 0, submissionId: m0.a.id }, voters[0].token)).body.error, 'bye_no_vote');
  const m1 = r.rounds[0].matchups[7]; // seed 8 vs seed 9
  assert.deepEqual([m1.a.title, m1.b.title], ['Idea 3', 'Idea 4']);
  assert.equal((await call('POST', '/v1/tournament/vote', { matchup: 7, submissionId: subs[9] }, voters[0].token)).body.error, 'not_in_matchup');
  assert.equal((await call('POST', '/v1/tournament/vote', { matchup: 7, submissionId: m1.b.id }, authors[4].token)).body.error, 'own_submission');
  // Upset: seed 9 beats seed 8; a voter changes their mind and only the latest vote counts
  await call('POST', '/v1/tournament/vote', { matchup: 7, submissionId: m1.a.id }, voters[0].token);
  await call('POST', '/v1/tournament/vote', { matchup: 7, submissionId: m1.b.id }, voters[0].token);
  await call('POST', '/v1/tournament/vote', { matchup: 7, submissionId: m1.b.id }, voters[1].token);
  r = (await call('GET', '/v1/season/results')).body;
  assert.deepEqual([r.rounds[0].matchups[7].a.votes, r.rounds[0].matchups[7].b.votes, r.rounds[0].matchups[7].winner], [0, 2, null]);
  tick(10 * min); // Week 2
  r = (await call('GET', '/v1/season/results')).body;
  assert.equal(r.rounds[0].matchups[7].winner, m1.b.id);
  assert.deepEqual([r.rounds[1].matchups[0].a.title, r.rounds[1].matchups[0].b.title], ['Idea 9', 'Idea 4']);
  tick(10 * min); // Week 3
  tick(10 * min); // Week 4: final
  tick(10 * min); // finished
  r = (await call('GET', '/v1/season/results')).body;
  assert.equal(r.season.phase, 'finished');
  assert.equal(r.rounds.length, 4);
  assert.ok(r.champion && r.champion.title);
  assert.equal((await call('POST', '/v1/tournament/vote', { matchup: 7, submissionId: m1.a.id }, voters[0].token)).body.error, 'no_matchups_open');
});

test('anti-spam caps: 5 submissions a day, 10 comments per 10 minutes', async () => {
  const { call, member, tick } = setup();
  const m = await member('spam@x.com');
  for (let i = 0; i < 5; i++) assert.equal((await call('POST', '/v1/submissions', { title: `t${i}`, body: 'b' }, m.token)).status, 201);
  assert.equal((await call('POST', '/v1/submissions', { title: 'six', body: 'b' }, m.token)).body.error, 'too_many_submissions');
  const s = (await call('GET', '/v1/submissions')).body.submissions[0].id;
  for (let i = 0; i < 10; i++) await call('POST', `/v1/submissions/${s}/comments`, { body: `c${i}` }, m.token);
  assert.equal((await call('POST', `/v1/submissions/${s}/comments`, { body: 'more' }, m.token)).status, 429);
  tick(10 * 60 * 1000 + 1);
  assert.equal((await call('POST', `/v1/submissions/${s}/comments`, { body: 'later' }, m.token)).status, 201);
  tick(24 * 3600 * 1000);
  assert.equal((await call('POST', '/v1/submissions', { title: 'next day', body: 'b' }, m.token)).status, 201);
});

test('deleting an account keeps other players\' replies; the deleted comment becomes an empty placeholder', async () => {
  const { call, member } = setup();
  const a = await member('leaving@x.com', 'Leaving');
  const b = await member('staying@x.com', 'Staying');
  const s = (await call('POST', '/v1/submissions', { title: 't', body: 'b' }, b.token)).body.id;
  const parent = (await call('POST', `/v1/submissions/${s}/comments`, { body: 'my take' }, a.token)).body.id;
  await call('POST', `/v1/submissions/${s}/comments`, { body: 'lonely' }, a.token);
  await call('POST', `/v1/submissions/${s}/comments`, { body: 'good point', parentId: parent }, b.token);
  await call('DELETE', '/v1/account', null, a.token);
  const view = (await call('GET', `/v1/submissions/${s}`)).body;
  assert.equal(view.comments.length, 2);
  assert.deepEqual([view.comments[0].id, view.comments[0].body, view.comments[0].author], [parent, null, null]);
  assert.deepEqual([view.comments[1].body, view.comments[1].parentId], ['good point', parent]);
});

test('guessing limits: 10 wrong passwords per email in 15 minutes, 5 sign-ups per address per hour', async () => {
  const { call, member, tick } = setup();
  await member('target@x.com');
  for (let i = 0; i < 10; i++) assert.equal((await call('POST', '/v1/auth/login', { email: 'target@x.com', password: `guess${i}xx` })).status, 401);
  const blocked = await call('POST', '/v1/auth/login', { email: 'target@x.com', password: 'hunter2hunter2' });
  assert.deepEqual([blocked.status, blocked.body.error], [429, 'slow_down']);
  tick(15 * 60 * 1000 + 1);
  assert.equal((await call('POST', '/v1/auth/login', { email: 'target@x.com', password: 'hunter2hunter2' })).status, 200);
  for (let i = 0; i < 5; i++) assert.equal((await call('POST', '/v1/auth/register', { email: `s${i}@x.com`, password: 'longenough', displayName: 'S' }, null, '9.9.9.9')).status, 201);
  assert.equal((await call('POST', '/v1/auth/register', { email: 's6@x.com', password: 'longenough', displayName: 'S' }, null, '9.9.9.9')).status, 429);
  assert.equal((await call('POST', '/v1/auth/register', { email: 's7@x.com', password: 'longenough', displayName: 'S' }, null, '8.8.8.8')).status, 201);
});

test('blocked players list shows who I blocked, and unblocking removes them', async () => {
  const { call, member } = setup();
  const me = await member('me@x.com', 'Me');
  const pest = await member('pest@x.com', 'Pest');
  assert.deepEqual((await call('GET', '/v1/blocks', null, me.token)).body.blocked, []);
  await call('POST', '/v1/blocks', { accountId: pest.id }, me.token);
  assert.deepEqual((await call('GET', '/v1/blocks', null, me.token)).body.blocked, [{ id: pest.id, displayName: 'Pest' }]);
  await call('DELETE', `/v1/blocks/${pest.id}`, null, me.token);
  assert.deepEqual((await call('GET', '/v1/blocks', null, me.token)).body.blocked, []);
  assert.equal((await call('GET', '/v1/blocks')).status, 401);
});

test('web deletion page (for Google Play): Google sign-in deletes that account, nothing else', async () => {
  const { env, call } = setup();
  const g = (await call('POST', '/v1/auth/google', { idToken: 'good:web1', displayName: 'Web' })).body.token;
  await call('POST', '/v1/auth/google', { idToken: 'good:other', displayName: 'Other' });
  const page = await handle(new Request('https://x/delete'), env);
  assert.equal(page.status, 200);
  const htmlText = await page.text();
  assert.match(htmlText, /accounts\.google\.com\/gsi\/client/);
  assert.match(htmlText, /data-client_id="cid\.apps\.googleusercontent\.com"/);
  assert.doesNotMatch(htmlText, /type="password"/);
  const post = (cred) => handle(new Request('https://x/delete', { method: 'POST', body: new URLSearchParams({ credential: cred }) }), env);
  assert.equal((await post('bad')).status, 401);
  assert.equal((await post('good:nobody')).status, 404);
  const ok = await post('good:web1');
  assert.equal(ok.status, 200);
  assert.match(await ok.text(), /Your account is deleted/);
  assert.equal((await call('GET', '/v1/me', null, g)).status, 401);
  assert.equal(env.DB.raw.prepare("SELECT COUNT(*) AS n FROM accounts WHERE google_sub = 'other'").get().n, 1);
});

test('launch is Google-only: email sign-up and login answer 404 unless EMAIL_SIGNIN is on', async () => {
  const { env, call } = setup();
  env.EMAIL_SIGNIN = '';
  for (const path of ['/v1/auth/register', '/v1/auth/resend', '/v1/auth/verify', '/v1/auth/login']) {
    assert.equal((await call('POST', path, { email: 'a@x.com', password: 'longenough', displayName: 'A', code: '123456' })).status, 404, path);
  }
  assert.equal((await call('POST', '/v1/auth/google', { idToken: 'good:ok' })).status, 200);
});

test('Google accounts store no email or password; the admin email in the token still makes the admin', async () => {
  const { env, call } = setup();
  env.EMAIL_SIGNIN = '';
  const p = await call('POST', '/v1/auth/google', { idToken: 'good:player1' });
  const row = env.DB.raw.prepare("SELECT email, password_hash FROM accounts WHERE google_sub = 'player1'").get();
  assert.equal(row.email, null);
  assert.equal(row.password_hash, null);
  assert.equal((await call('GET', '/v1/me', null, p.body.token)).body.admin, false);
  env.ADMIN_EMAILS = 'chris@gmail.com';
  const c = await call('POST', '/v1/auth/google', { idToken: 'good:chris' });
  assert.equal((await call('GET', '/v1/me', null, c.body.token)).body.admin, true);
  assert.equal(env.DB.raw.prepare("SELECT email FROM accounts WHERE google_sub = 'chris'").get().email, null);
  env.ADMIN_EMAILS = 'player1@gmail.com';
  const again = await call('POST', '/v1/auth/google', { idToken: 'good:player1' });
  assert.equal((await call('GET', '/v1/me', null, again.body.token)).body.admin, true);
});

test('an account made before its email was set as admin becomes admin at the next sign-in', async () => {
  const { env, call, member } = setup();
  env.ADMIN_EMAILS = '';
  await member('later@x.com', 'Later');
  env.ADMIN_EMAILS = 'later@x.com';
  const t = (await call('POST', '/v1/auth/login', { email: 'later@x.com', password: 'hunter2hunter2' })).body.token;
  assert.equal((await call('GET', '/v1/me', null, t)).body.admin, true);
});

test('admin can find players by part of their name or exact email, banned ones included', async () => {
  const { call, member } = setup();
  const chris = await member('chris@example.com', 'Chris');
  const t = await member('troll99@x.com', 'TrollFace');
  await call('POST', '/v1/admin/ban', { accountId: t.id }, chris.token);
  const byName = (await call('GET', '/v1/admin/accounts?q=troll', null, chris.token)).body.accounts;
  assert.deepEqual(byName.map((a) => [a.displayName, a.banned]), [['TrollFace', true]]);
  assert.equal((await call('GET', '/v1/admin/accounts?q=troll99@x.com', null, chris.token)).body.accounts[0].id, t.id);
  assert.equal((await call('GET', '/v1/admin/accounts?q=%25%25', null, chris.token)).body.accounts.length, 0); // % is literal
  assert.equal((await call('GET', '/v1/admin/accounts?q=t', null, chris.token)).body.error, 'search_too_short');
  assert.equal((await call('GET', '/v1/admin/accounts?q=troll', null, (await member('x@x.com')).token)).status, 403);
});

test('privacy policy page is served for Google Play', async () => {
  const { env } = setup();
  const r = await handle(new Request('https://x/privacy'), env);
  assert.equal(r.status, 200);
  const t = await r.text();
  assert.match(t, /ADHD Pixel Dungeon Privacy Policy/);
  assert.match(t, /\/delete/);
});

test('selected content: a finished season\'s champion gets a page with admin status, dev notes, comments and history', async () => {
  const { env, call, member, tick } = setup();
  env.DB.raw.prepare('DELETE FROM seasons').run();
  const min = 60 * 1000;
  const chris = await member('chris@example.com', 'Chris');
  const ana = await member('ana@x.com', 'Ana');
  const bo = await member('bo@x.com', 'Bo');
  const fan = await member('fan@x.com', 'Fan');
  const run = async (title) => {
    const sid = (await call('POST', '/v1/admin/seasons', { title, submissionMs: 10 * min, votingMs: 10 * min, tournamentMs: 40 * min }, chris.token)).body.id;
    const a = (await call('POST', '/v1/submissions', { title: `${title} Ana idea`, body: 'Ana body' }, ana.token)).body.id;
    tick(1);
    await call('POST', '/v1/submissions', { title: `${title} Bo idea`, body: 'Bo body' }, bo.token);
    tick(10 * min);
    await call('POST', `/v1/submissions/${a}/upvote`, null, fan.token);
    return sid;
  };
  const s1 = await run('S1');
  assert.deepEqual((await call('GET', '/v1/champions')).body.champions, []); // still voting: no champion yet
  assert.equal((await call('GET', `/v1/champions/${s1}`)).status, 404);
  tick(50 * min); // tournament done
  let list = (await call('GET', '/v1/champions')).body.champions;
  assert.equal(list.length, 1);
  assert.deepEqual([list[0].seasonId, list[0].seasonTitle, list[0].title, list[0].status, list[0].author.displayName],
    [s1, 'S1', 'S1 Ana idea', 'planned', 'Ana']);
  // Frozen: banning the only upvoter afterwards doesn't move the champion
  await call('POST', '/v1/admin/ban', { accountId: fan.id }, chris.token);
  assert.equal((await call('GET', `/v1/champions/${s1}`)).body.title, 'S1 Ana idea');
  // Admin controls: status (only the 4 values, admin only) and dev notes
  assert.equal((await call('POST', `/v1/admin/champions/${s1}/status`, { status: 'testing' }, ana.token)).status, 403);
  assert.equal((await call('POST', `/v1/admin/champions/${s1}/status`, { status: 'shipped' }, chris.token)).body.error, 'bad_status');
  assert.equal((await call('POST', `/v1/admin/champions/${s1}/status`, { status: 'in_development' }, chris.token)).status, 200);
  assert.equal((await call('POST', `/v1/admin/champions/${s1}/notes`, { body: 'Started the sprites.' }, ana.token)).status, 403);
  const n1 = (await call('POST', `/v1/admin/champions/${s1}/notes`, { body: 'Started the sprites.' }, chris.token)).body.id;
  tick(1);
  const n2 = (await call('POST', `/v1/admin/champions/${s1}/notes`, { body: 'Typo note' }, chris.token)).body.id;
  assert.equal((await call('DELETE', `/v1/admin/champions/${s1}/notes/${n2}`, null, chris.token)).status, 200);
  // Players comment, threaded; a reported comment can be hidden by the admin
  const c1 = (await call('POST', `/v1/champions/${s1}/comments`, { body: 'Cannot wait' }, bo.token)).body.id;
  assert.equal((await call('POST', `/v1/champions/${s1}/comments`, { body: 'Same', parentId: c1 }, ana.token)).status, 201);
  assert.equal((await call('POST', `/v1/champions/${s1}/comments`, { body: 'x', parentId: 'nope' }, ana.token)).body.error, 'bad_parent');
  assert.equal((await call('POST', `/v1/champions/${s1}/comments`, { body: 'anon' })).status, 401);
  assert.equal((await call('POST', '/v1/reports', { kind: 'championComment', id: c1, reason: 'spam' }, ana.token)).status, 201);
  const reports = (await call('GET', '/v1/admin/reports', null, chris.token)).body.reports;
  assert.deepEqual([reports[0].target_kind, reports[0].preview], ['championComment', 'Cannot wait']);
  assert.equal((await call('POST', '/v1/admin/hide', { kind: 'championComment', id: c1 }, chris.token)).status, 200);
  let page = (await call('GET', `/v1/champions/${s1}`)).body;
  assert.deepEqual([page.status, page.body, page.notes.map((n) => n.id)], ['in_development', 'Ana body', [n1]]);
  assert.deepEqual(page.comments.map((c) => c.body), [null, 'Same']);
  // History: a second season adds a second champion, newest first
  const s2 = await run('S2');
  tick(50 * min);
  list = (await call('GET', '/v1/champions')).body.champions;
  assert.deepEqual(list.map((c) => [c.seasonId, c.status]), [[s2, 'planned'], [s1, 'in_development']]);
  // The champion's author deletes their account: the page stays, without their name or text
  await call('DELETE', '/v1/account', null, ana.token);
  page = (await call('GET', `/v1/champions/${s1}`)).body;
  assert.deepEqual([page.title, page.author, page.body], ['S1 Ana idea', null, '']);
});
