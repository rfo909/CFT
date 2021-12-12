package rf.configtool.main.runtime.lib.dd;

/**
* This class represents a 2d-vector.
*
* All vectors are immutable: operations on them always create new vectors.
*/
public class Vector2d  {

	private double x,y;
	
	public Vector2d (double x, double y) {
		this.x=x;
		this.y=y;
	}

	/** Create new vector that is the result of adding another vector to this vector */
	public Vector2d add(Vector2d v) {
		return new Vector2d(x+v.x, y+v.y);
	}

	/** Create new vector represents the line from the end of this vector to the end
	* of the given vector: this is the subtraction operation, "v" minus "this". It is
	* the opposite of the add() method, in that adding the result of this method to "this"
	* reproduces the argument "v".
	*/
	public Vector2d sub(Vector2d v) {
		return new Vector2d(v.x-x, v.y-y);
	}

	/**
	* Calculate angle (in radians) between this vector and the given vector. It is
	* absolute in that it is in the range 0..pi/2, (0-90 deg).
	*/
	public double calcAbsoluteAngleRadians (Vector2d v) {
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

	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}

	public double calcAbsoluteAngleDeg (Vector2d v) {
		double rad = calcAbsoluteAngleRadians(v);
		double deg = (rad*180) / Math.PI;
		return deg;
	}

	/** Create new vector that is this vector multiplied by a factor */
	public Vector2d mul(double factor) {
		return new Vector2d (x*factor, y*factor);
	}

	/** Calculate absolute length of vector */
	public double length() {
		return Math.sqrt(x*x + y*y);
	}


	public Vector2d scaleToLength (double targetLength) {
		double currLength=length();
		double factor=targetLength/currLength;
		return this.mul(factor);
	}
	
	public Vector2d scale (double xScale, double yScale) {
		return new Vector2d(x*xScale, y*yScale); 
	}

	/** Create new vector that is this vector rotated in the xy-plane. Angle given in radians. */
	public Vector2d rotate (double radians) {
		double cosFactor=Math.cos(radians);
		double sinFactor=Math.sin(radians);
		return new Vector2d (x*cosFactor - y*sinFactor,	x*sinFactor + y*cosFactor);
	}

	/** Create new vector that is this vector rotated in the xy-plane. Angle given in degrees. */
	public Vector2d rotateDeg (double degrees) {
		return rotate (Math.PI * degrees / 180.0);
	}

	/** Rotate a fraction of the full circle (0..1) */
	public Vector2d rotateFract (double fraction) {
		return rotate (fraction * Math.PI * 2);
	}

	/**
	* Transform this vector to new coordinate system by
	* multiplying its components with the given unit vectors, adding the
	* result to create a new vector that is local to the system in which the
	* unit vectors are expressed.
	*/
	public Vector2d transform (Vector2d unitx, Vector2d unity) {
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
