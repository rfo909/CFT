package rf.xlang.parsetree;

import java.util.ArrayList;
import java.util.List;

import rf.configtool.lexer.TokenStream;
import rf.configtool.main.Ctx;

public class CodeFunction extends LexicalElement {

    private String functionName;
    private List<ParameterDef> parameters;
    private List<Stmt> statements;

    public CodeFunction (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("def","expected 'def'");

        functionName=ts.matchIdentifier("expected identifier");

        ts.matchStr("(", "expected '('");
        boolean comma=false;
        while (!ts.matchStr(")")) {
            parameters.add(new ParameterDef(ts));
            if (comma) ts.matchStr(",", "expected comma or ')'");
        }
    }
    
    
    public void execute (Ctx ctx) throws Exception {
        /*for (Stmt stmt:statements) {
            ctx.debug(stmt);
            
            stmt.execute(ctx);
        }
        */
    }


}
