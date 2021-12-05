package rf.configtool.main.runtime.lib.ddd.viewers;

import java.awt.Color;
import java.io.File;

import rf.configtool.main.runtime.lib.RasterImage;
import rf.configtool.main.runtime.lib.ddd.core.Bounds3d;
import rf.configtool.main.runtime.lib.ddd.core.Plane3d;
import rf.configtool.main.runtime.lib.ddd.core.Ref;
import rf.configtool.main.runtime.lib.ddd.core.Triangle;
import rf.configtool.main.runtime.lib.ddd.core.Vector3d;

/**
* This is an extension of abstract class Viewer, that performs
* area rendering of models. It is not very fast but also not very much optimized,
* so it could probably be made to work faster.
* 
* (c) Roar Foshaug 2001
*/
public class AreaViewer extends Viewer {

	private Color defaultColor;

	// simple statistics
	private long triCount=0;	// all triangles that are attempted drawn
	private long triPaintedCount=0;		// triangles actually painted
	
	private final int sizex, sizey;

	// Calculated results for each pixel
	private double depth[][];	// x-depth for each point
	private Color color[][];	// base color: if null, no point exists

	private Vector3d vectors[][];	// rays

	private Vector3d lightPos=new Vector3d(0,0,0);  // light at center of film
	private double maxLightDistance=1000;	// unit unknown, may be millimetres or metres

	private boolean metallicReflection=false;

	private ViewerNotificationListener listener;

	private final int notificationCount;
	private int notificationCounter=0;
	// when it passes notificationCount, a notification is sent and the counter is reset

	/**
	* Creates an area viewer. The focal length, filmwidth and filmheight may be given in
	* millimetres, like 50 mm focal length and 35x24 mm film size. The sizex and sizey is
	* the size of the screen in pixels, and should have the same proportions as the
	* film: a square film of say 35x35 millimetres should be displayed in a window that
	* is also square, say 400x400 pixels. The defaultColor gives the color to display
	* in pixels where no triangles have yet been rendered. The last two parameters constitutes
	* a feedback mechanism to the frame showing the results. It may give itself as the
	* ViewerNotificationListener parameter, plus a notificationCount requesting to be
	* notified every time that number of triangles have been rendered. The frame object
	* may then call the draw() method of this instance to have the actual content
	* (re)drawn into a Graphics object.
	*/
	public AreaViewer (double focalLength,
		double filmWidth, double filmHeight, int sizex, int sizey, Color defaultColor,
		ViewerNotificationListener listener, int notificationCount)
	{
		super(focalLength, filmWidth, filmHeight, sizex, sizey);
		this.sizex=sizex;
		this.sizey=sizey;

		this.defaultColor=defaultColor;
		depth=new double[sizex][sizey];
		color=new Color[sizex][sizey];
		// Colors default to null, means infinite distance, no point
		vectors=new Vector3d[sizex][sizey];
		for (int y=0; y<sizey; y++) {
			for (int x=0; x<sizex; x++) {
				vectors[x][y]=calcScreenVector (x,y);
				// if (x%100==0) System.out.println("Ray["+x+","+y+"] = " + vectors[x][y]);
			}
		}
		this.listener=listener;
		this.notificationCount=notificationCount;
	}

	/**
	* Clears the viewer of content
	*/
	public void clear() {
		color=new Color[sizex][sizey];  // creates new array of null-values
	}

	/**
	* Set position of light-source relative to the eye. To set the light-source
	* up to the left: setLightPos(new Ref().up(x).right(y)). Actual numbers to use
	* depend on chosen default scale.
	*/
	public void setLightPos (Ref pos) {
		lightPos=pos.getPos();
		//System.out.println("light source positioned at " + lightPos);
	}

	/**
	* Set the intensity of the light source. Currently the intensity of the light
	* drops linearly along distance from the light. This is not strictly correct
	* according to how point-shaped light sources really behave, but since we only
	* have this one light source, it has been set to act this way to become sort of
	* a crossing between parallell light, point light and ambient light. The value
	* given is that of a point, which is as far away as the light source will have
	* any influence on objects. The actual distance that the light source reaches is
	* calculated by finding the distance from current light position to the point
	* given as arg.
	*/
	public void setLightReach (Ref somePoint) {
		Vector3d v=somePoint.getPos().sub(lightPos);
		maxLightDistance=v.length();
		//System.out.println("light influence reaches zero at a distance of " + maxLightDistance);
	}

	/**
	* This method allows the client to change the way light from the
	* light source is reflected from the objects. Default value is false.
	*/
	public void setMetallicReflection (boolean b) {
		metallicReflection=b;
	}


