package rf.configtool.main.runtime.lib.java;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.lib.java.ObjJava.FunctionForName;
import java.lang.reflect.*;
import java.util.List;

public class ObjJavaConstructor extends Obj {
	
	private Class theClass;
	private Constructor constructor;
    
    public ObjJavaConstructor(Class theClass, Constructor constructor) {
    	this.theClass=theClass;
    	this.constructor=constructor;
    	
    	this.add(new FunctionCall());
    }
    
    @Override
    public boolean eq(Obj x) {
        return x==this;
    }


    @Override
    public String getTypeName() {
        return "JavaConstructor";
    }
    

    @Override
    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    
    private String getDesc() {
        return "JavaConstructor";
    }
    
    private Obj theObj () {
        return this;
    }
    
    
    class FunctionCall extends Function {
        public String getName() {
            return "call";
        }
        public String getShortDesc() {
            return "call(JavaValue...) - return JavaObject";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	Object[] args=new Object[params.size()];

        	for (int i=0; i<args.length; i++) {
        		Obj obj = getObj("JavaValue", params, i);
        		if (!(obj instanceof ObjJavaValue)) throw new Exception("Invalid parameter object: " + obj.getTypeName() + " - expected JavaValue objects");
        		args[i]=((ObjJavaValue) obj).getAsJavaValue();
        	}
        	
        	Object obj = constructor.newInstance(args);
        	return new ValueObj(new ObjJavaObject(obj));
        }
    }

    
}