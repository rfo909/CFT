/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2023 Roar Foshaug

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

package rf.configtool.main.runtime.lib.java;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;

public class ObjJavaClass extends Obj {
    
    private Class theClass;
    
    public ObjJavaClass (Class theClass) {
        this.theClass=theClass;
        this.add(new FunctionName());
        this.add(new FunctionGetMethod());
        this.add(new FunctionGetConstructor());

    }
    
    public Class getTheClass() {
        return theClass;
    }
    
    @Override
    public boolean eq(Obj x) {
        return x==this;
    }


    @Override
    public String getTypeName() {
        return "JavaClass";
    }
    

    @Override
    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    
    private String getDesc() {
        return "JavaClass";
    }
    
    private Obj theObj () {
        return this;
    }
    
    class FunctionName extends Function {
        public String getName() {
            return "name";
        }
        public String getShortDesc() {
            return "name() - name of java class";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueString(theClass.getName());
        }
    }
    
    
 
    class FunctionGetMethod extends Function {
        public String getName() {
            return "getMethod";
        }
        public String getShortDesc() {
            return "getMethod(name[, PClass, ...]) - return JavaMethod object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            Class[] paramTypes=new Class[params.size()-1];
            String name=getString("name", params, 0);
            for (int i=0; i<paramTypes.length; i++) {
                Obj obj = getObj("PClass", params, i+1);
                if (!(obj instanceof ObjJavaClass)) throw new Exception("Invalid parameter-class object: " + obj.getTypeName());
                paramTypes[i]=((ObjJavaClass) obj).getTheClass();
            }
            
            Method m = theClass.getMethod(name, paramTypes);
            return new ValueObj(new ObjJavaMethod(m));
        }
    }

    
    class FunctionGetConstructor extends Function {
        public String getName() {
            return "getConstructor";
        }
        public String getShortDesc() {
            return "getConstructor([, PClass, ...]) - return JavaConstructor object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            Class[] paramTypes=new Class[params.size()];
            for (int i=0; i<paramTypes.length; i++) {
                Obj obj = getObj("PClass", params, i);
                if (!(obj instanceof ObjJavaClass)) throw new Exception("Invalid parameter-class object: " + obj.getTypeName());
                paramTypes[i]=((ObjJavaClass) obj).getTheClass();
            }
            
            Constructor c=theClass.getConstructor(paramTypes);
            return new ValueObj(new ObjJavaConstructor(theClass,c));
        }
    }

    
}
