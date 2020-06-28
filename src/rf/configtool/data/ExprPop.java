package rf.configtool.data;

import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.*;
import rf.configtool.parser.TokenStream;

public class ExprPop extends LexicalElement {

    public ExprPop (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("_", "expected '_'");
    }
    
    public Value resolve (Ctx ctx) throws Exception {
        Value v=ctx.pop();
        if (v==null) v=new ValueNull();
        return v;
    }
}
