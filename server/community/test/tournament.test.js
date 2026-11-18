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

test('Week 1 is 1v16, 2v15 ... 8v9; every later round reseeds so the highest seed left plays the lowest', () => {
  const s = Array.from({ length: 16 }, (_, i) => ({ id: `s${i + 1}` }));
  const quiet = bracket(s, new Map(), 4);
  assert.deepEqual(quiet[0].matchups.map((m) => [m.a, m.b]),
    [['s1', 's16'], ['s2', 's15'], ['s3', 's14'], ['s4', 's13'], ['s5', 's12'], ['s6', 's11'], ['s7', 's10'], ['s8', 's9']]);
  assert.deepEqual(quiet.map((r) => r.matchups.length), [8, 4, 2, 1]);
  assert.deepEqual(quiet[1].matchups.map((m) => [m.a, m.b]), [['s1', 's8'], ['s2', 's7'], ['s3', 's6'], ['s4', 's5']]);
  assert.equal(quiet[3].matchups[0].winner, 's1');
  // Upsets: 16 beats 1 and 9 beats 8. Reseeded week 2: 2v16, 3v9, 4v7, 5v6. A tie goes to the higher seed.
  const v = (pairs) => new Map(pairs.map(([slot, a, va, b, vb]) => [slot, new Map([[a, va], [b, vb]])]));
  const r = bracket(s, new Map([[1, v([[0, 's1', 1, 's16', 4], [7, 's8', 0, 's9', 2]])], [2, v([[0, 's2', 3, 's16', 3]])]]), 2);
  assert.deepEqual(r[1].matchups.map((m) => [m.a, m.b]), [['s2', 's16'], ['s3', 's9'], ['s4', 's7'], ['s5', 's6']]);
  assert.equal(r[1].matchups[0].winner, 's2');
});

test('Fewer than 16 entries: missing opponents are byes', () => {
  const s = ['a', 'b', 'c'].map((id) => ({ id }));
  const r = bracket(s, new Map(), 4);
  assert.deepEqual(r[0].matchups.filter((m) => !m.bye).length, 0);
  assert.equal(r[r.length - 1].matchups[0].winner, 'a');
  assert.deepEqual(bracket([{ id: 'solo' }], new Map(), 4), []);
});
