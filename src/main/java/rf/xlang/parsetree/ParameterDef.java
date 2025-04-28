package rf.xlang.parsetree;

import java.util.ArrayList;
import java.util.List;

import rf.configtool.lexer.TokenStream;
import rf.configtool.main.Ctx;

public class ParameterDef extends LexicalElement {
    
    private CodeTupleType type;
    private String name;
    
    public ParameterDef (TokenStream ts) throws Exception {
        super(ts);
        type=new CodeTupleType(ts);
        name=ts.matchIdentifier("Expected identifier");
    }
    
    
    public void execute (Ctx ctx) throws Exception {
        /*for (Stmt stmt:statements) {
            ctx.debug(stmt);
            
            stmt.execute(ctx);
        }
        */
    }


}
