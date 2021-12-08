package rf.configtool.main.runtime.lib.ddd;

import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueFloat;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.lib.ddd.core.Ref;
import rf.configtool.main.runtime.lib.ddd.core.Vector3d;

/**
 * Publicly known object
 *
 */
public class DDDRef extends Obj {

	private Ref ref;
	
	public Ref getRef() {
		return ref;
	}
	
    public DDDRef (Ref ref) {
    	this.ref=ref;
    	this.add(new FunctionScaleUp());
    	this.add(new FunctionScaleDown());
    	
    	this.add(new FunctionTurnLeft());
    	this.add(new FunctionTurnRight());
    	this.add(new FunctionRollLeft());
    	this.add(new FunctionRollRight());
    	this.add(new FunctionTurnUp());
    	this.add(new FunctionTurnDown());
    	
        this.add(new FunctionFwd());
        this.add(new FunctionBack());
    	this.add(new FunctionLeft());
    	this.add(new FunctionRight());
        this.add(new FunctionUp());
        this.add(new FunctionDown());
        
        this.add(new FunctionTranslate());
        
        this.add(new FunctionGetPosVector());
        this.add(new FunctionGetScaleFactor());
        this.add(new FunctionSetScaleFactor());
        this.add(new FunctionGetTransformedVector());

    }

    public DDDRef () {
    	this(new Ref());
    }
    
    
    @Override
    public boolean eq(Obj x) {
        return x == this;
    }

    public String getTypeName() {
        return "DDD.Ref";
    }

    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    private String getDesc() {
        return "DDD.Ref";
    }

    private DDDRef self() {
        return this;
    }


    class FunctionScaleUp extends Function {
        public String getName() {
            return "scaleUp";
        }

        public String getShortDesc() {
            return "scaleUp(factor) - create new Ref";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 1) throw new RuntimeException("Expected factor parameter");
        	double factor=getFloat("factor", params, 0);
        	
