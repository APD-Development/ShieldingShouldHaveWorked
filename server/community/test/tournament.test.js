import { test } from 'node:test';
import assert from 'node:assert/strict';
import { seeds, bracket } from '../src/tournament.js';

const e = (id, score, created_at = 0) => ({ id, score, created_at });

test('Month 2: the 16 most-upvoted are seeded 1-16; a tie for a seed goes to the earlier submission', () => {
  assert.deepEqual(seeds([e('a', 1), e('b', 3)]).map((x) => x.id), ['b', 'a']);
  const many = [];
  for (let i = 0; i < 15; i++) many.push(e(`top${i}`, 100 - i));
  many.push(e('tieLate', 10, 3), e('tieEarly', 10, 1), e('low', 9));
  const s = seeds(many);
  assert.equal(s.length, 16);
  assert.equal(s[15].id, 'tieEarly');
  assert.ok(!s.some((x) => x.id === 'tieLate' || x.id === 'low'));
});

test('Week 1 is a 16-seed round: 1v16, 8v9, 5v12, 4v13, 6v11, 3v14, 7v10, 2v15, then 8 -> 4 -> 2 -> 1', () => {
  const s = Array.from({ length: 16 }, (_, i) => ({ id: `s${i + 1}` }));
  const quiet = bracket(s, new Map(), 4);
  assert.equal(quiet.length, 4);
  assert.deepEqual(quiet[0].matchups.map((m) => [m.a, m.b]),
    [['s1', 's16'], ['s8', 's9'], ['s5', 's12'], ['s4', 's13'], ['s6', 's11'], ['s3', 's14'], ['s7', 's10'], ['s2', 's15']]);
  assert.deepEqual(quiet.map((r) => r.matchups.length), [8, 4, 2, 1]);
  assert.deepEqual(quiet[1].matchups.map((m) => [m.a, m.b]), [['s1', 's8'], ['s5', 's4'], ['s6', 's3'], ['s7', 's2']]);
  assert.equal(quiet[3].matchups[0].winner, 's1'); // no votes anywhere: higher seed always wins
  // Upset in week 1, tie in week 2 goes to the higher seed
  const v = (pairs) => new Map(pairs.map(([slot, a, va, b, vb]) => [slot, new Map([[a, va], [b, vb]])]));
  const r = bracket(s, new Map([[1, v([[0, 's1', 1, 's16', 4]])], [2, v([[0, 's16', 2, 's8', 2]])]]), 2);
  assert.equal(r[0].matchups[0].winner, 's16');
  assert.equal(r[1].matchups[0].winner, 's8');
});

test('Fewer than 16 entries: missing opponents are byes', () => {
  const s = ['a', 'b', 'c'].map((id) => ({ id }));
  const r = bracket(s, new Map(), 4);
  assert.deepEqual(r[0].matchups.filter((m) => !m.bye).length, 0);
  assert.equal(r[r.length - 1].matchups[0].winner, 'a');
  assert.deepEqual(bracket([{ id: 'solo' }], new Map(), 4), []);
});
