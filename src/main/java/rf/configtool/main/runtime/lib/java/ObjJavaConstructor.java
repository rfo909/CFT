/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2025 Roar Foshaug

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
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueObj;

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
