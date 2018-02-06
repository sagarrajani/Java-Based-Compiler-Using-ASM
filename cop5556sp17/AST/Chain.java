package cop5556sp17.AST;

import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.Type.TypeName;


public abstract class Chain extends Statement {
	public TypeName chainType;
	public Dec identDec;
	
	
	
	public Chain(Token firstToken) {
		super(firstToken);
	}


//
//	public TypeName getTypename() {
//		// TODO Auto-generated method stub
//		return chainType;
//	}

	public void setTypeName(TypeName typeName)
	{
	this.chainType=typeName;
	}

	public TypeName getTypeName() {
		// TODO Auto-generated method stub
		return chainType;
	}
	
	public Dec getDec()
	{
		return identDec;
	}



	
	
	
	
	
	

}
