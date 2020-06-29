package rf.configtool.data;

import rf.configtool.main.Ctx;
import rf.configtool.main.Runtime;
import rf.configtool.main.runtime.*;
import rf.configtool.parser.TokenStream;
import java.util.*;

public class StmtIterate extends Stmt {
    
    private String loopVariable;
    private List<Stmt> body=new ArrayList<Stmt>();
    
    public StmtIterate (TokenStream ts) throws Exception {
        super(ts);
        
        ts.matchStr("->", "expected '->'");
        loopVariable=ts.matchIdentifier("expected loop variable name");
        
        while (!ts.atEOF() && !ts.peekStr(Runtime.PROGRAM_LINE_SEPARATOR) && !ts.peekStr("}")) {
            body.add(Stmt.parse(ts));
        }
        
    }
    
    public void execute (Ctx ctx) throws Exception {
        ctx.getOutData().setProgramContainsLooping();
        
        Value v=ctx.pop();
        if (v==null) return; // No value occurs if a program line executes no out() and leaves nothing on the stack
        
        List<Value> data;
        
        
        if (v instanceof ValueList) {
            // iterate over all values in list
            data=((ValueList) v).getVal();
        } else {
            if ((v instanceof ValueBoolean) && ((ValueBoolean) v).getVal()==false) {
                // no iteration 
                return;
            }
            // otherwise iterate over that one value only
            data=new ArrayList<Value>();
            data.add(v);
        }
        
    
        OUTER: for (Value currVal:data) {
            Ctx sub=ctx.sub();
            sub.setLoopVariable(loopVariable, currVal);
            for (Stmt stmt:body) {
                stmt.execute(sub);
                if (sub.hasBreakLoopFlag()) {
                    break OUTER;
                }
                if (sub.hasAbortIterationFlag()) {
                    // ctx.outln("Aborting '" + loopVariable + "' for value " + currVal.getContentDescription());
                    continue OUTER;
                }
            }

        }
    }

    

}
