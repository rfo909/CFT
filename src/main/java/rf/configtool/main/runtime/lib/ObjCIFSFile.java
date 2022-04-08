package rf.configtool.main.runtime.lib;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import jcifs.smb.SmbFile;
import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.*;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.util.CIFS;
import rf.configtool.util.TabUtil;

public class ObjCIFSFile extends Obj {
	
	private String url;
	private SmbFile smbFile;

    public ObjCIFSFile (String url) throws Exception {
    	this.url=url;
    	this.smbFile=new SmbFile(url);
    	
    	this.add(new FunctionRead());
    	this.add(new FunctionLength());
    }
    
    private ObjCIFSFile self() {
        return this;
    }
    
    @Override
    public boolean eq(Obj x) {
        return false;
    }

    
    @Override
    public String getTypeName() {
        return "CIFSFile";
    }
    
    @Override
    public ColList getContentDescription() {
        return ColList.list().regular("CIFSFile");
    }
    
    private BufferedReader getBufferedReader() throws Exception {
    	String encoding="ISO-8859-1";
    	InputStream in=CIFS.getInputStream(url);
    	return new BufferedReader(new InputStreamReader(in, encoding));
    }
   
    class FunctionRead extends Function {
        public String getName() {
            return "read";
        }
        public String getShortDesc() {
            return "read() - return list of lines";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            List<Value> result=new ArrayList<Value>();
            BufferedReader br=null;
            long lineNo=0;
            try {
                
                br = getBufferedReader();
                for (;;) {
                    String line=br.readLine();
                    lineNo++;
                    if (line==null) break;
                    
                    String deTabbed=TabUtil.substituteTabs(line,4);
                    result.add(new ValueString(deTabbed));  
                }
            } finally {
                if (br != null) try {br.close();} catch (Exception ex) {};
            }
            return new ValueList(result);
        }
    }

    
    class FunctionLength extends Function {
        public String getName() {
            return "length";
        }
        public String getShortDesc() {
            return "length() - return length of file as int";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueInt(smbFile.length());
        }
    }

}
