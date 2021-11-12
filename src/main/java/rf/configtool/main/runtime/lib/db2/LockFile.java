package rf.configtool.main.runtime.lib.db2;

import java.io.*;

/**
 * Attempt getting a file lock, using a separate file. May return true or false. Remember to always free lock after
 * obtaining it.
 * 
 * Getting a lock means getting ones lockId as the first line of the lock file. Other contenders may append to the
 * file, but this doesn't change the first line. 
 * 
 * This makes it harmless obtaining the same lock twice by the same thread.
 * 
 *
 */
public class LockFile {
	
	public static final int OBTAIN_LOCK_TIMEOUT_MS = 10000;
	public static final String lockIdPre=""+System.currentTimeMillis();
	
	/*
	 * Tested with the (improved) TestLock class 2021-11-10 RFO
	 */
	
	private LockFile() {}
		
	private static String getLockId () {
		return lockIdPre + "_"+Runtime.getRuntime().hashCode() + "_" + Thread.currentThread().getId();
	}

	private static boolean doGetLock (File theFile) throws Exception {
	
		String lockId=getLockId();
		
		PrintStream ps=null;

		try {
			ps=new PrintStream(new FileOutputStream(theFile,true)); // append
			ps.println(lockId);
		} finally {
			if (ps != null) ps.close();
		}
		Thread.sleep(10);
		BufferedReader br=null;
		try {
			br=new BufferedReader(new FileReader(theFile));
			String line=br.readLine();
			if (line.equals(lockId)) return true;
		} finally {
			if (br != null) br.close();
		}
		
		return false;
	}

	public static boolean getLock (File theFile) {
		try {
			return doGetLock(theFile);
		} catch (Exception ex) {
			return false;
		}
	}

	public static void obtainLock (File theFile) throws Exception {
		obtainLock(theFile, OBTAIN_LOCK_TIMEOUT_MS);
	}
	
	public static void obtainLock (File theFile, long timeoutMillis) throws Exception {
		long startTime=System.currentTimeMillis();
		int retries=0;
		for (;;) {
			if (getLock(theFile)) {
				//System.out.println("retries=" + retries);
				
				return;
			}
			
			retries++;

			
			if (System.currentTimeMillis()-startTime > timeoutMillis) break;
			
			try {
				Thread.sleep((int) (Math.random()*10) + 1);
			} catch (Exception ex) {
				// ignore
			}
		}
		throw new Exception("Could not obtain lock " + theFile.getAbsolutePath() + " for " + timeoutMillis + " millis");
	}

	
	
	public static void freeLock (File theFile) throws Exception {
		String lockId=getLockId();
		
		// verify we have the lock
		boolean verifyOk=false;
		try {
			BufferedReader br=null;
			try {
				br=new BufferedReader(new FileReader(theFile));
				String line=br.readLine();
				if (line != null && line.equals(lockId)) verifyOk=true;
			} finally {
				br.close();
			}
			// all ok
			if (verifyOk) {
				theFile.delete();
				return;
			}
		} catch (Exception ex) {
		}
		throw new Exception("Could not free lock " + theFile.getAbsolutePath() + " - not owner");
	}

}
