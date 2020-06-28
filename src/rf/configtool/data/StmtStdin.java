package rf.configtool.data;

import java.util.*;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Value;
import rf.configtool.parser.TokenStream;

public class StmtStdin extends Stmt {

    private List<Expr> exprList;
    
    public StmtStdin (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("stdin","expected 'stdin'");
        
        ts.matchStr("(", "expected '(' following stdin");
        if (!ts.matchStr(")")) {
            exprList=new ArrayList<Expr>();
            
            exprList.add(new Expr(ts));
            while (ts.matchStr(",")) {
                exprList.add(new Expr(ts));
            }
            ts.matchStr(")", "expected ')' closing stdin(...)");
        }
    }

    public void execute (Ctx ctx) throws Exception {
        if (exprList != null) {
            for (Expr expr:exprList) {
                String s=expr.resolve(ctx).getValAsString();
                ctx.getStdio().addBufferedInputLine(s);
            }
        } else {
            ctx.outln("Clearing buffered input lines");
            ctx.getStdio().clearBufferedInputLines();
        }
    }

}
