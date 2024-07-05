/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2024 Roar Foshaug

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

import java.lang.reflect.Field;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueObj;

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
            return new ValueObj(ObjJavaValue.getInstance(f.get(obj)));
        }
    }

}

