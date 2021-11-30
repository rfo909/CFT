package rf.configtool.main.runtime.lib.db;

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
	
	public static final long LOCK_HARD_EXPIRATION_MS = 10000L;
		// if first line of a lock file has time stamp older than this, that otherwise valid lock line is considered expired
	
	/*
	 * Tested with the (improved) TestLock class 2021-11-10 RFO
	 */
	
	private LockFile() {}
		
	private static String getLockId () {
		return "" + Runtime.getRuntime().hashCode() + "XX" + Thread.currentThread().getId();
	}
	
	
	private static String getCompleteLockLine() {
		String str=""+System.currentTimeMillis() + "_" + getLockId();
		// encoding as LEN:str  - to combat parallel writes, see isValidLockId called from ownsLock() method
		return ""+str.length()+":"+str;
	}
	
	
	/**
	 * True if s is a valid line for owning the lock, false if not. This check is important also as multiple writes in
	 * parallel may produce a first line that is a sum of more than one write. The Length:data syntax is aimed at
	 * detecting invalid lock id's
	 */
	private static boolean isValidLockId (String s) {
		try {
			// verify syntax N:data
			int pos=s.indexOf(':');
			int len=Integer.parseInt(s.substring(0,pos));
			String data=s.substring(pos+1);
			if (len != data.length()) {
				// simply bad format
				return false;
			}

			// separate time stamp from data
			int uscore=data.indexOf('_');
			long time=Long.parseLong(data.substring(0,uscore));
			
			// lock line is valid, but may have timed out
			if (System.currentTimeMillis() - time > LOCK_HARD_EXPIRATION_MS) {
				System.out.println("" + System.currentTimeMillis() + " Lock line " + s + " EXPIRED");
				return false;
			}
			
			// 
			return true;
		} catch (Exception ex) {
			return false;
		}
	}
	
	
	// Lock status
	public static final int ST_NOT_OWNER = 1;
	public static final int ST_IS_OWNER = 2;
	public static final int ST_RETRY = 3;
	
	
	private static int ownsLock (File theFile) throws Exception {
		BufferedReader br=null;
		String line=null;
		try {
			br=new BufferedReader(new FileReader(theFile));
			line=br.readLine();
		} finally {
			if (br != null) try {br.close();} catch (Exception ex) {};
		}

		if (line==null) return ST_NOT_OWNER;
		
		if (isValidLockId(line)) {
			// get lockId from line
			int pos=line.indexOf('_');
			String lockId=line.substring(pos+1);
			if (lockId.equals(getLockId())) {
				return ST_IS_OWNER;
			} else {
				return ST_NOT_OWNER;
			}
		} else {
			// The first line is (no longer) valid, so currently nobody owns this lock.
			
			// This means we perform a regular write to it, truncating it, then return
			// status RETRY, after a short delay, which means the caller will try again
			// immediately (getLock() method).
			

			PrintStream ps=null;
			try {
				ps=new PrintStream(new FileOutputStream(theFile)); // truncate 
				ps.println(getCompleteLockLine());
			} catch (Exception ex) {
				if (ps != null) try{ps.close();} catch(Exception ex2) {};
			}

			// As someone else may have found out this same thing at the same time, we wait a bit,
			// ensuring nobody else will continue deciding to truncate the file, but go back to
			// appending, as usual, making the lock stabilize (first line)
			Thread.sleep(10);
			
			return ST_RETRY;
		}

	}
	
	
	private static int doGetLock (File theFile) throws Exception {
	
		PrintStream ps=null;

		// append line on format N:data, where data in turn is milliseconds_ThreadLockId
		try {
			ps=new PrintStream(new FileOutputStream(theFile,true)); // append
			ps.println(getCompleteLockLine());
		} finally {
			if (ps != null) ps.close();
		}
		
		// return status code from ownsLock()
		return ownsLock(theFile);
	}

	
	public static boolean getLock (File theFile) {
		
		try {
			int status=doGetLock(theFile);
			if (status==ST_RETRY) {
				// immediate retry - delay was done inside doGetLock()
				return doGetLock(theFile)==ST_IS_OWNER;
			} else {
				return status==ST_IS_OWNER;
			}
		} catch (Exception ex) {
			return false;
		}
	}

	/**
	 * desc is for exception if failing to obtain lock, uses default OBTAIN_LOCK_TIMEOUT_MS timeout
	 */
	public static void obtainLock (File theFile, String desc) throws Exception {
		obtainLock(theFile, desc, OBTAIN_LOCK_TIMEOUT_MS);
	}
	
	/**
	 * desc is for exception if failing to obtain lock within timeout
	 */
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

	
	/**
	 * desc is for exception if failing not owning lock
	 */
	public static void freeLock (File theFile, String desc) throws Exception {
		if (ownsLock(theFile) != ST_IS_OWNER) throw new Exception("Could not free lock '" + desc + "' - not owner");
		theFile.delete();
	}

}
