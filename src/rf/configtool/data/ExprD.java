package rf.configtool.data;

import java.util.ArrayList;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueFloat;
import rf.configtool.main.runtime.ValueInt;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.parser.TokenStream;

public class ExprD extends LexicalElement {
    private String[] sep= {"*","/","%"};

    private List<ExprE> parts=new ArrayList<ExprE>();
    private List<String> separators=new ArrayList<String>();
    
    public ExprD (TokenStream ts) throws Exception {
        super(ts);
        for (;;) {
            parts.add(new ExprE(ts));
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
        
        // at least two parts
        Value currVal=parts.get(0).resolve(ctx);
        
        for (int pos=1; pos<parts.size(); pos++) {
            String sep=separators.get(pos-1);
            Value nextVal=parts.get(pos).resolve(ctx);
            
            currVal=combine(currVal, sep, nextVal);
        }
        return currVal;
    }
    
    private Exception ex(String msg) throws Exception {
        return new Exception(getSourceLocation() + " " + msg);
    }
    
    
    // int sep int -> int
    // int sep float -> float
    // float sep int/float -> float
    private Value combine (Value a, String sep, Value b) throws Exception {
        if (a instanceof ValueInt) {
            long x1=((ValueInt) a).getVal();
            if (b instanceof ValueInt) {
                long x2=((ValueInt) b).getVal();
                return calcInt(x1, sep, x2);
            } else if (b instanceof ValueFloat) {
                double x2=((ValueFloat) b).getVal();
                return calcFloat(x1, sep, x2);
            } else {
                throw ex("Expected int " + sep + " int/float");
            }
        }
        if (a instanceof ValueFloat) {
            double x1=((ValueFloat) a).getVal();
            double x2;
            if (b instanceof ValueInt) {
                x2=((ValueInt) b).getVal();
            } else if (b instanceof ValueFloat) {
                x2=((ValueFloat) b).getVal();
            } else {
                throw ex("Expected float " + sep + " int/float");
            }
            return calcFloat(x1,sep,x2);
        }
        throw ex("Invalid value " + a.getDescription() + " " + sep + " " + b.getDescription());

    }

    // 2020-02 making division always return float
    private Value calcInt (long a, String sep, long b) throws Exception {
        if (sep.equals("*")) return new ValueInt(a*b);
        if (sep.equals("/")) return new ValueFloat((double)a/(double)b);
        if (sep.equals("%")) return new ValueInt(a%b);
        throw ex("Internal error: invalid separator");
    }
    private Value calcFloat (double a, String sep, double b) throws Exception {
        if (sep.equals("*")) return new ValueFloat(a*b);
        if (sep.equals("/")) return new ValueFloat(a/b);
        if (sep.equals("%")) return new ValueFloat(a%b);
        throw ex("Internal error: invalid separator");
    }


}
