package rf.configtool.data;

import java.util.*;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.*;
import rf.configtool.main.runtime.lib.ObjDict;
import rf.configtool.parser.TokenStream;

public class ParamLookupDict extends LexicalElement {

    private List<Expr> names=new ArrayList<Expr>();
    
    public ParamLookupDict (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("PDict", "expected 'PDict'");
        
        
        boolean comma=false;
        
        
        ts.matchStr("(","expected '('");
    	for (;;) {
    		if (ts.matchStr(")")) break;
    		if (comma) ts.matchStr(",", "expected comma");
    		names.add(new Expr(ts));
    		comma=true;
    	}
    	if (names.size()==0) throw new Exception("Expected at least one field name");
        
    }
    
    public Value resolve (Ctx ctx) throws Exception {
        List<Value> params=ctx.getFunctionState().getParams();
        Map<String,Value> map=new HashMap<String,Value>();
        for (int i=0; i<names.size(); i++) {
        	String name=names.get(i).resolve(ctx).getValAsString();
        	Value value;
        	if (i < params.size()) {
        		value=params.get(i);
        	} else {
        		value=new ValueNull();
        	}
        	map.put(name, value);
        }
        return new ValueObj(new ObjDict(map));
    }
}
