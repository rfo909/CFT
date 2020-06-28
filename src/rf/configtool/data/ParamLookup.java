package rf.configtool.data;

import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.*;
import rf.configtool.parser.TokenStream;

public class ParamLookup extends LexicalElement {

    private Expr pos;
    private Expr defaultValue;
    
    public ParamLookup (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("P", "expected 'P'");
        if (ts.matchStr("(")) {
            pos=new Expr(ts);
            if (ts.matchStr(",")) {
                defaultValue=new Expr(ts);
            }
            ts.matchStr(")", "expected ')' closing P() expression");
        }
        
    }
    
    public Value resolve (Ctx ctx) throws Exception {
        List<Value> params=ctx.getFunctionState().getParams();
        if (pos==null) return new ValueList(params);
        Value pV=pos.resolve(ctx);
        if (!(pV instanceof ValueInt)) {
            throw new Exception("position must be int");
        }
        int iPos=(int) ((ValueInt) pV).getVal();
        if (iPos < 1) throw new Exception("invalid position, must be 1 or greater");
        iPos--;
        
        Value def;
        if (defaultValue != null) def=defaultValue.resolve(ctx); else def=new ValueNull();
        
        if (iPos >= params.size()) {
            return def;
        } else {
        	Value v=params.get(iPos);
        	if (v instanceof ValueNull) return def;
            return v;
        }
    }
}
