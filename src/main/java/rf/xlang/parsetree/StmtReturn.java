package rf.xlang.parsetree;

import rf.configtool.lexer.TokenStream;
import rf.configtool.main.Ctx;


/**
 *
 * @author roar
 */
public class StmtReturn extends Stmt {
    
    private Expr value;
    
    public StmtReturn (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("return", "expected 'return' keyword");
        value=new Expr(ts);
    }
    
    public void execute (Ctx ctx) throws Exception {
    }
}
