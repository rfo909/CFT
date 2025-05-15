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
<<<<<<< HEAD:src/main/java/rf/xlang/parsetree/CodeFile.java
    
    public List<CodeTupleType> getTypes() {
        return types;
    }
    
    public List<CodeFunction> getFunctions() {
=======

    public List<TupleType> getTupleTypes() {
        return types;
    }

    public List<CodeFunction> getCodeFunctions() {
>>>>>>> 8ad777f9e9ba47f24eb9122dfff78b42e4cf0d9e:XLang/src/main/java/rf/xlang/parsetree/Code.java
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
