package rf.configtool.main.runtime.lib;

import java.io.*;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueNull;

public class ObjLineReader extends ObjPersistent {
    
	private ObjFile file;
	private BufferedReader br;
	private long lineNumber;
	
    public ObjLineReader(ObjFile file) {
    	this.file=file;
    	
    	init();
    	
    	this.add(new FunctionRead());
        this.add(new FunctionClose());
    }
    
    private void init() {
    	try {
    		lineNumber=1;
    		
    		if (br != null) {
    			try {
    				br.close();
    			} catch (Exception ex) {
    				// ignore
    			}
    		}
    		String encoding=file.getEncoding();
    		File f=file.getFile();
        	br = new BufferedReader(
      			   new InputStreamReader(
      	                      new FileInputStream(f), encoding));
    	} catch (Exception ex) {
    		br=null;
    	}
    }
    
    @Override 
    public String getPersistenceId() {
        return "ObjLineReader: " + file.getPath();
    }
 

    
    @Override
    public boolean eq(Obj x) {
        return x==this;
    }


    @Override
    public String getTypeName() {
        return "LogFiles";
    }
    

    @Override
    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    
    private String getDesc() {
        return "LineReader: " + file.getPath();
    }
    
    
    @Override
    public void refreshPersistentObj() {
    	// read from start
    	init();
    }
    
    @Override
    public void cleanupOnExit() {
    	try {
    		br.close();
    	} catch (Exception ex) {
    		// 
    	}
    }

    class FunctionRead extends Function {
        public String getName() {
            return "read";
        }
        public String getShortDesc() {
            return "read() - read one line";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            if (br==null) throw new Exception("No such file '" + file.getPath() + "'");
            String line=br.readLine();
            if (line==null) return new ValueNull(); // EOF
            return new ValueObjFileLine(line, lineNumber++, file);  
        }
    }
 
    class FunctionClose extends Function {
        public String getName() {
            return "close";
        }
        public String getShortDesc() {
            return "close() - close file reader";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            br.close();
            return new ValueBoolean(true);
        }
    }
    
    
    
}