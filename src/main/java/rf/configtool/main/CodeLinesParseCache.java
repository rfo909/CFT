package rf.configtool.main;

import java.util.*;

import rf.configtool.data.ProgramLine;

/**
 * Caching parsed function bodies, with periodic purge, as command line inputs are
 * also considered function bodies.
 */
public class CodeLinesParseCache {
    
    public static final long timeout = 60000L; // one minute

    private Map<String,List<ProgramLine>> cache=new HashMap<String,List<ProgramLine>>();
    private Map<String,Long> used=new HashMap<String,Long>();
    
    private long lastPurge;
    
    public CodeLinesParseCache() {
        lastPurge=System.currentTimeMillis();
    }
    
    public synchronized List<ProgramLine> get (String key) {
        used.put(key, System.currentTimeMillis());
        return cache.get(key);
    }
    
    public synchronized void put (String key, List<ProgramLine> value) {
        cache.put(key, value);
        used.put(key, System.currentTimeMillis());
        
        if (System.currentTimeMillis()-lastPurge > 10000) {
            purge();
        }
        //System.out.println("#cache=" + cache.keySet().size());
    }
    
    
    private void purge() {
        List<String> toDelete=new ArrayList<String>();
        for (String key:used.keySet()) {
            long timestamp=used.get(key);
            if (System.currentTimeMillis()-timestamp > timeout) {
                toDelete.add(key);
            }
        }
        for (String key:toDelete) {
            cache.remove(key);
            used.remove(key);
        }
    }
    
    

}
