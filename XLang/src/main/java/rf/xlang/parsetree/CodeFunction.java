package rf.xlang.parsetree;

import java.util.ArrayList;
import java.util.List;

import rf.xlang.lexer.TokenStream;
import rf.xlang.main.Ctx;
import rf.xlang.main.ScriptFunctionState;
import rf.xlang.main.runtime.Value;
import rf.xlang.main.runtime.ValueNull;

public class CodeFunction extends LexicalElement {

    private String functionName;
    private List<String> parameters = new ArrayList<>();
    private StmtBlock body;
    public CodeFunction (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("def","expected 'def'");

        functionName=ts.matchIdentifier("expected identifier");

        ts.matchStr("(", "expected '(' starting parameter list");

        while (!ts.peekStr(")")) {
            parameters.add(ts.matchIdentifier("expected parameter name"));
            if (!ts.matchStr(",")) break;
        }
        ts.matchStr(")", "expected ')' closing parameter list");

        body=new StmtBlock(ts);
    }

    public String getFunctionName() {
        return functionName;
    }

    public List<String> getParameters() { return parameters; }

    /**
     * Execute function. Returns ValueNull if no return statement
     */
    public Value execute (Ctx parentCtx, List<Value> parameters) throws Exception {
        ScriptFunctionState fState = new ScriptFunctionState(this, parameters);
        Ctx ctx=parentCtx.sub(fState);
        body.execute(ctx);

        Value returnValue=ctx.getFunctionReturnValue();
        if (returnValue != null) {
            //System.out.println("CodeFunction.execute returnValue=" + returnValue.getValAsString());
            return returnValue;
        }
        return new ValueNull();
    }


}
