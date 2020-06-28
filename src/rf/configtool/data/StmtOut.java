package rf.configtool.data;

import java.util.*;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Value;
import rf.configtool.parser.TokenStream;

public class StmtOut extends Stmt {

    private Expr expr;
    
    public StmtOut (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("out","expected 'out'");
        ts.matchStr("(", "expected '(' following out");
        expr=new Expr(ts);
        ts.matchStr(")", "expected ')' closing out(...)");
    }

    public void execute (Ctx ctx) throws Exception {
        ctx.getOutData().out(expr.resolve(ctx));
    }

}
