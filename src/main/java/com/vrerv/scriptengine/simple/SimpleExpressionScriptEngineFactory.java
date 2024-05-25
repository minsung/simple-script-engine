package com.vrerv.scriptengine.simple;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleExpressionScriptEngineFactory implements ScriptEngineFactory {
	private final Properties properties = new Properties();
	private final String version;

	public SimpleExpressionScriptEngineFactory() {
		try (InputStream input = getClass().getClassLoader().getResourceAsStream("com/vrerv/scriptengine/simple/engine.properties")) {
			if (input == null) {
				throw new RuntimeException("Unable to find engine.properties");
			}
			properties.load(input);
			log.info("properties {}", properties);
		} catch (IOException ex) {
			throw new RuntimeException("Error loading properties", ex);
		}

		try (InputStream input = getClass().getClassLoader().getResourceAsStream("generated/engine.version")) {

			if (input == null) {
				throw new RuntimeException("Unable to find generated/engine.version");
			}
			version = new String(input.readAllBytes()).trim();
		} catch (IOException ex) {
			throw new RuntimeException("Error loading version", ex);
		}
	}

	@Override
	public String getEngineName() {
		return properties.getProperty("engine.name");
	}

	@Override
	public String getEngineVersion() {
		return version;
	}

	@Override
	public List<String> getExtensions() {
		return Collections.singletonList(properties.getProperty("extension"));
	}

	@Override
	public List<String> getMimeTypes() {
		return Collections.singletonList(properties.getProperty("mime.type"));
	}

	@Override
	public List<String> getNames() {
		return Collections.singletonList(properties.getProperty("engine.name"));
	}

	@Override
	public String getLanguageName() {
		return properties.getProperty("language.name");
	}

	@Override
	public String getLanguageVersion() {
		return properties.getProperty("language.version");
	}

	@Override
	public Object getParameter(String key) {
		return switch (key) {
			case ScriptEngine.ENGINE -> getEngineName();
			case ScriptEngine.ENGINE_VERSION -> getEngineVersion();
			case ScriptEngine.NAME -> getNames().get(0);
			case ScriptEngine.LANGUAGE -> getLanguageName();
			case ScriptEngine.LANGUAGE_VERSION -> getLanguageVersion();
			default -> null;
		};
	}

	@Override
	public String getMethodCallSyntax(String obj, String m, String... args) {
		StringBuilder syntax = new StringBuilder();
		syntax.append(obj).append(".").append(m).append("(");
		if (args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				syntax.append(args[i]);
				if (i < args.length - 1) {
					syntax.append(", ");
				}
			}
		}
		syntax.append(")");
		return syntax.toString();
	}

	@Override
	public String getOutputStatement(String toDisplay) {
		return "print(" + toDisplay + ")";
	}

	@Override
	public String getProgram(String... statements) {
		StringBuilder program = new StringBuilder();
		for (String statement : statements) {
			program.append(statement).append(";\n");
		}
		return program.toString();
	}

	@Override
	public ScriptEngine getScriptEngine() {
		return new SimpleExpressionScriptEngine();
	}
}
