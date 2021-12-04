package rf.configtool.main.runtime.lib.ddd;

import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.lib.ddd.core.Triangle;

public class DDDTriangle extends Obj {

	private Triangle tri;
	
	public Triangle getTri() {
		return tri;
	}
	
    public DDDTriangle (Triangle tri) {
    	this.tri=tri;
//    	
//    	this.add(new FunctionX());
//    	this.add(new FunctionY());
//    	this.add(new FunctionZ());
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


//    class FunctionX extends Function {
//        public String getName() {
//            return "x";
//        }
//
//        public String getShortDesc() {
//            return "x() - get component value";
//        }
//
//        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
//        	if (params.size() != 0) throw new RuntimeException("Expected no parameters");
//        	return new ValueFloat(vec.getX());
//        }
//    }
//
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
//        	if (params.size() != 0) throw new RuntimeException("Expected no parameters");
//        	return new ValueFloat(vec.getY());
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
//        	if (params.size() != 0) throw new RuntimeException("Expected no parameters");
//        	return new ValueFloat(vec.getZ());
//        }
//    }

}
