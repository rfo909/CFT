package rf.configtool.data;

import java.util.*;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Value;
import rf.configtool.parser.TokenStream;

public class StmtReport extends Stmt {

    private List<Expr> values=new ArrayList<Expr>();
    
    public StmtReport (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("report","expected 'report'");
        ts.matchStr("(", "expected '(' following report");
        boolean comma=false;
        while (!ts.matchStr(")")) {
            if (comma) ts.matchStr(",", "expected comma separating values, or ')' closing arglist");
            values.add(new Expr(ts));
            comma=true;
        }
    }

    public void execute (Ctx ctx) throws Exception {
        List<Value> result=new ArrayList<Value>();
        for (Expr expr:values) {
            result.add(expr.resolve(ctx));
        }
        ctx.getOutText().addReportData(result);
    }

}
