package rf.configtool.root.shell;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import rf.configtool.lexer.Lexer;
import rf.configtool.lexer.SourceLocation;
import rf.configtool.lexer.TokenStream;
import rf.configtool.main.Ctx;
import rf.configtool.main.ScriptSourceLine;
import rf.configtool.main.runtime.Value;
import rf.configtool.parsetree.Expr;

/**
 * Superclass of all Shell command implementations, such as ls, cd, pwd
 */
public abstract class ShellCommand {

	private final List<String> parts;
	private final String name;
	private final List<ShellCommandArg> args;
	
	public ShellCommand (List<String> parts) throws Exception {
		this.parts=parts;
		this.name=parts.get(0);
		this.args=new ArrayList<ShellCommandArg>();
		
		for (int i=1; i<parts.size(); i++) {
			String part=parts.get(i);
			String isExpr=null;
			
			if (part.startsWith("(")) {
				isExpr=part.substring(1,part.length()-1); // (xxx) -> xxx
			} else if (part.startsWith("%")) {
				isExpr=part;
			} 
			if (isExpr != null) {
				// create Expr
				Lexer lex=new Lexer();
				SourceLocation loc=new SourceLocation("<ShellCommand>",1);
				lex.processLine(new ScriptSourceLine(loc, isExpr));
				TokenStream ts=lex.getTokenStream();
				Expr expr=new Expr(ts);
				args.add(new ShellCommandArg(expr));
				continue;
			} else {
				args.add(new ShellCommandArg(part));
			}
			
		}
	}

	protected String getName() {
		return name;
	}
	
	protected List<ShellCommandArg> getArgs() {
		return args;
	}
	
	
	public abstract Value execute (Ctx ctx) throws Exception ;
	
	
    protected void sort (List<String> data) {
        Comparator<String> c=new Comparator<String>() {
            public int compare(String a, String b) {
                return a.compareTo(b);
            }
        };
        data.sort(c);

    }
    
}
