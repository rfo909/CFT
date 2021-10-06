package rf.configtool.main.runtime.lib.db2;

import java.io.File;

public class TestLock {
	
	private final File lockFile=new File("/tmp/myLock.txt"); 

	class Runner implements Runnable {
		private int id;
		public Runner (int id) {this.id=id;}
		
		public void run() {
			for (int i=0; i<10000; i++) {
				boolean gotIt=CollectionLock.getLock(lockFile);
				if (gotIt) System.out.println("" + id);
			
				try {
					Thread.sleep(gotIt ? 100 : (int) (Math.random()*13));
				} catch (Exception ex) {
					// ignore
				}
				
				if (gotIt) CollectionLock.freeLock(lockFile);

				try {
					Thread.sleep((int) (Math.random()*13));
				} catch (Exception ex) {
					// ignore
				}
				
			}
		}
	}

	public void runTest () {
		if (lockFile.exists()) lockFile.delete();
		for (int i=0; i<100; i++) {
			new Thread(new Runner(i)).start();
		}
	}
	
	public static void main (String[] args) {
		(new TestLock()).runTest();
	}
}
