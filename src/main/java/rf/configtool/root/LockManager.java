/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2024 Roar Foshaug

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 3 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>
*/

package rf.configtool.root;

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
public class LockManager {
    
    private static LockServer lockServer=new LockServer();
    
    private LockManager() {}
    
    public static void setShuttingDown() {
        lockServer.setShuttingDown();
    }
    
    public static boolean getLock (String name) {
        try {
            return lockServer.getLock(name);
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * desc is for exception if failing to obtain lock, uses default OBTAIN_LOCK_TIMEOUT_MS timeout
     */
    public static void obtainLock (String name) throws Exception {
        obtainLock(name, 10000);
    }
    
    /**
     * desc is for exception if failing to obtain lock within timeout
     */
    public static void obtainLock (String name, long timeoutMillis) throws Exception {
        boolean ok = lockServer.obtainLock(name, timeoutMillis);
        if (!ok) throw new Exception("Could not get lock: " + name);
    }

    
    /**
     * desc is for exception if failing not owning lock
     */
    public static void freeLock (String name) throws Exception {
        lockServer.releaseLock(name);
    }

}