	private Color applyLightSources (Triangle t, Vector3d intersectionPoint) {
		Color baseColor=t.getAttributes().getColor();

//		if (t.calcPlaneEquation(lightPos) <= 0) {
//			// light hits surface from the back
//			return baseColor;
//		}

		Vector3d triNorm=t.getNormalVector();

		Vector3d lightRay=lightPos.sub(intersectionPoint);
		// must calculate absolute angle between light ray and normal-vector: if small then
		// the light ray hits the triangle right on, and has a large influence on the
		// brightness, while if angle approaches 90 degrees, the influence becomes mimimal.

		double angle=lightRay.calcAbsoluteAngleNormalized(triNorm);
		// 0.0 if parallell, 1.0 if perpendicular
		if (metallicReflection) {
			angle*=1.3;		// metallic reflection: influence drops more rapidly
		}
		if (angle > 1.0) angle=1.0;

		double maxInfluence=1.2;
		if (metallicReflection) {
			maxInfluence=2.0;	// metallic reflection: max influence is higher
		}
		double influenceFactor=1.0-angle;		// 1.0 when parallell, 0.0 when 90 degreees

		// calculate influence from light source (linear)
		double distFactor=(maxLightDistance - lightRay.length()) / maxLightDistance;
		if (distFactor < 0) distFactor=0;

		int deltaColor=(int) (255*influenceFactor*maxInfluence*distFactor);

		/* THIS SHOULD BE OPTIMIZED. The point is to give away all the deltaColor points,
		* also when one or two of the rgb-values reach 255. This is important to make the
		* brightness increase correct regardless of color
		*/
		int r=baseColor.getRed();
		int g=baseColor.getGreen();
		int b=baseColor.getBlue();
		//deltaColor *= 3;  // this is the number of color values to add to the three
		while ( (r < 255 || g < 255 || b < 255) && deltaColor > 0) {
			if (r < 255) {
				r++;
				deltaColor--;
			}
			if (g < 255) {
				g++;
				deltaColor--;
			}
			if (b < 255) {
				b++;
				deltaColor--;
			}
		}
		return new Color ((int)r,(int)g,(int)b);
	}



	private void renderTriangle (Triangle t) {
		if (triangleNotVisible(t)) return;

		Vector3d points[]=t.getPoints();
		/*
		for (int i=0; i<points.length; i++) {
		System.out.println("tri point " + i + " = " + points[i]);
		}
		*/

		Bounds2d bounds=new Bounds2d(calcScreenCoordinates(points));

		int x1=(int) bounds.getXMin();
		int x2=(int) bounds.getXMax();
		int y1=(int) bounds.getYMin();
		int y2=(int) bounds.getYMax();
		x1-=2;
		x2+=2;
		y1-=2;
		y2+=2;
		if (x1 > sizex - 1 || x2 < 0 || y1 > sizey - 1 || y2 < 0) {
			//System.out.println("OUTSIDE: Bounds: [" + x1 + "," + y1 + "] - [" + x2 + "," + y2 + "]");
			return;
		}
		if (x1 < 0) x1=0;
		if (x2 > sizex-1) x2=sizex-1;
		if (y1 < 0) y1=0;
		if (y2 > sizey-1) y2=sizey-1;

		//		System.out.println("Bounds: [" + x1 + "," + y1 + "] - [" + x2 + "," + y2 + "]");

		// If triangle is facing us that means its corners are in an anti-clockwise
		// order as seen through the screen, and consequently that the three planes
		// above are defined so that the outside is the planes has positive values for the
		// equation. Conversely, if triangle does not face us, then negative values from
		// the plane equation means a point is outside the planes.
		Plane3d triCone[]=new Plane3d[3];
		if (t.calcPlaneEquation(origoVector) > 0) {
			triCone[0]=new Plane3d(origoVector, points[0], points[1]);
			triCone[1]=new Plane3d(origoVector, points[1], points[2]);
			triCone[2]=new Plane3d(origoVector, points[2], points[0]);
		} else {
			// defines each plane with points in opposite direction
			triCone[0]=new Plane3d(origoVector, points[1], points[0]);
			triCone[1]=new Plane3d(origoVector, points[2], points[1]);
			triCone[2]=new Plane3d(origoVector, points[0], points[2]);
		}
		// Now, any point from eye to screen within the 2d-bounding box that is outside
		// any of the planes (equation > 0) is outside the triangle and will be ignored
		for (int x=x1; x<=x2; x++) {
			for (int y=y1; y<=y2; y++) {
				Vector3d vec=vectors[x][y];	// "ray" from eye through pixel
				boolean isInside=true;
				for (int i=0; i<triCone.length && isInside; i++) {
					if (triCone[i].calcPlaneEquation(vec) > 0) {
						isInside=false;
					}
				}
				if (isInside) {
					// screen point is inside cone defined by triangle
					Vector3d intersect=t.findIntersectionPoint(vec);
					if (intersect != null) {
						double depthValue=intersect.getX();
						if (depthValue > focalLength) {
							if (color[x][y]==null || depth[x][y] > depthValue) {
								depth[x][y]=depthValue;
								color[x][y]=applyLightSources(t, intersect);
							}
						} else {
							// System.out.println("focal length problem:" + depthValue);
						}
					} else {
						//System.out.println("intersect==null");
					}
				}
			}
		}

		triPaintedCount++;

		notificationCounter++;
		if (notificationCounter >= notificationCount) {
			sendViewerNotification();
		}
	}

