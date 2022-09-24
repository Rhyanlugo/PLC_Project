package plc.project;

import java.util.ArrayList;
import java.util.List;

/**
 * The lexer works through three main functions:
 * <p>
 * - {@link #lex()}, which repeatedly calls lexToken() and skips whitespace
 * - {@link #lexToken()}, which lexes the next token
 * - {@link CharStream}, which manages the state of the lexer and literals
 * <p>
 * If the lexer fails to parse something (such as an unterminated string) you
 * should throw a {@link ParseException} with an index at the character which is
 * invalid or missing.
 * <p>
 * The {@link #peek(String...)} and {@link #match(String...)} functions are
 * helpers you need to use, they will make the implementation a lot easier.
 */
public final class Lexer
{

	private final CharStream chars;

	public Lexer(String input)
	{
		chars = new CharStream(input);
	}

	/**
	 * Repeatedly lexes the input using {@link #lexToken()}, also skipping over
	 * whitespace where appropriate.
	 */
	public List<Token> lex()
	{
		//throw new UnsupportedOperationException(); //TODO
		List<Token> tokenList = new ArrayList<>();

		while (chars.has(0))
		{
			if (!match("[\\s\b\n\r\t]"))
			{
				tokenList.add(lexToken());
			} else
			{
				chars.skip();
			}
		}

		return tokenList;
	}

	/**
	 * This method determines the type of the next token, delegating to the
	 * appropriate lex method. As such, it is best for this method to not change
	 * the state of the char stream (thus, use peek not match).
	 * <p>
	 * The next character should start a valid token since whitespace is handled
	 * by {@link #lex()}
	 */
	public Token lexToken()
	{
		//throw new UnsupportedOperationException(); //TODO

		/*
			GRAMMAR LIST:
			identifier ::= [A-Za-z_] [A-Za-z0-9_-]*
			number ::= [+-]? [0-9]+ ('.' [0-9]+)?
			character ::= ['] ([^'\n\r\\] | escape) [']
			string ::= '"' ([^"\n\r\\] | escape)* '"'
			escape ::= '\' [bnrt'"\\]
			operator ::= [<>!=] '='? | 'any character'

			whitespace ::= [ \b\n\r\t]
		*/

		// Since each token has specific characters associated with it, we
		// don't need to use the entire grammar rule. Instead, we can simplify
		// the process by using the token rules at the beginning of each.
		String identifierRules = "[A-Za-z_]";
		String numberRules = "[0-9]+(\\.[0-9]+)?";
		String characterRules = "[']";
		String stringRules = "[\"]";
		String operatorRules = "([<>!=] =?|(.))";

		if (peek(identifierRules))
		{
			return lexIdentifier();
		}
		else if (peek("[+-]?") || peek(numberRules))
		{
			return lexNumber();
		}
		else if (peek(characterRules))
		{
			return lexCharacter();
		}
		else if (peek(stringRules))
		{
			return lexString();
		}
		else if (peek(operatorRules))
		{
			return lexOperator();
		}

		throw new ParseException("Invalid Input", chars.index);
	}

	public Token lexIdentifier()
	{
		//throw new UnsupportedOperationException(); //TODO
		System.out.println("IDENTIFIER FOUND");

		match("[A-Za-z_]+");
		while (match("[A-Za-z0-9_-]*"));
		return chars.emit(Token.Type.IDENTIFIER);
	}

	public Token lexNumber()
	{
		//throw new UnsupportedOperationException(); //TODO
		System.out.println("NUMBER FOUND");

		while (match("[+-]?"));
		while (match("[0-9]+"));

		if (peek("\\."))
		{
			if (match("\\.", "[0-9]+"))
			{
				System.out.println("DECIMAL FOUND");
				while (match("[0-9]"));
				return chars.emit(Token.Type.DECIMAL);
			}
			else
			{
				throw new ParseException("Invalid: Trailing Decimal", chars.index);
			}
		}
		System.out.println("INTEGER FOUND");
		return chars.emit(Token.Type.INTEGER);
	}

