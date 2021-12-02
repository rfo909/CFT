package rf.configtool.main.runtime.lib.ddd.core;

import java.awt.Color;

/** Simple container-class containing common attributes of
* objects to be visualized: color
*/

public class VisibleAttributes {
	private Color color;

	public VisibleAttributes (Color color) {
		this.color=color;
	}

	public Color getColor() {
		return color;
	}

}
	
