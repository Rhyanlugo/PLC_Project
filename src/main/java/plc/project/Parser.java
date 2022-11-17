package plc.project;

import jdk.nashorn.internal.runtime.ParserException;

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
		String identifier;
		String typeName;

		match("LET");
		identifier = tokens.get(0).getLiteral();
		match(identifier);

		if (peek(":", Token.Type.IDENTIFIER))
		{
			match(":");
			typeName = tokens.get(0).getLiteral();
			match(tokens.get(0).getLiteral());
		}
		else
		{
			throw new ParseException("Type name not found", tokens.index);
		}

		if (peek("="))
		{
			match("=");
			Ast.Expr expr = parseExpression();

			if (peek(";"))
			{
				match(";");
				return new Ast.Field(identifier, typeName, Optional.of(expr));
			}
			else
			{
				throw new ParseException("';' expected", tokens.index);
			}
		}
		else
		{
			if (peek(";"))
			{
				match(";");
				return new Ast.Field(identifier, typeName, Optional.empty());
			}
			else
			{
				throw new ParseException("';' expected", tokens.index);
			}
		}
	}

	/**
	 * Parses the {@code method} rule. This method should only be called if the
	 * next tokens start a method, aka {@code DEF}.
	 */
	public Ast.Method parseMethod() throws ParseException
	{
		String identifier = "";
		String returnType = "";

		List<Ast.Stmt> statements = new ArrayList<>();
		List<String> parameters = new ArrayList<>();
		List<String> parameterTypes = new ArrayList<>();

		match("DEF");
		identifier = tokens.get(0).getLiteral();
		match(identifier); // TODO Combine
		match("(");

		if (peek(")"))
		{
			match(")");
		}
		else
		{
			while (!peek(")"))
			{
				if(peek(",",")"))
				{
					throw new ParseException("Expected token after comma", tokens.index);
				}
				else if (peek(","))
				{
					match(",");
				}
				else
				{
					if (peek(Token.Type.IDENTIFIER,":",Token.Type.IDENTIFIER))
					{
						parameters.add(tokens.get(0).getLiteral());

						match(tokens.get(0).getLiteral());
						match(":"); //TODO Combine

						parameterTypes.add(tokens.get(0).getLiteral());

						match(tokens.get(0).getLiteral());
					}
					else
					{
						throw new ParseException("Expected type name", tokens.index);
					}
				}
			}
			match(")");
		}

		if (peek(":",Token.Type.IDENTIFIER))
		{
			match(":");
			returnType = tokens.get(0).getLiteral();
			match(returnType);
		}

		if (peek("DO"))
		{
			match("DO");
		}
		else
		{
			throw new ParseException("'DO' expected", tokens.index);
		}

		while (!(peek("END")) && tokens.index < tokens.tokens.size())
		{
			statements.add(parseStatement());
		}

		if (peek("END"))
		{
			match("END");
		}
		else
		{
			throw new ParseException("'END' expected", tokens.index);
		}

		return new Ast.Method(identifier, parameters, parameterTypes, Optional.of(returnType), statements);
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
			parseReturnStatement();
		}

		Ast.Stmt.Expr lhs = parseExpression();

		if (!match("="))
		{
			if (!match(";"))
			{
				throw new ParseException("';' expected", tokens.get(-1).getIndex());
			}

			return new Ast.Stmt.Expression(lhs);
		}

		Ast.Stmt.Expr rhs = parseExpression();

		if (!match(";"))
		{
			throw new ParseException("';' expected", tokens.get(-1).getIndex());
		}

		return new Ast.Stmt.Assignment(lhs, rhs);

	}

	/**
	 * Parses a declaration statement from the {@code statement} rule. This
	 * method should only be called if the next tokens start a declaration
	 * statement, aka {@code LET}.
	 */
	public Ast.Stmt.Declaration parseDeclarationStatement() throws ParseException
	{
		throw new UnsupportedOperationException(); //TODO
	}

	/**
	 * Parses an if statement from the {@code statement} rule. This method
	 * should only be called if the next tokens start an if statement, aka
	 * {@code IF}.
	 */
	public Ast.Stmt.If parseIfStatement() throws ParseException
	{
		throw new UnsupportedOperationException(); //TODO
	}

	/**
	 * Parses a for statement from the {@code statement} rule. This method
	 * should only be called if the next tokens start a for statement, aka
	 * {@code FOR}.
	 */
	public Ast.Stmt.For parseForStatement() throws ParseException
	{
		throw new UnsupportedOperationException(); //TODO
	}

	/**
	 * Parses a while statement from the {@code statement} rule. This method
	 * should only be called if the next tokens start a while statement, aka
	 * {@code WHILE}.
	 */
	public Ast.Stmt.While parseWhileStatement() throws ParseException
	{
		throw new UnsupportedOperationException(); //TODO
	}

	/**
	 * Parses a return statement from the {@code statement} rule. This method
	 * should only be called if the next tokens start a return statement, aka
	 * {@code RETURN}.
	 */
	public Ast.Stmt.Return parseReturnStatement() throws ParseException
	{
		throw new UnsupportedOperationException(); //TODO
	}

	/**
	 * Parses the {@code expression} rule.
	 */
	public Ast.Expr parseExpression() throws ParseException
	{
		throw new UnsupportedOperationException(); //TODO
	}

	/**
	 * Parses the {@code logical-expression} rule.
	 */
	public Ast.Expr parseLogicalExpression() throws ParseException
	{
		throw new UnsupportedOperationException(); //TODO
	}

	/**
	 * Parses the {@code equality-expression} rule.
	 */
	public Ast.Expr parseEqualityExpression() throws ParseException
	{
		throw new UnsupportedOperationException(); //TODO
	}

	/**
	 * Parses the {@code additive-expression} rule.
	 */
	public Ast.Expr parseAdditiveExpression() throws ParseException
	{
		throw new UnsupportedOperationException(); //TODO
	}

	/**
	 * Parses the {@code multiplicative-expression} rule.
	 */
	public Ast.Expr parseMultiplicativeExpression() throws ParseException
	{
		throw new UnsupportedOperationException(); //TODO
	}

	/**
	 * Parses the {@code secondary-expression} rule.
	 */
	public Ast.Expr parseSecondaryExpression() throws ParseException
	{
		throw new UnsupportedOperationException(); //TODO
	}

	/**
	 * Parses the {@code primary-expression} rule. This is the top-level rule
	 * for expressions and includes literal values, grouping, variables, and
	 * functions. It may be helpful to break these up into other methods but is
	 * not strictly necessary.
	 */
	public Ast.Expr parsePrimaryExpression() throws ParseException
	{
		throw new UnsupportedOperationException(); //TODO
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

}