	public Token lexCharacter()
	{
		//throw new UnsupportedOperationException(); //TODO
		System.out.println("CHARACTER FOUND");

		if (match("'", "\\\\", "[bnrt'\"\\\\]{1}", "'"))
		{
			return chars.emit(Token.Type.CHARACTER);
		}
		else if (match("'", ".{1}", "'"))
		{
			return chars.emit(Token.Type.CHARACTER);
		}

		throw new ParseException("Invalid Character", chars.index);
	}

	public Token lexString()
	{
		//throw new UnsupportedOperationException(); //TODO
		System.out.println("STRING FOUND");

		match("\"{1}");

//		if (peek("\"{1,2}"))
//		{
//			match("\"{1}");
//			return chars.emit(Token.Type.STRING);
//		}

		while (peek("[^\"]"))
		{
			if (peek("\\\\"))
			{
				lexEscape();
			}
			else
			{
				match("[^\"]");
			}

		}

		if (peek("\"{1,2}"))
		{
			match("\"{1}");
			return chars.emit(Token.Type.STRING);
		}

		throw new ParseException("Invalid String", chars.index);
	}

	public void lexEscape()
	{
		//throw new UnsupportedOperationException(); //TODO
		if (peek("\\\\", "[bnrt'\"\\\\]"))
		{
			match("\\\\", "[bnrt'\"\\\\]");
		}
		else
		{
			throw new ParseException("Invalid Escape", chars.index);
		}
	}

	public Token lexOperator()
	{
		//throw new UnsupportedOperationException(); //TODO
		System.out.println("OPERATOR FOUND");

		if (peek("[<>!=]", "=?"))
		{
			match("[<>!=]", "=?");
			return chars.emit(Token.Type.OPERATOR);
		}

		match("([<>!=]|(.))");
		return chars.emit(Token.Type.OPERATOR);

//		throw new ParseException("Invalid Operator", chars.index);
	}

	/**
	 * Returns true if the next sequence of characters match the given patterns,
	 * which should be a regex. For example, {@code peek("a", "b", "c")} would
	 * return true if the next characters are {@code 'a', 'b', 'c'}.
	 */
	public boolean peek(String... patterns)
	{
		//throw new UnsupportedOperationException(); //TODO (in lecture)
		for (int i = 0; i < patterns.length; i++)
		{
			if (!chars.has(i) || !String.valueOf(chars.get(i)).matches(patterns[i]))
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * Returns true in the same way as {@link #peek(String...)}, but also
	 * advances the character stream past all matched characters if peek returns
	 * true. Hint - it's easiest to have this method simply call peek.
	 */
	public boolean match(String... patterns)
	{
		//throw new UnsupportedOperationException(); //TODO (in lecture)
		boolean peek = peek(patterns);

		if (peek)
		{
			for (int i = 0; i < patterns.length; i++)
			{
				chars.advance();
			}
		}

		return peek;
	}

	/**
	 * A helper class maintaining the input string, current index of the char
	 * stream, and the current length of the token being matched.
	 * <p>
	 * You should rely on peek/match for state management in nearly all cases.
	 * The only field you need to access is {@link #index} for any {@link
	 * ParseException} which is thrown.
	 */
	public static final class CharStream
	{

		private final String input;
		private int index = 0;
		private int length = 0;

		public CharStream(String input)
		{
			this.input = input;
		}

		public boolean has(int offset)
		{
			return index + offset < input.length();
		}

		public char get(int offset)
		{
			return input.charAt(index + offset);
		}

		public void advance()
		{
			index++;
			length++;
		}

		public void skip()
		{
			length = 0;
		}

		public Token emit(Token.Type type)
		{
			int start = index - length;
			skip();
			return new Token(type, input.substring(start, index), start);
		}
	}
}
