package rf.configtool.main.runtime.lib.ddd.viewers;

import java.awt.Graphics;
import java.awt.Color;

/**
* Helper class used in GridViewer. Used to store calculated lines between calls to
* the draw() method. Contains two screencoordinate objects and a draw method
* that takes as argument a Graphics object with preset color settings.
*/
public class ScreenLine {
	
	private ScreenCoordinates a,b;
	private Color color;
	
	public ScreenLine (ScreenCoordinates a, ScreenCoordinates b, Color color) {
		this.a=a;
		this.b=b;
		this.color=color;
	}
	
	public void draw (Graphics g) {
		g.setColor(color);
		//System.out.println("drawLine=" + a.getIntX() + ", " + a.getIntY()
		//	+ ", " + b.getIntX() + ", " + b.getIntY());
		g.drawLine (a.getIntX(), a.getIntY(), b.getIntX(), b.getIntY());
	}
}
