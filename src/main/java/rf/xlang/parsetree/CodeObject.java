package rf.xlang.parsetree;

import java.util.ArrayList;
import java.util.List;

import rf.configtool.lexer.TokenStream;
import rf.configtool.main.Ctx;

public class CodeObject extends LexicalElement {

    private String objectName;
    
    public CodeObject (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("object","expected 'object'");
        objectName=ts.matchIdentifier("expected identifier following 'object'");
    }
    
    
    public void execute (Ctx ctx) throws Exception {
        /*for (Stmt stmt:statements) {
            ctx.debug(stmt);
            
            stmt.execute(ctx);
        }
        */
    }


}
