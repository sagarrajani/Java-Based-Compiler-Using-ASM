package cop5556sp17;


import cop5556sp17.AST.Dec;
import java.util.*;


public class SymbolTable {
	
	int current_scope,next_scope;
	Stack<Integer> scope_stack =new Stack<Integer>();
	HashMap<String,ArrayList<SymTab>> map=new HashMap<String,ArrayList<SymTab>>();

	/** 
	 * to be called when block entered
	 */
	public void enterScope()
	{
		current_scope=++next_scope;
		scope_stack.push(current_scope);
	}
	
	
	/**
	 * leaves scope
	 */
	public void leaveScope()
	{
		scope_stack.pop();
		current_scope=scope_stack.peek();
	}
	
	public boolean insert(String ident, Dec dec)
	{
		ArrayList<SymTab> al=new ArrayList<SymTab>();
		SymTab st=new SymTab(current_scope,dec);
		if(map.containsKey(ident))
		{
			al=map.get(ident);
			for(int i=0;i<al.size();i++)
			{
				SymTab s=al.get(i);
				{
					if(s.scope==current_scope)
						return false;
				}
			}
		}
		al.add(st);
		map.put(ident, al);
		return true;
	}
	
	public Dec lookup(String ident){
		if(!map.containsKey(ident))
			return null;
		
		Dec dec=null;
		ArrayList<SymTab> ps = map.get(ident);
		for(int i=ps.size()-1;i>=0;i--)
		{
			int temp_scope = ps.get(i).getScope();
			if(scope_stack.contains(temp_scope))
			{
				dec = ps.get(i).getDec();
				break;
			}
		}
		return dec;
	}
	
	public SymbolTable()
	{
		this.current_scope=0;
		this.next_scope=0;
		scope_stack.push(0);
	}


	@Override
	public String toString() 
	{
		return this.toString();
	}
	
	class SymTab
	{
		int scope;
		Dec dec;
		public SymTab(int temp_scope,Dec temp_dec)
		{
			this.scope=temp_scope;
			this.dec=temp_dec;
		}
		
		public int getScope()
		{
			return scope;
		}
		
		public Dec getDec()
		{
			return dec;
		}
	}
	

}
