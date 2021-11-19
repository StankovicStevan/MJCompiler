package rs.ac.bg.etf.pp1;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.CompilerError.CompilerErrorType;
import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class SemanticAnalyzer extends VisitorAdaptor {
	
	Obj currentProgram;
	
	boolean errorDetected = false;
	boolean mainDefined = false;
	
	boolean returnFound = false;
	
	int setConstAdr = 0;
	
	int nVars;
	
	Logger log = Logger.getLogger(getClass());
	
	List<Struct> tempActArgs = new LinkedList<Struct>();
	
	List<CompilerError> list = new LinkedList<CompilerError>();

	private Struct currentType;

	private Obj currentMethod;

	public void report_error(String message, SyntaxNode info) {
		errorDetected = true;
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0: info.getLine();
		if (line != 0)
			msg.append (" na liniji ").append(line);
		log.error(msg.toString());
		list.add(new CompilerError(line, message, CompilerErrorType.SEMANTIC_ERROR));
	}

	public void report_info(String message, SyntaxNode info) {
		StringBuilder msg = new StringBuilder(message); 
		int line = (info == null) ? 0: info.getLine();
		if (line != 0)
			msg.append (" na liniji ").append(line);
		log.info(msg.toString());
	}
	
	public boolean passed() {
		return !errorDetected;
	}

	@Override
	public void visit(ProgName progName) {
		Tab.insert(Obj.Type, "bool", new Struct(Struct.Bool));
		String name = progName.getPName();
		currentProgram = Tab.insert(Obj.Prog, name, Tab.noType);//Pamtimo trenutni objektni cvor za trenutni program
		Tab.openScope();//Otvaranje opsega
	}
	
	

	@Override
	public void visit(Program Program) {//Kad se posete svi cvorovi do kraja, tek onda se posecuje program, pa se tu zatvara opseg
		nVars = Tab.currentScope.getnVars();
		Tab.chainLocalSymbols(currentProgram);//Ulancavanje, da polje locals pokazuje na tu listu
		Tab.closeScope();//Zatvaranje opsega
	}
	
	

	@Override
	public void visit(BoolConst boolConst) {
		boolConst.struct = Tab.find("bool").getType();
		setConstAdr = boolConst.getB1() ? 1 : 0;
	}

	@Override
	public void visit(CharConst charConst) {
		charConst.struct = Tab.charType;
		setConstAdr = charConst.getC1();
	}

	@Override
	public void visit(NumConst numConst) {
		numConst.struct = Tab.intType;
		setConstAdr = numConst.getN1();
	}

	//Sam
	@Override
	public void visit(ConstDeclList constDeclList) {
		if(!(constDeclList.getConstList().struct == currentType)) {
			report_error("Tip konstante se ne poklapa sa tipom vrednosti koja je dodeljena", constDeclList);
		}
		else {
			if(Tab.find(constDeclList.getI2()) != Tab.noObj) {
				report_error("Konstanta je vec deklarisana", constDeclList);
			}
			else {
				Obj cnst = Tab.insert(Obj.Con, constDeclList.getI2(), currentType);
				cnst.setAdr(setConstAdr);
				report_info("Konstanta je deklarisana " + constDeclList.getI2(), constDeclList);
			}
		}
	}
	
	

	@Override
	public void visit(DesignatorSingle designatorSingle) {//Proveravamo da li je ime vec postoji, tj da li je deklarisano, jer Designator u sebi ima uvek ident, tj ime
		String name = designatorSingle.getName();
		Obj found = Tab.find(name);//Ako ne nadje, vraca noObj
		if (found == Tab.noObj) {
			report_error("Nije definisan simbol " + name, designatorSingle);
		}
		designatorSingle.obj = found;
	}
	


	//Vars
	
	@Override
	public void visit(VarDeclIdentStmt2 VarDeclIdentStmt2) {//Da bismo dohvatili ime promenljive za ubacivanje u tabelu simbola
		String name = VarDeclIdentStmt2.getI1();
		Struct type = currentType;//Cuvamo tip preko Type-a koji smo prvo posetili
		if (Tab.currentScope.findSymbol(name) != null) {
			report_error("Vec definisan simbol " + name, VarDeclIdentStmt2);
		}
		Tab.insert(Obj.Var, name, new Struct(Struct.Array, type));//Promenljiva-niz elemenata
	}

	@Override
	public void visit(VarDeclIdentStmt varDeclIdentStmt) {
		String name = varDeclIdentStmt.getI1();
		Struct type = currentType;
		if (Tab.currentScope.findSymbol(name) != null) {
			report_error("Vec definisan simbol " + name, varDeclIdentStmt);
		}
		Tab.insert(Obj.Var, name, type);//Jedna promenljiva
	}

	@Override
	public void visit(Type type) {
		currentType = Tab.noType;
		String name = type.getTypeName();
		Obj found = Tab.find(name);
		if (found == Tab.noObj) {
			report_error("Nije definisan tip " + name, type);
		} else {
			if (found.getKind() != Obj.Type) {
				report_error(name + " ne predstavlja tip podatka", type);
			} else {
				currentType = found.getType(); //Dohvatamo tip
			}
		}
		type.struct = currentType;
	}

	//Dodati return kod metoda
	@Override
	public void visit(MethodName methodName) {
		String name = methodName.getMethName();
		if (Tab.find(name) != Tab.noObj) {
			report_error("Vec definisan metod " + name, methodName);
		}
		currentMethod = Tab.insert(Obj.Meth, name, currentType);
		Tab.openScope();
		
		if (name.equals("main")) {//Da li je main definisan?
			mainDefined = true;
			if (currentType != Tab.noType) {
				report_error("Metod main mora biti tipa VOID", methodName);
			}
		}
	}
	
	@Override
	public void visit(MethodDecl MethodDecl) {
		if(!returnFound && currentMethod.getType() != Tab.noType) {
			report_error("Ne postoji return iskaz za datu metodu", MethodDecl);
		}
		Tab.chainLocalSymbols(currentMethod);//Ulancavanje za trenutni metod i potom zatvarenje njenog opsega
		Tab.closeScope();
		
		returnFound = false;
		currentMethod = null;
	}

	@Override
	public void visit(VoidMethodTypeName VoidMethodTypeName) {
		currentType = Tab.noType;
	}
	
	
	
	@Override
	public void visit(NoRetun NoRetun) {
		returnFound = true;
		Struct currMethType = currentMethod.getType();
		if(currMethType != Tab.noType) {
			report_error("Void metod mora biti bez povratne vrednosti", NoRetun);
		}
	}
	
	@Override
	public void visit(Return Return) {
		returnFound = true;
		Struct currMethType = currentMethod.getType();
		if(currentMethod.getType() == Tab.noType) {
			report_error("Return ne sme biti izvan tela metoda, odnosno globalnih funkcija", Return);
		}
		if(!currMethType.compatibleWith(Return.getExpr().struct)) {
			report_error("Tip izraza u return metodi se ne slaze sa tipom povratne vrednosti date funkcije", Return);
		}
	}

	@Override
	public void visit(FormalParamDeclStmt2 formalParamDeclStmt2) {
		String name = formalParamDeclStmt2.getI2();
		Struct type = currentType;
		if (Tab.currentScope.findSymbol(name) != null) {
			report_error("Vec definisan simbol " + name, formalParamDeclStmt2);
		}
		Tab.insert(Obj.Var, name, new Struct(Struct.Array, type));
		
		if (currentMethod.getName().equals("main")) {
			report_error("main metod ne sme imati parametere", formalParamDeclStmt2);
		}
	}

	@Override
	public void visit(FormalParamDeclStmt formalParamDeclStmt) {
		String name = formalParamDeclStmt.getI2();
		Struct type = currentType;
		if (Tab.currentScope.findSymbol(name) != null) {
			report_error("Vec definisan simbol " + name, formalParamDeclStmt);
		}
		Tab.insert(Obj.Var, name, type);
		
		if (currentMethod.getName().equals("main")) {//Ako je slucajno trenutan metod main, on ne sme imati parametre
			report_error("main metod ne sme imati parametere", formalParamDeclStmt);
		}
	}

	//Sam sam pisao
	@Override
	public void visit(AssignopExpr assignopExpr) {
		Obj designator = assignopExpr.getDesignator().obj;
		if (designator.getKind() != Obj.Var && designator.getKind() != Obj.Elem) {
			report_error("Nije l-vrednost", assignopExpr); //Da li je leva vrednost promenljiva ili element niza, ne sme biti konstanta il itako nesto
		}
		
		Struct expr = assignopExpr.getExpr().struct;//Na dnu svakog Expr je factor
		
		if (!expr.compatibleWith(designator.getType())) {
			report_error("Nisu kompatibilni tipovi operanada", assignopExpr);
		}

	}
	
	
	
	
	@Override
	public void visit(ActPars actPars) {
		if (actPars.getDesignator().obj.getKind() != Obj.Meth) {
			report_error(actPars.getDesignator().obj.getName() + " nije funkcija", actPars);
		}
	}

	//Sam
	@Override
	public void visit(Dec dec) {
		Obj designator = dec.getDesignator().obj;
		if(designator.getKind() != Obj.Var && designator.getKind() != Obj.Elem) {
			report_error("Mora oznacavati promenljivu ili element niza", dec);
		}
		
		if(designator.getType() != Tab.intType) {
			report_error("Mora biti tipa int pri dekrementiranju", dec);
		}
	}
	//Sam
	@Override
	public void visit(Inc inc) {
		Obj designator = inc.getDesignator().obj;
		if(designator.getKind() != Obj.Var && designator.getKind() != Obj.Elem) {
			report_error("Mora oznacavati promenljivu ili element niza", inc);
		}
		
		if(designator.getType() != Tab.intType) {
			report_error("Mora biti tipa int pri inkrementiranju", inc);
		}
	}

	// Factor
	public void visit(DesignatorAct factor) {
		factor.struct = factor.getDesignator().obj.getType();//.obj nacin kako povezujemo tabelu simbola sa objektima
		if (factor.getDesignator().obj.getKind() != Obj.Meth) {
			report_error(factor.getDesignator().obj.getName() + " nije funkcija", factor);
		}
	}
	public void visit(NoDesignatorAct factor) {
		factor.struct = factor.getDesignator().obj.getType();
	}
	public void visit(NumConstF factor) {
		factor.struct = Tab.intType;
	}
	public void visit(CharConstF factor) {
		factor.struct = Tab.charType;
	}
	public void visit(BoolConstF factor) {
		factor.struct = Tab.find("bool").getType();
	}
	public void visit(NewType factor) {
		factor.struct = new Struct(Struct.Array, currentType);//Pravi se strukturni cvor da bi se napravio niz trenutnog tipa
		if(factor.getExpr().struct.getKind() != Struct.Int) {
			report_error("Mora biti tipa int", factor);
		}
	}
	public void visit(ExprFactor factor) {
		factor.struct = factor.getExpr().struct;
	}

	// Term
	@Override
	public void visit(TermStmtOnly termStmtOnly) {
		termStmtOnly.struct = termStmtOnly.getFactor().struct;
	}

	@Override
	public void visit(TermStmt termStmt) {
		Struct left = termStmt.getTerm().struct;
		Struct right = termStmt.getFactor().struct;
		termStmt.struct = Tab.intType;
		if (left != Tab.intType) {
			termStmt.struct = Tab.noType;
			report_error("Levi operand nije int", termStmt);
		}
		if (right != Tab.intType) {
			termStmt.struct = Tab.noType;
			report_error("Desni operand nije int", termStmt);
		}
	}
	
	//AddopTermList
	@Override
	public void visit(AddopTermListTerm addopTermListTerm) {
		addopTermListTerm.struct = addopTermListTerm.getTerm().struct;
	}

	@Override
	public void visit(AddopTermListStmt addopTermListStmt) {
		Struct left = addopTermListStmt.getAddopTermList().struct;
		Struct right = addopTermListStmt.getTerm().struct;
		addopTermListStmt.struct = Tab.intType;
		if (left != Tab.intType) {
			addopTermListStmt.struct = Tab.noType;
			report_error("Levi operand nije tipa int", addopTermListStmt);
		}
		if (right != Tab.intType) {
			addopTermListStmt.struct = Tab.noType;
			report_error("Desni operand nije tipa int", addopTermListStmt);
		}
		if(!left.compatibleWith(right)) {
			report_error("Nisu kompatibilni operandi", addopTermListStmt);
		}
	}
	
	// expr
	public void visit(ExprMinusStmt exprMinusStmt) {//Treva nesto za minus da se proveri
		exprMinusStmt.struct = exprMinusStmt.getAddopTermList().struct;//Jer addopTermList je istog tipa kao ceo izraz
		Struct expr = exprMinusStmt.getAddopTermList().struct;
		if(!(expr == Tab.intType)) {
			report_error("Mora biti tipa int", exprMinusStmt);
		}
	}
	
	public void visit(TermExpStmt termExpStmt) {
		termExpStmt.struct = termExpStmt.getAddopTermList().struct;
	}
	
	
	//Sam
	//Read
			
			
	@Override
	public void visit(Read read) {
		Obj designator = read.getDesignator().obj;
		if(designator.getKind() != Obj.Var && designator.getKind() != Obj.Elem) {
			report_error("Mora biti promenljiva ili element niza", read);
		}
		
		if(designator.getType() != Tab.intType && designator.getType() != Tab.charType && designator.getType() != Tab.find("bool").getType()) {
			report_error("Mora biti tipa ili int ili char ili bool", read);
		}
		
	}
		
		
	@Override
	public void visit(Print print) {
		Struct expr = print.getExpr().struct;
		if(expr.getKind() != Struct.Int && expr.getKind() != Struct.Char && expr.getKind() != Struct.Bool) {
			report_error("Mora biti tipa ili int ili char ili bool", print);
		}
	}
	
	@Override
	public void visit(NoNumPrint print) {
		Struct expr = print.getExpr().struct;
		if(expr.getKind() != Struct.Int && expr.getKind() != Struct.Char && expr.getKind() != Struct.Bool) {
			report_error("Mora biti tipa ili int ili char ili bool", print);
		}
	}
	
	

	@Override
	public void visit(CondFactExprStmt condFactExprStmt) {
		Struct cond = condFactExprStmt.getExpr().struct;
		if(cond != Tab.find("bool").getType()) {
			report_error("Uslov mora biti tipa bool" , condFactExprStmt);
		}
	}
	
	

	@Override
	public void visit(DoWhile doWhile) {
		Struct cond = doWhile.getCondition().struct;
		if(cond != Tab.find("bool").getType()) {
			report_error("Uslov mora biti tipa bool", doWhile);
		}
	}
	
	

	@Override
	public void visit(Switch Switch) {
		Struct expr = Switch.getExpr().struct;
		if(expr != Tab.intType) {
			report_error("Expr mora biti celobrojnog tipa", Switch);
		}
	}
	
	

	@Override
	public void visit(ActualParam actualParam) {
		tempActArgs.add(actualParam.getExpr().struct);
	}

	@Override
	public void visit(ActualParams actualParams) {
		tempActArgs.add(actualParams.getExpr().struct);
	}

	@Override
	public void visit(CondFactExprRelExprStmt condFactExprRelExprStmt) {
		Struct left = condFactExprRelExprStmt.getExpr().struct;
		Struct right = condFactExprRelExprStmt.getExpr1().struct;
		if(!left.compatibleWith(right)) {
			report_error("Operandi nisu kompatibilni", condFactExprRelExprStmt);
		}
		
		if((left.getKind() == Struct.Array) && (right.getKind() == Struct.Array)) {
			int op = condFactExprRelExprStmt.getRelop().integerwrapper.getValue();
			if (op != Code.eq && op != Code.ne) {
				report_error("Pogresno poredjenje", condFactExprRelExprStmt);
			}

		}
	}
	
	

	@Override
	public void visit(DesignatorName designatorName) {
		String name = designatorName.getName();
		Obj found = Tab.find(name);
		if (found == Tab.noObj) {
			report_error("Nije definisan simbol " + name, designatorName);
		}
		designatorName.obj = found;
	}

	@Override
	public void visit(DesignatorMulti designatorMulti) {
		Obj designator = designatorMulti.getDesignatorName().obj;
		if(designator.getType().getKind() != Struct.Array) {
			report_error("Designator mora biti niz", designatorMulti);
		}
		//Treba proveri Expr da li je tipa int
		if(!(designatorMulti.getExpr().struct == Tab.intType)){
			report_error("Expr mora biti int", designatorMulti);
		}
		
		// niz		  [		   1+2	]
		// IDENT:name LBRACKET Expr RBRACKET
		designatorMulti.obj = new Obj(Obj.Elem, designator.getName() + "[$]", designator.getType().getElemType());
	
	}
	

public void visit(Equalsop op) {
	op.integerwrapper = new IntegerWrapper(Code.eq);
}
public void visit(Differop op) {
	op.integerwrapper = new IntegerWrapper(Code.ne);
}
public void visit(Grtop op) {
	op.integerwrapper = new IntegerWrapper(Code.gt);
}
public void visit(Grteqop op) {
	op.integerwrapper = new IntegerWrapper(Code.ge);
}
public void visit(Lessop op) {
	op.integerwrapper = new IntegerWrapper(Code.lt);
}
public void visit(Lesseqop op) {
	op.integerwrapper = new IntegerWrapper(Code.le);
}


public void visit(Multiplyop op) {
	op.integerwrapper = new IntegerWrapper(Code.mul);
}
public void visit(Divop op) {
	op.integerwrapper = new IntegerWrapper(Code.div);
}
public void visit(Modop op) {
	op.integerwrapper = new IntegerWrapper(Code.rem);
}
public void visit(AddopPlus op) {
	op.integerwrapper = new IntegerWrapper(Code.add);
}
public void visit(SubopMinus op) {
	op.integerwrapper = new IntegerWrapper(Code.sub);
}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
