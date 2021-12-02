package rf.configtool.main.runtime.lib.ddd.viewers;

public class Bounds2d {
	
	double xmin, xmax, ymin, ymax;

	public Bounds2d(ScreenCoordinates list[]) {
		this(list[0]);
		for (int i=0; i<list.length; i++) {
			addPoint(list[i]);
		}
	}
	
	public Bounds2d(ScreenCoordinates firstPoint) {
		xmin=xmax=firstPoint.getX();
		ymin=ymax=firstPoint.getY();
	}
	
	public Bounds2d(int x, int y) {
		xmin=xmax=x;
		ymin=ymax=y;
	}
	
	public void addPoint (ScreenCoordinates point) {
		double x=point.getX();
		double y=point.getY();
		if (x < xmin) xmin=x;
		else if (x > xmax) xmax=x;
		if (y < ymin) ymin=y;
		else if (y > ymax) ymax=y;
	}
	
	public void addPoint (int x, int y) {
		if (x < xmin) xmin=x;
		else if (x > xmax) xmax=x;
		if (y < ymin) ymin=y;
		else if (y > ymax) ymax=y;
	}
	
	public void addPoints (ScreenCoordinates list[]) {
		for (int i=0; i<list.length; i++) {
			addPoint(list[i]);
		}
	}
	
	public double getWidth() {
		return xmax-xmin;
	}
	
	public double getHeight() {
		return ymax-ymin;
	}
	
	public double getXMin() {
		return xmin;
	}
	
	public double getXMax() {
		return xmax;
	}
	
	public double getYMin() {
		return ymin;
	}
	
	public double getYMax() {
		return ymax;
	}
	
}
