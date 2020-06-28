package rf.configtool.data;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.parser.TokenStream;

public class StmtBreak extends Stmt {
    
    private Expr expr;

    public StmtBreak (TokenStream ts) throws Exception {
        super(ts);
        
        ts.matchStr("break","expected 'break'");
        ts.matchStr("(", "expected '(' following break");
        expr=new Expr(ts);
        ts.matchStr(")", "expected ')' closing break statement");
    }
    
    public void execute (Ctx ctx) throws Exception {
        Value v=expr.resolve(ctx);
        boolean breakLoop=v.getValAsBoolean();
        
        if (breakLoop) {
            ctx.setBreakLoopFlag();
        }
    }


}
