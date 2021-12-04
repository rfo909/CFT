package rf.configtool.main.runtime.lib.ddd.core;


/** Simple container-class containing common attributes of
* objects to be visualized: color
*/

public class VisibleAttributes {
	private MyColor color;

	public VisibleAttributes (MyColor color) {
		this.color=color;
	}

	public MyColor getColor() {
		return color;
	}

}
	
