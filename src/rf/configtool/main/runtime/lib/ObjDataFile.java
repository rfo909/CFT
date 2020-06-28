package rf.configtool.main.runtime.lib;

import java.util.*;
import java.io.*;

import rf.configtool.main.Ctx;
import rf.configtool.main.OutText;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;

public class ObjDataFile extends Obj {
    
    private ObjFile file;
    private String prefix;
    private String comment;
    
    public ObjDataFile (ObjFile file, String prefix) throws Exception {
        this.file=file;
        this.prefix=prefix;
        
        add(new FunctionGet());
        add(new FunctionGetAll());
        add(new FunctionComment());
    }
    
    protected Obj self() {
        return this;
    }
    
    @Override
    public boolean eq(Obj x) {
        return x==this;
    }

    @Override
    public String getTypeName() {
        return "DataFile";
    }
    
    @Override
    public ColList getContentDescription() {
        return ColList.list().regular("File: " + file.getPath()).regular("Prefix: " + prefix).regular("Comment: " + comment);
    }
    
    private List<Value> getLines (String key, boolean includeBlanks) throws Exception {
        List<Value> result=new ArrayList<Value>();
        boolean emit=false;
        
        BufferedReader br=null;
        try {
            br=new BufferedReader(new FileReader(new File(file.getPath())));
            for (;;) {
                String s=br.readLine();
                if (s==null) break;

                if (s.startsWith(prefix)) {
                    String s2=s.substring(prefix.length());
                    emit = (s2.trim().equals(key));
                } else {
                    if (emit && (comment==null || !s.startsWith(comment))) {
                        if (includeBlanks || s.trim().length()>0) {
                            result.add(new ValueString(s));
                        }
                    }
                }
            }
        } finally {
            if (br != null) try {br.close();} catch (Exception e2) {};
        } 
        return result;
    }
    
    
    class FunctionGet extends Function {
        public String getName() {
            return "get";
        }
        public String getShortDesc() {
            return "get(key) - returns list of non-blank lines for a key";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected one parameter: key");
            String key=getString("key", params, 0);
            return new ValueList(getLines(key, false));
        }
    }
    
    class FunctionGetAll extends Function {
        public String getName() {
            return "getAll";
        }
        public String getShortDesc() {
            return "getAll(key) - returns list of all lines for a key, including blank lines";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected one parameter: key");
            String key=getString("key", params, 0);
            return new ValueList(getLines(key, true));
        }
    }
    
    class FunctionComment extends Function {
        public String getName() {
            return "comment";
        }
        public String getShortDesc() {
            return "comment(str?) - set or clear pattern that indicates comment lines - returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size()==0) comment=null;
            if (params.size() != 1) throw new Exception("Expected one optional parameter: str");
            comment=getString("str", params, 0);
            return new ValueObj(self());
        }
    }

}
