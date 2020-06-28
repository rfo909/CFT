package rf.configtool.data;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.parser.TokenStream;

public class StmtHelp extends Stmt {
    
    private Expr expr;
    
    public StmtHelp (TokenStream ts) throws Exception {
        super(ts);
        
        ts.matchStr("help", "expected 'help' keyword");
        if (ts.matchStr("(")) {
            expr=new Expr(ts);
            ts.matchStr(")", "expected ')' closing help()");
        }
    }
    
    public void execute (Ctx ctx) throws Exception {
        Obj v;
        if (expr != null) {
            v=expr.resolve(ctx);
            if (v instanceof ValueObj) v=((ValueObj)v).getVal();
        } else {
            v=ctx.pop();
            if (v==null) {
                v=ctx.getObjGlobal();
            } else if (v instanceof ValueObj) {
                v=((ValueObj)v).getVal();
            }
        }
        
        v.generateHelp(ctx);
    }


}
