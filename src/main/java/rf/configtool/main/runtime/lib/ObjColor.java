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

package rf.configtool.main.runtime.lib;

import java.awt.Color;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueInt;

public class ObjColor extends Obj {
    
    private int r,g,b;
    
    public ObjColor (int r, int g, int b) {
        this.r=r;
        this.g=g;
        this.b=b;
        
        this.add(new FunctionR());
        this.add(new FunctionG());
        this.add(new FunctionB());
    }
    

    public int getR() {
        return r;
    }


    public int getG() {
        return g;
    }


    public int getB() {
        return b;
    }


    public Color getAWTColor() {
        return new Color(r,g,b);
    }
    
    
    @Override
    public boolean eq(Obj x) {
        return x==this;
    }


    public String getTypeName() {
        return "Color";
    }
    

    public ColList getContentDescription() {
        return ColList.list().regular(""+r+","+g+","+b);
    }

    
    private String getDesc() {
        return "Term";
    }
    

    public Color getColor() {
        return new Color(r,g,b);
    }
    
    class FunctionR extends Function {
        public String getName() {
            return "r";
        }
        public String getShortDesc() {
            return "r() - get component value";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueInt(r);
        }
    }
    
    class FunctionG extends Function {
        public String getName() {
            return "g";
        }
        public String getShortDesc() {
            return "g() - get component value";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueInt(g);
        }
    }
    
    class FunctionB extends Function {
        public String getName() {
            return "b";
        }
        public String getShortDesc() {
            return "b() - get component value";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueInt(b);
        }
    }
    
}
