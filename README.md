# Simple Script Engine

## Overview

The **Simple Script Engine** is a lightweight and versatile library for evaluating mathematical and logical expressions.

This implements JSR-223 ScriptEngine interface and provides a simple expression language for evaluating expressions.

## Features

- Supports basic arithmetic operations: +, -, *, /, and %.
- Handles logical operations: AND, OR, and comparisons <, >, <=, >=, ==
- Supports parentheses for grouping expressions.
- Adding human friendly operator tokens.

| Token | Operation   |
|-------|-------------|
| and   | Logical AND |
| or    | Logical OR  |
| =     | Equal       |

## Usage

It is required Java 17 or higher.

To add gradle dependency:
```groovy
    implementation 'com.github.vrerv:simple-script-engine:v0.1.5'
```

To evaluate an expression:
```java
public static void main(String[] args) throws ScriptException {

	ScriptEngine engine = new ScriptEngineManager().getEngineByName("SimpleExpressionEvaluator");
	SimpleScriptContext scriptContext = new SimpleScriptContext();
	scriptContext.setBindings(new SimpleBindings(Map.of(
			"a", 10,
			"b", 20
	)), ScriptContext.ENGINE_SCOPE);
	System.out.println("Result: " + engine.eval("a + b > 10", scriptContext));
}
```
