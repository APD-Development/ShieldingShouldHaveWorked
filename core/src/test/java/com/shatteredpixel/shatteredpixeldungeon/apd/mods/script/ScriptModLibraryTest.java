package com.shatteredpixel.shatteredpixeldungeon.apd.mods.script;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.*;

/** Script mods step b: installed mods are found from files; broken ones are listed with a reason. */
public class ScriptModLibraryTest {

	@Rule public TemporaryFolder tmp = new TemporaryFolder();

	static final String JSON = "{\"format\":1,\"id\":\"test.mod\",\"name\":\"Test Mod\",\"author\":\"Tester\",\"description\":\"d\"}";

	private File mod(String folder, String json, String js) throws Exception {
		File d = tmp.newFolder("scriptmods", folder);
		if (json != null) Files.write(new File(d, "mod.json").toPath(), json.getBytes(StandardCharsets.UTF_8));
		if (js != null) Files.write(new File(d, "main.js").toPath(), js.getBytes(StandardCharsets.UTF_8));
		return d;
	}

	private File root(){ return new File(tmp.getRoot(), "scriptmods"); }

	@Test public void findsAValidMod() throws Exception {
		mod("a", JSON, "APD.effect({id:'x', name:'X', turns:1});");
		ScriptModLibrary lib = ScriptModLibrary.scan(root());
		assertEquals(1, lib.mods.size());
		assertTrue(lib.refused.isEmpty());
		ScriptModLibrary.Installed m = lib.find("test.mod");
		assertEquals("Test Mod", m.name);
		assertEquals("Tester", m.author);
		assertTrue(m.script.contains("APD.effect"));
	}

	@Test public void missingFolderIsEmpty(){
		ScriptModLibrary lib = ScriptModLibrary.scan(new File(tmp.getRoot(), "nope"));
		assertTrue(lib.mods.isEmpty());
		assertTrue(ScriptModLibrary.scan(null).mods.isEmpty());
	}

	private String refusal(String json, String js) throws Exception {
		mod("m", json, js);
		ScriptModLibrary lib = ScriptModLibrary.scan(root());
		assertTrue(lib.mods.isEmpty());
		assertEquals(1, lib.refused.size());
		return lib.refused.get(0).reason;
	}

	@Test public void missingFilesAreRefused() throws Exception {
		assertEquals("mod.json is missing", refusal(null, "x"));
	}

	@Test public void missingScriptIsRefused() throws Exception {
		assertEquals("main.js is missing", refusal(JSON, null));
	}

	@Test public void oversizedScriptIsRefused() throws Exception {
		StringBuilder b = new StringBuilder();
		while (b.length() <= ScriptModLibrary.MAX_SCRIPT_BYTES) b.append("// padding padding padding\n");
		assertEquals("main.js is larger than 64 KB", refusal(JSON, b.toString()));
	}

	@Test public void badJsonAndFieldsAreRefused() throws Exception {
		assertEquals("mod.json is not valid JSON", refusal("{nope", "x"));
	}

	@Test public void wrongFormatIsRefused() throws Exception {
		assertTrue(refusal(JSON.replace("\"format\":1", "\"format\":2"), "x").startsWith("unsupported format"));
	}

	@Test public void nonIntegerOrTextFormatIsRefused() {
		for (String f : new String[]{"1.5", "\"1\"", "\"one\""}){
			try {
				ScriptModLibrary.parse(JSON.replace("\"format\":1", "\"format\":" + f), "x", "m");
				fail("format " + f + " was accepted");
			} catch (ScriptModLibrary.Bad e){
				assertTrue(e.getMessage().startsWith("unsupported format"));
			}
		}
	}

	@Test public void badIdIsRefused() throws Exception {
		assertTrue(refusal(JSON.replace("test.mod", "Bad Id"), "x").startsWith("id may only use"));
	}

	@Test public void emptyScriptIsRefused() throws Exception {
		assertEquals("main.js is empty", refusal(JSON, "   \n"));
	}

	@Test public void duplicateIdsKeepTheFirstFolder() throws Exception {
		mod("a", JSON, "x");
		mod("b", JSON, "x");
		ScriptModLibrary lib = ScriptModLibrary.scan(root());
		assertEquals(1, lib.mods.size());
		assertEquals("a", lib.mods.get(0).folder);
		assertEquals("b", lib.refused.get(0).folder);
		assertTrue(lib.refused.get(0).reason.startsWith("another installed mod"));
	}

	@Test public void linkedScriptIsRefused() throws Exception {
		File outside = tmp.newFile("outside.js");
		Files.write(outside.toPath(), "x".getBytes(StandardCharsets.UTF_8));
		File d = mod("m", JSON, null);
		Files.createSymbolicLink(new File(d, "main.js").toPath(), outside.toPath());
		ScriptModLibrary lib = ScriptModLibrary.scan(root());
		assertTrue(lib.mods.isEmpty());
		assertEquals("main.js is a link", lib.refused.get(0).reason);
	}
}
