package plc.project;

import java.math.BigDecimal;
import java.math.BigInteger;
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
		throw new UnsupportedOperationException();  // TODO
	}

	@Override
	public Void visit(Ast.Stmt.Expression ast)
	{
		throw new UnsupportedOperationException();  // TODO
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
		throw new UnsupportedOperationException();  // TODO
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
		throw new UnsupportedOperationException();  // TODO
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
