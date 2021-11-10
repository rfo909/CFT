package rf.configtool.main.runtime.lib.db2;

import java.io.File;

public class TestLock {
	
	private final File lockFile=new File("/tmp/myLock.txt"); 
	
	private int currHolder=-1;
	private synchronized void setHolder (int holder) {
		if (currHolder != -1) throw new RuntimeException("Conflict - two holders");
		//System.out.println("holder ok");
		currHolder=holder;
	}
	private synchronized void clearHolder (int holder) {
		if (currHolder != holder) throw new RuntimeException("Conflict - two holders");
		currHolder=-1;
	}

	class Runner implements Runnable {
		private int id;
		public Runner (int id) {this.id=id;}
		
		public void run() {
			try {
				for (int i=0; i<1000; i++) {
					long start=System.currentTimeMillis();
					LockFile.obtainLock(lockFile);
					long delay=System.currentTimeMillis() - start;
					System.out.println("" + id + " delay=" + delay);
				
					setHolder(id);
					try {
						Thread.sleep(20);
					} catch (Exception ex) {
						// ignore
					}
					clearHolder(id);
					
					LockFile.freeLock(lockFile);
	
					try {
						Thread.sleep((int) (Math.random()*100));
					} catch (Exception ex) {
						// ignore
					}
					
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public void runTest () {
		if (lockFile.exists()) lockFile.delete();
		for (int i=0; i<10; i++) {
			new Thread(new Runner(i)).start();
		}
	}
	
	public static void main (String[] args) {
		(new TestLock()).runTest();
	}
}
