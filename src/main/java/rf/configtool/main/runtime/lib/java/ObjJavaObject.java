package rf.configtool.main.runtime.lib.java;

import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueFloat;
import java.lang.reflect.*;

public class ObjJavaObject extends Obj {
	
	private Object obj;
	private Class theClass;
    
    public ObjJavaObject(Object obj, Class theClass) {
    	this.obj=obj;
    	this.theClass=theClass;
    	
    }
    
    public Object getJavaObject() {
    	return obj;
    }
    
    public Class getJavaClass() {
    	return theClass;
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
    
    private Obj theObj () {
        return this;
    }
    
}

