package rf.configtool.data;

import java.util.*;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.parser.Token;
import rf.configtool.parser.TokenStream;

public class StmtReportList extends Stmt {

    private Expr listValue;
    
    public StmtReportList (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("reportList","expected 'reportList'");
        ts.matchStr("(", "expected '(' following reportList");
        
        listValue=new Expr(ts);
        ts.matchStr(")", "expected ')' closing reportList() statement");
    }

    public void execute (Ctx ctx) throws Exception {
        
        Value v=listValue.resolve(ctx);
        if (!(v instanceof ValueList)) throw new Exception("Expected parameter for reportList() to be list");

        List<Value> result=((ValueList) v).getVal();

        ctx.getOutText().addReportData(result);
    }

}
