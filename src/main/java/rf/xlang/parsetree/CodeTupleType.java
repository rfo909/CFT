package rf.xlang.parsetree;

import java.util.ArrayList;
import java.util.List;

import rf.configtool.lexer.TokenStream;
import rf.configtool.main.Ctx;

/**
 * A Tuple is a List with named content ... or a Dict
 *
 * To create, call like a function
 * type FileLine (file, line, lineNumber)
 *
 * x = FileLine(someFile, "text", 23)
 * x.someFile = y
 *
 */
public class CodeTupleType extends LexicalElement {
    
    private String type;
    private List<String> content=new ArrayList<>();
    
    public CodeTupleType (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("type", "expected 'type' keyword");
        type=ts.matchIdentifier("Expected type name");
        ts.matchStr("(", "expected '(' defining tuple data");

        while (!ts.peekStr(")")) {
            content.add(ts.matchIdentifier("expected field identifier"));
            if (!ts.matchStr(",")) break;
        }
        ts.matchStr(")", "expected ')' or comma");
    }
    
    
    public void execute (Ctx ctx) throws Exception {
        /*for (Stmt stmt:statements) {
            ctx.debug(stmt);
            
            stmt.execute(ctx);
        }
        */
    }


}
