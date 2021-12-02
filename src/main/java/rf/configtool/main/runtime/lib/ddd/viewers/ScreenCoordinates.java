package rf.configtool.main.runtime.lib.ddd.viewers;


/**
* Container-class, defines x and y. These are actually floating-point values,
* but may be converted to int as well.
*/
public class ScreenCoordinates {
	
	private double x, y;
	
	public ScreenCoordinates (int x, int y) {
		this.x=x;
		this.y=y;
	}
	
	public ScreenCoordinates (double x, double y) {
		this.x=x;
		this.y=y;
	}
	
	public int getIntX() {
		return (int) x;
	}
	
	public int getIntY() {
		return (int) y;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
}
