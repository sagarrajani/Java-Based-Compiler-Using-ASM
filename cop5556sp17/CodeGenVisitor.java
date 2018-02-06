package cop5556sp17;
import java.util.ArrayList;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import cop5556sp17.AST.ASTVisitor;
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
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.WhileStatement;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv;

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;
	private int paramdecCount = 0;
	private int slotNumber = 1;

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.getName();
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object",
				new String[] { "java/lang/Runnable" });
		cw.visitSource(sourceFileName, null);
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "([Ljava/lang/String;)V", null, null);
		mv.visitCode();
		Label constructorStart = new Label();
		mv.visitLabel(constructorStart);
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering <init>");
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		ArrayList<ParamDec> params = program.getParams();
		for (ParamDec dec : params) {
			dec.visit(this, mv);
			paramdecCount++;
		}
		mv.visitInsn(RETURN);
		Label constructorEnd = new Label();
		mv.visitLabel(constructorEnd);
		mv.visitLocalVariable("this", classDesc, null, constructorStart, constructorEnd, 0);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, constructorStart, constructorEnd, 1);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		mv.visitCode();
		Label mainStart = new Label();
		mv.visitLabel(mainStart);
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering main");
		mv.visitTypeInsn(NEW, className);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "([Ljava/lang/String;)V", false);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "run", "()V", false);
		mv.visitInsn(RETURN);
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		mv.visitLocalVariable("instance", classDesc, null, mainStart, mainEnd, 1);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
		mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
		mv.visitCode();
		Label startRun = new Label();
		mv.visitLabel(startRun);
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering run");
		program.getB().visit(this, mv);
		mv.visitInsn(RETURN);
		Label endRun = new Label();
		mv.visitLabel(endRun);
		mv.visitLocalVariable("this", classDesc, null, startRun, endRun, 0);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
		cw.visitEnd();
		return cw.toByteArray();
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception 
	{
		Dec d=assignStatement.getVar().getDec();
		Expression e=assignStatement.getE();
		if (d instanceof ParamDec) 
		{
			mv.visitVarInsn(ALOAD, 0);
		}
		e.visit(this, arg);
		if(e.getTypeName().isType(TypeName.IMAGE))
		{
			mv.visitInsn(DUP);
		}
		CodeGenUtils.genPrint(DEVEL, mv, "\nassignment: " + assignStatement.var.getText() + "=");
		CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE().getType());
		assignStatement.getVar().visit(this, arg);
		return null;
	}

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception 
	{
		Chain c=binaryChain.getE0();
		ChainElem ch=binaryChain.getE1();
		c.visit(this, 1);
		if (c.getTypeName().equals(TypeName.URL)) 
		{
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "readFromURL","(Ljava/net/URL;)Ljava/awt/image/BufferedImage;", false);
		} 
		else if (c.getTypeName().equals(TypeName.FILE)) 
		{
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "readFromFile","(Ljava/io/File;)Ljava/awt/image/BufferedImage;", false);
		}
		ch.visit(this, 2);
		return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception 
	{
		Expression e1=binaryExpression.getE0();
		Expression e2=binaryExpression.getE1();
		Kind op=binaryExpression.getOp().kind;
		if (e2.getType().equals(TypeName.IMAGE)) 
		{
			e2.visit(this, arg);
			e1.visit(this, arg);
		} 
		else
		{
			e1.visit(this, arg);
			e2.visit(this, arg);
		}

		switch (op) 
		{
		case PLUS:
			if (e1.getType().equals(TypeName.IMAGE)) 
			{
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "add","(Ljava/awt/image/BufferedImage;Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;",false);
			} 
			else 
			{
				mv.visitInsn(IADD);
			}
			break;
		case MINUS:
			if (e2.getType().equals(TypeName.IMAGE)) 
			{
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "sub","(Ljava/awt/image/BufferedImage;Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;",false);
			} 
			else
			{
				mv.visitInsn(ISUB);
			}
			break;
		case TIMES:
			if (e1.getType().equals(TypeName.IMAGE)|| e2.getType().equals(TypeName.IMAGE))
			{
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "mul","(Ljava/awt/image/BufferedImage;I)Ljava/awt/image/BufferedImage;",false);
			} 
			else 
			{
				mv.visitInsn(IMUL);
			}
			break;
		case DIV:
			if (e1.getType().equals(TypeName.IMAGE)) 
			{
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "div","(Ljava/awt/image/BufferedImage;I)Ljava/awt/image/BufferedImage;",false);
			} 
			else 
			{
				mv.visitInsn(IDIV);
			}
			break;
		case LT:
			LessThan();
			break;
		case LE:
			LessThanEqual();
			break;
		case GT:
			GreaterThan();
			break;
		case GE:
			GreaterThanEqual();
			break;
		case EQUAL:
			if(e1.getTypeName().equals(TypeName.INTEGER)||e1.getTypeName().equals(TypeName.BOOLEAN))
			{

				IntegerEqual();
			}
			else
			{
				ImageEqual();
			}
			break;
		case NOTEQUAL:
			if(e1.getTypeName().equals(TypeName.INTEGER)||e1.getTypeName().equals(TypeName.BOOLEAN))
			{
				IntegerNotEqual();
			}
			else
			{
				ImageNotEqual();
			}
			break;
		case AND:
			mv.visitInsn(IAND);
			break;
		case OR:
			mv.visitInsn(IOR);
			break;
		case MOD:
			if (e1.getType().equals(TypeName.IMAGE)) 
			{
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "mod","(Ljava/awt/image/BufferedImage;I)Ljava/awt/image/BufferedImage;", false);
			} 
			else 
			{
				mv.visitInsn(IREM);
			}
			break;
		default:
			break;
		}
		return null;
		
	}
	
	
	private void IntegerNotEqual()
	{
		Label start_label4 = new Label();
		Label end_label4 = new Label();
		mv.visitJumpInsn(IF_ICMPEQ, start_label4);
		mv.visitInsn(ICONST_1);
		mv.visitJumpInsn(GOTO, end_label4);
		mv.visitLabel(start_label4);
		mv.visitInsn(ICONST_0);
		mv.visitLabel(end_label4);
	}
	private void ImageNotEqual()
	{
		Label start_label4 = new Label();
		Label end_label4 = new Label();
		mv.visitJumpInsn(IF_ACMPEQ, start_label4);
		mv.visitInsn(ICONST_1);
		mv.visitJumpInsn(GOTO, end_label4);
		mv.visitLabel(start_label4);
		mv.visitInsn(ICONST_0);
		mv.visitLabel(end_label4);
	}
	
	private void ImageEqual()
	{
		Label start_label3 = new Label();
		Label end3_label = new Label();
		mv.visitJumpInsn(IF_ACMPEQ, start_label3);
		mv.visitInsn(ICONST_0);
		mv.visitJumpInsn(GOTO, end3_label);
		mv.visitLabel(start_label3);
		mv.visitInsn(ICONST_1);
		mv.visitLabel(end3_label);
	}
	private void IntegerEqual()
	{
		Label start_label3 = new Label();
		Label end3_label = new Label();
		mv.visitJumpInsn(IF_ICMPEQ, start_label3);
		mv.visitInsn(ICONST_0);
		mv.visitJumpInsn(GOTO, end3_label);
		mv.visitLabel(start_label3);
		mv.visitInsn(ICONST_1);
		mv.visitLabel(end3_label);
	}
	private void GreaterThanEqual()
	{
		Label start2_label = new Label();
		Label end_label2 = new Label();
		mv.visitJumpInsn(IF_ICMPGE, start2_label);
		mv.visitInsn(ICONST_0);
		mv.visitJumpInsn(GOTO, end_label2);
		mv.visitLabel(start2_label);
		mv.visitInsn(ICONST_1);
		mv.visitLabel(end_label2);
	}
	
	private void GreaterThan()
	{
		Label star_label1 = new Label();
		Label end_label1 = new Label();
		mv.visitJumpInsn(IF_ICMPGT, star_label1);
		mv.visitInsn(ICONST_0);
		mv.visitJumpInsn(GOTO, end_label1);
		mv.visitLabel(star_label1);
		mv.visitInsn(ICONST_1);
		mv.visitLabel(end_label1);
	}
	
	private void LessThan()
	{
		Label new_label = new Label();
		Label end_label = new Label();
		mv.visitJumpInsn(IF_ICMPLT, new_label);
		mv.visitInsn(ICONST_0);
		mv.visitJumpInsn(GOTO, end_label);
		mv.visitLabel(new_label);
		mv.visitInsn(ICONST_1);
		mv.visitLabel(end_label);
	}
	private void LessThanEqual()
	{
		Label start_label = new Label();
		Label end2_label = new Label();
		mv.visitJumpInsn(IF_ICMPLE, start_label);
		mv.visitInsn(ICONST_0);
		mv.visitJumpInsn(GOTO, end2_label);
		mv.visitLabel(start_label);
		mv.visitInsn(ICONST_1);
		mv.visitLabel(end2_label);
	}

	Label start_ptr;
	Label end_ptr;

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception 
	{
		MethodVisitor mv = (MethodVisitor)arg;
		Label Block_start = new Label();
		ArrayList<Dec> de=block.getDecs();
		for(Dec dec: de)
		{
			if(dec.getTypeName().isType(TypeName.IMAGE,TypeName.FRAME))
			{
//				mv.visitInsn(ACONST_NULL);
//				mv.visitVarInsn(ASTORE, slotNumber);
				blk();
			}
			dec.setSlot(slotNumber);
			slotNumber++;
		}
		start_ptr = Block_start;
		mv.visitLabel(Block_start);
		ArrayList<Statement> st=block.getStatements();
		for(Statement stmt:st)
		{
			stmt.visit(this, arg);
			if(stmt instanceof BinaryChain)
			{
				mv.visitInsn(POP);
			}
		}
		Label Block_end = new Label();
		end_ptr = Block_end;
		mv.visitLabel(Block_end);
		ArrayList<Dec> ded=block.getDecs();
		for(Dec dec: ded)
		{
			dec.visit(this, mv);
		}
		return null;
	}
	
	private void blk()
	{
		mv.visitInsn(ACONST_NULL);
		mv.visitVarInsn(ASTORE, slotNumber);
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception 
	{
		mv.visitLdcInsn(booleanLitExpression.getValue());
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) 
	{
		Token t=constantExpression.getFirstToken();
		if (t.isKind(Kind.KW_SCREENHEIGHT)) 
		{
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFrame", "getScreenHeight", "()I", false);
		} 
		else if (t.isKind(Kind.KW_SCREENWIDTH)) 
		{
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFrame", "getScreenWidth", "()I", false);
		}
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception 
	{
		mv.visitLocalVariable(declaration.getIdent().getText(), declaration.getTypeName().getJVMTypeDesc(), null,start_ptr, end_ptr, declaration.getSlot());
		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception 
	{
		mv.visitInsn(ACONST_NULL);
		Token t=filterOpChain.getFirstToken();
		if (t.isKind(Kind.OP_BLUR)) 
		{
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFilterOps", "blurOp","(Ljava/awt/image/BufferedImage;Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;",false);
		} 
		else if (t.isKind(Kind.OP_CONVOLVE)) 
		{
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFilterOps", "convolveOp","(Ljava/awt/image/BufferedImage;Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;",false);
		}
		if (t.isKind(Kind.OP_GRAY)) 
		{
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFilterOps", "grayOp","(Ljava/awt/image/BufferedImage;Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;",false);
		}
		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		frameOpChain.getArg().visit(this, arg);
		Token t=frameOpChain.getFirstToken();
		if (t.isKind(Kind.KW_SHOW)) 
		{
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKEVIRTUAL, "cop5556sp17/PLPRuntimeFrame", "showImage","()Lcop5556sp17/PLPRuntimeFrame;", false);
		} 
		else if (t.isKind(Kind.KW_MOVE)) 
		{
			mv.visitMethodInsn(INVOKEVIRTUAL, "cop5556sp17/PLPRuntimeFrame", "moveFrame","(II)Lcop5556sp17/PLPRuntimeFrame;", false);
		} 
		else if (t.isKind(Kind.KW_HIDE)) 
		{
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKEVIRTUAL, "cop5556sp17/PLPRuntimeFrame", "hideImage","()Lcop5556sp17/PLPRuntimeFrame;", false);
		} 
		else if (t.isKind(Kind.KW_XLOC)) 
		{
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKEVIRTUAL, "cop5556sp17/PLPRuntimeFrame", "getXVal", "()I", false);
		} 
		else if (t.isKind(Kind.KW_YLOC)) 
		{
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKEVIRTUAL, "cop5556sp17/PLPRuntimeFrame", "getYVal", "()I", false);
		}
		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception 
	{
		Integer part = (Integer) arg;
		Dec d=identChain.getDec();
		TypeName t=identChain.getTypeName();
		int s=identChain.getDec().getSlot();
		Token k=identChain.getFirstToken();
		if (part == 1) 
		{
			if (d instanceof ParamDec) 
			{
//				mv.visitVarInsn(ALOAD, 0);
//				mv.visitFieldInsn(GETFIELD, className, k.getText(),((TypeName) t).getJVMTypeDesc());
				param(k,t);
			} 
			else 
			{
				if (t.equals(TypeName.INTEGER) ||t.equals(TypeName.BOOLEAN))
				{
					mv.visitVarInsn(ILOAD, s);
				} 
				else if (t.equals(TypeName.FILE) || t.equals(TypeName.IMAGE)) {
					mv.visitVarInsn(ALOAD, s);
				}
			}
		}
		else 
		{
			if (t.equals(TypeName.FILE)) 
			{
				if (d instanceof ParamDec) 
				{
//					mv.visitVarInsn(ALOAD, 0);
//					mv.visitFieldInsn(GETFIELD, className, k.getText(),((TypeName) t).getJVMTypeDesc());
					param(k,t);
				} 
				else 
				{
					mv.visitVarInsn(ALOAD, s);
				}

				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "write","(Ljava/awt/image/BufferedImage;Ljava/io/File;)Ljava/awt/image/BufferedImage;", false);
			} 
			else if (t.equals(TypeName.FRAME)) 
			{
				if (d instanceof ParamDec) 
				{
//					mv.visitVarInsn(ALOAD, 0);
//					mv.visitFieldInsn(GETFIELD, className, k.getText(),((TypeName) t).getJVMTypeDesc());
					param(k,t);
				} 
				else 
				{
					mv.visitVarInsn(ALOAD, s);
				}
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFrame", "createOrSetFrame","(Ljava/awt/image/BufferedImage;Lcop5556sp17/PLPRuntimeFrame;)Lcop5556sp17/PLPRuntimeFrame;",false);
			}
			mv.visitInsn(DUP);
			if (t.equals(TypeName.IMAGE) || t.equals(TypeName.INTEGER)|| t.equals(TypeName.BOOLEAN)) 
			{
				if (d instanceof ParamDec) 
				{
//					mv.visitVarInsn(ALOAD, 0);
//					mv.visitInsn(SWAP);
//					mv.visitFieldInsn(PUTFIELD, className, k.getText(),((TypeName) t).getJVMTypeDesc());
					param2(k,t);
				} else 
				{
					if (t.equals(TypeName.IMAGE)) 
					{
						mv.visitVarInsn(ASTORE, s);
					} 
					else if (t.equals(TypeName.INTEGER)|| t.equals(TypeName.BOOLEAN)) 
					{
						mv.visitVarInsn(ISTORE, s);
					}
				}
			}
		}
		return null;
	}
	
	private void param(Token k,TypeName t)
	{
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, className, k.getText(),((TypeName) t).getJVMTypeDesc());
	}
	
	private void param2(Token k,TypeName t)
	{
		mv.visitVarInsn(ALOAD, 0);
		mv.visitInsn(SWAP);
		mv.visitFieldInsn(PUTFIELD, className, k.getText(),((TypeName) t).getJVMTypeDesc());
	}
	

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception 
	{
		Dec d=identExpression.getDec();
		Token t=identExpression.getFirstToken();
		TypeName k=identExpression.getType();
		if (d instanceof ParamDec) 
		{
			param(t,k);
		} 
		else 
		{
			if (k.equals(TypeName.INTEGER)|| k.equals(TypeName.BOOLEAN)) 
			{
				mv.visitVarInsn(ILOAD, d.getSlot());
			} 
			else if (k.equals(TypeName.IMAGE)|| k.equals(TypeName.FRAME)|| k.equals(TypeName.FILE)|| k.equals(TypeName.URL)) 
			{
				mv.visitVarInsn(ALOAD, d.getSlot());
			}
		}
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception 
	{
		MethodVisitor mvs = (MethodVisitor) arg;
		Dec d=identX.getDec();
		Token t=identX.getFirstToken();
		if (d instanceof ParamDec) 
		{
			mvs.visitFieldInsn(PUTFIELD, className, t.getText(),d.getTypeName().getJVMTypeDesc());
		} 
		else 
		{
			if (d.getTypeName().equals(TypeName.IMAGE)) 
			{
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "copyImage","(Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;", false);
				mv.visitVarInsn(ASTORE, d.getSlot());
			} 
			else if (d.getTypeName().equals(TypeName.INTEGER) || d.getTypeName().equals(TypeName.BOOLEAN)) 
			{
				mvs.visitVarInsn(ISTORE, d.getSlot());
			}
		}
		return null;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception 
	{
		Label next = new Label();
		ifStatement.getE().visit(this, arg);
		mv.visitJumpInsn(IFEQ, next);
		ifStatement.getB().visit(this, arg);
		mv.visitLabel(next);
		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception 
	{
		imageOpChain.getArg().visit(this, arg);
		Token t=imageOpChain.getFirstToken();
		if (t.isKind(Kind.OP_WIDTH)) 
		{
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/image/BufferedImage", "getWidth", "()I", false);
		} 
		else if (t.isKind(Kind.OP_HEIGHT)) 
		{
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/image/BufferedImage", "getHeight", "()I", false);
		} 
		else if (t.isKind(Kind.KW_SCALE)) 
		{
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "scale","(Ljava/awt/image/BufferedImage;I)Ljava/awt/image/BufferedImage;", false);
		}
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception 
	{
		mv.visitLdcInsn(new Integer(intLitExpression.value));
		return null;
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception 
	{
		MethodVisitor mv = (MethodVisitor) arg;
		Token t=paramDec.getIdent();
		TypeName p=paramDec.getTypeName();
		cw.visitField(ACC_PUBLIC, t.getText(), p.getJVMTypeDesc(), null, null);
		if (p.equals(TypeName.INTEGER)) 
		{
//			mv.visitVarInsn(ALOAD, 0);
//			mv.visitVarInsn(ALOAD, 1);
//			mv.visitLdcInsn(paramdecCount);
//			mv.visitInsn(AALOAD);
//			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
//			mv.visitFieldInsn(PUTFIELD, className, t.getText(), "I");
			paramDecInt(t);
		} else if (p.equals(TypeName.BOOLEAN)) 
		{
//			mv.visitVarInsn(ALOAD, 0);
//			mv.visitVarInsn(ALOAD, 1);
//			mv.visitLdcInsn(paramdecCount);
//			mv.visitInsn(AALOAD);
//			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
//			mv.visitFieldInsn(PUTFIELD, className, t.getText(), "Z");
			paramDecBool(t);
		} 
		else if (p.equals(TypeName.FILE)) 
		{
//			mv.visitVarInsn(ALOAD, 0);
//			mv.visitTypeInsn(NEW, "java/io/File");
//			mv.visitInsn(DUP);
//			mv.visitVarInsn(ALOAD, 1);
//			mv.visitLdcInsn(paramdecCount);
//			mv.visitInsn(AALOAD);
//			mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
//			mv.visitFieldInsn(PUTFIELD, className, t.getText(), "Ljava/io/File;");
			paramDecFile(t);
		}
		else if (p.equals(TypeName.URL)) 
		{
//			mv.visitVarInsn(ALOAD, 0);
//			mv.visitTypeInsn(NEW, "java/net/URL");
//			mv.visitInsn(DUP);
//			mv.visitVarInsn(ALOAD, 1);
//			mv.visitLdcInsn(paramdecCount);
//			mv.visitInsn(AALOAD);
//			mv.visitMethodInsn(INVOKESPECIAL, "java/net/URL", "<init>", "(Ljava/lang/String;)V", false);
//			mv.visitFieldInsn(PUTFIELD, className, t.getText(), "Ljava/net/URL;");
			paramDecUrl(t);
		}
		return null;
	}
	private void paramDecInt(Token t)
	{
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitLdcInsn(paramdecCount);
		mv.visitInsn(AALOAD);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
		mv.visitFieldInsn(PUTFIELD, className, t.getText(), "I");
	}
	private void paramDecBool(Token t)
	{
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitLdcInsn(paramdecCount);
		mv.visitInsn(AALOAD);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
		mv.visitFieldInsn(PUTFIELD, className, t.getText(), "Z");
	}
	private void paramDecFile(Token t)
	{
		mv.visitVarInsn(ALOAD, 0);
		mv.visitTypeInsn(NEW, "java/io/File");
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitLdcInsn(paramdecCount);
		mv.visitInsn(AALOAD);
		mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
		mv.visitFieldInsn(PUTFIELD, className, t.getText(), "Ljava/io/File;");
	}
	private void paramDecUrl(Token t)
	{
		mv.visitVarInsn(ALOAD, 0);
		mv.visitTypeInsn(NEW, "java/net/URL");
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitLdcInsn(paramdecCount);
		mv.visitInsn(AALOAD);
		mv.visitMethodInsn(INVOKESPECIAL, "java/net/URL", "<init>", "(Ljava/lang/String;)V", false);
		mv.visitFieldInsn(PUTFIELD, className, t.getText(), "Ljava/net/URL;");
	}
	
	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception 
	{
		Expression e=sleepStatement.getE();
		e.visit(this, arg);
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception 
	{
		for (Expression e : tuple.getExprList()) 
		{
			e.visit(this, arg);
		}
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception 
	{
		Label start_guard = new Label();
		Label end_guard = new Label();
		Label while_guard = new Label();
		mv.visitJumpInsn(GOTO, while_guard);
		mv.visitLabel(start_guard);
		whileStatement.getB().visit(this, arg);
		mv.visitLabel(while_guard);
		mv.visitLabel(end_guard);
		whileStatement.getE().visit(this, arg);
		mv.visitJumpInsn(IFNE, start_guard);
		return null;
	}


}