        	return new ValueObj(new DDDRef(ref.scaleUp(factor)));
        }
    }


    class FunctionScaleDown extends Function {
        public String getName() {
            return "scaleDown";
        }

        public String getShortDesc() {
            return "scaleDown(factor) - create new Ref";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 1) throw new RuntimeException("Expected factor parameter");
        	double factor=getFloat("factor", params, 0);
        	
        	return new ValueObj(new DDDRef(ref.scaleDown(factor)));
        }
    }
    
    
    
    class FunctionTurnLeft extends Function {
        public String getName() {
            return "turnLeft";
        }

        public String getShortDesc() {
            return "turnLeft(degrees) - create new Ref";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 1) throw new RuntimeException("Expected degrees parameter");
        	double degrees=getFloat("degrees", params, 0);
        	
        	return new ValueObj(new DDDRef(ref.turnLeftDeg(degrees)));
        }
    }
    
    
    
    class FunctionTurnRight extends Function {
        public String getName() {
            return "turnRight";
        }

        public String getShortDesc() {
            return "turnRight(degrees) - create new Ref";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 1) throw new RuntimeException("Expected degrees parameter");
        	double degrees=getFloat("degrees", params, 0);
        	
        	return new ValueObj(new DDDRef(ref.turnRightDeg(degrees)));
        }
    }
    
    
    
    class FunctionRollLeft extends Function {
        public String getName() {
            return "rollLeft";
        }

        public String getShortDesc() {
            return "rollLeft(degrees) - create new Ref";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 1) throw new RuntimeException("Expected degrees parameter");
        	double degrees=getFloat("degrees", params, 0);
        	
        	return new ValueObj(new DDDRef(ref.rollLeftDeg(degrees)));
        }
    }
    
    
    class FunctionRollRight extends Function {
        public String getName() {
            return "rollRight";
        }

        public String getShortDesc() {
            return "rollRight(degrees) - create new Ref";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 1) throw new RuntimeException("Expected degrees parameter");
        	double degrees=getFloat("degrees", params, 0);
        	
        	return new ValueObj(new DDDRef(ref.rollRightDeg(degrees)));
        }
    }
    
    
    class FunctionTurnUp extends Function {
        public String getName() {
            return "turnUp";
        }

        public String getShortDesc() {
            return "turnUp(degrees) - create new Ref";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 1) throw new RuntimeException("Expected degrees parameter");
        	double degrees=getFloat("degrees", params, 0);
        	
        	return new ValueObj(new DDDRef(ref.turnUpDeg(degrees)));
        }
    }
    
    
    class FunctionTurnDown extends Function {
        public String getName() {
            return "turnDown";
        }

        public String getShortDesc() {
            return "turnDown(degrees) - create new Ref";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 1) throw new RuntimeException("Expected degrees parameter");
        	double degrees=getFloat("degrees", params, 0);
        	
        	return new ValueObj(new DDDRef(ref.turnDownDeg(degrees)));
        }
    }
    
    
    class FunctionFwd extends Function {
        public String getName() {
            return "fwd";
        }

        public String getShortDesc() {
            return "fwd(dist) - create new Ref";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 1) throw new RuntimeException("Expected distance parameter");
        	double dist=getFloat("dist", params, 0);
        	
        	return new ValueObj(new DDDRef(ref.forward(dist)));
        }
    }


    class FunctionBack extends Function {
        public String getName() {
            return "back";
        }

        public String getShortDesc() {
            return "back(dist) - create new Ref";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 1) throw new RuntimeException("Expected distance parameter");
        	double dist=getFloat("dist", params, 0);
        	
        	return new ValueObj(new DDDRef(ref.backward(dist)));
        }
    }


    class FunctionLeft extends Function {
        public String getName() {
            return "left";
        }

        public String getShortDesc() {
            return "left(dist) - create new Ref";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 1) throw new RuntimeException("Expected distance parameter");
        	double dist=getFloat("dist", params, 0);
        	
        	return new ValueObj(new DDDRef(ref.left(dist)));
        }
    }


    class FunctionRight extends Function {
        public String getName() {
            return "right";
        }

        public String getShortDesc() {
            return "right(dist) - create new Ref";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 1) throw new RuntimeException("Expected distance parameter");
        	double dist=getFloat("dist", params, 0);
        	
        	return new ValueObj(new DDDRef(ref.right(dist)));
        }
    }


    class FunctionUp extends Function {
        public String getName() {
            return "up";
        }

        public String getShortDesc() {
            return "up(dist) - create new Ref";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 1) throw new RuntimeException("Expected distance parameter");
        	double dist=getFloat("dist", params, 0);
        	
        	return new ValueObj(new DDDRef(ref.up(dist)));
        }
    }


    class FunctionDown extends Function {
        public String getName() {
            return "down";
        }

        public String getShortDesc() {
            return "down(dist) - create new Ref";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 1) throw new RuntimeException("Expected distance parameter");
        	double dist=getFloat("dist", params, 0);
        	
        	return new ValueObj(new DDDRef(ref.down(dist)));
        }
    }

    
    class FunctionTranslate extends Function {
        public String getName() {
            return "translate";
        }

        public String getShortDesc() {
            return "translate(vec) - create new Ref";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 1) throw new RuntimeException("Expected 3d vector parameter");

        	Obj vec1=getObj("vec",params,0);
        	if (vec1 instanceof DDDVector) {
        		Vector3d vec=((DDDVector) vec1).getVec();
        		Ref newRef=self().ref.translate(vec);
        		return new ValueObj(new DDDRef(newRef));
        	} else {
        		throw new RuntimeException("Expected 3D vector parameter");
        	}
        }
    }

    
    class FunctionGetPosVector extends Function {
        public String getName() {
            return "getPosVector";
        }

        public String getShortDesc() {
            return "getPosVector() - get position vector";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 0) throw new RuntimeException("Expected no parameters");
        	DDDVector pos=new DDDVector( self().ref.getPos() );
        	return new ValueObj(pos);
        }
    }


    class FunctionGetScaleFactor extends Function {
        public String getName() {
            return "getScaleFactor";
        }

        public String getShortDesc() {
            return "getScaleFactor() - get current scale factor";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 0) throw new RuntimeException("Expected no parameters");
        	double factor=self().ref.getScaleFactor();
        	return new ValueFloat(factor);
        }
    }


    class FunctionGetTransformedVector extends Function {
        public String getName() {
            return "getTransformedVector";
        }

        public String getShortDesc() {
            return "getTransformedVector(vec) - transform local vector inside Ref system, to global vector";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 1) throw new RuntimeException("Expected 3D vector parameter");
        	Obj vec1=getObj("vec",params,0);
        	if (vec1 instanceof DDDVector) {
        		Vector3d vec=((DDDVector) vec1).getVec();
        		Vector3d result=self().ref.transformLocalToGlobal(vec);
        		return new ValueObj(new DDDVector(result));
        	} else {
        		throw new RuntimeException("Expected 3D vector parameter");
        	}
        }
    }


    
    class FunctionSetScaleFactor extends Function {
        public String getName() {
            return "setScaleFactor";
        }

        public String getShortDesc() {
            return "setScaleFactor(double) - set current scale factor - returns self";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 1) throw new RuntimeException("Expected scaleFactor parameter");
        	double factor=getFloat("factor", params, 0);
        	return new ValueObj(new DDDRef(ref.setScaleFactor(factor)));
        }
    }




}
