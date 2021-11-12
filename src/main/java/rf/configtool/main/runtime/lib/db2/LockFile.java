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
	public static final String lockIdPre=""+System.currentTimeMillis() + "_"+Runtime.getRuntime().hashCode();
	
	/*
	 * Tested with the (improved) TestLock class 2021-11-10 RFO
	 */
	
	private LockFile() {}
		
	private static String getLockId () {
		String str=lockIdPre + "_" + Thread.currentThread().getId();
		// LEN:str  - to combat parallel writes, see isValidLockId called from ownsLock() method
		return ""+str.length()+":"+str;
	}
	
	private static boolean isValidLockId (String s) {
		try {
			int pos=s.indexOf(':');
			int len=Integer.parseInt(s.substring(0,pos));
			return len==(s.substring(pos+1).length());
		} catch (Exception ex) {
			return false;
		}
	}

	private static boolean ownsLock (File theFile) throws Exception {
		BufferedReader br=null;
		try {
			br=new BufferedReader(new FileReader(theFile));
			for (;;) {
				String line=br.readLine();
				if (line==null) return false;
				if (isValidLockId(line)) {
					return line.equals(getLockId());
					
				}
			}
		} catch (Exception ex) {
			return false;
		} finally {
			if (br != null) try {br.close();} catch (Exception ex) {};
		}
	}
	
	
	private static boolean doGetLock (File theFile) throws Exception {
	
		PrintStream ps=null;

		try {
			ps=new PrintStream(new FileOutputStream(theFile,true)); // append
			ps.println(getLockId());
		} finally {
			if (ps != null) ps.close();
		}
		return ownsLock(theFile);
	}

	
	// Utility methods
	
	public static boolean getLock (File theFile) {
		try {
			return doGetLock(theFile);
		} catch (Exception ex) {
			return false;
		}
	}

	public static void obtainLock (File theFile, String desc) throws Exception {
		obtainLock(theFile, desc, OBTAIN_LOCK_TIMEOUT_MS);
	}
	
	public static void obtainLock (File theFile, String desc, long timeoutMillis) throws Exception {
		long startTime=System.currentTimeMillis();

		for (;;) {
			if (getLock(theFile)) {
				return;
			}
					
			if (System.currentTimeMillis()-startTime > timeoutMillis) break;
			
			Thread.sleep((int) (Math.random()*8) + 3);
		}
		throw new Exception("Could not obtain lock '" + desc + "' for " + timeoutMillis + " millis");
	}

	
	
	public static void freeLock (File theFile, String desc) throws Exception {
		if (!ownsLock(theFile)) throw new Exception("Could not free lock '" + desc + "' - not owner");
		theFile.delete();
	}

}
