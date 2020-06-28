package rf.configtool.data;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.parser.TokenStream;

public class StmtExpr extends Stmt {
    
    private Expr expr;
    
    public StmtExpr (TokenStream ts) throws Exception {
        super(ts);
        expr=new Expr(ts);
    }
    
    public void execute (Ctx ctx) throws Exception {
        ctx.push(expr.resolve(ctx));
    }

}
