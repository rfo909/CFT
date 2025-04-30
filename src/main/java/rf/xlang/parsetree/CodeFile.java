package rf.xlang.parsetree;

import java.util.ArrayList;
import java.util.List;

import rf.configtool.lexer.TokenStream;
import rf.configtool.main.Ctx;

public class CodeFile extends LexicalElement {
    
    private List<CodeTupleType> types=new ArrayList<CodeTupleType>();
    private List<CodeFunction> functions=new ArrayList<CodeFunction>();

    public CodeFile (TokenStream ts) throws Exception {
        super(ts);
        while (!ts.atEOF()) {
            if (ts.peekStr("type")) {
                types.add(new CodeTupleType(ts));
            } else if (ts.peekStr("def")) {
                functions.add(new CodeFunction(ts));
            } else {
                ts.error("Unknown code element, expected object, type or def");
            }
        }
    }
    
    public List<CodeTupleType> getTypes() {
        return types;
    }
    
    public List<CodeFunction> getFunctions() {
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
