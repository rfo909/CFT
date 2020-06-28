package rf.configtool.data;

import java.util.*;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Value;
import rf.configtool.parser.TokenStream;

public class ExprE extends LexicalElement {
    
    private ExprTerminal firstPart;
    private List<DottedCall> dottedLookups=new ArrayList<DottedCall>();
    
    public ExprE (TokenStream ts) throws Exception {
        super(ts);
        firstPart=new ExprTerminal(ts);
        while (ts.peekStr(".")) {
            dottedLookups.add(new DottedCall(ts));
        }
    }
    
    public Value resolve (Ctx ctx) throws Exception {
        Value v=firstPart.resolve(ctx);
        for (DottedCall x:dottedLookups) {
            v=x.resolve(ctx, v);
        }
        return v;
    }


}
