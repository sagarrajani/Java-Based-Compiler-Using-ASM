package cop5556sp17.AST;

import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.Type.TypeName;

public class Dec extends ASTNode {
	
	final Token ident;
	public TypeName decType;
	public int slot;
	boolean t;

	public Dec(Token firstToken, Token ident) {
		super(firstToken);

		this.ident = ident;
	}

	public int getSlot(){
		return slot;
	}
	
	public void setSlot(int slot){
		this.slot = slot;
	}
	
	public TypeName getTypeName(){
		return decType;
	}
	
//	public Token getType() {
//		return firstToken;
//	}

	public Token getIdent() {
		return ident;
	}
	
	public Token getfirstToken() {
		// TODO Auto-generated method stub
		return firstToken;
	}

	@Override
	public String toString() {
		return "Dec [ident=" + ident + ", firstToken=" + firstToken + "]";
	}
	
	public void setInitialized(boolean b)
	{
		t = b;
	}

	public boolean getInitialized()
	{
		return t;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((ident == null) ? 0 : ident.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof Dec)) {
			return false;
		}
		Dec other = (Dec) obj;
		if (ident == null) {
			if (other.ident != null) {
				return false;
			}
		} else if (!ident.equals(other.ident)) {
			return false;
		}
		return true;
	}

	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitDec(this,arg);
	}

//	public int getSlotNum() {
//		// TODO Auto-generated method stub
//		return 0;
//	}



	

}
