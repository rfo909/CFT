package rf.configtool.main;

import java.util.*;

/**
 * Whenever calling a script by "script:func" we can supply a list of named
 * values, via a Dict, that replaces corresponding named functions. This is
 * only available for values that can be synthesized.
 */
public class FuncOverrides {
    private Map<String,String> map;

    public FuncOverrides (Map<String,String> map) {
        this.map=map;
    }
    
    public String getFuncOverride (String name) {
        return map.get(name);
    }
}
