package rf.configtool.main.runtime.lib.net;

import java.util.Random;

public class MyRandom {
	
	private static MyRandom inst;
	public static MyRandom getInstance() {
		if (inst==null) inst=new MyRandom();
		return inst;
	}
	
	private Random random;
	
	private MyRandom() {
		random=new Random();
		random.setSeed(System.currentTimeMillis());
	}
	
	public double getRandom (double scale) {
		double d=random.nextDouble()*scale*2.0 - scale;
		//System.out.println("d=" + d);
		return d;
	}
	
	public int getPositiveInt (int scale) {
		return random.nextInt(scale);
	}
}
