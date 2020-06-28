package rf.configtool.main.runtime;

import java.util.*;

import rf.configtool.data.Stmt;
import rf.configtool.main.Ctx;
import rf.configtool.main.FunctionState;
import rf.configtool.main.ObjGlobal;
import rf.configtool.main.OutData;
import rf.configtool.main.OutText;
import rf.configtool.main.runtime.lib.ObjGrep;

public class ValueMacro extends Value {
    
    private List<Stmt> statements;
    private boolean inheritContext;
    
    public ValueMacro (List<Stmt> statements, boolean inheritContext) {
    	this.statements=statements;
    	this.inheritContext=inheritContext;
    	
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

    public Value call (Ctx ctx) throws Exception {
    	return call (ctx, new ArrayList<Value>());
    }
    
    
    public Value call (Ctx ctx, List<Value> params) throws Exception {
    	Ctx sub;
    	if (inheritContext) {
    		sub=ctx.sub(params);
    	} else {
    		sub=new Ctx(ctx.getObjGlobal(), new FunctionState(params));
    	}
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
