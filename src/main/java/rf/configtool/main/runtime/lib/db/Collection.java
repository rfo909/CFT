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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import rf.configtool.root.LockManager;

public class Collection {
    
    private FileInfo fileInfo;
    
    private Map<String,String> data;
    
    public Collection (FileInfo fileInfo) {
        this.fileInfo=fileInfo;
        if (fileInfo==null) throw new RuntimeException("fileInfo==null");
    }

    private String lockName() throws Exception {
        return "Collection|" + fileInfo.getFile().getCanonicalPath();
    }
    
    private void getLock() throws Exception {
        LockManager.obtainLock(lockName());
        //getLockTime=System.currentTimeMillis();
    }
    
    private void freeLock () throws Exception {
        LockManager.freeLock(lockName());
        //System.out.println("freeLock '" + getCollectionName() + "' held it for " + (System.currentTimeMillis()-getLockTime) + " ms");
    }
    
    public synchronized void addData (String key, String value) throws Exception {
        if (key.contains("\r") || key.contains("\n") || key.contains(" ")) throw new Exception("Invalid key: \\r\\n + space forbidden");
        if (value.contains("\r") || value.contains("\n")) throw new Exception("Invalid value: \\r\\n forbidden");

        // Can NOT do checkForUpdate here, since it will severely degrade write performance
        getLock();
        try {
            File f=fileInfo.getFile();
            PrintStream ps = new PrintStream (new FileOutputStream(f, true));
            try {
                ps.println(key + " " + value);
            } finally {
                if (ps != null) try {ps.close();} catch (Exception ex) {};
            }
        } finally {
            freeLock();
        }
    }
    
    public synchronized String getData (String key) throws Exception {
        checkForUpdate();
        return data.get(key);
    }
    
    public synchronized List<String> getKeys() throws Exception {
        checkForUpdate();
        List<String> list=new ArrayList<String>();
        Iterator<String> keys = data.keySet().iterator();
        while (keys.hasNext()) list.add(keys.next());
        return list;
    }

    public synchronized void deleteCollection() throws Exception {
        fileInfo.deleteFile();
        data=null;
    }
    
    private void checkForUpdate() throws Exception {
        if (data==null || fileInfo.isChanged()) {
            fileInfo.sync();
            readFromFile();
        }
    }
    
    private void readFromFile () throws Exception {
        getLock();
        try {
            data=new HashMap<String,String>();
            File f=fileInfo.getFile();
            if (!f.exists()) return;
            
            int dataLinesRead=0;
    
            BufferedReader br=new BufferedReader(new FileReader(f));
            try {
                for (;;) {
                    String line=br.readLine();
                    if (line==null) break;
                    if (line.length()==0) continue;
                    
                    dataLinesRead++;
                    int pos=line.indexOf(' ');
                    data.put(line.substring(0,pos), line.substring(pos+1));
                }
            } finally {
                if (br != null) try {br.close();} catch (Exception ex) {};
            }
            
            int diff=dataLinesRead - data.keySet().size();
            //System.out.println("#### Collection " + f.getName() + " diff=" + diff);
            if (dataLinesRead > data.keySet().size()*2 && diff > 5) {
                //System.out.println("Compacting collection " + f.getName() + " " + dataLinesRead + " -> " + data.keySet().size());
                
                // Note: can NOT call addData() as it uses the lock too, and nested calls
                // obtaining the lock WILL fail

                PrintStream ps = null;
                try {
                    ps = new PrintStream (new FileOutputStream(f, false));  // no append
                    for (String key:data.keySet()) {
                        String value=data.get(key);
                        ps.println(key + " " + value);
                    }
                } finally {
                    if (ps != null) try {ps.close();} catch (Exception ex) {};
                }
                
            }
        } finally {
            freeLock();
        }
    }


}
