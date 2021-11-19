package rs.ac.bg.etf.pp1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
//import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import java_cup.runtime.Symbol;
import rs.ac.bg.etf.pp1.CompilerError;
import rs.ac.bg.etf.pp1.Compiler;
import rs.ac.bg.etf.pp1.CompilerError.CompilerErrorType;
import rs.ac.bg.etf.pp1.ast.Program;
import rs.ac.bg.etf.pp1.ast.SyntaxNode;
import rs.ac.bg.etf.pp1.util.Log4JUtils;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;


public class MJParserTest implements Compiler{
	
	static {
		DOMConfigurator.configure(Log4JUtils.instance().findLoggerConfigFile());
		Log4JUtils.instance().prepareLogFile(Logger.getRootLogger());
	}
	
	public static void main(String[] args) throws Exception {
		Logger log = Logger.getLogger(MJParserTest.class);
		MJParserTest testing = new MJParserTest();
		if (args.length < 2) {
			log.error("Not enough arguments supplied! Usage: MJParser <source-file> <obj-file> ");
			return;
		}
		
		File sourceCode = new File(args[0]);
		if (!sourceCode.exists()) {
			log.error("Source file [" + sourceCode.getAbsolutePath() + "] not found!");
			return;
		}
			
		log.info("Compiling source file: " + sourceCode.getAbsolutePath());
		
		List<CompilerError> listOfErrors = new LinkedList<CompilerError>();
		listOfErrors = testing.compile(args[0], args[1]);
		
		if(listOfErrors == null) {
			System.out.println("Kompajliranje je uspesno izvrseno");
		}
		else{
			System.out.println("Velicina liste je : " + listOfErrors.size());
			for(CompilerError error: listOfErrors) {
				CompilerErrorType recentType = error.getType();
				if(recentType == CompilerErrorType.LEXICAL_ERROR) {
					System.out.println("Leksicka greska " + error.getMessage() + " se nalazi na liniji " + error.getLine() + " ! ");
				}
				else if(recentType == CompilerErrorType.SYNTAX_ERROR) {
					System.out.println("Sintaksna greska " + error.getMessage() + " se nalazi na liniji " + error.getLine() + " ! ");
				}
				else if(recentType == CompilerErrorType.SEMANTIC_ERROR) {
					System.out.println("Semanticka greska " + error.getMessage() + " se nalazi na liniji " + error.getLine() + " ! ");
				}
			}
			System.out.println("Komapajliranje je izvrseno sa greskama");
		}

	}

	@Override
	public List<CompilerError> compile(String sourceFilePath, String outputFilePath) {
		Logger log = Logger.getLogger(MJParserTest.class);
		List<CompilerError> list = new LinkedList<CompilerError>();
		
		Reader br = null;
		try {
			try {
				File sourceCode = new File(sourceFilePath);
				log.info("Compiling source file: " + sourceCode.getAbsolutePath());
				
				br = new BufferedReader(new FileReader(sourceCode));
				Yylex lexer = new Yylex(br);
				MJParser p = new MJParser(lexer);
		        Symbol s = p.parse();  //pocetak parsiranja

				list.addAll(lexer.list);
				list.addAll(p.list);

		        Program prog = (Program)(s.value); 
				// ispis sintaksnog stabla
				log.info(prog.toString(""));
				log.info("===================================");
	
				// pravljenje "universe" opsega u tabeli simbola
				Tab.init();
				
				// semanticka analiza
				SemanticAnalyzer v = new SemanticAnalyzer();
				prog.traverseBottomUp(v); 
				list.addAll(v.list);
		      	
				// ispis tabele simbola
				Tab.dump();
				
				if(!p.errorDetected && v.passed()) {
					
					log.info("Parsiranje uspesno zavrseno");
					
					CodeGenerator codeGenerator = new CodeGenerator();
					prog.traverseBottomUp(codeGenerator);
					Code.dataSize = v.nVars;
		        	Code.mainPc = codeGenerator.getMainPc();
					File objFile = new File(outputFilePath);
					if(objFile.exists())
						objFile.delete();
					Code.write(new FileOutputStream(objFile));
					
					
		        	
				}
				else {
					log.error("Parsiranje NIJE uspesno zavrseno");
				}
			} 
			finally {
				if (br != null) try { br.close(); } catch (IOException e1) { log.error(e1.getMessage(), e1); }
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return list.size() == 0 ? null : list;
	}

	
}
