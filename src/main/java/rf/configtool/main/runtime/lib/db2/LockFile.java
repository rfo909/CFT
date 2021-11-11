package rf.configtool.main.runtime.lib.db2;

import java.io.*;

/**
 * Attempt getting a file lock, using a separate file. May return true or false. Remember to always free lock after
 * obtaining it.
 * 
 * Also remember that obtaining lock for a certain file MUST NEVER BE NESTED, as these WILL FAIL.
 * 
 * (1) if lock file exists, return false
 * (2) otherwise append to file, writing local lockId, which is supposed to be unique per runtime + thread
 * (3) wait a short while
 * (4) read last line from file
 * (5) if it matches lockId, return true, otherwise false
 * 
 * The key here is that the file may be created by someone else between steps 1 and 2. The wait in step 3 is for the case
 * when someone else appends to the file between steps 2 and 4, having found the file not to exist in 1.
 *
 */
public class LockFile {
	
	public static final int OBTAIN_LOCK_TIMEOUT_MS = 10000;
	
	/*
	 * Tested with the (improved) TestLock class 2021-11-10 RFO
	 */
	
	private LockFile() {}
		
	private static String getLockId () {
		return ""+Runtime.getRuntime().hashCode() + "_" + Thread.currentThread().getId();
	}
	
	private static boolean doGetLock (File theFile) throws Exception {
	
		String lockId=getLockId();
		
		PrintStream ps=null;
		if (theFile.exists()) return false;
		
		try {
			ps=new PrintStream(new FileOutputStream(theFile,true)); // append
			ps.println(lockId);
		} finally {
			if (ps != null) ps.close();
		}
		Thread.sleep(20);
		BufferedReader br=null;
		try {
			br=new BufferedReader(new FileReader(theFile));
			String lastLine="";
			for (;;) {
				String line=br.readLine();
				if (line == null) break;
				lastLine=line;
			}
			if (lastLine.equals(lockId)) return true;
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
		for (;;) {
			if (getLock(theFile)) return;
			
			if (System.currentTimeMillis()-startTime > timeoutMillis) break;
			
			try {
				Thread.sleep((int) (Math.random()*10) + 1);
			} catch (Exception ex) {
				// ignore
			}
		}
		throw new Exception("Could not obtain lock " + theFile.getAbsolutePath());
	}

	
	
	public static void freeLock (File theFile) throws Exception {
		// verify we still have the lock
		BufferedReader br=null;
		String lockId=getLockId();
		
		br=new BufferedReader(new FileReader(theFile));
		String lastLine="";
		for (;;) {
			String line=br.readLine();
			if (line == null) break;
			lastLine=line;
		}
		br.close();
		if (!lastLine.equals(lockId)) throw new Exception("PANIC: freeLock: not owner of lock");
		theFile.delete();
	}

}
