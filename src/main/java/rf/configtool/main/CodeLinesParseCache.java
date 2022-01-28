/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2022 Roar Foshaug

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

package rf.configtool.main;

import java.util.*;

import rf.configtool.parsetree.ProgramLine;

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
