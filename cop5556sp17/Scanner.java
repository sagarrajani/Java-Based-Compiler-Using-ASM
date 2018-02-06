package cop5556sp17;

import java.util.ArrayList;
import java.util.HashMap;

import cop5556sp17.Scanner.Kind;



public class Scanner {
	/**
	 * Kind enum
	 */
	
	public static enum Kind {
		IDENT(""), INT_LIT(""), KW_INTEGER("integer"), KW_BOOLEAN("boolean"), 
		KW_IMAGE("image"), KW_URL("url"), KW_FILE("file"), KW_FRAME("frame"), 
		KW_WHILE("while"), KW_IF("if"), KW_TRUE("true"), KW_FALSE("false"), 
		SEMI(";"), COMMA(","), LPAREN("("), RPAREN(")"), LBRACE("{"), 
		RBRACE("}"), ARROW("->"), BARARROW("|->"), OR("|"), AND("&"), 
		EQUAL("=="), NOTEQUAL("!="), LT("<"), GT(">"), LE("<="), GE(">="), 
		PLUS("+"), MINUS("-"), TIMES("*"), DIV("/"), MOD("%"), NOT("!"), 
		ASSIGN("<-"), OP_BLUR("blur"), OP_GRAY("gray"), OP_CONVOLVE("convolve"), 
		KW_SCREENHEIGHT("screenheight"), KW_SCREENWIDTH("screenwidth"), 
		OP_WIDTH("width"), OP_HEIGHT("height"), KW_XLOC("xloc"), KW_YLOC("yloc"), 
		KW_HIDE("hide"), KW_SHOW("show"), KW_MOVE("move"), OP_SLEEP("sleep"), 
		KW_SCALE("scale"), EOF("eof");

		Kind(String text) {
			this.text = text;
		}

		final String text;

		String getText() {
			return text;
		}
	}
	
	public static enum State
	{
		START,
		IN_DIGIT,
		IN_IDENT,
		AFTER_EQUAL,
		AFTER_LESSTHAN,
		AFTER_GREATERTHAN,
		AFTER_MINUS,
		AFTER_OR,
		AFTER_NOT,
		AFTER_DIVISION,
		COMMENT
	}
	
	HashMap<String,Kind> map=new HashMap<String,Kind>();
	ArrayList<Integer> al=new ArrayList<Integer>();
	int positioninline=0;
	int lineno=0;
/**
 * Thrown by Scanner when an illegal character is encountered
 */
	@SuppressWarnings("serial")
	public static class IllegalCharException extends Exception {
		public IllegalCharException(String message) {
			super(message);
		}
	}
	
	/**
	 * Thrown by Scanner when an int literal is not a value that can be represented by an int.
	 */
	@SuppressWarnings("serial")
	public static class IllegalNumberException extends Exception {
	public IllegalNumberException(String message){
		super(message);
		}
	}
	

	/**
	 * Holds the line and position in the line of a token.
	 */
	static class LinePos {
		public final int line;
		public final int posInLine;
		
		public LinePos(int line, int posInLine) {
			super();
			this.line = line;
			this.posInLine = posInLine;
		}

		@Override
		public String toString() {
			return "LinePos [line=" + line + ", posInLine=" + posInLine + "]";
		}
	}
		

	

	public class Token {
		public final Kind kind;
		public final int pos; 
		public final int length;  

		public String getText() 
		{
			if(this.kind == Kind.IDENT ||this.kind == Kind.INT_LIT)
			{
				return chars.substring(pos, pos+length);
			}
			else
			return kind.getText();
		}
		

		LinePos getLinePos()
		{
			int line=0;
			int pos_line=0;

			if(al.size()>=2)
			{
			for(int k=0;k<al.size()-1;k++)
			{
				if(pos>=al.get(k) && pos < al.get(k+1))
				{
					line=k;
					pos_line=pos-al.get(k);
				}
				
				else if(pos>=al.get(al.size()-1))
				{
					line=al.size()-1;
					pos_line=pos-al.get(al.size()-1);
				}
			}
			}
			else
			{
				line=0;
				pos_line=pos;
			}

			LinePos l=new LinePos(line,pos_line);			
			return l;
		
		}

