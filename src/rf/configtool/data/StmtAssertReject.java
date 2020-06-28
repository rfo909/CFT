package rf.configtool.data;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.parser.TokenStream;

public class StmtAssertReject extends Stmt{
    
    private boolean isAssert;
    private Expr expr;

    public StmtAssertReject (TokenStream ts) throws Exception {
        super(ts);
        
        if (ts.matchStr("assert")) {
            isAssert=true;
        } else {
            ts.matchStr("reject","expected 'assert' or 'reject'");
            isAssert=false;
        }
        ts.matchStr("(", "expected '(' following assert/reject");
        expr=new Expr(ts);
        ts.matchStr(")", "expected ')' closing assert/reject statement");
    }
    
    public void execute (Ctx ctx) throws Exception {
        Value v=expr.resolve(ctx);
        boolean x=v.getValAsBoolean();
        
        
        
        boolean abortProcessing=(isAssert && !x) || (!isAssert && x);
        if (abortProcessing) {
            ctx.setAbortIterationFlag();
        }
    }


}
