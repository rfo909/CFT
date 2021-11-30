package rf.configtool.main.runtime.lib;

import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.lib.ObjDb.FunctionDb2;
import rf.configtool.main.runtime.lib.ObjDb.FunctionGetLockFile;
import rf.configtool.main.runtime.lib.ObjDb.FunctionObtainLock;
import rf.configtool.main.runtime.lib.ObjDb.FunctionReleaseLock;
import rf.configtool.main.runtime.lib.ObjDb.FunctionUUID;
import rf.configtool.main.runtime.lib.db.ObjDb2;

public class ObjAnnotatedValue extends Obj {
	
	private String annotation;
	private Value value;
	private ObjDict metadata;

	public ObjAnnotatedValue (String annotation, Value value, ObjDict metadata) {
		this.annotation=annotation;
		this.value=value;
		if (metadata==null) metadata=new ObjDict();
		this.metadata=metadata;
		
		this.add(new FunctionA());
		this.add(new FunctionVal());
		this.add(new FunctionMeta());
	}

	private ObjAnnotatedValue self() {
        return this;
    }
    
    @Override
    public boolean eq(Obj x) {
        return false;
    }

    
    @Override
    public String getTypeName() {
        return "AValue";
    }
    
    @Override
    public ColList getContentDescription() {
        return ColList.list().regular(getTypeName());
    }
    
    @Override
    public String synthesize() throws Exception {
    	return "AValue(" + (new ValueString(annotation)).synthesize() + ","+value.synthesize()+","+metadata.synthesize() + ")";
    }
    
   
    class FunctionA extends Function {
        public String getName() {
            return "a";
        }
        public String getShortDesc() {
            return "a() - get annotation string";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueString(annotation);
        }
    } 
    
    
    class FunctionVal extends Function {
        public String getName() {
            return "v";
        }
        public String getShortDesc() {
            return "v() - get value";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return value;
        }
    }
    
    class FunctionMeta extends Function {
        public String getName() {
            return "meta";
        }
        public String getShortDesc() {
            return "meta() - get metadata dictionary";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueObj(metadata);
        }
    } 

}
    
    