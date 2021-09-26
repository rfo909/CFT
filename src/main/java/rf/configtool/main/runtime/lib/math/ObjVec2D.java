package rf.configtool.main.runtime.lib.math;

import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueFloat;
import rf.configtool.main.runtime.ValueObj;

/**
* This class represents a 2d-vector.
*
* All vectors are immutable: operations on them always create new vectors.
*/
public class ObjVec2D extends Obj {

	private double x,y;

	public ObjVec2D (double x, double y) {
		this.x=x;
		this.y=y;
		this.add(new FunctionAdd());
		this.add(new FunctionSub());
		this.add(new FunctionAngleDeg());
		this.add(new FunctionLength());
		this.add(new FunctionX());
		this.add(new FunctionY());
		this.add(new FunctionRotateDeg());
		this.add(new FunctionScaleTo());
		this.add(new FunctionScale());
		
	}
	

	@Override
    public boolean eq(Obj x) {
        return x==this;
    }


    @Override
    public String getTypeName() {
        return "Vec2D";
    }
    

    @Override
    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    
    private String getDesc() {
        return "Vec2D";
    }
    
    private ObjVec2D theObj () {
        return this;
    }
	    

    public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}
	
	
	private ObjVec2D getVector (String name, List<Value> params, int pos, String err) throws Exception {
		Obj obj=getObj(name, params, pos);
		if (obj instanceof ObjVec2D) return (ObjVec2D) obj;
		throw new Exception(err);
	}
	
	class FunctionAdd extends Function {
		public String getName() {
			return "add";
        }
        public String getShortDesc() {
            return "add(Vec2d) - add vector";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected Vec2d parameter");
            ObjVec2D vec=getVector("Vec2d", params, 0, "Expected Vec2d parameter");
            return new ValueObj(theObj().add(vec));
        }
    }

	class FunctionSub extends Function {
		public String getName() {
			return "sub";
        }
        public String getShortDesc() {
            return "sub(Vec2d) - return vector that points from THIS to given vector";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected Vec2d parameter");
            ObjVec2D vec=getVector("Vec2d", params, 0, "Expected Vec2d parameter");
            return new ValueObj(theObj().sub(vec));
        }
    }


	class FunctionAngleDeg extends Function {
		public String getName() {
			return "angle";
        }
        public String getShortDesc() {
            return "angle(Vec2d) - get absolute angle in degrees between vectors";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected Vec2d parameter");
            ObjVec2D vec=getVector("Vec2d", params, 0, "Expected Vec2d parameter");
            return new ValueFloat(theObj().calcAbsoluteAngleDeg(vec));
        }
    }


	class FunctionLength extends Function {
		public String getName() {
			return "length";
        }
        public String getShortDesc() {
            return "length() - get length";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected  no parameters");
            return new ValueFloat(theObj().length());
        }
    }


	class FunctionX extends Function {
		public String getName() {
			return "x";
        }
        public String getShortDesc() {
            return "x() - get x component";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected  no parameters");
            return new ValueFloat(theObj().getX());
        }
    }


	class FunctionY extends Function {
		public String getName() {
			return "y";
        }
        public String getShortDesc() {
            return "y() - get y component";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected  no parameters");
            return new ValueFloat(theObj().getY());
        }
    }

	class FunctionRotateDeg extends Function {
		public String getName() {
			return "rotate";
        }
        public String getShortDesc() {
            return "rotate(angleDeg) - create rotated vector, positive is counter clockwise";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected parameter angle");
            double angle=getFloat("angleDef", params, 0);
            return new ValueObj(theObj().rotateDeg(angle));
        }
    }




	
	class FunctionScaleTo extends Function {
		public String getName() {
			return "scaleTo";
        }
        public String getShortDesc() {
            return "scaleTo(targetLength) - create vector of given length";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected parameter targetLength");
            double targetLength=getFloat("targetLength", params, 0);
            return new ValueObj(theObj().scaleToLength(targetLength));
        }
    }



	class FunctionScale extends Function {
		public String getName() {
			return "scale";
        }
        public String getShortDesc() {
            return "scale(xScale,yScale) - scale x and y (floats)";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 2) throw new Exception("Expected parameters xScale, yScale (floats)");
            double xScale=getFloat("xScale", params, 0);
            double yScale=getFloat("yScale", params, 0);
            return new ValueObj(theObj().scale(xScale,yScale));
        }
    }


	// ------------------------------------------------------------------------------------------
	// Code from 2001 ...
	// ------------------------------------------------------------------------------------------
	
	

	/** Create new vector that is the result of adding another vector to this vector */
	public ObjVec2D add(ObjVec2D v) {
		return new ObjVec2D(x+v.x, y+v.y);
	}

	/** Create new vector represents the line from the end of this vector to the end
	* of the given vector: this is the subtraction operation, "v" minus "this". It is
	* the opposite of the add() method, in that adding the result of this method to "this"
	* reproduces the argument "v".
	*/
	public ObjVec2D sub(ObjVec2D v) {
		return new ObjVec2D(v.x-x, v.y-y);
	}

	/**
	* Calculate angle (in radians) between this vector and the given vector. It is
	* absolute in that it is in the range 0..pi/2, (0-90 deg).
	*/
	public double calcAbsoluteAngleRadians (ObjVec2D v) {
		double u1=x;
		double u2=y;

		double v1=v.x;
		double v2=v.y;

		double dotProduct=u1*v1 + u2*v2;

		double cosAngle=dotProduct / (length()*v.length());
		cosAngle=Math.abs(cosAngle);		// only interested in 0..pi/2

		double angle=Math.acos(cosAngle);

//		if (angle < 0.0 || angle > Math.PI/2.0) {
//			System.out.println("unexpected result from acos: " + angle);
//		}

		return angle;
	}


	public double calcAbsoluteAngleDeg (ObjVec2D v) {
		double rad = calcAbsoluteAngleRadians(v);
		double deg = (rad*180) / Math.PI;
		return deg;
	}

	/** Create new vector that is this vector multiplied by a factor */
	public ObjVec2D mul(double factor) {
		return new ObjVec2D (x*factor, y*factor);
	}

	/** Calculate absolute length of vector */
	public double length() {
		return Math.sqrt(x*x + y*y);
	}


	public ObjVec2D scaleToLength (double targetLength) {
		double currLength=length();
		double factor=targetLength/currLength;
		return this.mul(factor);
	}
	
	public ObjVec2D scale (double xScale, double yScale) {
		return new ObjVec2D(x*xScale, y*yScale); 
	}

	/** Create new vector that is this vector rotated in the xy-plane. Angle given in radians. */
	public ObjVec2D rotate (double radians) {
		double cosFactor=Math.cos(radians);
		double sinFactor=Math.sin(radians);
		return new ObjVec2D (x*cosFactor - y*sinFactor,	x*sinFactor + y*cosFactor);
	}

	/** Create new vector that is this vector rotated in the xy-plane. Angle given in degrees. */
	public ObjVec2D rotateDeg (double degrees) {
		return rotate (Math.PI * degrees / 180.0);
	}

	/** Rotate a fraction of the full circle (0..1) */
	public ObjVec2D rotateFract (double fraction) {
		return rotate (fraction * Math.PI * 2);
	}

	/**
	* Transform this vector to new coordinate system by
	* multiplying its components with the given unit vectors, adding the
	* result to create a new vector that is local to the system in which the
	* unit vectors are expressed.
	*/
	public ObjVec2D transform (ObjVec2D unitx, ObjVec2D unity) {
		return unitx.mul(x).add(unity.mul(y));
	}


	private String fmt (double d) {
		int i=(int) d;
		int j=(int) (d*10);
		j=j%10;
		return ""+i+"."+j;
	}
	public String toString() {
		return "[" + fmt(x) + ", " + fmt(y) + "]";
	}
}
