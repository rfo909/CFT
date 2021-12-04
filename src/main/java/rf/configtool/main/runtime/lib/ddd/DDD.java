package rf.configtool.main.runtime.lib.ddd;

import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.lib.ddd.core.Vector3d;

/**
 * Publicly known object via Lib
 *
 */
public class DDD extends Obj {
	

    public DDD() {
        this.add(new FunctionRef());
        this.add(new FunctionVector());
        this.add(new FunctionWorld());
        this.add(new FunctionTriangle());
    }

    @Override
    public boolean eq(Obj x) {
        return x == this;
    }

    public String getTypeName() {
        return "DDD";
    }

    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    private String getDesc() {
        return "DDD";
    }

    private DDD self() {
        return this;
    }

    class FunctionRef extends Function {
        public String getName() {
            return "Ref";
        }

        public String getShortDesc() {
            return "Ref() - create DDD.Ref";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	return new ValueObj(new DDDRef());
        }
    }

    
  

    class FunctionVector extends Function {
        public String getName() {
            return "Vector";
        }

        public String getShortDesc() {
            return "Vector(x,y,z) - returns DDD.Vector";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 3) throw new RuntimeException("Expected parameters x,y,z");
        	double x=getFloat("x", params, 0);
        	double y=getFloat("y", params, 1);
        	double z=getFloat("z", params, 2);
        	Vector3d vec=new Vector3d(x,y,z);
        	return new ValueObj(new DDDVector(vec));
        }
    }
    
    class FunctionWorld extends Function {
        public String getName() {
            return "World";
        }

        public String getShortDesc() {
            return "World() - create scene object";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	return new ValueObj(new DDDWorld());
        }
    }
    
    class FunctionTriangle extends Function {
        public String getName() {
            return "Triangle";
        }

        public String getShortDesc() {
            return "Triangle(a,b,c,color) - a,b,c are 3D vectors";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 4) throw new RuntimeException("Expected 3D vector parameters a,b,c + color");
        	Obj a1=getObj("a",params,0);
        	Obj b1=getObj("b",params,1);
        	Obj c1=getObj("c",params,2);
        	Obj col1=getObj("color",params,3);
        	
        	if (!(a1 instanceof DDDVector) || !(b1 instanceof DDDVector) || !(c1 instanceof DDDVector)) {
        		throw new RuntimeException("Expected 3D vector parameters a,b,c + color");
        	}
        	Vector3d a=((DDDVector) a1).getVec();
        	Vector3d b=((DDDVector) b1).getVec();
        	Vector3d c=((DDDVector) c1).getVec();
        	
        	double x=getFloat("x", params, 0);
        	double y=getFloat("y", params, 1);
        	double z=getFloat("z", params, 2);
        	Vector3d vec=new Vector3d(x,y,z);
        	return new ValueObj(new DDDVector(vec));
        }
    }
    

 
    
}
