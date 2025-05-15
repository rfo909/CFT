
package rf.xlang.main.runtime;

/**
 * Superclass of all functions inside Obj (java objects of the language)
 * 
 */
import java.util.List;

import rf.xlang.main.Ctx;

public abstract class Function {
    
    public abstract String getName();
    public abstract String getShortDesc();
    
    public abstract Value callFunction (Ctx ctx, List<Value> params) throws Exception;
    
}
