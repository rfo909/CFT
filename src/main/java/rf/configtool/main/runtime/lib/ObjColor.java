package rf.configtool.main.runtime.lib;

import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueInt;
import rf.configtool.main.runtime.lib.ddd.core.MyColor;

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


	public MyColor getMyColor() {
		return new MyColor(r,g,b);
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