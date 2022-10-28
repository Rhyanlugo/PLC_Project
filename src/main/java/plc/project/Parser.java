package plc.project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * The parser takes the sequence of tokens emitted by the lexer and turns that
 * into a structured representation of the program, called the Abstract Syntax
 * Tree (AST).
 *
 * The parser has a similar architecture to the lexer, just with {@link Token}s
 * instead of characters. As before, {@link #peek(Object...)} and {@link
 * #match(Object...)} are helpers to make the implementation easier.
 *
 * This type of parser is called <em>recursive descent</em>. Each rule in our
 * grammar will have it's own function, and reference to other rules correspond
 * to calling that functions.
 */
public final class Parser {

	private final TokenStream tokens;

	public Parser(List<Token> tokens) {
		this.tokens = new TokenStream(tokens);
	}

	/**
	 * Parses the {@code source} rule.
	 */
	public Ast.Source parseSource() throws ParseException {
		List<Ast.Field> fields = new ArrayList<>();
		List<Ast.Method> methods = new ArrayList<>();

		while(peek("LET")) {
			fields.add(parseField());
		}

		while(peek("DEF")) {
			methods.add(parseMethod());
		}

		return new Ast.Source(fields, methods);
	}

	/**
	 * Parses the {@code field} rule. This method should only be called if the
	 * next tokens start a field, aka {@code LET}.
	 */
	public Ast.Field parseField() throws ParseException {
		if (!match("LET")) {
			throw new ParseException("Expected 'DEF'.", tokens.get(0).getIndex());
		}
		if (!match(Token.Type.IDENTIFIER)) {
			throw new ParseException("Expected Identifier.", tokens.get(0).getIndex());
		}
		String name = tokens.get(-1).getLiteral();
		Ast.Expr expr = null;

		if (match("=")) {
			expr = parseExpression();
		}
		if (!match(";")) {
			throw new ParseException("Expected semicolon.", tokens.get(0).getIndex());
		}

		if (expr == null) {
			return new Ast.Field(name, Optional.empty());
		}

		return new Ast.Field(name, Optional.of(expr));
	}

	/**
	 * Parses the {@code method} rule. This method should only be called if the
	 * next tokens start a method, aka {@code DEF}.
	 */
	public Ast.Method parseMethod() throws ParseException {
		if (!match("DEF")) {
			throw new ParseException("Expected 'DEF'.", tokens.get(0).getIndex());
		}
		if (!match(Token.Type.IDENTIFIER)) {
			throw new ParseException("Expected Identifier.", tokens.get(0).getIndex());
		}
		String name = tokens.get(-1).getLiteral();
		if (!match("(")) {
			throw new ParseException("Expected opening parenthesis.", tokens.get(0).getIndex());
		}
		List<String> parameters = new ArrayList<>();
		if (match(Token.Type.IDENTIFIER)) {
			String param = tokens.get(-1).getLiteral();
			parameters.add(param);

			while(match(",")) {
				if (match(Token.Type.IDENTIFIER)) {
					String innerParam = tokens.get(-1).getLiteral();
					parameters.add(innerParam);
				} else {
					throw new ParseException("Unexpected trailing comma.", tokens.get(0).getIndex());
				}
			}
		}
		if (!match(")")) {
			throw new ParseException("Expected closing parenthesis.", tokens.get(0).getIndex());
		}
		if (!match("DO")) {
			throw new ParseException("Expected 'DO'.", tokens.get(0).getIndex());
		}
		List<Ast.Stmt> statements = new ArrayList<>();

		while(!peek("END")) {
			statements.add(parseStatement());
		}

		if (!match("END")) {
			throw new ParseException("Expected 'END'.", tokens.get(0).getIndex());
		}

		return new Ast.Method(name, parameters, statements);
	}

	/**
	 * Parses the {@code statement} rule and delegates to the necessary method.
	 * If the next tokens do not start a declaration, if, while, or return
	 * statement, then it is an expression/assignment statement.
	 */
	public Ast.Stmt parseStatement() throws ParseException {

		if (match("LET")) {
			return parseDeclarationStatement();
		} else if (match("IF")) {
			return parseIfStatement();
		} else if (match("FOR")) {
			return parseForStatement();
		} else if (match("WHILE")) {
			return parseWhileStatement();
		} else if (match("RETURN")) {
			return parseReturnStatement();
		}

		Ast.Expr expr = parseExpression();
		Ast.Expr expr1 = null;

		if (match("=")) {
			expr1 = parseExpression();
		}
		if (!match(";")) {
			throw new ParseException("Expected semicolon.", tokens.get(0).getIndex());
		}

		if (expr1 == null) {
			return new Ast.Stmt.Expression(expr);
		}

		return new Ast.Stmt.Assignment(expr, expr1);



	}

	/**
	 * Parses a declaration statement from the {@code statement} rule. This
	 * method should only be called if the next tokens start a declaration
	 * statement, aka {@code LET}.
	 */
	public Ast.Stmt.Declaration parseDeclarationStatement() throws ParseException {
		if (!match(Token.Type.IDENTIFIER)) {
			throw new ParseException("Expected Identifier.", tokens.get(0).getIndex());
		}
		String name = tokens.get(-1).getLiteral();
		Ast.Expr expr = null;
		if (match("=")) {
			expr = parseExpression();
		}
		if (!match(";")) {
			throw new ParseException("Expected semicolon.", tokens.get(0).getIndex());
		}
		if (expr == null) {
			return new Ast.Stmt.Declaration(name, Optional.empty());
		}
		return new Ast.Stmt.Declaration(name, Optional.of(expr));
	}

	/**
	 * Parses an if statement from the {@code statement} rule. This method
	 * should only be called if the next tokens start an if statement, aka
	 * {@code IF}.
	 */
	public Ast.Stmt.If parseIfStatement() throws ParseException {
		Ast.Expr expr = parseExpression();
		if (!match("DO")) {
			throw new ParseException("Expected 'DO'.", tokens.get(0).getIndex());
		}
		List<Ast.Stmt> statements = new ArrayList<>();
		List<Ast.Stmt> elseStatements = new ArrayList<>();

		while(!peek("END") && !peek("ELSE")) {
			statements.add(parseStatement());
		}
		if (match("ELSE")) {
			while(!peek("END")) {
				elseStatements.add(parseStatement());
			}
		}
		if (!match("END")) {
			throw new ParseException("Expected 'END'.", tokens.get(0).getIndex());
		}

		return new Ast.Stmt.If(expr, statements, elseStatements);
	}

	/**
	 * Parses a for statement from the {@code statement} rule. This method
	 * should only be called if the next tokens start a for statement, aka
	 * {@code FOR}.
	 */
	public Ast.Stmt.For parseForStatement() throws ParseException {
		if (!match(Token.Type.IDENTIFIER)) {
			throw new ParseException("Expected Identifier.", tokens.get(0).getIndex());
		}
		String name = tokens.get(-1).getLiteral();
		if (!match("IN")) {
			throw new ParseException("Expected 'IN'.", tokens.get(0).getIndex());
		}
		Ast.Expr expr = parseExpression();

		if (!match("DO")) {
			throw new ParseException("Expected 'DO'.", tokens.get(0).getIndex());
		}

		List<Ast.Stmt> statements = new ArrayList<>();

		while(!peek("END")) {
			statements.add(parseStatement());
		}

		if (!match("END")) {
			throw new ParseException("Expected 'END'.", tokens.get(0).getIndex());
		}

		return new Ast.Stmt.For(name, expr, statements);
	}

	/**
	 * Parses a while statement from the {@code statement} rule. This method
	 * should only be called if the next tokens start a while statement, aka
	 * {@code WHILE}.
	 */
	public Ast.Stmt.While parseWhileStatement() throws ParseException {
		Ast.Expr expr = parseExpression();

		if (!match("DO")) {
			throw new ParseException("Expected 'DO'.", tokens.get(0).getIndex());
		}

		List<Ast.Stmt> statements = new ArrayList<>();

		while(!peek("END")) {
			statements.add(parseStatement());
		}

		if (!match("END")) {
			throw new ParseException("Expected 'END'.", tokens.get(0).getIndex());
		}

		return new Ast.Stmt.While(expr, statements);
	}

	/**
	 * Parses a return statement from the {@code statement} rule. This method
	 * should only be called if the next tokens start a return statement, aka
	 * {@code RETURN}.
	 */
	public Ast.Stmt.Return parseReturnStatement() throws ParseException {
		Ast.Expr expr = parseExpression();
		if (!match(";")) {
			throw new ParseException("Expected semicolon.", tokens.get(0).getIndex());
		}
		return new Ast.Stmt.Return(expr);
	}

	/**
	 * Parses the {@code expression} rule.
	 */
	public Ast.Expr parseExpression() throws ParseException {
		return parseLogicalExpression();
	}

	/**
	 * Parses the {@code logical-expression} rule.
	 */
	public Ast.Expr parseLogicalExpression() throws ParseException {
		Ast.Expr expr = parseComparisonExpression();

		while(match("AND") || match("OR")) {
			String op = tokens.get(-1).getLiteral();
			Ast.Expr expr1 = parseComparisonExpression();
			expr = new Ast.Expr.Binary(op, expr, expr1);
		}

		return expr;
	}

	/**
	 * Parses the {@code equality-expression} rule.
	 */
	public Ast.Expr parseComparisonExpression() throws ParseException {
		Ast.Expr expr = parseAdditiveExpression();

		while(match("<") || match("<=") || match(">") || match(">=") || match("==") || match("!=")) {
			String op = tokens.get(-1).getLiteral();
			Ast.Expr expr1 = parseAdditiveExpression();
			expr = new Ast.Expr.Binary(op, expr, expr1);
		}

		return expr;
	}

	/**
	 * Parses the {@code additive-expression} rule.
	 */
	public Ast.Expr parseAdditiveExpression() throws ParseException {
		Ast.Expr expr = parseMultiplicativeExpression();

		while(match("+") || match("-")) {
			String op = tokens.get(-1).getLiteral();
			Ast.Expr expr1 = parseMultiplicativeExpression();
			expr = new Ast.Expr.Binary(op, expr, expr1);
		}

		return expr;
	}

	/**
	 * Parses the {@code multiplicative-expression} rule.
	 */
	public Ast.Expr parseMultiplicativeExpression() throws ParseException {
		Ast.Expr expr = parseSecondaryExpression();

		while(match("*") || match("/")) {
			String op = tokens.get(-1).getLiteral();
			Ast.Expr expr1 = parseSecondaryExpression();
			expr = new Ast.Expr.Binary(op, expr, expr1);
		}

		return expr;
	}

	/**
	 * Parses the {@code secondary-expression} rule.
	 */
	public Ast.Expr parseSecondaryExpression() throws ParseException {
		Ast.Expr expr = parsePrimaryExpression();

		while(match(".")) {
			if (match(Token.Type.IDENTIFIER)) {
				String name = tokens.get(-1).getLiteral();
				if (match("(")) {
					List<Ast.Expr> args = new ArrayList<>();
					while (match(",")) {
						args.add(parseExpression());
					}

					if (match(")")) {
						expr = new Ast.Expr.Function(Optional.of(expr), name, args);
					} else {
						throw new ParseException("Expected closing parenthesis.", tokens.get(-1).getIndex());
					}

				} else {
					expr = new Ast.Expr.Access(Optional.of(expr), name);
				}
			} else {
				throw new ParseException("Expected identifier", tokens.get(0).getIndex());
			}
		}

		return expr;

	}

	/**
	 * Parses the {@code primary-expression} rule. This is the top-level rule
	 * for expressions and includes literal values, grouping, variables, and
	 * functions. It may be helpful to break these up into other methods but is
	 * not strictly necessary.
	 */

	private String clean(String str) {
		return str.replace("\\b", "\b")
				.replace("\\n", "\n")
				.replace("\\r", "\r")
				.replace("\\t", "\t")
				.replace("\\'", "'")
				.replace("\\\"", "\"")
				.replace("\\\\", "\\");
	}

	public Ast.Expr parsePrimaryExpression() throws ParseException {
		if (match("TRUE")) {
			return new Ast.Expr.Literal(true);
		} else if (match("FALSE")) {
			return new Ast.Expr.Literal(false);
		} else if (match("NIL")) {
			return new Ast.Expr.Literal(null);
		} else if (match(Token.Type.INTEGER)) {
			String numberString = tokens.get(-1).getLiteral();
			BigInteger integer = new BigInteger(numberString);
			return new Ast.Expr.Literal(integer);
		} else if (match(Token.Type.DECIMAL)) {
			String numberString = tokens.get(-1).getLiteral();
			BigDecimal decimal = new BigDecimal(numberString);
			return new Ast.Expr.Literal(decimal);
		} else if (match(Token.Type.CHARACTER)) {
			String str = tokens.get(-1).getLiteral();
			str = clean(str);
			str = str.replace("'", "");
			char c = str.charAt(0);
			return new Ast.Expr.Literal(c);
		} else if (match(Token.Type.STRING)) {
			String str = tokens.get(-1).getLiteral();
			str = clean(str);
			str = str.replace("\"", "");
			return new Ast.Expr.Literal(str);
		} else if (match(Token.Type.IDENTIFIER)) {
			String name = tokens.get(-1).getLiteral();
			if (match("(")) {
				List<Ast.Expr> args = new ArrayList<>();
				// handle no args
				if (match(")")) {
					return new Ast.Expr.Function(Optional.empty(), name, args);
				}
				Ast.Expr expr = parseExpression();

				args.add(expr);

				while (match(",")) {
					args.add(parseExpression());
				}
				// done with args
				if (match(")")) {
					return new Ast.Expr.Function(Optional.empty(), name, args);
				}
				// no parenthesis after args
				throw new ParseException("Expected closing parenthesis.", tokens.get(-1).getIndex());
			}
			return new Ast.Expr.Access(Optional.empty(), name);

		} else if (match("(")) {
			Ast.Expr expr = parseExpression();
			if (!match(")")) {
				throw new ParseException("Expected closing parenthesis.", tokens.get(-1).getIndex());
			}
			return new Ast.Expr.Group(expr);
		} else {
			throw new ParseException("Invalid primary expression.", tokens.get(-1).getIndex());
		}
	}

	/**
	 * As in the lexer, returns {@code true} if the current sequence of tokens
	 * matches the given patterns. Unlike the lexer, the pattern is not a regex;
	 * instead it is either a {@link Token.Type}, which matches if the token's
	 * type is the same, or a {@link String}, which matches if the token's
	 * literal is the same.
	 *
	 * In other words, {@code Token(IDENTIFIER, "literal")} is matched by both
	 * {@code peek(Token.Type.IDENTIFIER)} and {@code peek("literal")}.
	 */
	private boolean peek(Object... patterns) {

		for (int i = 0; i < patterns.length; i++) {
			if (!tokens.has(i)) {
				return false;
			} else if (patterns[i] instanceof Token.Type) {
				if (patterns[i] != tokens.get(i).getType()) {
					return false;
				}
			} else if (patterns[i] instanceof String) {
				if (!patterns[i].equals(tokens.get(i).getLiteral())) {
					return false;
				}
			} else {
				throw new AssertionError("Invalid pattern object: " + patterns[i].getClass());
			}
		}
		return true;
	}

	/**
	 * As in the lexer, returns {@code true} if {@link #peek(Object...)} is true
	 * and advances the token stream.
	 */
	private boolean match(Object... patterns) {

		boolean peek = peek(patterns);

		if (peek) {
			for (int i = 0; i < patterns.length; i++) {
				tokens.advance();
			}
		}
		return peek;
	}

	private static final class TokenStream {

		private final List<Token> tokens;
		private int index = 0;

		private TokenStream(List<Token> tokens) {
			this.tokens = tokens;
		}

		/**
		 * Returns true if there is a token at index + offset.
		 */
		public boolean has(int offset) {
			return index + offset < tokens.size();
		}

		/**
		 * Gets the token at index + offset.
		 */
		public Token get(int offset) {
			return tokens.get(index + offset);
		}

		/**
		 * Advances to the next token, incrementing the index.
		 */
		public void advance() {
			index++;
		}

	}

}
