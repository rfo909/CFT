package rf.configtool.data;

import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.*;
import rf.configtool.parser.TokenStream;

public class ExprIf extends LexicalElement {

    private Expr bool, exprIf, exprElse;
    
    public ExprIf (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("if", "expected 'if'");
        ts.matchStr("(", "expected '(' following keyword 'if'");
        bool=new Expr(ts);
        ts.matchStr(",", "expected comma following boolean expr");
        exprIf=new Expr(ts);
        ts.matchStr(",", "expected comma following ifExpr");
        exprElse=new Expr(ts);
        ts.matchStr(")", "expected ')' closing if expression");
    }
    
    public Value resolve (Ctx ctx) throws Exception {
        boolean b=bool.resolve(ctx).getValAsBoolean();
        Value result;
        if (b) {
            result = exprIf.resolve(ctx);
        } else {
        	result = exprElse.resolve(ctx);
        }
        
        return result;
    }
}
