/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2022 Roar Foshaug

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

import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;

public abstract class ObjJavaValue extends Obj {
    
    public ObjJavaValue () {
        this.add(new FunctionValue());
    }

    public abstract Object getAsJavaValue() throws Exception;
    public abstract Value getAsCFTValue() throws Exception;
    
    public static ObjJavaValue getInstance (Object obj) throws Exception {
        if (obj==null) return new ObjJavaValueNull();
        if (obj instanceof Integer) return new ObjJavaValueInt((Integer) obj);
        if (obj instanceof Long) return new ObjJavaValueLong((Long) obj);
        if (obj instanceof String) return new ObjJavaValueString((String) obj);
        if (obj instanceof Boolean) return new ObjJavaValueBoolean((Boolean) obj);
        throw new Exception("Unsupported Java type: " + obj.getClass().getName());
    }
    
    
    @Override
    public boolean eq(Obj x) {
        return x==this;
    }


    @Override
    public String getTypeName() {
        return "JavaValue";
    }
    

    @Override
    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    
    protected String getDesc() {
        return "JavaValue";
    }
    
    private Obj theObj () {
        return this;
    }
    
    class FunctionValue extends Function {
        public String getName() {
            return "value";
        }
        public String getShortDesc() {
            return "value() - get as CFT value";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return getAsCFTValue();
        }
    }

}

