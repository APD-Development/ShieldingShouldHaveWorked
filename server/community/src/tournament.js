// Season results, worked out from the votes every time they're asked for (nothing stored that could drift).
// Rules (Chris, Nov 18 2026, final): a March Madness style bracket.
//  Month 2: upvote any number of submissions, one upvote each, never your own. The 16 most-upvoted are seeded 1-16.
//  Tournament weeks 1-4: seeded 1v1 bracket from week 1 (1v16, 8v9, 5v12, 4v13, 6v11, 3v14, 7v10, 2v15, winners meet:
//           16 -> 8 -> 4 -> 2 -> 1). One vote per account per matchup, changeable until the week ends.
//           A tied matchup goes to the higher seed.
// Decisions made for Chris (revert if he says no): a tie for a seed (including at 16th) goes to the earlier submission;
// you can't vote for your own submission in the tournament either; votes from banned accounts and hidden/removed
// submissions don't count; with fewer than 16 entries the missing opponents are byes.

export const SEEDS = 16;

// entries: [{id, created_at, score}] -> sorted best first, ties to the earlier submission.
function rank(entries) {
  return [...entries].sort((a, b) => b.score - a.score || a.created_at - b.created_at || (a.id < b.id ? -1 : 1));
}

// Month 2 result: the bracket seeding, best first, at most 16.
export function seeds(entries) {
  return rank(entries).slice(0, SEEDS);
}

// Bracket order so 1 and 2 can only meet in the final (standard 16-team order).
const FIRST_ROUND = [[0, 15], [7, 8], [4, 11], [3, 12], [5, 10], [2, 13], [6, 9], [1, 14]];

// seeded: seeds() order. votesByWeek: week -> Map(matchupIndex -> Map(submissionId -> votes)).
// Returns the rounds for weeks 1..upToWeek: each matchup {a, b, votesA, votesB, winner}.
export function bracket(seeded, votesByWeek, upToWeek) {
  if (seeded.length < 2) return [];
  const seedOf = new Map(seeded.map((e, i) => [e.id, i]));
  let pairs = FIRST_ROUND.map(([x, y]) => [seeded[x]?.id ?? null, seeded[y]?.id ?? null]);
  const rounds = [];
  for (let week = 1; week <= Math.min(upToWeek, 4); week++) {
    const votes = votesByWeek.get(week) || new Map();
    const round = pairs.map(([a, b], i) => {
      if (!a || !b) return { a, b, votesA: 0, votesB: 0, winner: a || b, bye: true };
      const v = votes.get(i) || new Map();
      const va = v.get(a) || 0, vb = v.get(b) || 0;
      return { a, b, votesA: va, votesB: vb, winner: va > vb ? a : vb > va ? b : (seedOf.get(a) < seedOf.get(b) ? a : b) };
    });
    rounds.push({ week, matchups: round });
    const winners = round.map((m) => m.winner);
    if (winners.length < 2) break;
    pairs = [];
    for (let i = 0; i + 1 < winners.length; i += 2) pairs.push([winners[i], winners[i + 1]]);
  }
  return rounds;
}
