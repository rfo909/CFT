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

package rf.configtool.main.runtime.lib;

import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.IsSynthesizable;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;

public class ObjAValue extends Obj implements IsSynthesizable {
    
    private String annotation;
    private Value value;
    private ObjDict metadata;

    public ObjAValue (String annotation, Value value, ObjDict metadata) {
        this.annotation=annotation;
        this.value=value;
        //if (metadata==null) metadata=new ObjDict();  -- to costly if used in great numbers, modify .meta function instead
        this.metadata=metadata;
        
        this.add(new FunctionA());
        this.add(new FunctionVal());
        this.add(new FunctionMeta());
    }

    private ObjAValue self() {
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
    public String createCode() throws Exception {
        String str = "AValue(" + (new ValueString(annotation)).synthesize()
                + ","+value.synthesize();
        if (metadata != null) {
            return str + "," + metadata.synthesize() + ")";
        } else {
            return str + ")";
        }
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
            // creating empty dictionary on demand, if no dictionary given initially
            if (metadata==null) metadata = new ObjDict();
            return new ValueObj(metadata);
        }
    } 

}
    
    
