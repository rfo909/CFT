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
import rf.configtool.main.runtime.ValueString;

public class ExprSymGetSet extends ExprCommon {

	private final String symbol;
	private final boolean isSet;
    
    public ExprSymGetSet (TokenStream ts, boolean isSet) throws Exception {
        super(ts);
        this.isSet=isSet;
        String pre=(isSet ? "%%" : "%");
        
    	ts.matchStr(pre, "expected '" + pre + "'");
//    	if (isSet) {
//        	ts.matchStr("%", "expected '" + pre + "'");
//    	}
    	
        symbol = ts.matchIdentifier("expected identifier following '" + pre + "'");
    }
    
    private Value callMacro (Ctx ctx) throws Exception {
        PropsFile propsFile=ctx.getObjGlobal().getRoot().getPropsFile();
        SourceLocation loc=getSourceLocation();
        
        String lambda;
        if (isSet) {
        	lambda = propsFile.getMSymSet();
        } else {
        	lambda = propsFile.getMSymGet();
        }

        FunctionBody codeLines=new FunctionBody(lambda, loc);
        
    	CFTCallStackFrame caller=new CFTCallStackFrame("Lambda for SymGetSet");

    	
        Value ret = ctx.getObjGlobal().getRuntime().processFunction(ctx.getStdio(), caller, codeLines, new FunctionState(null,null));
        if (!(ret instanceof ValueBlock)) throw new Exception("Not a lambda: " + lambda + " ---> " + ret.synthesize());
        
        ValueBlock macroObj=(ValueBlock) ret;
        
        List<Value> params=new ArrayList<Value>();
        params.add(new ValueString(symbol));
        
        Value result = macroObj.callLambda(ctx.sub(), params);
        return result;
        
    }
        
    
    public Value resolve (Ctx ctx) throws Exception {
    	return callMacro(ctx);
    }
}
