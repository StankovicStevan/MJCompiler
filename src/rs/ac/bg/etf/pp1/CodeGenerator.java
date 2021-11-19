package rs.ac.bg.etf.pp1;

import java.util.Stack;

import rs.ac.bg.etf.pp1.CounterVisitor.FormParamCounter;
import rs.ac.bg.etf.pp1.CounterVisitor.VarCounter;
import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class CodeGenerator extends VisitorAdaptor {
	
	private int mainPC;
	public int getMainPc() {
		return mainPC;
	}
	Stack<Integer> fixAnd = new Stack<>();
	Stack<Integer> fixOr = new Stack<>();
	int			   skipThen, skipElse;

	@Override
	public void visit(MethodName MethodName) {
		if (MethodName.getMethName().equals("main")) {
			mainPC = Code.pc;
		}
		
		//MethodName.obj.setAdr(Code.pc);
		
		//Collect arguments and local variables
		SyntaxNode methodNode = MethodName.getParent();
		
		VarCounter varCnt = new VarCounter();
		methodNode.traverseBottomUp(varCnt);
		
		FormParamCounter fpCnt = new FormParamCounter();
		methodNode.traverseBottomUp(fpCnt);
		
		//Generate the entry
		Code.put(Code.enter);
		Code.put(fpCnt.getCount()); // Broj formalnih parametara
		Code.put(fpCnt.getCount() + varCnt.getCount()); // Lokalne deklarisane promenljive i formalne parametre
	}

	@Override
	public void visit(MethodDecl MethodDecl) {
		Code.put(Code.exit);
		Code.put(Code.return_);
	}

	// Factor
	public void visit(DesignatorAct factor) {
		// TODO: poziv funckije
	}

	public void visit(NoDesignatorAct factor) {
		Obj designator = factor.getDesignator().obj;
		Code.load(designator);
	}

	public void visit(NumConstF factor) {
		int value = factor.getN1();
		Code.loadConst(value);
	}

	public void visit(CharConstF factor) {
		int value = factor.getC1();
		Code.loadConst(value);
	}

	public void visit(BoolConstF factor) {
		int value = factor.getB1() ? 1 : 0;
		Code.loadConst(value);
	}

	public void visit(NewType factor) {
		Code.put(Code.newarray);
		if (factor.getType().struct == Tab.charType || factor.getType().struct.getKind() == Struct.Bool) {
			Code.put(0);
		} else {
			Code.put(1);
		}
	}

	public void visit(ExprFactor factor) {
	}

	public void visit(TermStmt stmt) {
		int op = stmt.getMulop().integerwrapper.getValue();
		Code.put(op);
	}
	
	public void visit(ExprMinusStmt exprMinusStmt) {
		Code.put(Code.neg);
	}

	public void visit(AddopTermListStmt stmt) {
		int op = stmt.getAddop().integerwrapper.getValue();
		Code.put(op);
	}

	@Override
	public void visit(AssignopExpr AssignopExpr) {
		Obj designator = AssignopExpr.getDesignator().obj;
		Code.store(designator);
	}

	@Override
	public void visit(NoNumPrint NoNumPrint) {
		int length = 1;
		Code.loadConst(length);
		if (NoNumPrint.getExpr().struct == Tab.charType)
			Code.put(Code.bprint);
		else
			Code.put(Code.print);
	}

	@Override
	public void visit(Print Print) {
		int length = Print.getN2();
		Code.loadConst(length);
		if (Print.getExpr().struct == Tab.charType) {
			Code.loadConst(1);
			Code.put(Code.bprint);
		}
			
		else {
			Code.loadConst(5);
			Code.put(Code.print);
		}
				
	}

	@Override
	public void visit(Read Read) {
		Obj designator = Read.getDesignator().obj;
		if (designator.getKind() == Struct.Char)
			Code.put(Code.bread);
		else
			Code.put(Code.read);
		//Code.put(Code.read);
		
		Code.store(designator);
	}
	
	public void visit(Inc inc) {
		//x++
		Obj designator = inc.getDesignator().obj;
		
		Code.load(designator);
		Code.loadConst(1);
		Code.put(Code.add);
		Code.store(designator);
	}
	
	public void visit(Dec dec) {
		Obj designator = dec.getDesignator().obj;
		
		Code.load(designator);
		Code.loadConst(1);
		Code.put(Code.sub);
		Code.store(designator);
	}
	
	@Override
	public void visit(DesignatorName designatorName) {
		Code.load(designatorName.obj);
	}
	
	public void visit(CondFactExprRelExprStmt condFact) {
		int op = condFact.getRelop().integerwrapper.getValue();
		Code.putFalseJump(op, 0);//3 bajta zauzima, poslednji je adresa, znaci -2
		fixAnd.push(Code.pc-2);
	}
	
	//TODO: CondFactExprStmt
	
	public void visit(CondFactExprStmt condFact) {
		int op = Code.ne;
		Code.loadConst(0);
		Code.putFalseJump(op, 0);//3 bajta zauzima, poslednji je adresa, znaci -2
		fixAnd.push(Code.pc-2);
	}
	
	public void visit(ConditionTermStmt condTerm) {
		
		Code.putJump(0); // skok na then granu
		fixOr.push(Code.pc-2);
		
		while(!fixAnd.isEmpty()) {
			int fixme = fixAnd.pop();
			Code.fixup(fixme);
		}
	}
	
	
	public void visit(ConditionoR condTerm) {
		
		Code.putJump(0);//Skok na else granu
		fixOr.push(Code.pc-2);
		
		while(!fixAnd.isEmpty()) {
			int fixme = fixAnd.pop();
			Code.fixup(fixme);
		}
		
		
	}
	
	public void visit(Condition condition) {
		
		Code.putJump(0);			// skok na else
		skipThen = Code.pc - 2;
		
		while(!fixOr.isEmpty()) {
			int fixme = fixOr.pop();
			Code.fixup(fixme);
		}
	}
	
	public void visit(Else else_) {
		Code.putJump(0); // skip else
		skipElse = Code.pc - 2;
		Code.fixup(skipThen);
	}
	
	public void visit(UnmatchedIf unmatchedIf) {
		while(!fixOr.isEmpty()) {
			int fixme = fixOr.pop();
			Code.fixup(fixme);
		}
	}
	
	public void visit(UnmatchedIfElse unmatchedIfElse) {
		Code.fixup(skipElse);
	}
	
	public void visit(IfCond ifCond) {
		Code.fixup(skipElse);
	}
	
	
	
	
	
	

}
