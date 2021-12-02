package rf.configtool.main.runtime.lib.ddd.core;

/**
* This class identifies a 3d plane, defined by three points.
* It contains methods to solve the plane equation for particular points in space,
* so that one can decide if single points are inside or outside the plane. What is inside
* and outside is defined by the order of the three points in the constructor: seen from
* the outside, points should always be given in anti-clockwise order. In a right-handed
* coordinate system. Which is what we use, anyway.
* <P>
* An other important method that will be
* used a lot by surface viewers is the method to find the point in 3d-space where a ray
* from the center of the coordinate system (eye) intersects with the plane.
*/
public class Plane3d {

	protected Vector3d[] points;  // Three POINTS (vectors from [0,0,0])
	protected double A,B,C,D;  // plane equation - calculated in constructor
	protected Vector3d normalVector;


	/**
	* Instantiate a plane from three points, as identified by these vectors
	* from the eye (0,0,0) to the point. For the plane equation to be of any use
	* in deciding whether a point is on the inside or outside of the plane, the
	* points must be given in an anti-clockwise order seen from the outside.
	* Outside is identified by (Ax+By+Cz+D > 0) for a point [x,y,z].
	*/
	public Plane3d (Vector3d a, Vector3d b, Vector3d c) {
		this.points=new Vector3d[3];
		this.points[0]=a;
		this.points[1]=b;
		this.points[2]=c;

		// Calculate ABCD
		double x1=a.getX();
		double y1=a.getY();
		double z1=a.getZ();
		double x2=b.getX();
		double y2=b.getY();
		double z2=b.getZ();
		double x3=c.getX();
		double y3=c.getY();
		double z3=c.getZ();

		A=y1*(z2-z3) + y2*(z3-z1) + y3*(z1-z2);
		B=z1*(x2-x3) + z2*(x3-x1) + z3*(x1-x2);
		C=x1*(y2-y3) + x2*(y3-y1) + x3*(y1-y2);
		D=-x1*(y2*z3 - y3*z2)
			-x2*(y3*z1 - y1*z3)
			-x3*(y1*z2 - y2*z1);

		normalVector=new Vector3d(A,B,C);
	}

	/**
	* Return the three points that define the triangle - in the order as given to
	* the constructor. Returned as array of length 3.
	*/
	public Vector3d[] getPoints() {
		return points;
	}
	
	public Bounds3d getBoundingBox() {
		return new Bounds3d(points);
	}

	
	/**
	* Do the back-face test for a point, returning positive value if the point is OUTSIDE
	* the plane described by the triangle, 0 if IN the plane, and negative if INSIDE.
	*/
	public double calcPlaneEquation (double x, double y, double z) {
		return (A*x + B*y + C*z + D);
	}

	public double calcPlaneEquation (Vector3d point) {
		double x=point.getX();
		double y=point.getY();
		double z=point.getZ();
		return calcPlaneEquation (x,y,z);
	}

	public double calcPlaneEquation (Ref point) {
		return calcPlaneEquation (point.getPos());
	}

	/**
	* This method calculates the point where the given ray intersects with
	* the plane, by multiplying it with some number. If no such point exists
	* (ray is parallell to plane), null is returned.
	*/
	public Vector3d findIntersectionPoint (Vector3d ray) {
		// The ray consists of [x,y,z]. These are known, as well as ABCD of the
		// plane equation. What we now want to do is find a value k to multiply
		// the ray with so that the plane equation solved for [kx,ky,kz] is zero:
		// -> A(kx) + B(ky) + C(kz) + D = 0;
		// -> k(Ax + By + Cz) = -D
		// -> k = -D/(Ax + By + Cz)
		double AxByCz = A*ray.getX() + B*ray.getY() + C*ray.getZ();
		double k=-D/AxByCz;
		if (k==Double.POSITIVE_INFINITY || k==Double.NEGATIVE_INFINITY) {
			return null;
		}
		return ray.mul(k);
	}


	/**
	* Return normal-vector for the plane
	*/
	public Vector3d getNormalVector() {
		return normalVector;
	}

}