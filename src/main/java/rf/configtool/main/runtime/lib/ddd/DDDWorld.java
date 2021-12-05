package rf.configtool.main.runtime.lib.ddd;

import java.awt.Color;
import java.io.File;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.lib.ObjFile;
import rf.configtool.main.runtime.lib.ddd.core.Triangle;
import rf.configtool.main.runtime.lib.ddd.core.TriangleReceiver;
import rf.configtool.main.runtime.lib.ddd.viewers.AreaViewer;
import rf.configtool.main.runtime.lib.ddd.viewers.ViewerNotificationListener;

/**
 *
 */
public class DDDWorld extends Obj {
	
	private final AreaViewer viewer;
	private final TriangleReceiver triRecv;

	private double mm(double meters) {
		return meters/1000.0;
	}
	
    public DDDWorld() {
    	// Defining viewer in millimetres
    	ViewerNotificationListener listener=null;
    	int viewerNofificationTriCount=1000;
    	
    	/**
    	 * Defining camera in meter scale, which becomes scale 1
    	 */
    	this.viewer=new AreaViewer(mm(35), mm(36), mm(24), 800, 600, Color.BLACK, listener, viewerNofificationTriCount);
    	this.triRecv=this.viewer; 
    	
    	this.add(new FunctionSetLightPos());
    	this.add(new FunctionSetLightRange());
        this.add(new FunctionBrush());
        this.add(new FunctionOut());
        this.add(new FunctionRender());
    	this.add(new FunctionSetMetallicReflection());

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

    private DDDWorld self() {
        return this;
    }
    
    
    class FunctionSetLightPos extends Function {
        public String getName() {
            return "setLightPos";
        }

        public String getShortDesc() {
            return "setLightPos(DDD.Ref) - returns self";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 1) throw new Exception("Expected DDD.Ref parameter");
        	Obj obj=getObj("ref", params, 0);
        	if (obj instanceof DDDRef) {
        		self().viewer.setLightPos( ((DDDRef) obj).getRef() );
        		return new ValueObj(self());
        	} else {
        		throw new Exception("Expected DDD.Ref parameter");
        	}
        }
    }


    class FunctionSetLightRange extends Function {
        public String getName() {
            return "setLightRange";
        }

        public String getShortDesc() {
            return "setLightRange(Ref) - returns self";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 1) throw new Exception("Expected DDD.Ref parameter");
        	Obj obj=getObj("ref", params, 0);
        	if (obj instanceof DDDRef) {
        		self().viewer.setLightReach( ((DDDRef) obj).getRef() );
        		return new ValueObj(self());
        	} else {
        		throw new Exception("Expected DDD.Ref parameter");
        	}
        }
    }



    
    class FunctionBrush extends Function {
        public String getName() {
            return "Brush";
        }

        public String getShortDesc() {
            return "Brush() - create 3D Brush object";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	return new ValueObj(new DDDBrush(triRecv));
        }
    }


    
    class FunctionRender extends Function {
        public String getName() {
            return "render";
        }

        public String getShortDesc() {
            return "Render(file) - render as PNG to file";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 1) throw new RuntimeException("Expected File parameter");
        	Obj file=getObj("file",params,0);
        	if (file instanceof ObjFile) {
        		File f=((ObjFile) file).getFile();
        		self().viewer.writePNG(f);
        		return new ValueObj(self());
        	} else {
        		throw new RuntimeException("Expected File parameter");
        	}
        }
    }


    class FunctionOut extends Function {
        public String getName() {
            return "out";
        }

        public String getShortDesc() {
            return "out(Triangle) - add triangle to scene";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 1) throw new RuntimeException("Expected Triangle parameter");
        	Obj tri=getObj("triangle",params,0);
        	if (tri instanceof DDDTriangle) {
        		Triangle t=((DDDTriangle) tri).getTri();
        		self().viewer.tri(t);
        		return new ValueObj(self());
        	} else {
        		throw new RuntimeException("Expected Triangle parameter");
        	}
        }
    }


    class FunctionSetMetallicReflection extends Function {
        public String getName() {
            return "setMetallicReflection";
        }

        public String getShortDesc() {
            return "setMetallicReflection(bool) - modify influence from light source - returns self";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 1) throw new RuntimeException("Expected boolean parameter");
        	boolean bool=getBoolean("bool", params, 0);
        	self().viewer.setMetallicReflection(bool);
        	return new ValueObj(self());
        }
    }
    

    
}
