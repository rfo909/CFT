package rf.configtool.main.runtime.lib.ddd.core;

public class MyColor {
	
	public static final MyColor BLACK = new MyColor(0,0,0);

	private int red;
	private int green;
	private int blue;
	
	public MyColor(int red, int green, int blue) {
		super();
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	public int getRed() {
		return red;
	}

	public int getGreen() {
		return green;
	}

	public int getBlue() {
		return blue;
	}
	
	
}
