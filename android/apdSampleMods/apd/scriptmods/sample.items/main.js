// Sample script mod (dev builds only): shows the items hook with existing game effects.
APD.effect({ id: 'sample_regen', name: 'Sample Effect', description: 'Heals 1 HP each turn.', turns: 5,
  onStart(h){ h.message('The sample effect starts.'); },
  onTurn(h, left){ h.heal(1); },
  onEnd(h){ h.message('The sample effect ends.'); } });

APD.item({ id: 'sample_potion', kind: 'potion', name: 'Sample Potion', sprite: 'POTION_AMBER',
  description: 'Sample mod potion: heals 5 HP and gives 5 shielding.', dropWeight: 1, startCount: 1,
  onUse(h){ h.heal(5); h.shield(5); } });

APD.item({ id: 'sample_scroll', kind: 'scroll', name: 'Sample Scroll',
  description: 'Sample mod scroll: 10 turns of Haste and the Sample Effect.', dropWeight: 1, startCount: 1,
  onUse(h){ h.addBuff('haste', 10); h.addEffect('sample_regen'); } });

APD.monster({ id: 'sample_crab', name: 'Sample Crab', sprite: 'CrabSprite',
  description: 'Sample mod monster: a sturdy crab that heals a little when hit and sometimes drops a Sample Potion.',
  hp: 18, attack: [2, 5], accuracy: 10, evasion: 4, armor: 2, xp: 3,
  spawn: { floors: [1, 10], weight: 2 }, loot: 'sample_potion', lootChance: 0.3,
  onHurt(m, dmg){ m.heal(1); } });
