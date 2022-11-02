package plc.project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Interpreter implements Ast.Visitor<Environment.PlcObject>
{

	private Scope scope = new Scope(null);

	public Interpreter(Scope parent)
	{
		scope = new Scope(parent);
		scope.defineFunction("print", 1, args -> {
			System.out.println(args.get(0).getValue());
			return Environment.NIL;
		});
	}

	public Scope getScope()
	{
		return scope;
	}

	@Override
	public Environment.PlcObject visit(Ast.Source ast)
	{
		//throw new UnsupportedOperationException(); //TODO
		ast.getFields().forEach(this::visit);
		ast.getMethods().forEach(this::visit);

		return scope.lookupFunction("main", 0).invoke(new ArrayList<>());
	}

	@Override
	public Environment.PlcObject visit(Ast.Field ast)
	{
		//throw new UnsupportedOperationException(); //TODO
		if (ast.getValue().isPresent())
		{
			scope.defineVariable(ast.getName(), visit(ast.getValue().get()));
		}
		else
		{
			scope.defineVariable(ast.getName(), Environment.NIL);
		}

		return Environment.NIL;
	}

	@Override
	public Environment.PlcObject visit(Ast.Method ast)
	{
		//throw new UnsupportedOperationException(); //TODO

		scope.defineFunction(ast.getName(), ast.getParameters().size(), arguments -> {
			try
			{
				scope = new Scope(scope);

				for (int i = 0; i < arguments.size(); i++)
				{
					scope.defineVariable(ast.getParameters().get(i), arguments.get(i));
				}

				for (Ast.Stmt statement : ast.getStatements())
				{
					visit(statement);
				}
			} catch (Return e)
			{
				return e.value;
			} finally
			{
				scope = scope.getParent();
			}
			return Environment.NIL;
		});

		return Environment.NIL;
	}

	@Override
	public Environment.PlcObject visit(Ast.Stmt.Expression ast)
	{
		//throw new UnsupportedOperationException(); //TODO
		visit(ast.getExpression());

		return Environment.NIL;
	}

	@Override
	public Environment.PlcObject visit(Ast.Stmt.Declaration ast)
	{
		throw new UnsupportedOperationException(); //TODO (in lecture)
	}

	@Override
	public Environment.PlcObject visit(Ast.Stmt.Assignment ast)
	{
		throw new UnsupportedOperationException(); //TODO
	}

	@Override
	public Environment.PlcObject visit(Ast.Stmt.If ast)
	{
		throw new UnsupportedOperationException(); //TODO
	}

	@Override
	public Environment.PlcObject visit(Ast.Stmt.For ast)
	{
		throw new UnsupportedOperationException(); //TODO
	}

	@Override
	public Environment.PlcObject visit(Ast.Stmt.While ast)
	{
		//throw new UnsupportedOperationException(); //TODO (in lecture)
		while (requireType(Boolean.class, visit(ast.getCondition())))
		{
			try
			{
				scope = new Scope(scope);

				for (Ast.Stmt stmt : ast.getStatements())
				{
					visit(stmt);
				}
			} finally
			{
				scope = scope.getParent();
			}
		}

		return Environment.NIL;
	}

	@Override
	public Environment.PlcObject visit(Ast.Stmt.Return ast)
	{
		//throw new UnsupportedOperationException(); //TODO
		throw new Return(visit(ast.getValue()));
	}

	@Override
	public Environment.PlcObject visit(Ast.Expr.Literal ast)
	{
		//throw new UnsupportedOperationException(); //TODO

		if (ast.getLiteral() == null)
		{
			return Environment.NIL;
		}

		return Environment.create(ast.getLiteral());

	}

	@Override
	public Environment.PlcObject visit(Ast.Expr.Group ast)
	{
		throw new UnsupportedOperationException(); //TODO
	}

	@Override
	public Environment.PlcObject visit(Ast.Expr.Binary ast)
	{
		//throw new UnsupportedOperationException(); //TODO
		Environment.PlcObject leftObject = visit(ast.getLeft());
		Environment.PlcObject rightObject = visit(ast.getRight());

		switch (ast.getOperator())
		{
			case "AND":
				if (requireType(Boolean.class, leftObject) && requireType(Boolean.class, rightObject))
				{
					return Environment.create(true);
				}
				else
				{
					return Environment.create(false);
				}
			case "OR":
				if (requireType(boolean.class, leftObject) || requireType(boolean.class, rightObject))
				{
					return Environment.create(true);
				}
				else
				{
					return Environment.create(false);
				}
			case "+":
				if (leftObject.getValue() instanceof String && rightObject.getValue() instanceof String)
				{
					return Environment.create(requireType(String.class, leftObject) + requireType(String.class, rightObject));
				}
				else if (leftObject.getValue() instanceof BigInteger && rightObject.getValue() instanceof BigInteger)
				{
					return Environment.create(requireType(BigInteger.class, leftObject).add(requireType(BigInteger.class, rightObject)));
				}
				else if (leftObject.getValue() instanceof BigDecimal && rightObject.getValue() instanceof BigDecimal)
				{
					return Environment.create(requireType(BigDecimal.class, leftObject).add(requireType(BigDecimal.class, rightObject)));
				}
				else
				{
					throw new RuntimeException();
				}
			case "-":
				if (leftObject.getValue() instanceof BigInteger && rightObject.getValue() instanceof BigInteger)
				{
					return Environment.create(requireType(BigInteger.class, leftObject).subtract(requireType(BigInteger.class, rightObject)));
				}
				else if (leftObject.getValue() instanceof BigDecimal && rightObject.getValue() instanceof BigDecimal)
				{
					return Environment.create(requireType(BigDecimal.class, leftObject).subtract(requireType(BigDecimal.class, rightObject)));
				}
				else
				{
					throw new RuntimeException();
				}
			case "*":
				if (leftObject.getValue() instanceof BigInteger && rightObject.getValue() instanceof BigInteger)
				{
					return Environment.create(requireType(BigInteger.class, leftObject).multiply(requireType(BigInteger.class, rightObject)));
				}
				else if (leftObject.getValue() instanceof BigDecimal && rightObject.getValue() instanceof BigDecimal)
				{
					return Environment.create(requireType(BigDecimal.class, leftObject).multiply(requireType(BigDecimal.class, rightObject)));
				}
				else
				{
					throw new RuntimeException();
				}
			case "/":
				if (Objects.equals(rightObject.getValue(), BigInteger.ZERO) || Objects.equals(rightObject.getValue(), BigDecimal.ZERO))
				{
					throw new RuntimeException();
				}
				else if (leftObject.getValue() instanceof BigInteger && rightObject.getValue() instanceof BigInteger)
				{
					return Environment.create(requireType(BigInteger.class, leftObject).divide(requireType(BigInteger.class, rightObject)));
				}
				else if (leftObject.getValue() instanceof BigDecimal && rightObject.getValue() instanceof BigDecimal)
				{
					return Environment.create(requireType(BigDecimal.class, leftObject).divide(requireType(BigDecimal.class, rightObject), RoundingMode.HALF_EVEN));
				}
				else
				{
					throw new RuntimeException();
				}
			case "==":
				return Environment.create(Objects.equals(leftObject.getValue(), rightObject.getValue()));
			case "!=":
				return Environment.create(!Objects.equals(leftObject.getValue(), rightObject.getValue()));
			case "<":
				if (leftObject.getValue() instanceof Comparable)
				{
					return Environment.create(((Comparable) leftObject.getValue()).compareTo(requireType(leftObject.getValue().getClass(), rightObject)) < 0);
				}
				break;
			case "<=":
				if (leftObject.getValue() instanceof Comparable)
				{
					return Environment.create(((Comparable) leftObject.getValue()).compareTo(requireType(leftObject.getValue().getClass(), rightObject)) <= 0);
				}
				break;
			case ">":
				if (leftObject.getValue() instanceof Comparable)
				{
					return Environment.create(((Comparable) leftObject.getValue()).compareTo(requireType(leftObject.getValue().getClass(), rightObject)) > 0);
				}
				break;
			case ">=":
				if (leftObject.getValue() instanceof Comparable)
				{
					return Environment.create(((Comparable) leftObject.getValue()).compareTo(requireType(leftObject.getValue().getClass(), rightObject)) >= 0);
				}
				break;
		}
		return Environment.NIL;
	}

	@Override
	public Environment.PlcObject visit(Ast.Expr.Access ast)
	{
		//throw new UnsupportedOperationException(); //TODO
		if (ast.getReceiver().isPresent())
		{
			return visit(ast.getReceiver().get()).getField(ast.getName()).getValue();
		}
		else
		{
			return scope.lookupVariable(ast.getName()).getValue();
		}
	}

	@Override
	public Environment.PlcObject visit(Ast.Expr.Function ast)
	{
		throw new UnsupportedOperationException(); //TODO
	}

	/**
	 * Helper function to ensure an object is of the appropriate type.
	 */
	private static <T> T requireType(Class<T> type, Environment.PlcObject object)
	{
		if (type.isInstance(object.getValue()))
		{
			return type.cast(object.getValue());
		}
		else
		{
			throw new RuntimeException("Expected type " + type.getName() + ", received " + object.getValue().getClass().getName() + ".");
		}
	}

	/**
	 * Exception class for returning values.
	 */
	private static class Return extends RuntimeException
	{

		private final Environment.PlcObject value;

		private Return(Environment.PlcObject value)
		{
			this.value = value;
		}

	}

}
