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

package rf.configtool.main.runtime.lib.ddd;

import java.util.ArrayList;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.lib.ddd.core.Triangle;
import rf.configtool.main.runtime.lib.ddd.core.Vector3d;

public class DDDTriangle extends Obj {

    private Triangle tri;
    
    public Triangle getTri() {
        return tri;
    }
    
    public DDDTriangle (Triangle tri) {
        this.tri=tri;
        this.add(new FunctionPoints());
        this.add(new FunctionNormal());
    }

    
    @Override
    public boolean eq(Obj x) {
        return x == this;
    }

    public String getTypeName() {
        return "DDD.Triangle";
    }

    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    private String getDesc() {
        return "DDD.Triangle";
    }

    private DDDTriangle self() {
        return this;
    }


    class FunctionPoints extends Function {
        public String getName() {
            return "points";
        }

        public String getShortDesc() {
            return "points() - returns list of 3D vectors";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new RuntimeException("Expected no parameters");
            List<Value> list=new ArrayList<Value>();
            Vector3d[] points=self().tri.getPoints();
            for (Vector3d p:points) {
                list.add(new ValueObj(new DDDVector(p)));
            }
            return new ValueList(list);
        }
    }

    class FunctionNormal extends Function {
        public String getName() {
            return "normal";
        }

        public String getShortDesc() {
            return "normal() - returns normal vector";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new RuntimeException("Expected no parameters");
            Vector3d normal=self().getTri().getNormalVector();
            return new ValueObj(new DDDVector(normal));
        }
    }

    
    //    class FunctionY extends Function {
//        public String getName() {
//            return "y";
//        }
//
//        public String getShortDesc() {
//            return "y() - get component value";
//        }
//
//        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
//          if (params.size() != 0) throw new RuntimeException("Expected no parameters");
//          return new ValueFloat(vec.getY());
//        }
//    }
//
//    class FunctionZ extends Function {
//        public String getName() {
//            return "z";
//        }
//
//        public String getShortDesc() {
//            return "z() - get component value";
//        }
//
//        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
//          if (params.size() != 0) throw new RuntimeException("Expected no parameters");
//          return new ValueFloat(vec.getZ());
//        }
//    }

}
