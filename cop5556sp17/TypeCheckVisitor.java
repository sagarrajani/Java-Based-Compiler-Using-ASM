package cop5556sp17;

import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.Chain;
import cop5556sp17.AST.ChainElem;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.Expression;
import cop5556sp17.AST.FilterOpChain;
import cop5556sp17.AST.FrameOpChain;
import cop5556sp17.AST.IdentChain;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IdentLValue;
import cop5556sp17.AST.IfStatement;
import cop5556sp17.AST.ImageOpChain;
import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.ParamDec;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.SleepStatement;
import cop5556sp17.AST.Statement;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

import java.util.ArrayList;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.LinePos;
import cop5556sp17.Scanner.Token;
import static cop5556sp17.AST.Type.TypeName.*;
import static cop5556sp17.Scanner.Kind.ARROW;
import static cop5556sp17.Scanner.Kind.KW_HIDE;
import static cop5556sp17.Scanner.Kind.KW_MOVE;
import static cop5556sp17.Scanner.Kind.KW_SHOW;
import static cop5556sp17.Scanner.Kind.KW_XLOC;
import static cop5556sp17.Scanner.Kind.KW_YLOC;
import static cop5556sp17.Scanner.Kind.OP_BLUR;
import static cop5556sp17.Scanner.Kind.OP_CONVOLVE;
import static cop5556sp17.Scanner.Kind.OP_GRAY;
import static cop5556sp17.Scanner.Kind.OP_HEIGHT;
import static cop5556sp17.Scanner.Kind.OP_WIDTH;
import static cop5556sp17.Scanner.Kind.*;

public class TypeCheckVisitor implements ASTVisitor {

	@SuppressWarnings("serial")
	public static class TypeCheckException extends Exception {
		TypeCheckException(String message) {
			super(message);
		}
	}

