package rf.configtool.data;

import rf.configtool.main.Ctx;
import rf.configtool.main.Runtime;
import rf.configtool.main.runtime.*;
import rf.configtool.parser.TokenStream;
import java.util.*;

public class StmtLoop extends Stmt {
    
    private List<Stmt> body=new ArrayList<Stmt>();
    
    public StmtLoop (TokenStream ts) throws Exception {
        super(ts);
        
        ts.matchStr("loop","expected keyword 'loop'");
        
        while (!ts.atEOF() && !ts.peekStr(Runtime.PROGRAM_LINE_SEPARATOR) && !ts.peekStr("}")) {
            body.add(Stmt.parse(ts));
        }
        
    }
    
    public void execute (Ctx ctx) throws Exception {
        ctx.getOutData().setProgramContainsLooping();
        
        OUTER: for (;;) {
            Ctx sub=ctx.sub();
            for (Stmt stmt:body) {
                stmt.execute(sub);
                if (sub.hasBreakLoopFlag()) {
                    break OUTER;
                }
                if (sub.hasAbortIterationFlag()) {
                    continue OUTER;
                }
             }

        }
    }

    

}
