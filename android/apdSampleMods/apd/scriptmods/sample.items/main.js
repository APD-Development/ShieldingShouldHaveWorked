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
