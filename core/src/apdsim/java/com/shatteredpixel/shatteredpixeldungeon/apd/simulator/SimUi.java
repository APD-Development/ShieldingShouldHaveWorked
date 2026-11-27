package com.shatteredpixel.shatteredpixeldungeon.apd.simulator;

import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Button;
import com.shatteredpixel.shatteredpixeldungeon.ui.InventoryPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Group;
import com.watabou.noosa.Scene;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

//Reads and presses the game's real UI the same way a finger would (the buttons' own click code runs).
final class SimUi {

	private SimUi(){}

	private static Field membersField;

	@SuppressWarnings("unchecked")
	static List<Gizmo> members(Group g){
		try {
			if (membersField == null){
				membersField = Group.class.getDeclaredField("members");
				membersField.setAccessible(true);
			}
			List<Gizmo> m = (List<Gizmo>) membersField.get(g);
			return m == null ? new ArrayList<>() : new ArrayList<>(m);
		} catch (Exception e){
			return new ArrayList<>();
		}
	}

	//the window on top, or null
	static Window topWindow(){
		Scene s = Game.scene();
		if (s == null) return null;
		Window top = null;
		for (Gizmo g : members(s)){
			if (g instanceof Window && g.exists && g.alive) top = (Window) g;
		}
		return top;
	}

	static void collect(Group g, List<Gizmo> out){
		for (Gizmo m : members(g)){
			if (m == null || !m.exists) continue;
			out.add(m);
			if (m instanceof Group) collect((Group) m, out);
		}
	}

	//every text shown inside a group (window text, button labels)
	static List<String> texts(Group g){
		List<Gizmo> all = new ArrayList<>();
		collect(g, all);
		List<String> out = new ArrayList<>();
		for (Gizmo m : all){
			if (m instanceof RenderedTextBlock){
				String t = ((RenderedTextBlock) m).text();
				if (t != null && !t.isEmpty()) out.add(t);
			}
		}
		return out;
	}

	static final class Choice {
		final Button button;
		final String label;
		Choice(Button b, String l){ button = b; label = l; }
	}

	//buttons a player could press right now
	static List<Choice> buttons(Group g){
		List<Gizmo> all = new ArrayList<>();
		collect(g, all);
		List<Choice> out = new ArrayList<>();
		for (Gizmo m : all){
			if (m instanceof Button && m.active && m.visible){
				String label = "";
				if (m instanceof StyledButton){
					String t = ((StyledButton) m).text();
					if (t != null) label = t;
				} else {
					for (String t : texts((Group) m)) { label = t; break; }
				}
				out.add(new Choice((Button) m, label));
			}
		}
		return out;
	}

	static void click(Button b) throws Exception {
		Method m = find(b.getClass(), "onClick");
		m.setAccessible(true);
		m.invoke(b);
	}

	private static Method find(Class<?> c, String name) throws NoSuchMethodException {
		while (c != null){
			try { return c.getDeclaredMethod(name); } catch (NoSuchMethodException ignored){}
			c = c.getSuperclass();
		}
		throw new NoSuchMethodException(name);
	}

	static Object staticField(Class<?> c, String name){
		try {
			Field f = c.getDeclaredField(name);
			f.setAccessible(true);
			return f.get(null);
		} catch (Exception e){
			return null;
		}
	}

	static Object field(Object o, String name){
		Class<?> c = o.getClass();
		while (c != null){
			try {
				Field f = c.getDeclaredField(name);
				f.setAccessible(true);
				return f.get(o);
			} catch (NoSuchFieldException ignored){
				c = c.getSuperclass();
			} catch (Exception e){
				return null;
			}
		}
		return null;
	}

	static CellSelector cellSelector(){
		return (CellSelector) staticField(GameScene.class, "cellSelector");
	}

	static CellSelector.Listener defaultCellListener(){
		return (CellSelector.Listener) staticField(GameScene.class, "defaultCellListener");
	}

	//a cell target is being asked for (a wand, a thrown item, an ability)
	static boolean choosingCell(){
		CellSelector cs = cellSelector();
		return cs != null && cs.listener != null && cs.listener != defaultCellListener();
	}

	//an item is being asked for in the side inventory pane (landscape screens) instead of a window
	static WndBag.ItemSelector paneSelector(){
		Object scene = staticField(GameScene.class, "scene");
		if (scene == null) return null;
		Object pane = field(scene, "inventory");
		if (pane instanceof InventoryPane && ((InventoryPane) pane).isSelecting()){
			return ((InventoryPane) pane).getSelector();
		}
		return null;
	}

	static void clearPaneSelector(){
		Object scene = staticField(GameScene.class, "scene");
		if (scene == null) return;
		Object pane = field(scene, "inventory");
		if (pane instanceof InventoryPane){
			try {
				Field f = InventoryPane.class.getDeclaredField("selector");
				f.setAccessible(true);
				f.set(pane, null);
				InventoryPane.refresh();
			} catch (Exception ignored){}
		}
	}
}
