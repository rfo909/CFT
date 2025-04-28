
package rf.xlang.parsetree;

import java.util.ArrayList;
import java.util.List;

import rf.configtool.lexer.TokenStream;
import rf.configtool.main.Ctx;

public class CodeType extends LexicalElement {

    private String typeName;
    private List<ParameterDef> fields;

    public CodeType (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("type","expected 'type'");
        typeName=ts.matchIdentifier("expected identifier following 'type'");
        ts.matchStr("(", "expected '('");
        boolean comma=false;
        while (!ts.matchStr(")")) {
            fields.add(new ParameterDef(ts));
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
