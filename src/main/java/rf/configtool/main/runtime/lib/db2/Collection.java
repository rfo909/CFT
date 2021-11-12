package rf.configtool.main.runtime.lib.db2;

import java.util.*;
import java.io.*;

public class Collection {
    
    private FileInfo fileInfo;
    private File lockFile;
    
    private Map<String,String> data;
    
    private String collectionName() {
    	return fileInfo.getFile().getName();
    }

    private void getLock() throws Exception {
    	LockFile.obtainLock(lockFile, collectionName());
    }
    
    private void freeLock () throws Exception {
    	LockFile.freeLock(lockFile, collectionName());
    }

    public Collection (FileInfo fileInfo) {
        this.fileInfo=fileInfo;
        if (fileInfo==null) throw new RuntimeException("fileInfo==null");
        lockFile=new File(fileInfo.getFile().getPath()+".lock");
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
