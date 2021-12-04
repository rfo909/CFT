package rf.configtool.main.runtime.lib.java;

import java.lang.reflect.Method;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueObj;

public class ObjJavaMethod extends Obj {
	
	private Method method;
    
    public ObjJavaMethod(Method method) {
    	this.method=method;
    	
    	this.add(new FunctionCall());
    }
    
    @Override
    public boolean eq(Obj x) {
        return x==this;
    }


    @Override
    public String getTypeName() {
        return "JavaMethod";
    }
    

    @Override
    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    
    private String getDesc() {
        return "JavaMethod";
    }
    
    private Obj theObj () {
        return this;
    }
    
    
    class FunctionCall extends Function {
        public String getName() {
            return "call";
        }
        public String getShortDesc() {
            return "call(JavaObject[, JavaValue, ...]) - return JavaValue";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	Obj obj=getObj("JavaObject", params, 0);
        	if (!(obj instanceof ObjJavaObject)) throw new Exception("Not a JavaObject: " + obj.getTypeName());
        	Object javaObject=((ObjJavaObject) obj).getJavaObject();

        	Object[] args=new Object[params.size()-1];

        	for (int i=0; i<args.length; i++) {
        		Obj x = getObj("JavaValue", params, i+1);
        		if (!(x instanceof ObjJavaValue)) throw new Exception("Invalid parameter object: " + x.getTypeName() + " - expected JavaValue objects");
        		args[i]=((ObjJavaValue) x).getAsJavaValue();
        	}
        	
        	Object returnValue = method.invoke(javaObject, args);
        	return new ValueObj(ObjJavaValue.getInstance(returnValue));
        }
    }

    
}