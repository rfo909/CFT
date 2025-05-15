package rf.xlang.parsetree;

import rf.xlang.lexer.TokenStream;
import rf.xlang.main.Ctx;


/**
 *
 * @author roar
 */
public class StmtReturn extends Stmt {
    
    private Expr valueExpr;
    
    public StmtReturn (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("return", "expected 'return' keyword");
        valueExpr =new Expr(ts);
    }
    
    public void execute (Ctx ctx) throws Exception {
        ctx.setFunctionReturnValue(valueExpr.resolve(ctx));
    }
}
