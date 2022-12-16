package plc.project;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

public final class Generator implements Ast.Visitor<Void>
{
	private final PrintWriter writer;
	private int indent = 0;

	public Generator(PrintWriter writer)
	{
		this.writer = writer;
	}

	private void print(Object... objects)
	{
		for (Object object : objects)
		{
			if (object instanceof Ast)
			{
				visit((Ast) object);
			}
			else
			{
				writer.write(object.toString());
			}
		}
	}

	private void newline(int indent)
	{
		writer.println();
		for (int i = 0; i < indent; i++)
		{
			writer.write("    ");
		}
	}

	@Override
	public Void visit(Ast.Source ast)
	{
		print("public class Main {");
		newline(0);
		indent++;
		newline(indent);

		for (Ast.Field field : ast.getFields())
		{
			newline(indent);
			print(field);
		}

		print("public static void main(String[] args) {");
		indent++;
		newline(indent);
		print("System.exit(new Main().main());");
		indent--;
		newline(indent);
		print("}");

		for (Ast.Method method : ast.getMethods())
		{
			indent--;
			newline(indent);

			indent++;
			newline(indent);

			print(method);
		}

		indent--;
		newline(indent);
		newline(indent);
		print("}");

		return null;
	}

	@Override
	public Void visit(Ast.Field ast)
	{
		print(ast.getVariable().getType().getJvmName(), " ", ast.getVariable().getJvmName());

		if (ast.getValue().isPresent())
		{
			print(" = ", ast.getValue().get());
		}

		print(";");
		return null;
	}

	@Override
	public Void visit(Ast.Method ast)
	{
		print(ast.getFunction().getReturnType().getJvmName(), " ", ast.getFunction().getJvmName(), "(");

		if (!ast.getParameters().isEmpty())
		{
			for (int i = 0; i < ast.getParameters().size(); i++)
			{
				if (i != 0)
				{
					print(", ");
				}
				print(ast.getFunction().getParameterTypes().get(i).getJvmName(), " ", ast.getParameters().get(i));
			}
		}

		print(") {");
		if (!ast.getStatements().isEmpty())
		{
			indent++;

			for (Ast.Stmt statement : ast.getStatements())
			{
				newline(indent);
				print(statement);
			}

			indent--;
			newline(indent);
			print("}");
		}
		else
		{
			print("}");
		}

		return null;
	}

	@Override
	public Void visit(Ast.Stmt.Expression ast)
	{
		print(ast.getExpression(), ";");
		return null;
	}

	@Override
	public Void visit(Ast.Stmt.Declaration ast)
	{
		print(ast.getVariable().getType().getJvmName(), " ", ast.getVariable().getJvmName());

		if (ast.getValue().isPresent())
		{
			print(" = ", ast.getValue().get());
		}

		print(";");
		return null;
	}

	@Override
	public Void visit(Ast.Stmt.Assignment ast)
	{
		print(ast.getReceiver(), " = ", ast.getValue(), ";");
		return null;
	}

	@Override
	public Void visit(Ast.Stmt.If ast)
	{
		print("if ", "(", ast.getCondition(), ") ", "{");
		indent++;

		for (Ast.Stmt statement : ast.getThenStatements())
		{
			newline(indent);
			print(statement);
		}

		indent--;
		newline(indent);
		print("}");

		if (!ast.getElseStatements().isEmpty())
		{
			print(" else {");
			indent++;

			for (Ast.Stmt statement : ast.getElseStatements())
			{
				newline(indent);
				print(statement);
			}

			indent--;
			newline(indent);
			print("}");
		}

		return null;
	}

	@Override
	public Void visit(Ast.Stmt.For ast)
	{
		print("for ", "(", "int ", ast.getName(), " : ", ast.getValue(), ") {");
		indent++;

		for (Ast.Stmt statement : ast.getStatements())
		{
			newline(indent);
			print(statement);
		}

		indent--;
		newline(indent);
		print("}");

		return null;
	}

	@Override
	public Void visit(Ast.Stmt.While ast)
	{
		print("while (", ast.getCondition(), ") {");
		indent++;

		if (!ast.getStatements().isEmpty())
		{
			for (Ast.Stmt statement : ast.getStatements())
			{
				newline(indent);
				print(statement);
			}

			indent--;
			newline(indent);
		}

		print("}");
		return null;
	}

	@Override
	public Void visit(Ast.Stmt.Return ast)
	{
		print("return ", ast.getValue(), ";");
		return null;
	}

	@Override
	public Void visit(Ast.Expr.Literal ast)
	{
		if (ast.getType() == Environment.Type.STRING)
		{
			print("\"", ast.getLiteral(), "\"");
		}
		else if (ast.getType() == Environment.Type.CHARACTER)
		{
			print("'", ast.getLiteral(), "'");
		}
		else if (ast.getType() == Environment.Type.INTEGER)
		{
			print(new BigInteger(ast.getLiteral().toString()));
		}
		else if (ast.getType() == Environment.Type.DECIMAL)
		{
			print(new BigDecimal(ast.getLiteral().toString()));
		}
		else
		{
			print(ast.getLiteral());
		}

		return null;
	}

	@Override
	public Void visit(Ast.Expr.Group ast)
	{
		print("(", ast.getExpression(), ")");
		return null;
	}

	@Override
	public Void visit(Ast.Expr.Binary ast)
	{
		if (ast.getOperator().equals("AND"))
		{
			print(ast.getLeft(), " && ", ast.getRight());
		}
		else if (ast.getOperator().equals("OR"))
		{
			print(ast.getLeft(), " || ", ast.getRight());
		}
		else
		{
			print(ast.getLeft(), " ", ast.getOperator(), " ", ast.getRight());
		}

		return null;
	}

	@Override
	public Void visit(Ast.Expr.Access ast)
	{
		if (ast.getReceiver().isPresent())
		{
			print(ast.getReceiver().get(), ".");
		}

		print(ast.getVariable().getJvmName());

		return null;
	}

	@Override
	public Void visit(Ast.Expr.Function ast)
	{
		if (ast.getReceiver().isPresent())
		{
			print(ast.getReceiver().get(), ".");
		}

		print(ast.getFunction().getJvmName(), "(");

		for (int i = 0; i < ast.getArguments().size(); i++)
		{
			if (i != 0)
			{
				print(", ");
			}

			print(ast.getArguments().get(i));
		}

		print(")");

		return null;
	}

}
