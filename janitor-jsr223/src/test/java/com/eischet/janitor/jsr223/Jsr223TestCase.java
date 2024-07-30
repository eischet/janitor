package com.eischet.janitor.jsr223;

import com.eischet.janitor.api.types.JanitorObject;
import org.junit.jupiter.api.Test;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Test JSR224 integration of Janitor (javax.script).
 */
public class Jsr223TestCase {

    /**
     * Simple test of the Janitor script engine.
     * @throws ScriptException on errors
     */
    @Test
    public void scriptEngineTest() throws ScriptException {
        final ScriptEngineManager manager = new ScriptEngineManager();
        final ScriptEngine engine = manager.getEngineByName("Janitor");
        assertInstanceOf(JanitorScriptEngine.class, engine);

        final Object result = engine.eval("2 + 2");
        assertInstanceOf(JanitorObject.class, result);

        final JanitorObject janitorResult = (JanitorObject) result;
        assertEquals(4L, janitorResult.janitorGetHostValue());

        final Bindings bindings = engine.createBindings();
        bindings.put("foo", 42);
        final Object result2 = engine.eval("foo + 1", bindings);
        assertInstanceOf(JanitorObject.class, result2);
        assertEquals(43L, ((JanitorObject) result2).janitorGetHostValue());
    }


}
