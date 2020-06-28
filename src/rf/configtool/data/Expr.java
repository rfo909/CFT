package rf.configtool.data;

import java.util.*;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.parser.TokenStream;

public class Expr extends LexicalElement {
    
    protected static String matchSeparator (TokenStream ts, String[] sep) {
        for (String s:sep) {
            try {
                if (ts.matchStr(s)) return s;
            } catch (Exception ex) {
                return null;
            }
        }
        return null;
    }
    
    private String[] sep= {"||"};
    private List<ExprA> parts=new ArrayList<ExprA>();
    private List<String> separators=new ArrayList<String>();
    
    public Expr (TokenStream ts) throws Exception {
        super(ts);
        for (;;) {
            parts.add(new ExprA(ts));
            String x=Expr.matchSeparator(ts,sep);
            if (x != null) {
                separators.add(x);
            } else {
                break;
            }
        }
    }
    
    public Value resolve (Ctx ctx) throws Exception {
        if (parts.size() == 1) {
            return parts.get(0).resolve(ctx);
        }

        // logical or, implement short-cut processing
        for (ExprA part:parts) {
            Value v=part.resolve(ctx);
            if (!(v instanceof ValueBoolean)) {
                throw new Exception(getSourceLocation() + " expected boolean value");
            }
            if ( ((ValueBoolean) v).getVal()) {
                return v;
            }
        }
        
        return new ValueBoolean(false);
    }
}
