package cop5556sp17;

import cop5556sp17.Scanner.Kind;
import static cop5556sp17.Scanner.Kind.*;

import java.util.ArrayList;
import java.util.List;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.*;

public class Parser {

	/**
	 * Exception to be thrown if a syntax error is detected in the input.
	 * You will want to provide a useful error message.
	 *
	 */
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		public SyntaxException(String message) {
			super(message);
		}
	}
	
	/**
	 * Useful during development to ensure unimplemented routines are
	 * not accidentally called during development.  Delete it when 
	 * the Parser is finished.
	 *
	 */
	@SuppressWarnings("serial")	
	public static class UnimplementedFeatureException extends RuntimeException {
		public UnimplementedFeatureException() {
			super();
		}
	}

	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	/**
	 * parse the input using tokens from the scanner.
	 * Check for EOF (i.e. no trailing junk) when finished
	 * 
	 * @throws SyntaxException
	 */
	ASTNode parse() throws SyntaxException {
		Program p=program();
		matchEOF();
		return p;
	}

	Expression expression() throws SyntaxException 
	{
		Token t2=t;
		Expression e1=term();
		while(t.kind==LT||t.kind==LE||t.kind==GT||t.kind==GE||t.kind==EQUAL||t.kind==NOTEQUAL)
		{
			Token t1=null;
			t1=relOp();
			Expression e2=term();
			e1=new BinaryExpression(t2,e1,t1,e2);
		}
		return e1;
	}

	Expression term() throws SyntaxException 
	{
		Token t2=t;
		Expression e1=elem();
		while(t.kind==PLUS||t.kind==MINUS||t.kind==OR)
		{
			Token t1=null;
			t1=weakOp();
			Expression e2=elem();
			e1=new BinaryExpression(t2,e1,t1,e2);
			
		}
		return e1;
	}

	Expression elem() throws SyntaxException 
	{
		Token t2=t;
		Expression e1=factor();
		while(t.kind==TIMES||t.kind==MOD||t.kind==DIV||t.kind==AND)
		{
			Token t1=null;
			//consume();
			t1=strongOp();
			Expression e2=factor();
			e1=new BinaryExpression(t2,e1,t1,e2);
		}
		return e1;
	}
	
	Token relOp() throws SyntaxException 
	{
		Token t1=null;
		if(t.kind==LT||t.kind==LE||t.kind==GT||t.kind==GE||t.kind==EQUAL||t.kind==NOTEQUAL)
		{	t1=t;
			consume();
			return t1;
		}
		else
			throw new SyntaxException("Illegal relOp");
	}
	
	Token weakOp() throws SyntaxException 
	{
		Token t1=null;
		if(t.kind==PLUS||t.kind==MINUS||t.kind==OR)
		{	t1=t;
			consume();
			return t1;
		}
		else
			throw new SyntaxException("Illegal weakOp");
	}
	
	Token strongOp() throws SyntaxException 
	{
		Token t1=null;
		if(t.kind==TIMES||t.kind==DIV||t.kind==AND||t.kind==MOD)
		{	t1=t;
			consume();
			return t1;
		}
		else
			throw new SyntaxException("Illegal strongOp");
	}
	
	Token arrowOp() throws SyntaxException 
	{
		Token t1=null;
		if(t.kind==ARROW||t.kind==BARARROW)
		{
			t1=t;
			consume();
			return t1;
		}
		else
			throw new SyntaxException("Illegal arrowOp");
	}
	
	Token filterOp() throws SyntaxException 
	{
		Token t1=null;
		if(t.kind==OP_BLUR||t.kind==OP_GRAY||t.kind==OP_CONVOLVE)
		{	
			t1=t;
			consume();
			return t1;
		}
		else
			throw new SyntaxException("Illegal filterOp");
	}
	
	Token frameOp() throws SyntaxException 
	{
		Token t1=null;
		if(t.kind==KW_SHOW||t.kind==KW_HIDE||t.kind==KW_MOVE||t.kind==KW_XLOC||t.kind==KW_YLOC)
		{	
			t1=t;
			consume();
			return t1;
		}
		else
			throw new SyntaxException("Illegal frameOp");
	}
	
	Token imageOp() throws SyntaxException 
	{
		Token t1=null;
		if(t.kind==OP_WIDTH||t.kind==OP_HEIGHT||t.kind==KW_SCALE)
		{	
			t1=t;
			consume();
			return t1;
		}
		else
			throw new SyntaxException("Illegal imageOp");
	}

	Expression factor() throws SyntaxException 
	{
		Expression e1=null;
		Kind kind = t.kind;
		Token t1=null;
		switch (kind) 
		{
			case IDENT: 
			{
				t1=t;
				consume();
				e1=new IdentExpression(t1);
				
			}
			break;
			case INT_LIT: 
			{
				t1=t;
				consume();
				e1=new IntLitExpression(t1);
			}
			break;
			case KW_TRUE:
			{
				t1=t;
				consume();
				e1=new BooleanLitExpression(t1);
			}
			break;
			case KW_FALSE: 
			{
				t1=t;
				consume();
				e1=new BooleanLitExpression(t1);
			}
			break;
			case KW_SCREENWIDTH:
			{
				t1=t;
				consume();
				e1=new ConstantExpression(t1);
				
			}
			break;
			case KW_SCREENHEIGHT: 
			{
				t1=t;
				consume();
				e1=new ConstantExpression(t1);
			}
			break;
			case LPAREN: 
			{
				consume();
				e1=expression();
				match(RPAREN);
			}
			break;
			default:
				//you will want to provide a more useful error message
				throw new SyntaxException("illegal factor");
		}
		return e1;
	}
	
	Block block() throws SyntaxException 
	{
//		System.out.println(t.kind);
		Token t1=t;
		ArrayList<Dec> l1=new ArrayList<Dec>();
		ArrayList<Statement> l2=new ArrayList<Statement>();
		if(t.kind==LBRACE)
		{
			consume();
			while(!t.kind.equals(RBRACE))
			{
				
				if(t.kind==KW_INTEGER||t.kind==KW_BOOLEAN||t.kind==KW_IMAGE||t.kind==KW_FRAME)
				{	
					Dec d=dec();
					l1.add(d);
				}
				else if(t.kind==OP_SLEEP||t.kind==KW_WHILE||t.kind==KW_IF||t.kind==IDENT||t.kind==OP_BLUR||t.kind==OP_GRAY||t.kind==OP_CONVOLVE||t.kind==KW_SHOW||t.kind==KW_HIDE||t.kind==KW_MOVE||t.kind==KW_XLOC||t.kind==KW_YLOC||t.kind==OP_WIDTH||t.kind==OP_HEIGHT||t.kind==KW_SCALE)
				{

					Statement st1=statement();
					l2.add(st1);
				}
				else
				{
					throw new SyntaxException("Illegal block");
				}
			}
			match(RBRACE);
			
			return new Block(t1,l1,l2);
		}
		else
			throw new SyntaxException("Illegal block");
	}

	Program program() throws SyntaxException 
	{
		ArrayList<ParamDec> p=new ArrayList<ParamDec>();
		Token t1=t;
		if(t.kind==IDENT)
		{
			consume();
			if(t.kind==KW_INTEGER||t.kind==KW_BOOLEAN||t.kind==KW_URL||t.kind==KW_FILE)
			{
				ParamDec p1=paramDec();
				p.add(p1);
				while(t.kind==COMMA)
				{
					consume();
					ParamDec p2=paramDec();
					p.add(p2);
				}
				Block b1=block();
				return new Program(t1,p,b1);
			}
			else
			{
				Block b2=block();
				return new Program(t1,p,b2);
			}
		}
		else
			throw new SyntaxException("Illegal program");
	}
	

	ParamDec paramDec() throws SyntaxException 
	{
		if(t.kind==KW_INTEGER||t.kind==KW_BOOLEAN||t.kind==KW_URL||t.kind==KW_FILE)
		{	
			Token t1=t;
			consume();
			ParamDec pd=new ParamDec(t1,t);
			match(IDENT);
			return pd;
		}
		else
			throw new SyntaxException("Illegal paramdec");
	}

	Dec dec() throws SyntaxException 
	{
		if(t.kind==KW_INTEGER||t.kind==KW_BOOLEAN||t.kind==KW_IMAGE||t.kind==KW_FRAME)
		{	
			Token t1=t;
			consume();
			Dec d=new Dec(t1,t);
			match(IDENT);
			return d;
		}
		else
			throw new SyntaxException("Illegal dec");
		
	}
	
	AssignmentStatement assign() throws SyntaxException
	{
		if(t.kind==IDENT)
		{
			Token t1=t;
			IdentLValue ild=new IdentLValue(t);
			consume();
			if(t.kind==ASSIGN)
			{
				consume();
				Expression e1=expression();
				return new AssignmentStatement(t1,ild,e1);
			}
			else
				throw new SyntaxException("Illegal assign");	
		}
		else
			throw new SyntaxException("Illegal assign");
	}

	Statement statement() throws SyntaxException 
	{
		
		if(t.kind==OP_SLEEP)
		{
			Token t1=t;
			consume();
			Expression e1=expression();
			Statement st=new SleepStatement(t1,e1);
			match(SEMI);
			return st;
		}
		else if(t.kind==KW_WHILE)
		{
			Statement st=whileStatement();
			return st;
		}
		else if(t.kind==KW_IF)
		{
			Statement st=ifStatement();
			return st;
		}
		else if(t.kind==IDENT)
		{
			if(scanner.peek().kind.equals(ASSIGN))
			{
				Statement st=assign();
				match(SEMI);
				return st;
			}
			else if(scanner.peek().kind.equals(ARROW)||scanner.peek().kind.equals(BARARROW))
			{
					Statement st=chain();
					match(SEMI);
					return st;
			}
			else
				throw new SyntaxException("Illegal statement");
		}
		else if(t.kind==OP_BLUR||t.kind==OP_GRAY||t.kind==OP_CONVOLVE||
				t.kind==KW_SHOW||t.kind==KW_HIDE||t.kind==KW_MOVE||t.kind==KW_XLOC||t.kind==KW_YLOC||
				t.kind==OP_WIDTH||t.kind==OP_HEIGHT||t.kind==KW_SCALE)
		{
				Statement st=chain();
				match(SEMI);
				return st;
		}
		else
			throw new SyntaxException("Illegal statement");
	}

	WhileStatement whileStatement() throws SyntaxException
	{
		Token t1=t;
		match(KW_WHILE);
		match(LPAREN);
		Expression e1=expression();
		match(RPAREN);
		Block b1=block();
		return new WhileStatement(t1,e1,b1);
	}
	IfStatement ifStatement() throws SyntaxException
	{
		Token t1=t;
		match(KW_IF);
		match(LPAREN);
		Expression e1=expression();
		match(RPAREN);
		Block b1=block();
		return new IfStatement(t1,e1,b1);
	}
	Chain chain() throws SyntaxException 
	{
		
//		System.out.println(t.kind);
		Chain ch=null;
		Token t1=t;
		Chain ch1=chainElem();
		Token ao=arrowOp();
		ChainElem ch2=chainElem();
		ch=new BinaryChain(t1,ch1,ao,ch2);
			while(t.kind==ARROW||t.kind==BARARROW)
			{
				Token t2=t;
				consume();
				ChainElem ch3=chainElem();
				ch=new BinaryChain(t1,ch,t2,ch3);
			}
			return ch;
	}

	ChainElem chainElem() throws SyntaxException 
	{
		if(t.kind==IDENT)
		{
			ChainElem ce=new IdentChain(t);
			consume();
			return ce;
		}
		else if(t.kind==OP_BLUR||t.kind==OP_GRAY||t.kind==OP_CONVOLVE)
		{
			ChainElem ce=null;
			Token t1=filterOp();
			Tuple t2=arg();
			ce=new FilterOpChain(t1,t2);
			return ce;
		}
		else if(t.kind==KW_SHOW||t.kind==KW_HIDE||t.kind==KW_MOVE||t.kind==KW_XLOC||t.kind==KW_YLOC)
		{
			ChainElem ce=null;
			Token t1=frameOp();
			Tuple t2=arg();
			ce=new FrameOpChain(t1,t2);
			return ce;
		}
		else if(t.kind==OP_WIDTH||t.kind==OP_HEIGHT||t.kind==KW_SCALE)
		{
			ChainElem ce=null;
			Token t1=imageOp();
			Tuple t2=arg();
			ce=new ImageOpChain(t1,t2);
			return ce;
		}
		else
			throw new SyntaxException("Illegal chainElem");
	}

	Tuple arg() throws SyntaxException 
	{
		List<Expression> l1=new ArrayList<Expression>();
		Token to=t;
		if(!t.kind.equals(LPAREN))
		{
			return new Tuple(to,l1);
		}
		else if(t.kind==LPAREN)
		{
			Expression e1=null;
			Expression e2=null;
			consume();
			e1=expression();
			l1.add(e1);
			while(t.kind==COMMA)
			{
				consume();
				e2=expression();
				l1.add(e2);
			}
			match(RPAREN);
			return new Tuple(to,l1);
		}
		else
			throw new SyntaxException("Illegal arg");
	}

	/**
	 * Checks whether the current token is the EOF token. If not, a
	 * SyntaxException is thrown.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException 
	{
		if (t.kind.equals(EOF)) {
			return t;
		}
		throw new SyntaxException("expected EOF");
	}

	/**
	 * Checks if the current token has the given kind. If so, the current token
	 * is consumed and returned. If not, a SyntaxException is thrown.
	 * 
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException 
	{
		if (t.kind.equals(kind)) 
		{
			return consume();
		}
		throw new SyntaxException("saw " + t.kind + "expected " + kind);
	}

	/**
	 * Checks if the current token has one of the given kinds. If so, the
	 * current token is consumed and returned. If not, a SyntaxException is
	 * thrown.
	 * 
	 * * Precondition: for all given kinds, kind != EOF
	 * 
	 * @param kinds
	 *            list of kinds, matches any one
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind... kinds) throws SyntaxException 
	{
		// TODO. Optional but handy
		return null; //replace this statement
	}

	/**
	 * Gets the next token and returns the consumed token.
	 * 
	 * Precondition: t.kind != EOF
	 * 
	 * @return
	 * 
	 */
	private Token consume() throws SyntaxException {
		Token tmp = t;
		t = scanner.nextToken();
		return tmp;
	}

}
