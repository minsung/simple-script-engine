package com.vrerv.scriptengine.simple;

import java.io.Reader;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

public class SimpleExpressionScriptEngine extends AbstractScriptEngine {

	private final SimpleExpressionEvaluator evaluator = new SimpleExpressionEvaluator();

    @Override
    public Object eval(String script, ScriptContext context) throws ScriptException {
        Bindings bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);
        try {
            return evaluator.eval(script, bindings);
        } catch (RuntimeException e) {
            throw new ScriptException(e);
        }
    }

	@Override
	public Object eval(Reader reader, ScriptContext context) throws ScriptException {
		throw new UnsupportedOperationException();
	}

	@Override
    public Bindings createBindings() {
        return new SimpleBindings();
    }

    @Override
    public ScriptEngineFactory getFactory() {
        return new SimpleExpressionScriptEngineFactory();
    }
}
