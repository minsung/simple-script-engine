package com.vrerv.scriptengine.simple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class SimpleExpressionEvaluator {

	private enum TokenType {
		NUMBER, VARIABLE, OPERATOR, LOGICAL_OPERATOR, PARENTHESIS
	}

	@Getter
	private enum Token {
		PLUS("+", 2, TokenType.OPERATOR),
		MINUS("-", 2, TokenType.OPERATOR),
		MULTIPLY("*", 1, TokenType.OPERATOR),
		DIVIDE("/", 1, TokenType.OPERATOR),
		MODULO("%", 1, TokenType.OPERATOR),
		LESS_THAN_EQUAL("<=", 3, TokenType.LOGICAL_OPERATOR),
		GREATER_THAN_EQUAL(">=", 3, TokenType.LOGICAL_OPERATOR),
		LESS("<", 3, TokenType.LOGICAL_OPERATOR),
		GREATER(">", 3, TokenType.LOGICAL_OPERATOR),
		EQUALS("=", 3, TokenType.LOGICAL_OPERATOR),
		EQUALS2("==", 3, TokenType.LOGICAL_OPERATOR),
		AND("and", 4, TokenType.LOGICAL_OPERATOR),
		AND2("&&", 4, TokenType.LOGICAL_OPERATOR),
		OR("or", 4, TokenType.LOGICAL_OPERATOR),
		OR2("||", 4, TokenType.LOGICAL_OPERATOR),
		OPEN_PAREN("(", 10, TokenType.PARENTHESIS),
		CLOSE_PAREN(")", 10, TokenType.PARENTHESIS),
		;

		private final String symbol;
		private final int precedence;
		private final TokenType type;

		Token(String symbol, int precedence, TokenType type) {
			this.symbol = symbol;
			this.precedence = precedence;
			this.type = type;
		}

		public static Token fromString(String text) {
			for (Token token : Token.values()) {
				if (token.symbol.equals(text)) {
					return token;
				}
			}
			throw new IllegalArgumentException("Unknown token: " + text);
		}
	}

	@AllArgsConstructor
	private static class TokenInfo {
		TokenType type;
		String value;
	}

	private static class Node {
		String value;
		Node left, right;

		Node(String value) {
			this.value = value;
			this.left = this.right = null;
		}
	}

	@AllArgsConstructor
	private static class AST {
		Node root;
	}

	private final Map<Character, List<Token>> tokenSymbolMap = Stream.of(Token.values())
			.collect(Collectors.groupingBy(token -> token.symbol.charAt(0)));
	{
		// sort list of tokenSymbolMap values by symbol size
		tokenSymbolMap.values().forEach(list -> list.sort((t1, t2) -> Integer.compare(t2.symbol.length(), t1.symbol.length())));
	}

	public Object eval(String expression, Map<String, Object> context) {
		List<TokenInfo> tokens = tokenize(expression, context);
		AST ast = parse(tokens);
		return evaluateAST(ast, context);
	}

	private List<TokenInfo> tokenize(String expression, Map<String, Object> context) {
		List<TokenInfo> tokens = new ArrayList<>();

		String[] words = expression.split("\\s+");
		for (String word : words) {
			tokenizeWord(word, context, tokens);
		}

		return tokens;
	}

	private void tokenizeWord(String expression, Map<String, Object> context, List<TokenInfo> tokens) {
		StringBuilder token = new StringBuilder();

		Consumer<StringBuilder> checkAndAddToken = (t) -> {
			if (!t.isEmpty()) {
				String tokenStr = t.toString();
				addToken(context, tokens, tokenStr);
				t.setLength(0);
			}
		};

		for (int i = 0; i < expression.length(); i++) {
			char ch = expression.charAt(i);

			// handle negative number
			if (i == 0 && ch == '-' && expression.length() > 1 && expression.charAt(i + 1) >= '0' && expression.charAt(i + 1) <= '9') {
				token.append(ch);
				continue;
			}
			if (tokenSymbolMap.containsKey(ch)) {
				List<Token> tokenList = tokenSymbolMap.get(ch);
				// find the longest token - the list already sorted by symbol size as descending
				// to prevent same characters in different tokens. i.e. ">=" and ">"
				TokenInfo tokenInfo = null;
				for (Token tokenSymbol : tokenList) {
					if (expression.startsWith(tokenSymbol.symbol, i)) {
						tokenInfo = new TokenInfo(tokenSymbol.type, tokenSymbol.symbol);
						i += tokenSymbol.symbol.length() - 1;
						break;
					}
				}
				if (tokenInfo == null) {
					token.append(ch);
				} else {
					checkAndAddToken.accept(token);
					// should add later
					tokens.add(tokenInfo);
				}
			} else {
				token.append(ch);
			}
		}

		checkAndAddToken.accept(token);
	}

	private void addToken(Map<String, Object> context, List<TokenInfo> tokens, String tokenStr) {
		if (isNumber(tokenStr)) {
			tokens.add(new TokenInfo(TokenType.NUMBER, tokenStr));
		} else if (context.containsKey(tokenStr)) {
			tokens.add(new TokenInfo(TokenType.VARIABLE, tokenStr));
		} else {
			throw new IllegalArgumentException("No such property: " + tokenStr);
		}
	}

	private boolean isNumber(String token) {
		try {
			Double.parseDouble(token);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private AST parse(List<TokenInfo> tokens) {
		Stack<Node> nodes = new Stack<>();
		Stack<Token> operators = new Stack<>();

		for (TokenInfo tokenInfo : tokens) {
			switch (tokenInfo.type) {
				case NUMBER:
				case VARIABLE:
					nodes.push(new Node(tokenInfo.value));
					break;
				case OPERATOR:
				case LOGICAL_OPERATOR:
					Token token = Token.fromString(tokenInfo.value);
					while (!operators.isEmpty() && operators.peek().getPrecedence() <= token.getPrecedence()) {
						nodes.push(buildNode(operators.pop(), nodes));
					}
					operators.push(token);
					break;
				case PARENTHESIS:
					if (tokenInfo.value.equals(Token.OPEN_PAREN.symbol)) {
						operators.push(Token.OPEN_PAREN);
					} else if (tokenInfo.value.equals(Token.CLOSE_PAREN.symbol)) {
						while (operators.peek() != Token.OPEN_PAREN) {
							nodes.push(buildNode(operators.pop(), nodes));
						}
						operators.pop();
					}
					break;
			}
		}

		while (!operators.isEmpty()) {
			nodes.push(buildNode(operators.pop(), nodes));
		}

		return new AST(nodes.pop());
	}

	private Node buildNode(Token operator, Stack<Node> nodes) {
		Node right = nodes.pop();
		Node left = nodes.pop();
		Node node = new Node(operator.getSymbol());
		node.left = left;
		node.right = right;
		return node;
	}

	private Object evaluateAST(AST ast, Map<String, Object> context) {
		return evaluateNode(ast.root, context);
	}

	private Object evaluateNode(Node node, Map<String, Object> context) {
		if (node.left == null && node.right == null) {
			if (isNumber(node.value)) {
				if (node.value.contains(".")) {
					return Double.parseDouble(node.value);
				} else {
					return Long.parseLong(node.value);
				}
			} else {
				return context.getOrDefault(node.value, node.value);
			}
		}

		Object leftVal = evaluateNode(node.left, context);
		Object rightVal = evaluateNode(node.right, context);
		Token operator = Token.fromString(node.value);

		return performOperation(leftVal, rightVal, operator);
	}

	private Object performOperation(Object a, Object b, Token operator) {
		if (a instanceof Number && b instanceof Number) {
			if (operator.type == TokenType.LOGICAL_OPERATOR) {
				return calculateBoolean(operator, (Number) a, (Number) b);
			}
			return calculate(operator, (Number) a, (Number) b);
		} else if (a instanceof Boolean && b instanceof Boolean) {
			return calculateLogical(operator, (Boolean) a, (Boolean) b);
		} else {
			throw new IllegalArgumentException("Invalid operands for operator: " + operator.getSymbol() + ", a(" + (a == null ? null : a.getClass()) + ")=" + a + ", b(" + (b == null ? null : b.getClass()) + ")=" + b);
		}
	}

	private Number calculate(Token operator, Number aNum, Number bNum) {
		Double aDouble = aNum.doubleValue();
		Double bDouble = bNum.doubleValue();

		Double result = switch (operator) {
			case PLUS -> aDouble + bDouble;
			case MINUS -> aDouble - bDouble;
			case MULTIPLY -> aDouble * bDouble;
			case DIVIDE -> aDouble / bDouble;
			case MODULO -> aDouble % bDouble;
			default -> throw new IllegalArgumentException("Invalid operation: " + operator.getSymbol());
		};
		if ((aNum instanceof Long || aNum instanceof Integer) && (bNum instanceof Long || bNum instanceof Integer)) {
			return result.longValue();
		} else {
			return result;
		}
	}

	private Boolean calculateBoolean(Token operator, Number a, Number b) {
		return switch (operator) {
			case GREATER_THAN_EQUAL -> a.doubleValue() >= b.doubleValue();
			case LESS_THAN_EQUAL -> a.doubleValue() <= b.doubleValue();
			case LESS -> a.doubleValue() < b.doubleValue();
			case GREATER -> a.doubleValue() > b.doubleValue();
			case EQUALS, EQUALS2 -> a.doubleValue() == b.doubleValue();
			default -> throw new IllegalArgumentException("Invalid logical operation: " + operator.getSymbol());
		};
	}

	private Boolean calculateLogical(Token operator, Boolean a, Boolean b) {
		return switch (operator) {
			case AND, AND2 -> a && b;
			case OR, OR2 -> a || b;
			default -> throw new IllegalArgumentException("Invalid logical operation: " + operator.getSymbol());
		};
	}

}
