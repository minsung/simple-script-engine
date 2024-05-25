package com.vrerv.scriptengine.simple;

import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
public class SimpleExpressionScriptEngineTest {

    @Test
    public void testEvalMethod() throws ScriptException {

		ScriptEngine engine = new ScriptEngineManager().getEngineByName("SimpleExpressionEvaluator");

        SimpleScriptContext scriptContext = new SimpleScriptContext();
        scriptContext.setBindings(new SimpleBindings(Map.of(
				"a", 10,
				"b", 20
		)), ScriptContext.ENGINE_SCOPE);

		assertEquals(true, engine.eval("a + b > 10", scriptContext));

		assertThrows(ScriptException.class, () -> engine.eval("a + d", scriptContext));
    }
}