	SymbolTable symtab = new SymbolTable();

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception
	{
		TypeName val1 = (TypeName) binaryChain.getE0().visit(this,arg);
        TypeName val2 = (TypeName) binaryChain.getE1().visit(this,arg);
        Token binaryToken = binaryChain.getE1().getFirstToken();
        switch(binaryChain.getArrow().kind)
        {
        case ARROW:
        {
        	if(val1.equals(URL) && val2.equals(IMAGE))
        	{
        		binaryChain.chainType = val2;
        	}
        	else if(val1.equals(FILE) && val2.equals(IMAGE))
        	{
        		binaryChain.chainType = val2;
        	}
        	else if(val1.equals(FRAME) && binaryChain.getE1() instanceof FrameOpChain)
        	{
        		if(binaryToken.kind.equals(KW_XLOC) || binaryToken.kind.equals(KW_YLOC))
        		{
        			binaryChain.chainType = INTEGER;
        		}
        		else if(binaryToken.kind.equals(KW_SHOW) || binaryToken.kind.equals(KW_HIDE) || binaryToken.kind.equals(KW_MOVE))
        		{
        			binaryChain.chainType = FRAME;
        		}
        		else 
        			throw new TypeCheckException(" ");
        	}
        	else if(val1.equals(IMAGE) && binaryChain.getE1() instanceof ImageOpChain)
        	{
        		if(binaryToken.kind.equals(OP_WIDTH) || binaryToken.kind.equals(OP_HEIGHT))
        		{
        			binaryChain.chainType = INTEGER;
        		}
        		else if(binaryToken.kind.equals(KW_SCALE))
        		{
        			binaryChain.chainType = IMAGE;
        		}
        		else 
        			throw new TypeCheckException(" ");
        	}
        	else if(val1.equals(IMAGE) && val2.equals(FRAME))
        	{
        		binaryChain.chainType = val2;
        	}
        	else if(val1.equals(IMAGE) && val2.equals(FILE))
        	{
        		binaryChain.chainType = NONE;
        	}
        	else if(val1.equals(IMAGE) && (binaryChain.getE1() instanceof IdentChain) && val2.equals(IMAGE))
        	{
        		binaryChain.chainType = IMAGE;
        	}
        	else if(val1.equals(INTEGER) && (binaryChain.getE1() instanceof IdentChain) && val2.equals(INTEGER))
        	{
        		binaryChain.chainType = INTEGER;
        	}
        	else if(val1.equals(IMAGE) && binaryChain.getE1() instanceof FilterOpChain)
        	{
        		if(binaryToken.kind.equals(OP_GRAY) || binaryToken.kind.equals(OP_BLUR) || binaryToken.kind.equals(OP_CONVOLVE))
        		{
        			binaryChain.chainType = IMAGE;
        		}
        		else 
        			throw new TypeCheckException(" ");
        	}

        	else 
        		throw new TypeCheckException(" ");

        }
        break;
        case BARARROW:
        {
        	if(val1.equals(IMAGE) && binaryChain.getE1() instanceof FilterOpChain)
        	{
        		if(binaryToken.kind.equals(OP_GRAY) || binaryToken.kind.equals(OP_BLUR) || binaryToken.kind.equals(OP_CONVOLVE))
        		{
        			binaryChain.chainType = IMAGE;
        		}
        		else 
        			throw new TypeCheckException(" ");
        	}
        	else 
        		throw new TypeCheckException(" ");
        }
        break;
        default:
        	throw new TypeCheckException(" ");
        }
		return binaryChain.chainType;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception
	{
		TypeName val1 = (TypeName) binaryExpression.getE0().visit(this,arg);
        TypeName val2 = (TypeName) binaryExpression.getE1().visit(this,arg);

        switch(binaryExpression.getOp().kind)
        {
        case TIMES:
        {
        	if(val1.equals(INTEGER) && val2.equals(INTEGER))
        	{
        		binaryExpression.expressionType = val1;
        	}
        	else if(val1.equals(INTEGER) && val2.equals(IMAGE))
        	{
        		binaryExpression.expressionType = val2;
        	}
        	else if(val1.equals(IMAGE) && val2.equals(INTEGER))
        	{
        		binaryExpression.expressionType = val1;
        	}
        	else
        		throw new TypeCheckException(" ");
        }
        break;
        case DIV:
        {
        	if(val1.equals(INTEGER) && val2.equals(INTEGER))
        	{
        		binaryExpression.expressionType = val1;
        	}
        	else if(val1.equals(IMAGE) && val2.equals(INTEGER))
        	{
        		binaryExpression.expressionType = val1;
        	}
        	else
        		throw new TypeCheckException(" ");
        }
        break;
        case MOD:
        	if(val1.equals(IMAGE) && val2.equals(INTEGER))
        	{
        		binaryExpression.expressionType = val1;
        	}
        	else if(val1.equals(INTEGER) && val2.equals(INTEGER))
        	{
        		binaryExpression.expressionType = val1;
        	}
        	else
        		throw new TypeCheckException(" ");
        break;
        case PLUS:
        case MINUS:
        {
        	if(val1.equals(INTEGER) && val2.equals(INTEGER))
        	{
        		binaryExpression.expressionType = val1;
        	}
        	else if(val1.equals(IMAGE) && val2.equals(IMAGE))
        	{
        		binaryExpression.expressionType = val1;
        	}
        	else
        		throw new TypeCheckException(" ");
        }
        break;
        case LT:
        case GT:
        case LE:
        case GE:
        {
        	if(val1.equals(INTEGER) && val2.equals(INTEGER))
        	{
        		binaryExpression.expressionType = BOOLEAN;
        	}
        	else if(val1.equals(BOOLEAN) && val2.equals(BOOLEAN))
        	{
        		binaryExpression.expressionType = val1;
        	}
        	else
        		throw new TypeCheckException(" ");
        }
        break;
        case EQUAL:
        case NOTEQUAL:
        {
        	if(val1.equals(val2))
        	{
        		binaryExpression.expressionType = BOOLEAN;
        	}
        	else
        		throw new TypeCheckException(" ");
        }
        break;
        case AND:
        case OR:
        {
        	if(val1.equals(val2))
        	{
        		binaryExpression.expressionType = BOOLEAN;
        	}
        	else
        		throw new TypeCheckException(" ");

        }
        break;
        default:
        	throw new TypeCheckException(" ");
        }
        return binaryExpression.expressionType;


	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception 
	{
		symtab.enterScope();
		ArrayList<Dec> de=block.getDecs();
		ArrayList<Statement> st=block.getStatements();
		for(Dec d:de)
		{
			d.visit(this, null);
		}
		for(Statement s:st)
		{
			s.visit(this, null);
		}
		symtab.leaveScope();
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception
	{
		booleanLitExpression.expressionType = BOOLEAN;
		return booleanLitExpression.expressionType;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception 
	{
		filterOpChain.getArg().visit(this, null);
		if(filterOpChain.getArg().getExprList().size()==0)
		{
			filterOpChain.chainType=IMAGE;
		}
		else
		{
			throw new TypeCheckException(" ");
		}
		return filterOpChain.chainType;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception 
	{
		frameOpChain.getArg().visit(this, null);
		Kind frop=frameOpChain.getFirstToken().kind;
		switch(frop)
		{
		case KW_SHOW:
		case KW_HIDE:
		{
			if(frameOpChain.getArg().getExprList().size()==0)
			{
				frameOpChain.chainType=NONE;
			}
			else
			{
				throw new TypeCheckException(" ");
			}
		}
		break;
		case KW_YLOC:
		case KW_XLOC:
		{
			if(frameOpChain.getArg().getExprList().size()==0)
			{
				frameOpChain.chainType=INTEGER;
			}
			else
			{
				throw new TypeCheckException(" ");
			}
		}
		break;
		case KW_MOVE:
		{
			if(frameOpChain.getArg().getExprList().size()==2)
			{
				frameOpChain.chainType=NONE;
			}
			else
			{
				throw new TypeCheckException(" ");
			}
		}
		break;
		default:
			throw new TypeCheckException(" ");
		}
		return frameOpChain.chainType;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception 
	{
		Dec ident=symtab.lookup(identChain.getFirstToken().getText());
		if(ident!=null)
		{
			identChain.chainType=ident.decType;
			identChain.identDec=ident;
		}
		else
		{
			throw new TypeCheckException("Not present in symbol table");
		}
		return identChain.chainType;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception 
	{
		Dec ident=symtab.lookup(identExpression.getFirstToken().getText());
		if(ident!=null)
		{
			identExpression.expressionType=ident.decType;
			identExpression.expressionDec=ident;
		}
		else
		{
			throw new TypeCheckException("Not present in symbol table");
		}
		return identExpression.expressionType;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception 
	{
		Expression e=ifStatement.getE();
		Block b=ifStatement.getB();
		if(!e.visit(this, null).equals(BOOLEAN))
		{
			throw new TypeCheckException("not equal to boolean");
		}
		b.visit(this, null);
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception 
	{
		intLitExpression.expressionType=INTEGER;
		return intLitExpression.expressionType;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception 
	{
		Expression s=sleepStatement.getE();
		if(!s.visit(this, null).equals(INTEGER))
		{
			throw new TypeCheckException("not equal to integer");
		}
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception 
	{
		Expression e=whileStatement.getE();
		Block b=whileStatement.getB();
		if(!e.visit(this, null).equals(BOOLEAN))
		{
			throw new TypeCheckException("Not equal to boolean");
		}
		b.visit(this, null);
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception 
	{
		declaration.decType=Type.getTypeName(declaration.getFirstToken());
		if(!symtab.insert(declaration.getIdent().getText(), declaration))
		{
			throw new TypeCheckException("Not able to insert in symbol table maybe present");
		}
		return declaration.decType;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception 
	{
		ArrayList<ParamDec> pd=program.getParams();
		Block b=program.getB();
		for(Dec d:pd)
		{
			d.visit(this, null);
		}
		b.visit(this, null);
		return null;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception 
	{
		TypeName t1=(TypeName)assignStatement.getVar().visit(this, null);
		TypeName t2=(TypeName)assignStatement.getE().visit(this, null);
		if(t1!=t2)
		{
			throw new TypeCheckException(" ");
		}
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception 
	{
		Dec ident=symtab.lookup(identX.firstToken.getText());
		if(ident!=null)
		{
			identX.identlvaluedec=ident;
		}
		else
		{
			throw new TypeCheckException("Not present in symbol table");
		}
		return identX.identlvaluedec.decType;
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception 
	{
		paramDec.decType = Type.getTypeName(paramDec.getFirstToken());
		
		if(!symtab.insert(paramDec.getIdent().getText(), paramDec))
		{
			throw new TypeCheckException("Not able to insert in symbol table maybe present");
		}
		return paramDec.decType;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg)
	{
		constantExpression.expressionType = INTEGER;
		return constantExpression.expressionType;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception 
	{
		imageOpChain.getArg().visit(this, null);
		Kind io=imageOpChain.getFirstToken().kind;
		switch(io)
		{
		case OP_HEIGHT:
		case OP_WIDTH:
		{
			if(imageOpChain.getArg().getExprList().size()==0)
			{
				imageOpChain.chainType=INTEGER;
			}
			else
			{
				throw new TypeCheckException("Length greater than 0");
			}
		}
		break;
		case KW_SCALE:
		{
			if(imageOpChain.getArg().getExprList().size()==1)
			{
				imageOpChain.chainType=IMAGE;
			}
			else
			{
				throw new TypeCheckException("Length greater than 1 or 0");
			}
		}
		break;
		default:
		{
			throw new TypeCheckException("Illegal ImageOp");
		}
		}
		return imageOpChain.chainType;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception 
	{
		for(Expression e:tuple.getExprList())
		{
			if(e.visit(this, null).equals(INTEGER))
			{
				
			}
			else
			{
				throw new TypeCheckException("Illegal Tuple");
			}
		}

		return null;
	}


}