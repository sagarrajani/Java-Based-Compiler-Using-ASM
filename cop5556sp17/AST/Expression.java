package cop5556sp17.AST;

import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.Type.TypeName;

public abstract class Expression extends ASTNode {
	
	public TypeName expressionType;
//	public Dec expressionDec;
//	public TypeName val;
	
	protected Expression(Token firstToken) {
		super(firstToken);
	}

	@Override
	abstract public Object visit(ASTVisitor v, Object arg) throws Exception;
	
	public TypeName getType(){
		return expressionType;
	}

	public TypeName getTypeName() {
		// TODO Auto-generated method stub
		return expressionType;
	}
	

}
