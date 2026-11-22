package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import org.junit.Test;
import static org.junit.Assert.*;

/** UI polish (Chris, Nov 22): every Community hub card gets an icon. */
public class CommunityIconsTest {
	@Test public void hubCardsAllHaveIcons(){
		String[] hub = {"Submissions", "Voting and results", "Selected content", "Run reviews", "Account", "Moderation",
				"What's coming next", "Back", "Sign in with Google", "Sign in with email", "Create an account", "Try again"};
		for (String h : hub) assertNotNull(h, CommunityScene.communityIcon(h));
		assertEquals(Icons.LEFTARROW, CommunityScene.communityIcon("Back"));
		assertNull("unknown items just have no icon", CommunityScene.communityIcon("Some idea title"));
	}
}
