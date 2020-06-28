package rf.configtool.data;

import java.util.*;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.*;
import rf.configtool.parser.TokenStream;

public class ExprWhen extends LexicalElement {

    private Expr bool, expr;
    
    public ExprWhen (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("when", "expected 'when'");
        ts.matchStr("(", "expected '(' following keyword 'when'");
        bool=new Expr(ts);
        ts.matchStr(",", "expected comma following boolean expr");
        expr=new Expr(ts);
        ts.matchStr(")", "expected ')' closing when expression");
    }
    
    public Value resolve (Ctx ctx) throws Exception {
        boolean b=bool.resolve(ctx).getValAsBoolean();
        if (b) {
            Value val=expr.resolve(ctx);
            if (val instanceof ValueMacro) {
            	val=((ValueMacro) val).call(ctx);
            }
            return val;
        } else {
        	return new ValueNull();
        }
    }
}
