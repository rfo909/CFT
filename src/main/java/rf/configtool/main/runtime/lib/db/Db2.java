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

package rf.configtool.main.runtime.lib.db;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rf.configtool.main.PropsFile;

/**
 * A CFT object storage is a map of strings, which represent code, synthesized from data. 
 */
public class Db2 {
    
    private static Db2 inst;
    
    public synchronized static Db2 getInstance() {
        if (inst==null) inst=new Db2();
        return inst;
    }
    
    
    private String db2Dir;
    
    private Db2() {
        try {
            this.db2Dir = (new PropsFile()).getDb2Dir();
            File f=new File(this.db2Dir);
            f.mkdirs();
        } catch (Exception ex) {
            this.db2Dir="Db2";
        }
    }
    
    private static final String PREFIX="LibDb2_";
    private static final String ENDING=".txt";
    
    // Collection data
    private Map<String,Collection> collections=new HashMap<String,Collection>();
    

    private File getCollectionFile (String collection) {
        return new File(db2Dir+File.separator+PREFIX+collection+ENDING);
    }
    
    private synchronized Collection getCollection (String collection) throws Exception {
        Collection x=collections.get(collection);
        if (x==null) {
            x=new Collection(new FileInfo(getCollectionFile(collection)));
            collections.put(collection, x);
        }
        return x;
    }
    
    public synchronized void set (String collection, String key, String value) throws Exception {
        if (collection.contains("\r") || collection.contains("\n") || collection.contains(" ")) throw new Exception("Invalid collection name: \\r\\n + space forbidden");
        
        getCollection(collection).addData(key, value);
    }
    
    public synchronized String get (String collection, String key) throws Exception {
        return getCollection(collection).getData(key);
    }

    public synchronized List<String> getKeys (String collection) throws Exception {
        return getCollection(collection).getKeys();
    }
    
    
    public synchronized List<String> getCollections() throws Exception {
        List<String> result = new ArrayList<String>();
        for (File f: (new File(db2Dir)).listFiles()) {
            String name=f.getName();
            if (f.isFile() && name.startsWith(PREFIX) && name.endsWith(ENDING)) {
                name=name.substring(PREFIX.length(),name.length()-ENDING.length());
                result.add(name);
            }
        }
        return result;
    }
    
    public synchronized void deleteCollection(String collection) throws Exception {
        getCollection(collection).deleteCollection();
    }



}