		Token(Kind kind, int pos, int length) {
			this.kind = kind;
			this.pos = pos;
			this.length = length;
		}

		/** 
		 * Precondition:  kind = Kind.INT_LIT,  the text can be represented with a Java int.
		 * Note that the validity of the input should have been checked when the Token was created.
		 * So the exception should never be thrown.
		 * 
		 * @return  int value of this token, which should represent an INT_LIT
		 * @throws NumberFormatException
		 */
		public int intVal() throws NumberFormatException
		{
			float f=Integer.parseInt(chars.substring(pos, pos+length));
			if(f>Integer.MAX_VALUE)
			{
				throw new NumberFormatException();
			}
			else
				return Integer.parseInt(chars.substring(pos, pos+length));
		}
		
		@Override
		  public int hashCode() {
		   final int prime = 31;
		   int result = 1;
		   result = prime * result + getOuterType().hashCode();
		   result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		   result = prime * result + length;
		   result = prime * result + pos;
		   return result;
		  }

		  @Override
		  public boolean equals(Object obj) {
		   if (this == obj) {
		    return true;
		   }
		   if (obj == null) {
		    return false;
		   }
		   if (!(obj instanceof Token)) {
		    return false;
		   }
		   Token other = (Token) obj;
		   if (!getOuterType().equals(other.getOuterType())) {
		    return false;
		   }
		   if (kind != other.kind) {
		    return false;
		   }
		   if (length != other.length) {
		    return false;
		   }
		   if (pos != other.pos) {
		    return false;
		   }
		   return true;
		  }

		 

		  private Scanner getOuterType() {
		   return Scanner.this;
		  }


		public String getJVMTypeDesc() {
			// TODO Auto-generated method stub
			return kind.getText();
		}


		public boolean isKind(Kind kind) 
		{
			// TODO Auto-generated method stub
			return this.kind.equals(kind);
		}
		
	}

	 


