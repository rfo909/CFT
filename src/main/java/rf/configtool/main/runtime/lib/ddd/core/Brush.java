package rf.configtool.main.runtime.lib.ddd.core;

import java.awt.Color;
import java.util.Vector;


/**
* A brush is a set of 3d-vectors. Applying these in a state (Ref) produces a set of new
* Ref instances. Repeating the process with other states means dragging a list of
* connected points through space - it describes a surface. 
*/

public class Brush {
	
	private TriangleReceiver triDest;
	private VisibleAttributes defaultAttributes;
	
	private Vector points=new Vector();			// Vector3d
	private Vector attributes=new Vector();		// VisibleAttributes or null if not visible
	private Vector prevPoints=null;
	
	
	private boolean visibleTrianglesOnly=false;
	// decides whether calling triVisible() or tri() in triDest.
	private boolean splitBothWays=false;
	// if true, generate double set of triangles, splitting both possible ways
	
	
	
	/**
	* Create a new brush, giving as argument some object capable of further processing
	* of the triangles that will be generated when the brush is used.
	* Remember also to call setAttributes() before creating segments,
	* or they will be all red (default).
	*/
	public Brush (TriangleReceiver triDest) {
		this.triDest=triDest;
		defaultAttributes=new VisibleAttributes(Color.red);
	}
	
	/**
	* Private helper method to add a point and an accompanying attributes object
	* to the two vectors that are stored internally. 
	*/
	private void add (Vector3d point, VisibleAttributes attr) {
		points.addElement(point);
		attributes.addElement(attr);
	}
	
	/**
	* Create the first or a new starting point for a sequence of segments.
	*/
	public void addPoint (Vector3d point) {
		add(point,null);
	}
	
	public void addPoint (double x, double y, double z) {
		addPoint(new Vector3d(x,y,z));
	}

	/** 
	* Add a point being the position vector of a reference system. Note that the ref
	* must be local, that is, created with an initial pos of (0,0,0).
	*/
	public void addPoint (Ref ref) {
		addPoint(ref.getPos());
	}
	/**
	* Defines a drawing segment going from the end of the previous segment or
	* from a more recently defined starting point. If no points have been added,
	* [0,0,0] will be used as startingpoint for the segment. Attributes as given
	* in the constructor or latest call to setAttributes() will be used for
	* the segment.
	*/
	public void addSegment (Vector3d point) {
		if (points.size() == 0) {
			addPoint(0,0,0);
		}
		add(point, defaultAttributes);
	}
	
	public void addSegment (double x, double y, double z) {
		addSegment(new Vector3d(x,y,z));
	}
	
	/** 
	* Add a segment being the position vector of a reference system. Note that the ref
	* must be local, that is, created with an initial pos of (0,0,0).
	*/
	public void addSegment (Ref ref) {
		addSegment(ref.getPos());
	}
	/**
	* Set default color and opaqueness - used by the addSegment() methods. The value
	* for the opaqueness parameter is 0.0 for totally transparency and 1.0 for
	* totally opaque.
	*/
	public void setAttributes(Color defaultColor) {
		defaultAttributes=new VisibleAttributes(defaultColor);
	}
	
	
	/**
	* Add point giving coordinates as forward, right, up. These left-handed values
	* are converted to the right values in our right-handed system where forward is
	* positive along the x-axis, right is negative along the y-axis and up is 
	* positive along the z-axis.
	*/
	public void addFRUPoint (double forward, double right, double up) {
		addPoint(forward, -right, up);
	}
	
	/**
	* Add segment giving coordinates as forward, right, up
	*/
	public void addFRUSegment (double forward, double right, double up) {
		addSegment(forward, -right, up);
	}
	
	/**
	* Add point giving coordinates as right and up only
	*/
	public void addRUPoint (double right, double up) {
		addPoint (0.0, -right, up);
	}
	
	/**
	* Add segment giving coordinates as right and up only
	*/
	public void addRUSegment (double right, double up) {
		addSegment (0.0, -right, up);
	}
	
	/** Call this method to change what triangles are displayed. If b is true, then
	* only triangles for which the eye is on the outside, are visible. If b is false,
	* the all triangles will be displayed. Default is TRUE !!!
	*/
	public void setVisibleTrianglesOnly (boolean b) {
		this.visibleTrianglesOnly=b;
	}
	
	/** Call this method to change how triangles are generated when the brush is moved.
	* Defaults to false, creating two triangles for each rectangle. If true, both ways
	* of dividing the triangle will be used. This may be suitable when the brush twists
	* a lot from one penDown() to the next. Naturally this will make the triDest work
	* twice as hard as well...
	*/
	public void setSplitBothWays (boolean b) {
		this.splitBothWays=b;
	}
	
	/**
	* This method lifts the virtual pen of the brush, making it possible to do
	* a penDown somewhere else without generating the in-between "line".
	*/
	public void penUp() {
		prevPoints=null;
	}
	
	/**
	* Starts a new "line" or contines drawing from the previous penDown() position,
	* unless penUp() has been called inbetween.
	*/
	public void penDown(Ref ref) {
		Vector newPoints=new Vector();
		for (int i=0; i<points.size(); i++) {
			Vector3d transVector=(Vector3d) points.elementAt(i);
			newPoints.addElement(ref.translate(transVector).getPos());
		}
		if (prevPoints != null) {
			// start at 1 since the first must be a point without attributes
			for (int i=1; i<prevPoints.size(); i++) {
				VisibleAttributes attr=(VisibleAttributes) attributes.elementAt(i);
				if (attr != null) {
					Vector3d prev_a=(Vector3d) prevPoints.elementAt(i-1);
					Vector3d prev_b=(Vector3d) prevPoints.elementAt(i);
					Vector3d new_a=(Vector3d) newPoints.elementAt(i-1);
					Vector3d new_b=(Vector3d) newPoints.elementAt(i);
					generateTriangles (prev_a, prev_b, new_a, new_b, attr);
				}
			}
		}
		prevPoints=newPoints;
	}
	
	
	/**
	* Helper method to generate triangles from four points. Optimized so that if
	* visibleTrianglesOnly and the first triangle is found to be not visible, then
	* it aborts without generating triangles. If that passes, then the first two 
	* triangles are sent to the triDest. Then, if splitBothWays is set, another pair
	* of triangles are given to the triDest, splitted along the other diagonal.
	*/
	private void generateTriangles (Vector3d prev_a, Vector3d prev_b, Vector3d new_a, Vector3d new_b,
		VisibleAttributes attr) 
	{
		Triangle t=new Triangle (prev_a, prev_b, new_b, attr);
		if (visibleTrianglesOnly) {
			if (t.calcPlaneEquation(0.0, 0.0, 0.0) <= 0) {
				// eye is in or behind the plane
				return;
			}
		}
		triDest.tri(t);
		triDest.tri(new Triangle(prev_a, new_b, new_a, attr));
		if (splitBothWays) {
			triDest.tri(new Triangle(prev_a, prev_b, new_a, attr));
			triDest.tri(new Triangle(new_a, prev_b, new_b, attr));
		}
	}
	
}
