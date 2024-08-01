package com.vrerv.scriptengine.simple;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class SimpleExpressionEvaluatorTest {


	private SimpleExpressionEvaluator evaluator = new SimpleExpressionEvaluator();

    @ParameterizedTest
	@CsvSource({
			"a=2|b=3|c=4,   a + b,             5",
			"a=2|b=3|c=4,   a+b,               5",
			"a=5|b=2|c=3,   (a + b ) * c,      21",
	})
    void testSimpleCalculationLong(String contextString, String expression, Long expected) {

		Map<String, Object> context = StreamSupport.stream(
				StreamSupport.stream(Arrays.spliterator(contextString.split("\\|")), false).spliterator(), false)
				.map(s -> s.split("="))
				.collect(Collectors.toMap(s -> s[0], s -> Long.parseLong(s[1])));

		Object result = evaluator.eval(expression, context);
		assertEquals(expected, result);
	}

	@ParameterizedTest
	@CsvSource({
			"a=5|b=2.5,     a * b,             12.5",
	})
	void testSimpleCalculationDouble(String contextString, String expression, Double expected) {

		Map<String, Object> context = StreamSupport.stream(
						StreamSupport.stream(Arrays.spliterator(contextString.split("\\|")), false).spliterator(), false)
				.map(s -> s.split("="))
				.collect(Collectors.toMap(s -> s[0], s -> Double.parseDouble(s[1])));

		Object result = evaluator.eval(expression, context);
		assertEquals(expected, result);
	}



	@ParameterizedTest
	@MethodSource("provideSimpleExpression")
	void testSimpleExpression(Map<String, Object> context, String expression, Object expected) {
		Object result2 = evaluator.eval(expression, context);
		assertEquals(expected, result2);
	}

	static List<Arguments> provideSimpleExpression() {
		return Arrays.asList(
				Arguments.of(Map.of("x", 100), "x = 100", true),
				Arguments.of(Map.of("x", 1, "y", 2), "x != y", true),
				Arguments.of(Map.of("x", 3, "y", 5), "x%y", 3L),
				Arguments.of(Map.of("x", 42, "y", 43), "x + y", 85L),
				Arguments.of(Map.of("x", 1, "y", 2, "z", 3), "(x+y)*z", 9L),
				Arguments.of(Map.of("x", 1, "y", 2, "z", 3), "x+y * z", 7L),
				Arguments.of(Map.of("x", 1, "y", 2, "z", 3), "(x+y) * (z+1)", 12L),
				Arguments.of(Map.of("x", 1, "y", 2, "z", 3), "(x+y) * (z+1.0)", 12.0D),


				Arguments.of(Map.of("x", 12345, "y", 54321.9), "x + y", 66666.9D),
				Arguments.of(Map.of("x", 1, "y", 2, "z", 3), "x>=y", false),
				Arguments.of(Map.of("x", 1, "y", 2, "z", 3), "x >= y", false),
				Arguments.of(Map.of("x", 1, "y", 2, "z", 3), "x <= y", true),
				Arguments.of(Map.of("x", 1, "y", 2, "z", 3), "x < y", true),
				Arguments.of(Map.of("x", 1, "y", 2, "z", 3), "x > y", false)
		);
	}

	@Test
	void testBooleanOperatorsComplex() {
		Map<String, Object> context5 = Map.of("x", -1, "y", 1);
		Object result5 = evaluator.eval("(x > 0 and y > 0) or (x = -1)", context5);
		assertEquals(true, result5);
	}

	@Test
	void testSimpleEquality() {
		Map<String, Object> context2 = Map.of("x", 100);
		Object result2 = evaluator.eval("x = 100", context2);
		assertEquals(true, result2);
	}

	@Test
	void testSimpleNotEquality() {
		Map<String, Object> context3 = Map.of("x", 100);
		Object result3 = evaluator.eval("x != 100", context3);
		assertEquals(false, result3);
	}

	@Test
	void testNegativeNumber() {
		Map<String, Object> context2 = Map.of("x", 100);
		Object result2 = evaluator.eval("x > -1", context2);
		assertEquals(true, result2);
	}
    
    @Test
    void testBooleanOperators() {
        Map<String, Object> context5 = Map.of("x", true, "y", false);
        Object result5 = evaluator.eval("x and y", context5);
        assertEquals(false, result5);
    }

	@Test
	void testInvalidExpression() {
		Map<String, Object> context = Map.of("x", 1, "y", 2);
		assertThrows(IllegalArgumentException.class, () -> {
			evaluator.eval("x $ y", context);
		});
	}

	@Test
	void testNotDefinedVariable() {
		Map<String, Object> context = Map.of();
		assertThrows(IllegalArgumentException.class, () -> {
			evaluator.eval("x", context);
		});
	}
}