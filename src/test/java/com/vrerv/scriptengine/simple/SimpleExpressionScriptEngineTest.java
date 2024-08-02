package com.vrerv.scriptengine.simple;

import java.util.Map;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
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

	@Test
	public void test_using_given_bindings() throws ScriptException {

		ScriptEngine engine = new ScriptEngineManager().getEngineByName("SimpleExpressionEvaluator");
		final int defaultValue = 20;

		class DefaultValueBindings extends SimpleBindings {

			public DefaultValueBindings(Map<String, Object> map) {
				super(map);
			}

			@Override
			public Object get(Object key) {
				return super.get(key) == null ? defaultValue : super.get(key);
			}

			@Override
			public boolean containsKey(Object key) {
				return true;
			}
		}

		SimpleScriptContext scriptContext = new SimpleScriptContext();
		scriptContext.setBindings(new DefaultValueBindings(Map.of(
				"a", 10
		)), ScriptContext.ENGINE_SCOPE);

		assertEquals(true, engine.eval("a + b > 10", scriptContext));
	}
}