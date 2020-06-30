package rf.configtool.main.runtime;

import java.util.*;

import rf.configtool.data.Stmt;
import rf.configtool.main.Ctx;
import rf.configtool.main.FunctionState;
import rf.configtool.main.ObjGlobal;
import rf.configtool.main.OutData;
import rf.configtool.main.OutText;
import rf.configtool.main.runtime.lib.ObjGrep;

/**
 * A macro is by default a local code block that is executed immediately, and is a way of grouping
 * multiple statements and expressions as a single expression. Bit it can also be created as a
 * stand-alone object, that is invoked via .call(params). It then runs in an independent context.
 */
public class ValueMacro extends Value {
    
    private List<Stmt> statements;
    
    public ValueMacro (List<Stmt> statements) {
    	this.statements=statements;
    	
        add(new FunctionCall());
    }
    
    @Override
    public String getTypeName() {
        return "macro";
    }


    @Override
    public String getValAsString() {
    	return "{}";
    }
    
    
    @Override
    public boolean eq(Obj v) {
        if (v==this) return true;
        return false;
    }

    @Override
    public boolean getValAsBoolean() {
        return true;
    }

    
    private void invoke (Ctx ctx) throws Exception {
        for (Stmt stmt:statements) {
            stmt.execute(ctx);
        }
    }

    /**
     * Call local macro ("in line code block"). It runs in sub-context, and inherits
     * lookup of as well parameters and variables. 
     */
    public Value callLocalMacro (Ctx ctx) throws Exception {
    	// Execute local macro, which means it has Ctx lookup up the Ctx stack, including
    	// parameters to the function, but that the loop flag stops
    	Ctx sub=ctx.subContextForCodeBlock(); 
    	invoke(sub);
    	return sub.getResult();
    }
    
    
    /**
     * Call independent macro which runs in an isolated Ctx
     */
    public Value call (Ctx ctx, List<Value> params) throws Exception {
    	Ctx sub=new Ctx(ctx.getObjGlobal(), new FunctionState(params));
    	invoke(sub);
    	return sub.getResult();
    }
    
    class FunctionCall extends Function {
        public String getName() {
            return "call";
        }
        public String getShortDesc() {
            return "call(...) - call macro with parameters";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	return call(ctx,params);
        }
    }



}
