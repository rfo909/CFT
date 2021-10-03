package rf.configtool.main.runtime.lib.java;

/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020 Roar Foshaug

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 3 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>
*/

import java.util.*;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.*;

/**
 * Interface Java objects, constructors and methods, with parameters and return values.
 */

public class ObjJava extends Obj {
    
    public ObjJava() {
    	this.add(new FunctionForName());
    	this.add(new FunctionNull());
    	this.add(new FunctionInt());
    	this.add(new FunctionLong());
    	this.add(new FunctionString());
    	this.add(new FunctionBoolean());
    	this.add(new FunctionObject());
    	this.add(new Function_Example());
    }
    
    @Override
    public boolean eq(Obj x) {
        return x==this;
    }


    @Override
    public String getTypeName() {
        return "Java";
    }
    

    @Override
    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    
    private String getDesc() {
        return "Java";
    }
    
    private Obj theObj () {
        return this;
    }
    
    class FunctionForName extends Function {
        public String getName() {
            return "forName";
        }
        public String getShortDesc() {
            return "forName(className) - returns class object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected class name parameter");
            String className=getString("className", params, 0);
            
            Class c=Class.forName(className);
            Obj obj=new ObjJavaClass(c);
            
            return new ValueObj(obj);
        }
    }
    
    
    class FunctionNull extends Function {
        public String getName() {
            return "Null";
        }
        public String getShortDesc() {
            return "Null() - returns JavaValue object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueObj(new ObjJavaValueNull());
        }
    }
   
    class FunctionInt extends Function {
        public String getName() {
            return "Int";
        }
        public String getShortDesc() {
            return "Int(int) - returns JavaValue object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected int parameter");
            int i=(int) getInt("int",params,0);
            return new ValueObj(new ObjJavaValueInt(i));
        }
    }
   

    class FunctionLong extends Function {
        public String getName() {
            return "Long";
        }
        public String getShortDesc() {
            return "Long(long) - returns JavaValue object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected long parameter");
            long l = getInt("int",params,0);
            return new ValueObj(new ObjJavaValueLong(l));
        }
    }
    
    class FunctionString extends Function {
        public String getName() {
            return "String";
        }
        public String getShortDesc() {
            return "String(str) - returns JavaValue object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected String parameter");
            String str = getString("str",params,0);
            return new ValueObj(new ObjJavaValueString(str));
        }
    }
   
    
    class FunctionBoolean extends Function {
        public String getName() {
            return "Boolean";
        }
        public String getShortDesc() {
            return "Boolean(bool) - returns JavaValue object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected bool parameter");
            boolean b = getBoolean("bool",params,0);
            return new ValueObj(new ObjJavaValueBoolean(b));
        }
    }
   
    class FunctionObject extends Function {
        public String getName() {
            return "Object";
        }
        public String getShortDesc() {
            return "Object(javaObject) - returns JavaValue object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected JavaObject parameter");
            Obj obj = getObj("javaObject",params,0);
            if (!(obj instanceof ObjJavaObject)) throw new Exception("Expected JavaObject parameter");
            
            ObjJavaObject x=(ObjJavaObject) obj;
            return new ValueObj(new ObjJavaValueObject(x));
        }
    }
   
    class Function_Example extends Function {
        public String getName() {
            return "_Example";
        }
        public String getShortDesc() {
            return "_Example() - display example";
        }
        private String[] data= {
        	"",
        	"Example",
        	"-------",
        	"",
			"Lib.Java.forName(\"java.lang.String\") => String",
			"String.getConstructor(String).call(Lib.Java.String(\"test\")) => obj",
			"String.getConstructor(String).call(Lib.Java.String(\"123\")) => obj2",
			"Lib.Java.Object(obj2) => paramObj",
			"String.getMethod(\"concat\",String).call(obj,paramObj).value",
			"",
			"Returns string \"test123\"",
			"",
        };
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	for (String line:data) {
                ctx.getObjGlobal().addSystemMessage(line);
        	}
        	return new ValueBoolean(true);
        }
    }


}
