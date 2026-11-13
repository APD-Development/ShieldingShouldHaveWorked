package com.shatteredpixel.shatteredpixeldungeon.effects;

import org.junit.Test;
import static org.junit.Assert.*;

/** Playback-only classification; no damage numbers or gameplay values are changed. */
public class FloatingTextPlaybackClassificationTest {
    @Test public void numericDamageAndHealingLabelsUseCombatLayer() {
        for (String value : new String[]{"1","-3","+12","1.5","1,250","0"}) {
            assertTrue(value,FloatingText.isNumericFeedback(value));
        }
    }

    @Test public void countdownsStatusWordsAndMessagesStayAtNormalSpeed() {
        for (String message : new String[]{"5...","3..","5.","...5","MISS","DODGE",
                "Burning","2 turns","1.2.3","1?","", "+",null}) {
            assertFalse(String.valueOf(message),FloatingText.isNumericFeedback(message));
        }
    }
}
