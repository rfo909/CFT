package rf.xlang.parsetree;

import java.util.ArrayList;
import java.util.List;

import rf.xlang.lexer.TokenStream;
import rf.xlang.main.Ctx;

public class Code extends LexicalElement {
    
    private List<TupleType> types=new ArrayList<TupleType>();
    private List<CodeFunction> functions=new ArrayList<CodeFunction>();

    public Code(TokenStream ts) throws Exception {
        super(ts);
        while (!ts.atEOF()) {
            if (ts.peekStr("type")) {
                types.add(new TupleType(ts));
            } else if (ts.peekStr("def")) {
                functions.add(new CodeFunction(ts));
            } else {
                ts.error("Unknown code element, expected type or def");
            }
        }
    }

    public List<TupleType> getTupleTypes() {
        return types;
    }

    public List<CodeFunction> getCodeFunctions() {
        return functions;
    }
    
    public void execute (Ctx ctx) throws Exception {
        /*for (Stmt stmt:statements) {
            ctx.debug(stmt);
            
            stmt.execute(ctx);
        }
        */
    }


}
