package rf.configtool.main.runtime.lib.db2;

import java.util.*;
import java.io.*;

public class Collection {
    
    private FileInfo fileInfo;
    
    private Map<String,String> data;
    

    public Collection (FileInfo fileInfo) {
        this.fileInfo=fileInfo;
        if (fileInfo==null) throw new RuntimeException("fileInfo==null");
    }
    
    public synchronized void addData (String key, String value) throws Exception {
        if (key.contains("\r") || key.contains("\n") || key.contains(" ")) throw new Exception("Invalid key: \\r\\n + space forbidden");
        if (value.contains("\r") || value.contains("\n")) throw new Exception("Invalid value: \\r\\n forbidden");

        // Can NOT do checkForUpdate here, since it will severely degrade write performance
        File f=fileInfo.getFile();
        PrintStream ps = new PrintStream (new FileOutputStream(f, true));
        try {
            ps.println(key + " " + value);
        } finally {
            if (ps != null) try {ps.close();} catch (Exception ex) {};
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
        data=new HashMap<String,String>();
        File f=fileInfo.getFile();
        if (!f.exists()) return;

        BufferedReader br=new BufferedReader(new FileReader(f));
        try {
            for (;;) {
                String line=br.readLine();
                if (line==null) break;
                if (line.length()==0) continue;
                int pos=line.indexOf(' ');
                data.put(line.substring(0,pos), line.substring(pos+1));
            }
        } finally {
            if (br != null) try {br.close();} catch (Exception ex) {};
        }
    }


}
