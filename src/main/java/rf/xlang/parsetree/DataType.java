package rf.xlang.parsetree;

import java.util.ArrayList;
import java.util.List;

import rf.configtool.lexer.TokenStream;
import rf.configtool.main.Ctx;

public class DataType extends LexicalElement {
    
    private String type;
    private String genericParameter;
    
    public DataType (TokenStream ts) throws Exception {
        super(ts);
        type=ts.matchIdentifier("Expected type name");
        if (ts.matchStr("<")) {
            genericParameter=ts.matchIdentifier("expected generic type parameter");
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
