package rf.configtool.main.runtime.lib.ddd;

import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueFloat;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.lib.ddd.core.Vector3d;

public class DDDVector extends Obj {

	private Vector3d vec;
	
	public Vector3d getVec() {
		return vec;
	}
	
    public DDDVector (Vector3d vec) {
    	this.vec=vec;
    	
    	this.add(new FunctionX());
    	this.add(new FunctionY());
    	this.add(new FunctionZ());
    	this.add(new FunctionLength());
    	this.add(new FunctionAdd());
    	this.add(new FunctionSub());
    	this.add(new FunctionScale());
    	this.add(new FunctionScaleTo());
    	this.add(new FunctionAngle());
    	
    }

    
    @Override
    public boolean eq(Obj x) {
        return x == this;
    }

    public String getTypeName() {
        return "DDD.Vector";
    }

    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    private String getDesc() {
        return "DDD.Vector";
    }

    private DDDVector self() {
        return this;
    }


    class FunctionX extends Function {
        public String getName() {
            return "x";
        }

        public String getShortDesc() {
            return "x() - get component value";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 0) throw new RuntimeException("Expected no parameters");
        	return new ValueFloat(vec.getX());
        }
    }

    class FunctionY extends Function {
        public String getName() {
            return "y";
        }

        public String getShortDesc() {
            return "y() - get component value";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 0) throw new RuntimeException("Expected no parameters");
        	return new ValueFloat(vec.getY());
        }
    }

    class FunctionZ extends Function {
        public String getName() {
            return "z";
        }

        public String getShortDesc() {
            return "z() - get component value";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 0) throw new RuntimeException("Expected no parameters");
        	return new ValueFloat(vec.getZ());
        }
    }

    class FunctionLength extends Function {
        public String getName() {
            return "length";
        }

        public String getShortDesc() {
            return "length() - get vector length";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 0) throw new RuntimeException("Expected no parameters");
        	return new ValueFloat(self().vec.length());
        }
    }

    class FunctionAdd extends Function {
        public String getName() {
            return "add";
        }

        public String getShortDesc() {
            return "add(DDD.Vector) - return vector sum";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 1) throw new RuntimeException("Expected 3d Vector parameter");
        	Obj vec1=getObj("vector",params,0);
        	if (vec1 instanceof DDDVector) {
        		Vector3d x=((DDDVector) vec1).getVec();
        		return new ValueObj(new DDDVector(getVec().add(x)));
        	} else {
        		throw new RuntimeException("Expected 3d Vector parameter");
        	}
        }
    }

    class FunctionSub extends Function {
        public String getName() {
            return "sub";
        }

        public String getShortDesc() {
            return "sub(DDD.Vector) - subtract vector";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 1) throw new RuntimeException("Expected 3d Vector parameter");
        	Obj vec1=getObj("vector",params,0);
        	if (vec1 instanceof DDDVector) {
        		Vector3d x=((DDDVector) vec1).getVec();
        		return new ValueObj(new DDDVector(getVec().sub(x)));
        	} else {
        		throw new RuntimeException("Expected 3d Vector parameter");
        	}
        }
    }

    
    class FunctionScale extends Function {
        public String getName() {
            return "scale";
        }

        public String getShortDesc() {
            return "scale(factor) - scale vector length";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 1) throw new RuntimeException("Expected Scale parameter");
        	double factor = getFloat("factor",params,0);
        	return new ValueObj(new DDDVector(getVec().mul(factor)));
        }
    }


    
    class FunctionScaleTo extends Function {
        public String getName() {
            return "scaleTo";
        }

        public String getShortDesc() {
            return "scale(length) - scale vector length";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 1) throw new RuntimeException("Expected Length parameter");
        	double length = getFloat("length",params,0);
        	double factor=length/vec.length();
        	
        	return new ValueObj(new DDDVector(getVec().mul(factor)));
        }
    }
    
    
    
    class FunctionAngle extends Function {
        public String getName() {
            return "angle";
        }

        public String getShortDesc() {
            return "angle(DDD.Vector) - return absolute angle (0-90)";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 1) throw new RuntimeException("Expected 3d Vector parameter");
        	Obj vec1=getObj("vector",params,0);
        	if (vec1 instanceof DDDVector) {
        		Vector3d x=((DDDVector) vec1).getVec();
        		return new ValueFloat(getVec().calcAbsoluteAngleNormalized(x)*90);
        	} else {
        		throw new RuntimeException("Expected 3d Vector parameter");
        	}
        }
    }

}
