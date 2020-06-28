package rf.configtool.data;

import rf.configtool.main.Ctx;
import rf.configtool.main.ObjGlobal;
import rf.configtool.main.Runtime;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueMacro;
import rf.configtool.parser.Token;
import rf.configtool.parser.TokenStream;
import java.util.*;

public class ExprMacro extends LexicalElement {

	private boolean inheritContext;
	
    private List<Stmt> statements=new ArrayList<Stmt>();
    
    // See Runtime.processCodeLines() method to extend to loops and supporting PROGRAM_LINE_SEPARATOR - must in addition 
    // add '}' as terminator character inside ProgramLine

    public ExprMacro (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("{", "expected '{'");
        
        inheritContext=false;
        
        if (ts.matchStr("<-")) {
        	inheritContext=true;
        }
        
        while (!ts.matchStr("}")) {
            statements.add(Stmt.parse(ts));
        }
        
    }
    
    
    public Value resolve (Ctx ctx) throws Exception {
    	return new ValueMacro(statements, inheritContext);
    }


}
