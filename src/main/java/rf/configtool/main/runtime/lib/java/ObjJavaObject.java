package rf.configtool.main.runtime.lib.java;

import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueFloat;
import rf.configtool.main.runtime.ValueObj;

import java.lang.reflect.*;

public class ObjJavaObject extends Obj {
	
	private Object obj;
    
    public ObjJavaObject(Object obj) {
    	this.obj=obj;
    	
    	add(new FunctionGetFieldValue());
    	
    }
    
    public Object getJavaObject() {
    	return obj;
    }
        
    @Override
    public boolean eq(Obj x) {
        return x==this;
    }


    @Override
    public String getTypeName() {
        return "JavaObject";
    }
    

    @Override
    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    
    private String getDesc() {
        return "JavaObject";
    }
    

    class FunctionGetFieldValue extends Function {
        public String getName() {
            return "getFieldValue";
        }
        public String getShortDesc() {
            return "getFieldValue(name) - returns JavaValue object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	String name=getString("name", params, 0);
        	
        	Class theClass=obj.getClass();
        	Field f = theClass.getDeclaredField(name);
        	if (!f.canAccess(obj)) throw new Exception("No access to field '" + name + "'");
        	return new ValueObj(ObjJavaValue.getInstance(f.get(obj)));
        }
    }

}
