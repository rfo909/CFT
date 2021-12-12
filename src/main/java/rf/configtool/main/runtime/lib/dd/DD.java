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

package rf.configtool.main.runtime.lib.dd;

import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueObj;

public class DD extends Obj {
    
    public DD() {
    	this.add(new FunctionVec());
    	this.add(new FunctionRef());
    	this.add(new FunctionWorld());
    }
    
    @Override
    public boolean eq(Obj x) {
        return x==this;
    }


    @Override
    public String getTypeName() {
        return "DD";
    }
    

    @Override
    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    
    private String getDesc() {
        return "DD";
    }
    
    private Obj theObj () {
        return this;
    }
    

    
    class FunctionVec extends Function {
        public String getName() {
            return "Vector";
        }
        public String getShortDesc() {
            return "Vector(x,y) - return Vector2d object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 2) throw new Exception("Expected parameters x,y");
            
            double x=getFloat("x", params, 0);
            double y=getFloat("y", params, 1);
            return new ValueObj(new DDVector(new Vector2d(x,y)));
        }
    }

    
    class FunctionRef extends Function {
        public String getName() {
            return "Ref";
        }
        public String getShortDesc() {
            return "Ref() - return Ref2d object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueObj(new DDRef(new Ref()));
        }
    }
    
	   
 class FunctionWorld extends Function {
     public String getName() {
         return "World";
     }
     public String getShortDesc() {
         return "World() - return World object";
     }
     public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
         if (params.size() != 0) throw new Exception("Expected no parameters");
         return new ValueObj(new DDWorld());
     }
 }
    
}
