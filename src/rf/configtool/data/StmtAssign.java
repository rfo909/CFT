package rf.configtool.data;

import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.ObjGlobal;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.parser.TokenStream;

public class StmtAssign extends Stmt {

    private String varName;
    public StmtAssign (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("=","expected '='");
        varName=ts.matchIdentifier("expected variable name");
    }

    public void execute (Ctx ctx) throws Exception {
        Value v=ctx.pop();
        ctx.getFunctionState().set(varName, v);
    }

}
