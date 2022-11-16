package plc.project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * See the specification for information about what the different visit
 * methods should do.
 */
public final class Analyzer implements Ast.Visitor<Void>
{

	public Scope scope;
	private Ast.Method method;

	public Analyzer(Scope parent)
	{
		scope = new Scope(parent);
		scope.defineFunction("print", "System.out.println", Arrays.asList(Environment.Type.ANY), Environment.Type.NIL, args -> Environment.NIL);
	}

	public Scope getScope()
	{
		return scope;
	}

	@Override
	public Void visit(Ast.Source ast)
	{
		//throw new UnsupportedOperationException();  // TODO
		ast.getFields().forEach(this::visit);
		ast.getMethods().forEach(this::visit);
		requireAssignable(Environment.Type.INTEGER, scope.lookupFunction("main", 0).getReturnType());
		return null;
	}

	@Override
	public Void visit(Ast.Field ast)
	{
		//throw new UnsupportedOperationException();  // TODO
		if (ast.getValue().isPresent())
		{
			visit(ast.getValue().get());
			requireAssignable(Environment.getType(ast.getTypeName()), ast.getValue().get().getType());
		}

		ast.setVariable(scope.defineVariable(ast.getName(), ast.getName(), Environment.getType(ast.getTypeName()), Environment.NIL));

		return null;
	}

	@Override
	public Void visit(Ast.Method ast)
	{
		//throw new UnsupportedOperationException();  // TODO
		List<Environment.Type> parameterTypes = new ArrayList<>();
		Environment.Type returnType = Environment.Type.NIL;

		for (String param : ast.getParameterTypeNames())
		{
			parameterTypes.add(Environment.getType(param));
		}

		if (ast.getReturnTypeName().isPresent())
		{
			returnType = Environment.getType(ast.getReturnTypeName().get());
		}

		ast.setFunction(scope.defineFunction(ast.getName(), ast.getName(), parameterTypes, returnType, args -> Environment.NIL));

		try
		{
			scope = new Scope(scope);

			for (int i = 0; i < ast.getParameters().size(); i++)
			{
				scope.defineVariable(ast.getParameters().get(i), ast.getParameters().get(i), parameterTypes.get(i), Environment.NIL);
			}

			ast.getStatements().forEach(this::visit);
		}
		finally
		{
			scope = scope.getParent();
		}

		return null;

	}

	@Override
	public Void visit(Ast.Stmt.Expression ast)
	{
		//throw new UnsupportedOperationException();  // TODO

		if (!(ast.getExpression() instanceof Ast.Expr.Function))
		{
			throw new RuntimeException("Expression must be of type Ast.Expr.Function.");
		}
		visit(ast.getExpression());
		return null;
	}

	@Override
	public Void visit(Ast.Stmt.Declaration ast)
	{
		throw new UnsupportedOperationException();  // TODO
	}

	@Override
	public Void visit(Ast.Stmt.Assignment ast)
	{
		throw new UnsupportedOperationException();  // TODO
	}

	@Override
	public Void visit(Ast.Stmt.If ast)
	{
		throw new UnsupportedOperationException();  // TODO
	}

	@Override
	public Void visit(Ast.Stmt.For ast)
	{
		throw new UnsupportedOperationException();  // TODO
	}

	@Override
	public Void visit(Ast.Stmt.While ast)
	{
		throw new UnsupportedOperationException();  // TODO
	}

	@Override
	public Void visit(Ast.Stmt.Return ast)
	{
		throw new UnsupportedOperationException();  // TODO
	}

	@Override
	public Void visit(Ast.Expr.Literal ast)
	{
		//throw new UnsupportedOperationException();  // TODO

		if (ast.getLiteral() == null)
		{
			ast.setType(Environment.Type.NIL);
		}
		else if (ast.getLiteral() instanceof Boolean)
		{
			ast.setType(Environment.Type.BOOLEAN);
		}
		else if (ast.getLiteral() instanceof Character)
		{
			ast.setType(Environment.Type.CHARACTER);
		}
		else if (ast.getLiteral() instanceof String)
		{
			ast.setType(Environment.Type.STRING);
		}
		else if (ast.getLiteral() instanceof BigInteger)
		{
			if (((BigInteger) ast.getLiteral()).compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0 || ((BigInteger) ast.getLiteral()).compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) < 0)
			{
				throw new RuntimeException("Integer value is out of range");
			}
			else
			{
				ast.setType(Environment.Type.INTEGER);
			}
		}
		else if (ast.getLiteral() instanceof BigDecimal)
		{
			if (((BigDecimal) ast.getLiteral()).doubleValue() == Double.POSITIVE_INFINITY || ((BigDecimal) ast.getLiteral()).doubleValue() == Double.NEGATIVE_INFINITY)
			{
				throw new RuntimeException("Decimal value is out of range");
			}
			else
			{
				ast.setType(Environment.Type.DECIMAL);
			}
		}

		return null;

	}

	@Override
	public Void visit(Ast.Expr.Group ast)
	{
		throw new UnsupportedOperationException();  // TODO
	}

	@Override
	public Void visit(Ast.Expr.Binary ast)
	{
		throw new UnsupportedOperationException();  // TODO
	}

	@Override
	public Void visit(Ast.Expr.Access ast)
	{
		throw new UnsupportedOperationException();  // TODO
	}

	@Override
	public Void visit(Ast.Expr.Function ast)
	{
		//throw new UnsupportedOperationException();  // TODO

		if (ast.getReceiver().isPresent())
		{
			visit(ast.getReceiver().get());

			Environment.Function function = ast.getReceiver().get().getType().getMethod(ast.getName(), ast.getArguments().size());

			for (int i = 1; i < ast.getArguments().size(); i++)
			{
				visit(ast.getArguments().get(i));
				requireAssignable(function.getParameterTypes().get(i), ast.getArguments().get(i).getType());
			}

			ast.setFunction(function);
		}
		else
		{
			Environment.Function function = scope.lookupFunction(ast.getName(), ast.getArguments().size());

			for (int i = 0; i < ast.getArguments().size(); i++)
			{
				visit(ast.getArguments().get(i));
				requireAssignable(function.getParameterTypes().get(i), ast.getArguments().get(i).getType());
			}

			ast.setFunction(function);
		}

		return null;
	}

	public static void requireAssignable(Environment.Type target, Environment.Type type)
	{
		//throw new UnsupportedOperationException();  // TODO

		if (target.getName().equals(type.getName()))
		{
			return;
		}
		else if (target.getName().equals("Any"))
		{
			return;
		}
		else if (target.getName().equals("Comparable") && (type.getName().equals("Integer") || type.getName().equals("Decimal") || type.getName().equals("Character") || type.getName().equals("String")))
		{
			return;
		}

		throw new RuntimeException("Target type does not match the type being used or assigned");
	}

}
