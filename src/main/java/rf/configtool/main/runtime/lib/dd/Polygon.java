package rf.configtool.main.runtime.lib.dd;

import java.util.*;
import java.awt.Color;

/**
 * A polygon to be rendered, in global coordinates
 */
public class Polygon {
	private List<Vector2d> points;
	private Color color;
	private boolean linesOnly=false;
	
	public Polygon (List<Vector2d> points, Color color) {
		this.points=points;
		this.color = color;
	}
	
	public void setLinesOnly() {
		this.linesOnly=true;
	}

	
	public List<Vector2d> getPoints() {
		return points;
	}

	public Color getColor() {
		return color;
	}
	
	public boolean getLinesOnly() {
		return linesOnly;
	}

	public List<Line> getLines() {
		List<Line> lines=new ArrayList<Line>();
		for (int i=0; i<points.size()-1; i++) {
			lines.add(new Line(points.get(i), points.get(i+1), color));
		}
		return lines;
	}
	
	
}
