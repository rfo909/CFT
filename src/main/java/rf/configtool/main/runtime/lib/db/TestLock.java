package rf.configtool.main.runtime.lib.db;

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

	final int NUM_THREADS = 20;
	
	/**
	 * The current test is purposely initiated with too many threads holding the lock too long. The result
	 * is that threads will terminate with exceptions ("Could not obtain lock"), as they fail getting the lock. 
	 * This is ok, as long as the rest of the threads continue getting the lock, in other words that
	 * failure to obtain the lock doesn't corrupt the lock file for others.
	 * 
	 * Typically boils down to about 3-5 threads, which is okay, as long as they continue running!
	 *
	 */
	class Runner implements Runnable {
		private int id;
		public Runner (int id) {this.id=id;}
		
		public void run() {
			try {
				for (int i=0; i<200; i++) {
					long start=System.currentTimeMillis();
					LockFile.obtainLock(lockFile, "testlock");
					long obtainDelay=System.currentTimeMillis() - start;
					System.out.println(""+System.currentTimeMillis() + " Job " + id + " obtainDelay=" + obtainDelay + " i=" + i);
				
					setHolder(id);
					try {
						Thread.sleep((int) (Math.random()*100+10) );
					} catch (Exception ex) {
						// ignore
					}
					clearHolder(id);
					
					LockFile.freeLock(lockFile, "testlock");
	
					try {
						Thread.sleep((int) (Math.random()*100));
					} catch (Exception ex) {
						// ignore
					}
					
				}
				System.out.println(""+System.currentTimeMillis() + " JOB " + id + " Terminating Normally");
			} catch (Exception ex) {
				System.out.println(""+System.currentTimeMillis() + " JOB " + id + " failing");
				ex.printStackTrace();
			}
		}
	}

	public void runTest () {
		if (lockFile.exists()) lockFile.delete();
		for (int i=0; i<NUM_THREADS; i++) {
			new Thread(new Runner(i)).start();
		}
	}
	
	public static void main (String[] args) {
		(new TestLock()).runTest();
	}
}
