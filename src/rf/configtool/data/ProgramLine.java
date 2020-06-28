package rf.configtool.data;

import rf.configtool.main.Ctx;
import rf.configtool.main.ObjGlobal;
import rf.configtool.main.Runtime;
import rf.configtool.main.runtime.Obj;
import rf.configtool.parser.Token;
import rf.configtool.parser.TokenStream;
import java.util.*;

public class ProgramLine extends LexicalElement {
    
    private List<Stmt> statements=new ArrayList<Stmt>();

    public ProgramLine (TokenStream ts) throws Exception {
        super(ts);
        while (!ts.atEOF() && !ts.peekStr(Runtime.PROGRAM_LINE_SEPARATOR)) {
            statements.add(Stmt.parse(ts));
        }
    }
    
    
    public void execute (Ctx ctx) throws Exception {
        for (Stmt stmt:statements) {
            stmt.execute(ctx);
        }
    }


}
