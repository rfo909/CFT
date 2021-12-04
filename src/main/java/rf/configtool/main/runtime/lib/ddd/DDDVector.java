package rf.configtool.main.runtime.lib.ddd;

import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueFloat;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.lib.ddd.DDDRef.FunctionBack;
import rf.configtool.main.runtime.lib.ddd.DDDRef.FunctionDown;
import rf.configtool.main.runtime.lib.ddd.DDDRef.FunctionFwd;
import rf.configtool.main.runtime.lib.ddd.DDDRef.FunctionLeft;
import rf.configtool.main.runtime.lib.ddd.DDDRef.FunctionRight;
import rf.configtool.main.runtime.lib.ddd.DDDRef.FunctionRollLeft;
import rf.configtool.main.runtime.lib.ddd.DDDRef.FunctionRollRight;
import rf.configtool.main.runtime.lib.ddd.DDDRef.FunctionScaleDown;
import rf.configtool.main.runtime.lib.ddd.DDDRef.FunctionScaleUp;
import rf.configtool.main.runtime.lib.ddd.DDDRef.FunctionTurnDown;
import rf.configtool.main.runtime.lib.ddd.DDDRef.FunctionTurnLeft;
import rf.configtool.main.runtime.lib.ddd.DDDRef.FunctionTurnRight;
import rf.configtool.main.runtime.lib.ddd.DDDRef.FunctionTurnUp;
import rf.configtool.main.runtime.lib.ddd.DDDRef.FunctionUp;
import rf.configtool.main.runtime.lib.ddd.core.Ref;
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

}
