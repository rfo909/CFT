package rf.xlang.parsetree;

import java.util.ArrayList;
import java.util.List;

import rf.configtool.lexer.TokenStream;
import rf.configtool.main.Ctx;

public class CodeFunction extends LexicalElement {

    private String functionName;
    private List<String> parameters = new ArrayList<>();
    private List<Stmt> statements = new ArrayList<>();

    public CodeFunction (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("def","expected 'def'");

        functionName=ts.matchIdentifier("expected identifier");

        ts.matchStr("(", "expected '('");
        while (!ts.peekStr(")")) {
            parameters.add(ts.matchIdentifier("expected parameter name"));
            if (!ts.matchStr(",")) break;
        }
        ts.matchStr(")", "expected closing ')' for parameter list");

        ts.matchStr("{", "expected '{' starting function body");
        while (!ts.peekStr("}")) {
            statements.add(Stmt.parse(ts));
        }
        ts.matchStr("}", "expected '}' closing function body");
    }
    
    
    public void execute (Ctx ctx) throws Exception {
        /*for (Stmt stmt:statements) {
            ctx.debug(stmt);
            
            stmt.execute(ctx);
        }
        */
    }


}
