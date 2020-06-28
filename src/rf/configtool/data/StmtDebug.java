package rf.configtool.data;

import java.util.*;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Value;
import rf.configtool.parser.TokenStream;

public class StmtDebug extends Stmt {

    private Expr value;
    
    public StmtDebug (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("debug","expected 'debug'");
        ts.matchStr("(", "expected '(' following debug");
        value=new Expr(ts);
        ts.matchStr(")", "expected ')' closing debug stmt");
    }

    public void execute (Ctx ctx) throws Exception {
        ctx.outln("%DEBUG% " + value.resolve(ctx).getValAsString());
    }

}
