package rf.configtool.main.runtime.lib.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;

/**
 * The lock file didn't work very well. It got stuck on Windows, for unknown reason.
 * 
 * So created a simple network daemon, the LockServer, and routed methods in this
 * class to it. 
 * 
 * The idea is that whoever gets to create a server listening on a particular port
 * number, starts a server that serially processes requests, and maintains
 * lock states in memory.
 * 
 * If the CFT process owning the daemon is terminated, another will create the server,
 * but lose the data. Still, good enough while waiting for the VGY database?
 * 
 * 
 *
 */
public class LockFile {
	
	private static LockServer lockServer=new LockServer();
	
	private LockFile() {}
		
	public static boolean getLock (File theFile) {
		try {
			return lockServer.getLock(theFile.getCanonicalPath());
		} catch (Exception ex) {
			return false;
		}
	}

	/**
	 * desc is for exception if failing to obtain lock, uses default OBTAIN_LOCK_TIMEOUT_MS timeout
	 */
	public static void obtainLock (File theFile, String desc) throws Exception {
		obtainLock(theFile, desc, 10000);
	}
	
	/**
	 * desc is for exception if failing to obtain lock within timeout
	 */
	public static void obtainLock (File theFile, String desc, long timeoutMillis) throws Exception {
		boolean ok = lockServer.obtainLock(theFile.getCanonicalPath(),timeoutMillis);
		if (!ok) throw new Exception(desc + " could not get lock");
	}

	
	/**
	 * desc is for exception if failing not owning lock
	 */
	public static void freeLock (File theFile, String desc) throws Exception {
		lockServer.releaseLock(theFile.getCanonicalPath());
	}

}
