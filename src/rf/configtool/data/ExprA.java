package rf.configtool.data;

import java.util.ArrayList;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.parser.TokenStream;

public class ExprA extends LexicalElement {
    private String[] sep= {"&&"};

    private List<ExprB> parts=new ArrayList<ExprB>();
    private List<String> separators=new ArrayList<String>();
    
    public ExprA (TokenStream ts) throws Exception {
        super(ts);
        for (;;) {
            parts.add(new ExprB(ts));
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
        
        // logical and, implement short-cut processing
        for (ExprB part:parts) {
            Value v=part.resolve(ctx);
            if (!(v instanceof ValueBoolean)) {
                throw new Exception(getSourceLocation() + " expected boolean value");
            }
            if ( ! ((ValueBoolean) v).getVal()) {
                return new ValueBoolean(false);
            }
        }
        
        return new ValueBoolean(true);
    }

}