	Scanner(String chars) 
	{
		this.chars = chars;
		tokens = new ArrayList<Token>();
		al.add(0);
		map.put("integer",Kind.KW_INTEGER);
		map.put("boolean",Kind.KW_BOOLEAN);
		map.put("image",Kind.KW_IMAGE);
		map.put("url",Kind.KW_URL);
		map.put("file",Kind.KW_FILE);
		map.put("frame",Kind.KW_FRAME);
		map.put("while",Kind.KW_WHILE);
		map.put("if",Kind.KW_IF);
		map.put("true", Kind.KW_TRUE);
		map.put("false",Kind.KW_FALSE);
		map.put("blur", Kind.OP_BLUR);
		map.put("gray", Kind.OP_GRAY);
		map.put("convolve", Kind.OP_CONVOLVE);
		map.put("screenheight", Kind.KW_SCREENHEIGHT);
		map.put("screenwidth", Kind.KW_SCREENWIDTH);
		map.put("width", Kind.OP_WIDTH);
		map.put("height",Kind.OP_HEIGHT);
		map.put("xloc", Kind.KW_XLOC);
		map.put("yloc",Kind.KW_YLOC);
		map.put("hide", Kind.KW_HIDE);
		map.put("show",Kind.KW_SHOW);
		map.put("move",Kind.KW_MOVE);
		map.put("sleep",Kind.OP_SLEEP);
		map.put("scale", Kind.KW_SCALE);
		map.put("eof", Kind.EOF);
	 
	}


	
	/**
	 * Initializes Scanner object by traversing chars and adding tokens to tokens list.
	 * 
	 * @return this scanner
	 * @throws IllegalCharException
	 * @throws IllegalNumberException
	 */
	State state;
	public Scanner scan() throws IllegalCharException, IllegalNumberException 
	{
		int pos = 0; 
		int length=chars.length();
		state=State.START;
		int startPos=0;
		int ch;
		
		while(pos<=length)
		{
			ch = pos<length?chars.charAt(pos):-1;
			switch(state)
			{
				case START:
				{
					ch=pos<length?chars.charAt(pos):-1; 
					startPos=pos; 
					switch (ch) 
					{
					case -1: 
					{
						tokens.add(new Token(Kind.EOF, pos, 0)); 
						pos++;
					}  
					break; 
					case '+': 
					{
						tokens.add(new Token(Kind.PLUS, startPos, 1));
						pos++;
					} 
					break; 
					case '*': 
					{
						tokens.add(new Token(Kind.TIMES, startPos, 1));
						pos++;
						} 
					break; 
					case '=': 
					{
						state = State.AFTER_EQUAL;
						pos++;
					}
					break; 
					case ';':
					{
						//
						tokens.add(new Token(Kind.SEMI,startPos,1));
						pos++;

					}
					break;
					case ',':
					{
						tokens.add(new Token(Kind.COMMA,startPos,1));
						pos++;
					}
					break;
					case '(':
					{
						tokens.add(new Token(Kind.LPAREN,startPos,1));
						pos++;
					}
					break;
					case ')':
					{
						tokens.add(new Token(Kind.RPAREN,startPos,1));
						pos++;
					}
					break;
					case '{':
					{
						tokens.add(new Token(Kind.LBRACE,startPos,1));
						pos++;
					}
					break;
					case '}':
					{
						tokens.add(new Token(Kind.RBRACE,startPos,1));
						pos++;
					}
					break;
					case '&':
					{
						tokens.add(new Token(Kind.AND,startPos,1));
						pos++;
					}
					break;
					case '%':
					{
						tokens.add(new Token(Kind.MOD,startPos,1));
						pos++;
					}
					break;
					case '-':
					{
						state=State.AFTER_MINUS;
						pos++;
					}
					break;
					case '|':
					{
						state=State.AFTER_OR;
						pos++;
					}
					break; 
					case '!':
					{
						state=State.AFTER_NOT;
						pos++;
					}
					break;
					case '<':
					{
						pos++;
						state=State.AFTER_LESSTHAN;
					}
					break; 
					case '>':
					{
						state=State.AFTER_GREATERTHAN;
						pos++;
					}
					break; 
					case '/':
					{
						state=State.AFTER_DIVISION;
						pos++;
					}
					break; 
					case ' ':
					{
						pos++;
						state=State.START;
					}
					break;
					case '\r':
					{
						pos++;
						state=State.START;
					}
					break;
					case '\t':
					{
						pos++;
						state=State.START;
					}
					break;
					case '\n':
					{
						
						lineno++;
						pos++;
						al.add(pos);
						state=State.START;
					}
					break;
					default: 
					{
						
						if (Character.isDigit(ch)) 
						{
							if(ch=='0')
							{
								tokens.add(new Token(Kind.INT_LIT,startPos, 1));
								pos++;
							}
							else{
							state = State.IN_DIGIT;
							pos++;
							}
						} 
						else if (Character.isJavaIdentifierStart(ch)) 
						{
							state = State.IN_IDENT;
							pos++; 
						} 
						else 
						{
							System.out.println();
							throw new IllegalCharException( "illegal char "+String.valueOf(Character.toChars(ch))+" at pos "+pos); 
						} 
					}
					break;
					} 
					}
				break;
				case IN_DIGIT:
				{
					if(Character.isDigit(ch))
					{
						pos++;
						state=State.IN_DIGIT;
					}
					
					else
					{
						String num=chars.substring(startPos,pos);
						try
						{
							
							Integer.parseInt(num);
						tokens.add(new Token(Kind.INT_LIT,startPos,pos-startPos));
						state=State.START;
						}
						catch(NumberFormatException e)
						{
							
							throw new IllegalNumberException("Input Number "+num+" exceed the limit of Integer range");
						}
					}
				}
				break;
				case IN_IDENT:
				{
					if(Character.isJavaIdentifierPart(ch))
					{
						pos++;
						state=State.IN_IDENT;
					}
					else if(map.containsKey(chars.substring(startPos, pos).toString()))
					{
						tokens.add(new Token(map.get(chars.substring(startPos,pos)),startPos,pos-startPos));
						state=State.START;
					}
					else
					{
						tokens.add(new Token(Kind.IDENT,startPos,pos-startPos));
						state=State.START;
					}
				}
				break;
				case AFTER_EQUAL:
				{

					switch(ch)
					{
					case '=':
					{
						tokens.add(new Token(Kind.EQUAL,startPos,2));
						pos++;
						state=State.START;
					}
					break;
					default:
					{
						throw new IllegalCharException( "illegal char " +ch+" at pos "+pos);
					}
					}
				}
				break;
				case AFTER_MINUS:
				{

					switch(ch)
					{
					case '>':
					{
						tokens.add(new Token(Kind.ARROW,startPos,2));
						pos++;
						state=State.START;
					}
					break;
					default:
					{
						tokens.add(new Token(Kind.MINUS,startPos,1));
						state=State.START;
					}
					}
				}
				break;
				case AFTER_OR:
				{

					switch(ch)
					{
					case '-':
					{
					int	cha = pos<length-1?chars.charAt(pos):-1;
						if(cha==-1)
						{
							tokens.add(new Token(Kind.OR,startPos,1));
							state=State.START;
						}
						else
						{
						if(chars.charAt(pos+1)=='>')
						{
						tokens.add(new Token(Kind.BARARROW,startPos,3));
						pos=pos+2;
						state=State.START;
						}
						else
						{
							tokens.add(new Token(Kind.OR,startPos,1));
							//pos--;
							//tokens.add(new Token(Kind.MINUS,startPos+1,1));
							state=State.START;
						}
						}
						
					}
					break;
					default:
					{
						tokens.add(new Token(Kind.OR,startPos,1));
						//tokens.add(new Token(Kind.MINUS,startPos+1,1));
						state=State.START;
					}
					break;
					}
					
				}
				break;
				case AFTER_LESSTHAN:
				{

					switch(ch)
					{
					case '=':
					{
						tokens.add(new Token(Kind.LE,startPos,2));
						pos++;
						state=State.START;
					}
					break;
					case '-':
					{
						tokens.add(new Token(Kind.ASSIGN,startPos,2));
						pos++;
						state=State.START;
					}
					break;
					default:
					{
						tokens.add(new Token(Kind.LT,startPos,1));
						state=State.START;
					}
					break;
					}
					
					
				}
				break;
				case AFTER_GREATERTHAN:
				{

					switch(ch)
					{
					case '=':
					{
						tokens.add(new Token(Kind.GE,startPos,2));
						pos++;
						state=State.START;
					}
					break;
					default:
					{
						tokens.add(new Token(Kind.GT,startPos,1));
						state=State.START;
					}
					
					}
				}
				break;
				case AFTER_DIVISION:
				{
					switch(ch)
					{
					case '*':
						{
							state=State.COMMENT;
							pos++;
					
						}
						break;
						default :
						{
						
						tokens.add(new Token(Kind.DIV,startPos,1));
						state=State.START;
						}
						break;
					}
				}
				break;
				case AFTER_NOT:
				{

					switch(ch)
					{
					case '=':
					{
						tokens.add(new Token(Kind.NOTEQUAL,startPos,2));
						pos++;
						state=State.START;
					}
					break;
					default:
					{
						tokens.add(new Token(Kind.NOT,startPos,1));
						state=State.START;
					}
					
					}
				}
				break;
				case COMMENT:
				{
					ch=pos<length?chars.charAt(pos):-1; 
					
					switch (ch) 
					{
					case -1: 
					{
						state=State.START;
					}  
					break;
					case '*':
					{
						if(pos+1<length)
						{
						if(chars.charAt(pos+1)=='/')
						{
						
							state=State.START;
							pos=pos+2;
						}
						else
						{
							pos++;
							
							state=State.COMMENT;
							
						}
						}
						else
						{
							state=State.START;
							pos++;
						}
					}
						break;
					default:
					{
					
							pos++;
							
					}
					break;
					}
					
					}
				 
				break;
				default:
					assert false;
			}
		}
		
		return this;  
	}





	final ArrayList<Token> tokens;
	final String chars;

	int tokenNum;

	/*
	 * Return the next token in the token list and update the state so that
	 * the next call will return the Token..  
	 */
	public Token nextToken() {
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum++);
	}
	
	/*
	 * Return the next token in the token list without updating the state.
	 * (So the following call to next will return the same token.)
	 */
	public Token peek(){
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum);		
	}

	

	/**
	 * Returns a LinePos object containing the line and position in line of the 
	 * given token.  
	 * 
	 * Line numbers start counting at 0
	 * 
	 * @param t
	 * @return
	 */
	public LinePos getLinePos(Token t) {
		//TODO IMPLEMENT THIS
		return t.getLinePos();
	}


}
