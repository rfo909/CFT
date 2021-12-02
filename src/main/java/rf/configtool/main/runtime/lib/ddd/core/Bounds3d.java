package rf.configtool.main.runtime.lib.ddd.core;


public class Bounds3d {
	
	private double x1, x2, y1, y2, z1, z2;

	public Bounds3d (Vector3d vec) {
		x1=x2=vec.getX();
		y1=y2=vec.getY();
		z1=z2=vec.getZ();
	}
	
	public Bounds3d (Vector3d[] points) {
		this(points[0]);
		for (int i=1; i<=points.length; i++) add(points[i]);
	}

	public Bounds3d (Ref ref) {
		this(ref.getPos());
	}

	public Bounds3d (Ref ref, double dx1, double dx2, double dy1, double dy2, double dz1, double dz2) {
		this(ref.getPos());
		this.x1=x1-dx1;
		this.x2=x2+dx2;
		this.y1=y1-dy1;
		this.y2=y2+dy2;
		this.z1=z1-dz1;
		this.z2=z2+dz2;
	}

	public void add (Vector3d vec) {
		double x=vec.getX();
		double y=vec.getY();
		double z=vec.getZ();
		if (x<x1) x1=x;
		if (x>x2) x2=x;
		if (y<y1) y1=y;
		if (y>y2) y2=y;
		if (z<z1) z1=z;
		if (z>z2) z2=z;
	}

	public double getX1 () { return x1; }
	public double getX2 () { return x2; }
	public double getY1 () { return y1; }
	public double getY2 () { return y2; }
	public double getZ1 () { return z1; }
	public double getZ2 () { return z1; }

	/**
	 * Return true if any of the bounds corners is on the inside (according to plane equation)
	 * of given plane.
	 */
	public boolean inside (Plane3d plane) {
		// plane equation returns positive value if point outside plane, 0 if in plane,
		// and negative if on inside
		if (plane.calcPlaneEquation(x1,y1,z1) <= 0) return true;
		if (plane.calcPlaneEquation(x1,y1,z2) <= 0) return true;
		if (plane.calcPlaneEquation(x1,y2,z1) <= 0) return true;
		if (plane.calcPlaneEquation(x1,y2,z2) <= 0) return true;
		if (plane.calcPlaneEquation(x2,y1,z1) <= 0) return true;
		if (plane.calcPlaneEquation(x2,y1,z2) <= 0) return true;
		if (plane.calcPlaneEquation(x2,y2,z1) <= 0) return true;
		if (plane.calcPlaneEquation(x2,y2,z2) <= 0) return true;
		return false;
	}

}