	/**
	* Inform the listener that it is time to update the image on screen. May be called
	* automatically as a consequence of notificationCounter reaching max value, or it
	* may be called directly from the generator via the method update()
	*/
	private void sendViewerNotification () {
		if (listener != null) listener.viewerNotification();
		notificationCounter=0;
	}

	/** Display visible triangles only. Parameters MUST BE GIVEN IN COUNTER-CLOCK ORDER
	* SEEN FROM OUTSIDE, for the determinant to work.
	*/
	public void triVisible (Triangle t) {
		triCount++;
		if (t.calcPlaneEquation(origoVector) > 0) {
			renderTriangle(t);
		}
	}

	/** Display a triangle. */
	public void tri (Triangle t) {
		triCount++;
		renderTriangle(t);
	}
//
//	/** Overriding method - still calling super-version: adding checking
//	* against depth-mask: if all parts of objects will be hidden by previously
//	* drawn objects, then return true.
//	*/
//	public boolean notVisible (Bounds3d bounds) {
//		if (super.notVisible(bounds)) return true;
//		//Vector3d points[]=bounds.getPoints();
//		//double smallestDepth=points[0].getX();
//		//for (int i=1; i<points.length; i++) {
//		//	if (points[i].getX() < smallestDepth) smallestDepth=points[i].getX();
//		//}
//		double smallestDepth=bounds.getX1();
//		
//		Bounds2d bounds2d = new Bounds2d(calcScreenCoordinates(points));
//
//		int x1=(int) bounds2d.getXMin();
//		int x2=(int) bounds2d.getXMax();
//		int y1=(int) bounds2d.getYMin();
//		int y2=(int) bounds2d.getYMax();
//		x1-=1;
//		x2+=1;
//		y1-=1;
//		y2+=1;
//		if (x1 > sizex - 1 || x2 < 0 || y1 > sizey - 1 || y2 < 0) {
//			System.out.println("notVisible: OUTSIDE: Bounds: [" + x1 + "," + y1 + "] - [" + x2 + "," + y2 + "]");
//			return true;
//		}
//		if (x1 < 0) x1=0;
//		if (x2 > sizex-1) x2=sizex-1;
//		if (y1 < 0) y1=0;
//		if (y2 > sizey-1) y2=sizey-1;
//
//		// Do a fast precheck on the corners and center points of the 2d-bounds. If the
//		// object is partially visible, hopefully this test will decide.
//		int preCheckX[]={x1,x1,x2,x2};
//		int preCheckY[]={y1,y2,y1,y2};
//		for (int i=0; i<preCheckX.length; i++) {
//			if (color[preCheckX[i]][preCheckY[i]]==null) return false;
//			if (depth[preCheckX[i]][preCheckY[i]] > smallestDepth) return false;
//		}
//
//		// Now check distance info in each pixel, if any is bigger than smallestDepth, then
//		// return false. If loop completes, return true.
//		for (int x=x1; x<=x2; x++) {
//			for (int y=y1; y<=y2; y++) {
//				if (color[x][y]==null) return false;  // no object has been painted in this pixel yet
//				if (depth[x][y] > smallestDepth) return false;
//			}
//		}
//
//		// System.out.println("notVisible: HIDDEN");
//
//		// Object is definitely hidden by other objects!
//		return true;
//	}
//
//


	/**
	* Override the triangeNotVisible() method. Rejects triangles
	* if its 2d-bounds are hidden, or all its are components behind the viewscreen
	if any of its component points
	* are behind the viewscreen (focal distance).
	*/
	protected boolean triangleNotVisible (Triangle t) {
		// if all points behind view-plane, then reject
		boolean allBehind=true;

		Vector3d arr[]=t.getPoints();
		for (int i=0; i<arr.length && allBehind; i++) {
			double x=arr[i].getX();

			if (x > focalLength) allBehind=false;
		}
		if (allBehind) return true;		// not visible

		// call notVisible() to work on the bounds
		Bounds3d b=new Bounds3d(t.getPoints());
		if(notVisible(b)) return true;

		// In addition, ignore triangles with components behind the view-screen
		return false;
	}


	/**
	* Save as PNG
	*/
	public void writePNG (File file) throws Exception {
		RasterImage img=new RasterImage(sizex,sizey);
		for (int y=0; y<sizey; y++) {
			for (int x=0; x<sizex; x++) {
				Color c;
				if (color[x][y]==null) {
					c=defaultColor;
				} else {
					c=color[x][y];
				}
				img.setPixel(x,y,c);
			}
		}
		
		img.savePNG(file.getAbsolutePath());
	}


	/** Return number of rectangles and triangles processed.
	*/
	public String getStats() {
		String s="tri=" + triCount + " triPainted=" + triPaintedCount;
		return s;
	}

}

