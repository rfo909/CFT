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

import java.util.*;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueFloat;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.lib.ddd.core.Ref;
import rf.configtool.main.runtime.lib.ddd.core.Vector3d;

/**
 * Bezier curve in 3d
 *
 */
public class DDDBezier extends Obj {

    private List<Vector3d> points=new ArrayList<Vector3d>();
    
    public DDDBezier () {
        
        this.add(new FunctionAddPoint());
        this.add(new FunctionCalculate());
    }

    
    @Override
    public boolean eq(Obj x) {
        return x == this;
    }

    public String getTypeName() {
        return "DDD.Bezier";
    }

    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    private String getDesc() {
        return "DDD.Bezier";
    }

    private DDDBezier self() {
        return this;
    }


    class FunctionAddPoint extends Function {
        public String getName() {
            return "addPoint";
        }

        public String getShortDesc() {
            return "addPoint(refOrVector3d) - add world point to spline";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new RuntimeException("Expected parameter refOrVector3d");
            Obj obj=getObj("refOrVector3d", params, 0);
            Vector3d point;
            if (obj instanceof DDDVector) {
                point=((DDDVector) obj).getVec();
            } else if (obj instanceof DDDRef) {
                point=((DDDRef) obj).getRef().getPos();
            } else {
                throw new RuntimeException("Expected parameter refOrVector3d");
            }
            points.add(point);
            return new ValueObj(self());
        }
    }

    class FunctionCalculate extends Function {
        public String getName() {
            return "calculate";
        }

        public String getShortDesc() {
            return "calculate(numSteps) - returns list of Ref objects created from template ref";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new RuntimeException("Expected parameters numSteps");
            int numSteps=(int) getInt("numSteps", params, 0);
            if (points.size() < 3) throw new RuntimeException("Must add at least 2 points");
            
            List<Value> result=new ArrayList<Value>();
            for (int iter=0; iter<numSteps; iter++) {
                double factor=((double) iter)/(numSteps-1); // 0 to 1 inclusive
                Vector3d point = findPoint(points,factor);
                result.add(new ValueObj(new DDDVector(point)));
            }
            return new ValueList(result);
        }
        
        
        private Vector3d findPoint (List<Vector3d> points, double factor) {
            // need to reduce list of points to two (one line segment), through iterations where
            // each iteration cuts down one 
            
            while (points.size()>=2) {
                List<Vector3d> nextList=new ArrayList<Vector3d>();
                
                for (int i=0; i<points.size()-1; i++) {
                    Vector3d a=points.get(i);
                    Vector3d b=points.get(i+1);
                    Vector3d dv=a.sub(b); // from a to b
                    nextList.add(a.add(dv.mul(factor)));
                }
                points=nextList;
            }
            return points.get(0);
        }
    }

}
