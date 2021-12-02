package rf.configtool.main.runtime.lib.ddd.viewers;

import java.awt.Graphics;
import java.awt.Color;
import java.util.Vector;

import rf.configtool.main.runtime.lib.ddd.core.*;

/**
* This is an extension of abstract class Viewer that does grid viewing
* of models. It is fast and fairly simple.
*/
public class GridViewer extends Viewer {

	// simple statistics
	private long triCount=0;	// all triangles that are attempted drawn
	private long triPaintedCount=0;		// triangles actually painted

	private Vector lines=new Vector();	// list of ScreenLine objects


	public GridViewer (double focalLength,
		double filmWidth, double filmHeight, int sizex, int sizey)
	{
		super(focalLength, filmWidth, filmHeight, sizex, sizey);
	}

	public void clear() {
		lines=new Vector();
	}

	public void update() {
	}


	private void line (Vector3d a, Vector3d b, Color color) {
		ScreenCoordinates sca = calcScreenCoordinates (a);
		if (sca==null) return;
		// It seems like the line-drawing method in Graphics chokes in cases where
		// the pixel-positions are extremely much outside the viewing area. Currently
		// we only allow lines that extends one screen-size in each direction to be
		// drawn. This is a limitation for close or very big objects only, and prevents
		// the occasional lockup that was experienced earlier (with coordinates
		// like x=-86000 and y=-3000)...
		//
		if (sca.getIntX() < -sizex || sca.getIntX() > sizex*2) return;
		if (sca.getIntY() < -sizey || sca.getIntY() > sizey*2) return;

		ScreenCoordinates scb = calcScreenCoordinates (b);
		if (scb==null) return;
		if (scb.getIntX() < -sizex || scb.getIntX() > sizex*2) return;
		if (scb.getIntY() < -sizey || scb.getIntY() > sizey*2) return;

		lines.addElement(new ScreenLine(sca,scb, color));
	}

	private void renderTriangle (Triangle t) {
		if (triangleNotVisible(t)) return;
		Vector3d arr[]=t.getPoints();
		line (arr[0],arr[1], t.getAttributes().getColor());
		line (arr[1],arr[2], t.getAttributes().getColor());
		line (arr[2],arr[0], t.getAttributes().getColor());
		triPaintedCount++;
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

	/**
	* Override the triangeNotVisible() method. Includes call to the super-method,
	* but adds functionality to reject triangles if any of its component points
	* are behind the viewscreen (focal distance).
	*/
	protected boolean triangleNotVisible (Triangle t) {
		if (super.triangleNotVisible(t)) return true;
		// In addition, ignore triangles with components behind the view-screen
		Vector3d arr[]=t.getPoints();
		for (int i=0; i<arr.length; i++) {
			double x=arr[i].getX();
			if (x <= focalLength) return true;
		}
		return false;
	}


	/** Draws lines since last draw() */
	public void draw (Graphics g) {
		for (int i=0; i<lines.size(); i++) {
			ScreenLine line=(ScreenLine) lines.elementAt(i);
			line.draw(g);
		}
		lines=new Vector();
	}

	/** Return number of rectangles and triangles processed.
	*/
	public String getStats() {
		String s="tri=" + triCount + " triPainted=" + triPaintedCount;
		triCount=0;
		triPaintedCount=0;
		return s;
	}

}

