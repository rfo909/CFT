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

package rf.configtool.main.runtime.lib.dd;

import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueBoolean;

import rf.configtool.main.runtime.lib.dd.DD.FunctionRef;
import rf.configtool.main.runtime.lib.dd.DD.FunctionVec;
import rf.configtool.main.runtime.ValueNull;

public class DD extends Obj {
    
    public DD() {
        this.add(new FunctionVec());
        this.add(new FunctionRef());
        this.add(new FunctionWorld());
        this.add(new FunctionIntersect());
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
            return "Ref(world?) - return Ref2d object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            DDWorld world=null;
            if (params.size()==1) {
                world=(DDWorld) getObj("world",params,0);
            } else if (params.size() != 0) {
                throw new Exception("Expected optional parameter world");
            }
            return new ValueObj(new DDRef(new Ref(), world));
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


 
 class FunctionIntersect extends Function {
    public String getName() {
        return "intersect";
    }
    public String getShortDesc() {
        return "intersect(pos1,vec1,pos2,vec2) - returns boolean";
    }
    private DDVector get (String name, List<Value> params, int pos, String err) throws Exception {
        Obj obj=getObj(name,params,pos);
        if (!(obj instanceof DDVector)) throw new Exception(err);
        return ((DDVector) obj);
    }

    public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        String err="Expected params vpos1, vec1, vpos2, vec2";
        if (params.size() != 4) throw new Exception(err);
        DDVector pos1=get("vpos1",params,0,err);
        DDVector vec1=get("vec1",params,1,err);
        DDVector pos2=get("vpos2",params,2,err);
        DDVector vec2=get("vec2",params,3,err);

        double ax1=pos1.getVec().getX();
        double ay1=pos1.getVec().getY();

        double ax2=ax1+vec1.getVec().getX();
        double ay2=ay1+vec1.getVec().getY();

        double bx1=pos2.getVec().getX();
        double by1=pos2.getVec().getY();

        double bx2=bx1+vec2.getVec().getX();
        double by2=by1+vec2.getVec().getY();

        return new ValueBoolean(LineSegmentIntersection.doSegmentsIntersect(ax1, ay1, ax2, ay2, bx1, by1, bx2, by2));
    }
 }
   

}
