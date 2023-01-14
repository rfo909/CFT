package rf.configtool.parsetree;

import java.util.ArrayList;
import java.util.List;

import rf.configtool.lexer.SourceLocation;
import rf.configtool.lexer.TokenStream;
import rf.configtool.main.CFTCallStackFrame;
import rf.configtool.main.Ctx;
import rf.configtool.main.FunctionBody;
import rf.configtool.main.FunctionState;
import rf.configtool.main.PropsFile;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBlock;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueString;

public class ExprLastResult extends ExprCommon {

	private final Long pos;
	
    public ExprLastResult (TokenStream ts) throws Exception {
        super(ts);
        if (ts.matchStr("::")) {
        	pos=null;
        } else {
        	ts.matchStr(":","ExprLastResult: expected '::' or ':N'");
        	pos=ts.matchInt("ExprLastResult: expected int following ':'");
        }
    }
    
    
    public Value resolve (Ctx ctx) throws Exception {
    	Value v=ctx.getObjGlobal().getRoot().getLastResult();
    	if (pos==null) return v;
    	if (!(v instanceof ValueList)) throw new Exception("LastResult not list");
    	List<Value> list=((ValueList) v).getVal();
    	return list.get(pos.intValue());
    	
    }
}
