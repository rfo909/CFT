package rf.configtool.main.runtime.lib;

import rf.configtool.main.runtime.Obj;

/**
 * Common super class of persistent Obj instances
 */
public abstract class ObjPersistent extends Obj {
    
    public abstract String getPersistenceId();
    
    /**
     * Called once for each object with a unique persistence-id
     */
    public void initPersistentObj() {
        // empty
    }
    public void cleanupOnExit() {
        // empty
    }

}
