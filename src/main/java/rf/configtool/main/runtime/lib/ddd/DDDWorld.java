package rf.configtool.main.runtime.lib.ddd;

import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.lib.ddd.core.MyColor;
import rf.configtool.main.runtime.lib.ddd.core.TriangleReceiver;
import rf.configtool.main.runtime.lib.ddd.viewers.AreaViewer;
import rf.configtool.main.runtime.lib.ddd.viewers.ViewerNotificationListener;

/**
 *
 */
public class DDDWorld extends Obj {
	
	private final AreaViewer viewer;
	private final TriangleReceiver triRecv;

    public DDDWorld() {
    	// Defining viewer in millimetres
    	ViewerNotificationListener listener=null;
    	int viewerNofificationTriCount=1000;
    	
    	this.viewer=new AreaViewer(50, 36, 24, 800, 600, MyColor.BLACK, listener, viewerNofificationTriCount);
    	this.triRecv=this.viewer; 
    	
    	this.add(new FunctionSetLightPos());
    	this.add(new FunctionSetLightReach());
        this.add(new FunctionBrush());
        this.add(new FunctionRender());
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
            return "SetLightPos";
        }

        public String getShortDesc() {
            return "SetLightPos(DDD.Ref) - returns self";
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


    class FunctionSetLightReach extends Function {
        public String getName() {
            return "SetLightReach";
        }

        public String getShortDesc() {
            return "SetLightReach(DDD.Ref) - returns self";
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
            return "Render";
        }

        public String getShortDesc() {
            return "Render(file) - render as PNG to file";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	// Create image
        	// get Graphics-object
        	// viewer.draw(g);
        	// save as PNG
        	throw new Exception("Not implemented");
        }
    }


  
    
}
