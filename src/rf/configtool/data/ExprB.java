package rf.configtool.data;

import java.util.ArrayList;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueFloat;
import rf.configtool.main.runtime.ValueInt;
import rf.configtool.parser.TokenStream;

public class ExprB extends LexicalElement {
    private String[] sep= {">","<",">=","<=","==","!="};

    private List<ExprC> parts=new ArrayList<ExprC>();
    private List<String> separators=new ArrayList<String>();
    
    public ExprB (TokenStream ts) throws Exception {
        super(ts);
        for (;;) {
            parts.add(new ExprC(ts));
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
        Value a=parts.get(0).resolve(ctx);
        Value b=parts.get(1).resolve(ctx);
        
        String sep=separators.get(0);
        if (sep.equals("==")) {
            return new ValueBoolean(a.eq(b));
        }
        if (sep.equals("!=")) {
            return new ValueBoolean(!a.eq(b));
        }

        double va, vb;
        if ((a instanceof ValueInt) && (b instanceof ValueInt)) {
            va=((ValueInt) a).getVal();
            vb=((ValueInt) b).getVal();
        } else if ((a instanceof ValueInt) && (b instanceof ValueFloat)) {
            va=((ValueInt) a).getVal();
            vb=((ValueFloat) b).getVal();
        } else if ((a instanceof ValueFloat) && (b instanceof ValueFloat)) {
            va=((ValueFloat) a).getVal();
            vb=((ValueFloat) b).getVal();
        } else if ((a instanceof ValueFloat) && (b instanceof ValueInt)) {
            va=((ValueFloat) a).getVal();
            vb=((ValueInt) b).getVal();
        } else {
            throw new Exception("Expected int/float comparison");
        }
        
        if (sep.equals(">")) {
            return new ValueBoolean(va>vb);
        }

        if (sep.equals(">=")) {
            return new ValueBoolean(va>=vb);
        }

        if (sep.equals("<")) {
            return new ValueBoolean(va<vb);
        }

        if (sep.equals("<=")) {
            return new ValueBoolean(va<=vb);
        }
        
        throw new Exception("Internal error");
    }

}
