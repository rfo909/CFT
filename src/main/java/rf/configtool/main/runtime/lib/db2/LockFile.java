package rf.configtool.main.runtime.lib.db2;

import java.io.*;

/**
 * Attempt getting a file lock, using a separate file. May return true or false. Remember to always free lock after
 * obtaining it.
 * 
 * Also remember that obtaining lock for a certain file MUST NEVER BE NESTED, as these WILL FAIL.
 *
 */
public class LockFile {
	
	public static final int OBTAIN_LOCK_TIMEOUT_MS = 10000;
	
	/*
	 * Tested with the TestLock class 2021-10-06 RFO
	 */
	
	private LockFile() {}
		
	private static String getLockId () {
		return ""+Runtime.getRuntime().hashCode() + "_" + Thread.currentThread().getId();
	}
	
	private static boolean doGetLock (File theFile) throws Exception {
	
		String lockId=getLockId();
		
		PrintStream ps=null;
		try {
			// assuming parallel writes to same file will throw exception for all but the first,
			// or in other words, that we don't mess up the write of the initial process
			// writing to the file.
			ps=new PrintStream(new FileOutputStream(theFile,true)); // append
			ps.println(lockId);
		} finally {
			if (ps != null) ps.close();
		}
		BufferedReader br=null;
		try {
			br=new BufferedReader(new FileReader(theFile));  
			String line=br.readLine();		// first line only
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
		for (;;) {
			if (getLock(theFile)) return;
			
			if (System.currentTimeMillis()-startTime > timeoutMillis) break;
			
			try {
				Thread.sleep((int) (Math.random()*4) + 1);
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
		String line=br.readLine(); // single line
		br.close();
		if (!line.equals(lockId)) throw new Exception("PANIC: freeLock: not owner of lock");
		theFile.delete();
	}

}
