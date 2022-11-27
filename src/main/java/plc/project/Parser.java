package plc.project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The parser takes the sequence of tokens emitted by the lexer and turns that
 * into a structured representation of the program, called the Abstract Syntax
 * Tree (AST).
 * <p>
 * The parser has a similar architecture to the lexer, just with {@link Token}s
 * instead of characters. As before, {@link #peek(Object...)} and {@link
 * #match(Object...)} are helpers to make the implementation easier.
 * <p>
 * This type of parser is called <em>recursive descent</em>. Each rule in our
 * grammar will have its own function, and reference to other rules correspond
 * to calling that functions.
 */
public final class Parser
{

	private final TokenStream tokens;

	public Parser(List<Token> tokens)
	{
		this.tokens = new TokenStream(tokens);
	}

	/**
	 * Parses the {@code source} rule.
	 */
	public Ast.Source parseSource() throws ParseException
	{
		List<Ast.Field> fields = new ArrayList<>();
		List<Ast.Method> methods = new ArrayList<>();

		while (tokens.has(0))
		{
			if (match("LET"))
			{
				if (methods.size() > 0)
				{
					throw new ParseException("Adding fields after methods", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
				}
				fields.add(parseField());
			}
			else if (match("DEF"))
			{
				methods.add(parseMethod());
			}
			else
			{
				throw new ParseException("Expected methods", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
			}
		}

		return new Ast.Source(fields, methods);
	}

	/**
	 * Parses the {@code field} rule. This method should only be called if the
	 * next tokens start a field, aka {@code LET}.
	 */
	public Ast.Field parseField() throws ParseException
	{
		if (!match(Token.Type.IDENTIFIER))
		{
			throw generateParseException("'Identifier' expected");
		}

		String identifier = tokens.get(-1).getLiteral();
		String typeName;

		if (match(":",Token.Type.IDENTIFIER))
		{
			typeName = tokens.get(-1).getLiteral();
		}
		else
		{
			throw generateParseException("Incorrect type");
		}

		if (match("="))
		{
			Ast.Expr exprValue = parseExpression();

			if (match(";"))
			{
				return new Ast.Field(identifier, typeName, Optional.of(exprValue));
			}
			else
			{
				throw generateParseException("';' expected");
			}
		}
		else if (match(";"))
		{
			return new Ast.Field(identifier, typeName, Optional.empty());
		}
		else
		{
			throw generateParseException("';' expected");
		}

	}

	/**
	 * Parses the {@code method} rule. This method should only be called if the
	 * next tokens start a method, aka {@code DEF}.
	 */
	public Ast.Method parseMethod() throws ParseException
	{
		if (!match(Token.Type.IDENTIFIER))
		{
			throw generateParseException("'Identifier' expected");
		}

		String identifier = tokens.get(-1).getLiteral();
		Optional<String> returnType = Optional.empty();

		List<Ast.Stmt> statements = new ArrayList<>();
		List<String> parameters = new ArrayList<>();
		List<String> parameterTypes = new ArrayList<>();

		if (!match("("))
		{
			throw generateParseException("'(' expected");
		}
		if (!peek(Token.Type.IDENTIFIER) && !peek(")"))
		{
			throw generateParseException("')' expected");
		}

		if (match(Token.Type.IDENTIFIER))
		{
			parameters.add(tokens.get(-1).getLiteral());

			if (match(":",Token.Type.IDENTIFIER))
			{
				parameterTypes.add(tokens.get(-1).getLiteral());
				match(Token.Type.IDENTIFIER);
			}
			else
			{
				throw generateParseException("Incorrect type");
			}

			while (match(",") && !peek(")"))
			{
				if (match(Token.Type.IDENTIFIER))
				{
					parameters.add(tokens.get(-1).getLiteral());
					parameterTypes.add(tokens.get(-1).getLiteral());
					match(Token.Type.IDENTIFIER);
				}
				else
				{
					throw generateParseException("'Identifier' expected");
				}
			}
		}

		if (!match(")"))
		{
			throw generateParseException("')' expected");
		}

		if (match(":"))
		{
			returnType = Optional.of(tokens.get(0).getLiteral());
			match(Token.Type.IDENTIFIER);
		}

		if (match("DO"))
		{
			while (tokens.has(0))
			{
				if (match("END"))
				{
					return new Ast.Method(identifier, parameters, parameterTypes, returnType, statements);
				}

				statements.add(parseStatement());
			}
		}
		else
		{
			throw generateParseException("'DO' expected");
		}

		throw generateParseException("'END' expected");

	}

	/**
	 * Parses the {@code statement} rule and delegates to the necessary method.
	 * If the next tokens do not start a declaration, if, while, or return
	 * statement, then it is an expression/assignment statement.
	 */
	public Ast.Stmt parseStatement() throws ParseException
	{
		if (match("LET"))
		{
			return parseDeclarationStatement();
		}
		else if (match("IF"))
		{
			return parseIfStatement();
		}
		else if (match("FOR"))
		{
			return parseForStatement();
		}
		else if (match("WHILE"))
		{
			return parseWhileStatement();
		}
		else if (match("RETURN"))
		{
			return parseReturnStatement();
		}
		else if (tokens.has(0))
		{
			Ast.Expr lhs = parseExpression();

			if (match("="))
			{
				Ast.Expr rhs = parseExpression();

				if (match(";"))
				{
					return new Ast.Stmt.Assignment(lhs, rhs);
				}
				else
				{
					throw generateParseException("';' expected");
				}
			}

			if (match(";"))
			{
				return new Ast.Stmt.Expression(lhs);
			}
			else
			{
				throw generateParseException("';' expected");
			}
		}

		throw generateParseException("Incorrect statement");

	}

	/**
	 * Parses a declaration statement from the {@code statement} rule. This
	 * method should only be called if the next tokens start a declaration
	 * statement, aka {@code LET}.
	 */
	public Ast.Stmt.Declaration parseDeclarationStatement() throws ParseException
	{
		if (!match(Token.Type.IDENTIFIER))
		{
			throw generateParseException("'Identifier' expected");
		}

		String identifier = tokens.get(-1).getLiteral();
		Optional<String> typeName = Optional.empty();

		if (match(":"))
		{
			if (!peek(Token.Type.IDENTIFIER))
			{
				throw generateParseException("'Identifier' expected");
			}

			typeName = Optional.of(tokens.get(0).getLiteral());
			match(Token.Type.IDENTIFIER);
		}

		if (match("="))
		{
			Ast.Expr lhs = parseExpression();

			if (match(";"))
			{
				return new Ast.Stmt.Declaration(identifier, typeName, Optional.of(lhs));
			}
			else
			{
				throw generateParseException("';' expected");
			}
		}

		if (match(";"))
		{
			return new Ast.Stmt.Declaration(identifier, typeName, Optional.empty());
		}
		else
		{
			throw generateParseException("';' expected");
		}
	}

	/**
	 * Parses an if statement from the {@code statement} rule. This method
	 * should only be called if the next tokens start an if statement, aka
	 * {@code IF}.
	 */
	public Ast.Stmt.If parseIfStatement() throws ParseException
	{
		Ast.Expr condition = parseExpression();

		if (match("DO"))
		{
			List<Ast.Stmt> thenStatements = new ArrayList<>();
			List<Ast.Stmt> elseStatements = new ArrayList<>();

			while (tokens.has(0) && !peek("ELSE") && !peek("END"))
			{
				thenStatements.add(parseStatement());
			}

			if (match("ELSE"))
			{
				while (tokens.has(0) && !peek("END"))
				{
					elseStatements.add(parseStatement());
				}
			}

			if (match("END"))
			{
				return new Ast.Stmt.If(condition, thenStatements, elseStatements);
			}
			else
			{
				throw generateParseException("'END' expected");
			}
		}
		else
		{
			throw generateParseException("'DO' expected");
		}
	}

	/**
	 * Parses a for statement from the {@code statement} rule. This method
	 * should only be called if the next tokens start a for statement, aka
	 * {@code FOR}.
	 */
	public Ast.Stmt.For parseForStatement() throws ParseException
	{
		if (!match(Token.Type.IDENTIFIER))
		{
			throw generateParseException("'Identifier' expected");
		}

		String identifier = tokens.get(-1).getLiteral();

		if (match("IN"))
		{
			Ast.Expr exprValue = parseExpression();

			if (match("DO"))
			{
				List<Ast.Stmt> statements = new ArrayList<>();

				while (tokens.has(0))
				{
					if (match("END"))
					{
						return new Ast.Stmt.For(identifier, exprValue, statements);
					}
					statements.add(parseStatement());
				}

				throw generateParseException("'END' expected");
			}
			else
			{
				throw generateParseException("'DO' expected");
			}
		}
		else
		{
			throw generateParseException("'IN' expected");
		}
	}

	/**
	 * Parses a while statement from the {@code statement} rule. This method
	 * should only be called if the next tokens start a while statement, aka
	 * {@code WHILE}.
	 */
	public Ast.Stmt.While parseWhileStatement() throws ParseException
	{
		Ast.Expr condition = parseExpression();

		if (match("DO"))
		{
			List<Ast.Stmt> statements = new ArrayList<>();

			while (tokens.has(0))
			{
				if (match("END"))
				{
					return new Ast.Stmt.While(condition, statements);
				}
				statements.add(parseStatement());
			}

			throw generateParseException("'END' expected");
		}
		else
		{
			throw generateParseException("'DO' expected");
		}
	}

	/**
	 * Parses a return statement from the {@code statement} rule. This method
	 * should only be called if the next tokens start a return statement, aka
	 * {@code RETURN}.
	 */
	public Ast.Stmt.Return parseReturnStatement() throws ParseException
	{
		Ast.Expr exprValue = parseExpression();

		if (match(";"))
		{
			return new Ast.Stmt.Return(exprValue);
		}
		else
		{
			throw generateParseException("';' expected");
		}

	}

	/**
	 * Parses the {@code expression} rule.
	 */
	public Ast.Expr parseExpression() throws ParseException
	{
		return parseLogicalExpression();
	}

	/**
	 * Parses the {@code logical-expression} rule.
	 */
	public Ast.Expr parseLogicalExpression() throws ParseException
	{
		Ast.Expr left = parseEqualityExpression();

		while (match("AND") || match("OR"))
		{
			if (!tokens.has(0))
			{
				throw generateParseException("'Identifier' expected");
			}

			String operator = tokens.get(-1).getLiteral();
			Ast.Expr right = parseEqualityExpression();

			left = new Ast.Expr.Binary(operator, left, right);
		}

		return left;
	}

	/**
	 * Parses the {@code equality-expression} rule.
	 */
	public Ast.Expr parseEqualityExpression() throws ParseException
	{
		Ast.Expr left = parseAdditiveExpression();

		while (match(">") || match(">=") || match("<") || match("<=") || match("==") || match("!="))
		{
			if (!tokens.has(0))
			{
				throw generateParseException("'Identifier' expected");
			}

			String operator = tokens.get(-1).getLiteral();
			Ast.Expr right = parseAdditiveExpression();
			left = new Ast.Expr.Binary(operator, left, right);
		}

		return left;
	}

	/**
	 * Parses the {@code additive-expression} rule.
	 */
	public Ast.Expr parseAdditiveExpression() throws ParseException
	{
		Ast.Expr left = parseMultiplicativeExpression();

		while (match("+") || match("-"))
		{
			if (!tokens.has(0))
			{
				throw generateParseException("'Identifier' expected");
			}

			String operator = tokens.get(-1).getLiteral();
			Ast.Expr right = parseMultiplicativeExpression();
			left = new Ast.Expr.Binary(operator, left, right);
		}

		return left;
	}

	/**
	 * Parses the {@code multiplicative-expression} rule.
	 */
	public Ast.Expr parseMultiplicativeExpression() throws ParseException
	{
		Ast.Expr left = parseSecondaryExpression();

		while (match("*") || match("/"))
		{
			if (!tokens.has(0))
			{
				throw generateParseException("'Identifier' expected");
			}

			String operator = tokens.get(-1).getLiteral();
			Ast.Expr right = parseSecondaryExpression();
			left = new Ast.Expr.Binary(operator, left, right);
		}

		return left;
	}

	/**
	 * Parses the {@code secondary-expression} rule.
	 */
	public Ast.Expr parseSecondaryExpression() throws ParseException
	{
		Ast.Expr receiver = parsePrimaryExpression();

		while (peek(".",Token.Type.IDENTIFIER))
		{
			if (peek(".", Token.Type.IDENTIFIER,"("))
			{
				while (peek(".", Token.Type.IDENTIFIER,"("))
				{
					String name = tokens.get(1).getLiteral();

					List<Ast.Expr> arguments = new ArrayList<>();

					match(".", Token.Type.IDENTIFIER, "(");

					while (tokens.has(0))
					{
						if (match(")"))
						{
							receiver = new Ast.Expr.Function(Optional.of(receiver), name, arguments);
							break;
						}

						if (!match(","))
						{
							arguments.add(parseExpression());
						}
						else
						{
							throw generateParseException("'Expression' expected");
						}
					}

					if (!tokens.has(0) && (!tokens.get(-1).getLiteral().equals(")")))
					{
						throw generateParseException("')' expected");
					}
				}
			}
			else
			{
				receiver = new Ast.Expr.Access(Optional.of(receiver), tokens.get(1).getLiteral());
				match(".",Token.Type.IDENTIFIER);
			}
		}

		if (peek("."))
		{
			if (!tokens.has(0))
			{
				throw generateParseException("'Identifier' expected");
			}
		}

		return receiver;
	}

	/**
	 * Parses the {@code primary-expression} rule. This is the top-level rule
	 * for expressions and includes literal values, grouping, variables, and
	 * functions. It may be helpful to break these up into other methods but is
	 * not strictly necessary.
	 */
	public Ast.Expr parsePrimaryExpression() throws ParseException
	{
		if (match("NIL"))
		{
			return new Ast.Expr.Literal(null);
		}
		else if (match("TRUE"))
		{
			return new Ast.Expr.Literal(true);
		}
		else if (match("FALSE"))
		{
			return new Ast.Expr.Literal(false);
		}
		else if (match(Token.Type.INTEGER))
		{
			return new Ast.Expr.Literal(new BigInteger(tokens.get(-1).getLiteral()));
		}
		else if (match(Token.Type.DECIMAL))
		{
			return new Ast.Expr.Literal(new BigDecimal(tokens.get(-1).getLiteral()));
		}
		else if (match(Token.Type.CHARACTER))
		{
			char token = tokens.get(-1).getLiteral().charAt(1);

			if (token == '\\')
			{
				char temp = tokens.get(-1).getLiteral().charAt(2);

				switch (temp)
				{
					case 'n':
						token = '\n';
						break;
					case 'b':
						token = '\b';
						break;
					case 'r':
						token = '\r';
						break;
					case 't':
						token = '\t';
						break;
					case '\'':
						token = '\'';
						break;
					case '\"':
						token = '\"';
						break;
					case '\\':
						token = '\\';
						break;
				}
			}

			return new Ast.Expr.Literal(token);
		}
		else if (match(Token.Type.STRING))
		{
			String token = tokens.get(-1).getLiteral();
			token = token.substring(1, token.length() - 1);

			if (token.contains("\\"))
			{
				token = token.replace("\\n", "\n")
						.replace("\\t", "\t")
						.replace("\\b", "\b")
						.replace("\\r", "\r")
						.replace("\\'", "'")
						.replace("\\\\", "\\")
						.replace("\\\"", "\"");
			}

			return new Ast.Expr.Literal(token);
		}
		else if (match("("))
		{
			Ast.Expr expression = parseExpression();

			if (match(")"))
			{
				return new Ast.Stmt.Expr.Group(expression);
			}
			else
			{
				throw generateParseException("')' expected");
			}
		}
		else if (match(Token.Type.IDENTIFIER))
		{
			String name = tokens.get(-1).getLiteral();

			if (match("("))
			{
				List<Ast.Expr> arguments = new ArrayList<>();

				if (!peek(")") && !peek(","))
				{
					arguments.add(parseExpression());

					while (match(",") && !peek(")"))
					{
						arguments.add(parseExpression());
					}
				}

				if (match(")"))
				{
					return new Ast.Expr.Function(Optional.empty(), name, arguments);
				}
				else
				{
					throw generateParseException("')' expected");
				}
			}
			else
			{
				return new Ast.Expr.Access(Optional.empty(), name);
			}

		}
		else
		{
			throw generateParseException("Invalid expression");
		}
	}

	/**
	 * As in the lexer, returns {@code true} if the current sequence of tokens
	 * matches the given patterns. Unlike the lexer, the pattern is not a regex;
	 * instead it is either a {@link Token.Type}, which matches if the token's
	 * type is the same, or a {@link String}, which matches if the token's
	 * literal is the same.
	 * <p>
	 * In other words, {@code Token(IDENTIFIER, "literal")} is matched by both
	 * {@code peek(Token.Type.IDENTIFIER)} and {@code peek("literal")}.
	 */
	private boolean peek(Object... patterns)
	{
		for (int i = 0; i < patterns.length; i++)
		{
			if (!tokens.has(i))
			{
				return false;
			}
			else if (patterns[i] instanceof Token.Type)
			{
				if (patterns[i] != tokens.get(i).getType())
				{
					return false;
				}
			}
			else if (patterns[i] instanceof String)
			{
				if (!patterns[i].equals(tokens.get(i).getLiteral()))
				{
					return false;
				}
			}
			else
			{
				throw new AssertionError("Invalid pattern object: " + patterns[i].getClass());
			}
		}

		return true;
	}

	/**
	 * As in the lexer, returns {@code true} if {@link #peek(Object...)} is true
	 * and advances the token stream.
	 */
	private boolean match(Object... patterns)
	{
		boolean peek = peek(patterns);

		if (peek)
		{
			for (int i = 0; i < patterns.length; i++)
			{
				tokens.advance();
			}
		}

		return peek;
	}

	private static final class TokenStream
	{

		private final List<Token> tokens;
		private int index = 0;

		private TokenStream(List<Token> tokens)
		{
			this.tokens = tokens;
		}

		/**
		 * Returns true if there is a token at index + offset.
		 */
		public boolean has(int offset)
		{
			return index + offset < tokens.size();
		}

		/**
		 * Gets the token at index + offset.
		 */
		public Token get(int offset)
		{
			return tokens.get(index + offset);
		}

		/**
		 * Advances to the next token, incrementing the index.
		 */
		public void advance()
		{
			index++;
		}

	}

	private ParseException generateParseException(String message)
	{
		if (!tokens.has(0))
		{
			return new ParseException(message, tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
		}
		else
		{
			return new ParseException(message, tokens.get(0).getIndex());
		}
	}

}
