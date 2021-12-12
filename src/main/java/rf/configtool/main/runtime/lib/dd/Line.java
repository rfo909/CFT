package rf.configtool.main.runtime.lib.dd;

import java.awt.Color;

/**
 * A line to be rendered, in global coordinates
 */
public class Line {
	private Vector2d from, to;
	private Color color;
	
	public Line(Vector2d from, Vector2d to, Color color) {
		this.from = from;
		this.to = to;
		this.color = color;
	}

	public Vector2d getFrom() {
		return from;
	}

	public Vector2d getTo() {
		return to;
	}

	public Color getColor() {
		return color;
	}

	
	
}
