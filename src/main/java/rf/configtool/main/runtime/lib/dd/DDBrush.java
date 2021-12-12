package rf.configtool.main.runtime.lib.dd;

import java.awt.Color;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.lib.ObjColor;


/**
 * The 2D brush is simply for drawing lines, nothing fancy
 */
public class DDBrush extends Obj {

	private ViewReceiver recv;
	private Color color;
	private Vector2d prevPos;

	public DDBrush (ViewReceiver recv) {
		this.recv=recv;
		this.color=new Color(0,0,0);
		
		this.add(new FunctionPenDown());
		this.add(new FunctionPenUp());
		this.add(new FunctionSetColor());
		
	}
	@Override
	public boolean eq(Obj x) {
		return x == this;
	}

	@Override
	public String getTypeName() {
		return "DD.Ref";
	}

	@Override
	public ColList getContentDescription() {
		return ColList.list().regular(getDesc());
	}

	private String getDesc() {
		return "DD.Ref";
	}

	private DDBrush self() {
		return this;
	}

	
	   class FunctionPenDown extends Function {
	        public String getName() {
	            return "penDown";
	        }

	        public String getShortDesc() {
	            return "penDown(Ref) - draw line since last penDown";
	        }

	        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
	        	if (params.size() != 1) throw new RuntimeException("Expected Ref parameter");
	        	Obj ref1=getObj("Ref",params,0);
	        	if (ref1 instanceof DDRef) {
	        		Ref ref=((DDRef) ref1).getRef();
	        		Vector2d pos=ref.getPos();
	        		if (prevPos != null) {
	        			Line line=new Line(prevPos, pos, color);
	        			recv.addLine(line);
	        		}
	        		prevPos=pos;
	        		return new ValueObj(self());
	        	} else {
	        		throw new RuntimeException("Expected Ref parameter");
	        	}
	        }
	    }

	
	   class FunctionPenUp extends Function {
	        public String getName() {
	            return "penUp";
	        }

	        public String getShortDesc() {
	            return "penUp() - stop drawing";
	        }

	        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
	        	if (params.size() != 0) throw new RuntimeException("Expected no parameters");
	        	prevPos=null;
	        	return new ValueObj(self());
	        }
	    }

	
	   class FunctionSetColor extends Function {
	        public String getName() {
	            return "setColor";
	        }

	        public String getShortDesc() {
	            return "setColor(Color) - set color";
	        }

	        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
	        	if (params.size() != 1) throw new RuntimeException("Expected Color parameter");
	        	Obj col1=getObj("Color",params,0);
	        	if (col1 instanceof ObjColor) {
	        		Color color=((ObjColor) col1).getAWTColor();
	        		self().color=color;
	        		return new ValueObj(self());
	        	} else {
	        		throw new RuntimeException("Expected Color parameter");
	        	}
	        }
	    }
}
