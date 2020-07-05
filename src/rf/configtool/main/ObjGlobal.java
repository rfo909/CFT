/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020 Roar Foshaug

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

import java.io.*;
import java.util.*;

import rf.configtool.main.runtime.*;
import rf.configtool.main.runtime.lib.ObjDataFile;
import rf.configtool.main.runtime.lib.ObjDate;
import rf.configtool.main.runtime.lib.ObjDateSort;
import rf.configtool.main.runtime.lib.ObjDict;
import rf.configtool.main.runtime.lib.ObjDir;
import rf.configtool.main.runtime.lib.ObjFile;
import rf.configtool.main.runtime.lib.ObjFilter;
import rf.configtool.main.runtime.lib.ObjGlob;
import rf.configtool.main.runtime.lib.ObjGrep;
import rf.configtool.main.runtime.lib.ObjInput;
import rf.configtool.main.runtime.lib.ObjLib;
import rf.configtool.main.runtime.lib.ObjPersistent;
import rf.configtool.main.runtime.lib.ObjRegex;
import rf.configtool.main.runtime.lib.ValueObjFileLine;
import rf.configtool.main.runtime.lib.ValueObjInt;
import rf.configtool.main.runtime.lib.ValueObjFloat;
import rf.configtool.main.runtime.lib.ValueObjStr;
import rf.configtool.parser.Parser;
import rf.configtool.parser.SourceLocation;

/**
 * Note that actions without a return value, are statements, and must be parsed
 */
public class ObjGlobal extends Obj {
    
	private PropsFile props;
    private Stdio stdio;

    private String currDir;
    private CodeHistory codeHistory;
    
    private String savename;
    
    private HashMap<String,ObjPersistent> sessionObjects=new HashMap<String,ObjPersistent>();
    private HashMap<String,Value> sessionValues=new HashMap<String,Value>();
    private HashMap<String,ExternalScriptState> externalScriptStates=new HashMap<String,ExternalScriptState>();
    private Runtime runtime;
    
    private ObjCfg cfg;
    
    
    public ObjCfg getObjCfg() {
        return cfg;
    }
     
    public void outln (String s) {
        stdio.println(s);
    }
    
    public void outln() {
        stdio.println();
    }
    
    public Stdio getStdio() {
        return stdio;
    }
    
    public ObjGlobal(Stdio stdio) throws Exception {
    	props=new PropsFile();

        this.stdio=stdio;
        //props.report(stdio);
        
        
        cfg=new ObjCfg();
        
        codeHistory=new CodeHistory(stdio, props, cfg);
        
        add(new FunctionList());
        add(new FunctionDir());
        add(new FunctionFile());
        add(new FunctionFilter());
        add(new FunctionGrep());
        add(new FunctionObjInput());
        add(new FunctionDateSort());
        add(new FunctionInt());
        add(new FunctionFloat());
        add(new FunctionStr());
        add(new FunctionCurrentTime());
        add(new FunctionDict());
        add(new FunctionDate());
        add(new FunctionRegex());
        add(new FunctionGlob());
        add(new FunctionDataFile());
        add(new FunctionEval());
        add(new FunctionSyn());
        add(new FunctionVal());
        add(new FunctionValDef());
        add(new FunctionReadLines());
        add(new FunctionReadLine());
        add(new FunctionCfg());
        add(new FunctionIsWindows());
        add(new FunctionSavefile());
        add(new FunctionPrintln());
        add(new FunctionFileLine());
        add(new FunctionError());
        add(new FunctionCodeDirs());
        
        add(new FunctionLib());
    }
    
    public ObjGlobal objGlobal() {
        return this;
    }
    
    public void setRuntime (Runtime runtime) {
        this.runtime=runtime;
    }
    
    public Runtime getRuntime() {
        return runtime;
    }
    
    /**
     * ObjGlobal persists states of all external scripts invoked, so as their ValDef and other
     * session persistent values are remembered between calls, allowing external scripts
     * to act like stateful objects. If an external script in turn calls another external script,
     * the same takes place inside its ObjGlobal (which is created and kept inside the ExternalScriptState object.
     * 
     * Called from ExprCall
     */
    public ExternalScriptState getOrCreateExternalScriptState (String scriptName) throws Exception {
        ExternalScriptState x=externalScriptStates.get(scriptName);
        if (x==null) {
            x=new ExternalScriptState(stdio, scriptName);
            externalScriptStates.put(scriptName,  x);
        }
        return x;
    }
    
    public String getCurrDir() {
        if (currDir==null) {
            File f=new File(".");
            try {
                return f.getCanonicalPath();
            } catch (Exception ex) {
                return ".";
            }
        }
        return currDir;
    }
    
    public boolean runningOnWindows() {
        return (File.separatorChar=='\\');
    }
    
    public void setCurrDir(String currDir) {
        if (currDir==null) {
            File f=new File(".");
            try {
                currDir = f.getCanonicalPath();
            } catch (Exception ex) {
                currDir = ".";
            }
            
        }
        this.currDir=currDir;
    }
    
    @Override
    public boolean eq(Obj x) {
        return false;
    }

    /**
     * Called from constructor functions for persistent objects
     */
    public ValueObj getOrAddPersistentObject(ObjPersistent obj) {
        ObjPersistent x=sessionObjects.get(obj.getPersistenceId());
        if (x==null) {
            sessionObjects.put(obj.getPersistenceId(), obj);
            x=obj;
            x.initPersistentObj();
        } 
        return new ValueObj(x);
    }
    
    /**
     * Session persistent values for collecting data without having to encode content
     * into a text file.
     */
    public Value getPersistentValue (String name) throws Exception {
        Value v=sessionValues.get(name);
        if (v==null) return new ValueNull();
        return v;
    }
    
    /**
     * Session persistent values for collecting data without having to encode content
     * into a text file.
     */
    public void setPersistentValue (String name, Value value) {
        sessionValues.put(name, value);
    }

    
    public void cleanupOnExit() {
        Iterator<String> keys=sessionObjects.keySet().iterator();
        while (keys.hasNext()) {
            String key=keys.next();
            ObjPersistent x=sessionObjects.get(key);
            x.cleanupOnExit();
        }
    }
    
    public String getTypeName() {
        return "%GLOBAL%";
    }
    public ColList getContentDescription() {
        return ColList.list().status("GLOBAL");
    }

    // -----------------------------------------------------------------
    // Code management
    // -----------------------------------------------------------------
    
    private String savefileState="";
    
    private void updateSavefileState() {
        savefileState="";
        try {
            File f=getSavefile();
            if (f != null) {
                savefileState = "" + f.length() + "x" + f.lastModified();
            }
        } catch (Exception ex) {
            // ignore
        }
    }

    public void refreshIfSavefileUpdated() throws Exception {
        if (savename==null) return;
        
        String copy=savefileState;
        updateSavefileState();
        if (!copy.equals(savefileState)) {
            //stdout.println("% Refreshing from file");
            loadCode(savename);
        }
    }

    public CodeHistory getCodeHistory() {
        return codeHistory;
    }
    
    public void saveCode (String name) throws Exception {
        if (name==null) name=savename;
        if (name==null) throw new Exception("No save name defined");
        savename=name;
        codeHistory.save(savename);

        updateSavefileState();
    }
    
    public void loadCode (String name) throws Exception {
        if (name==null) name=savename;
        if (name==null) throw new Exception("No save name defined");
        savename=name;
        codeHistory.load(savename);
        
        updateSavefileState();
    }

    public String getSavename() {
        return savename;
    }


    public File getSavefile() throws Exception {
        if (savename==null) throw new Exception("No save name defined");
        return codeHistory.getSaveFile(savename);
    }
    
    
    // -----------------------------------------------------------------
    // Functions
    // -----------------------------------------------------------------

    class FunctionList extends Function {
        public String getName() {
            return "List";
        }
        public String getShortDesc() {
            return "List(a,b,c,...,x) - creates list object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            return new ValueList(params);
        }
    }
    
    class FunctionDir extends Function {
        public String getName() {
            return "Dir";
        }
        public String getShortDesc() {
            return "Dir(str?) - creates Dir object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size()==0) {
                return new ValueObj(new ObjDir(getCurrDir()));
            } else if (params.size()==1) {
                String s=getString("str", params, 0);
                return new ValueObj(new ObjDir( s ));
            } else {
                throw new Exception("Expected one optional string parameter");
            }
        }
    }
    
    class FunctionFile extends Function {
        public String getName() {
            return "File";
        }
        public String getShortDesc() {
            return "File(str) - creates File object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected 1 parameter");
            if (!(params.get(0) instanceof ValueString)) throw new Exception("Expected String parameter");
            return new ValueObj(new ObjFile( ((ValueString) params.get(0)).getVal()));
        }
    }
    

    class FunctionFilter extends Function {
        public String getName() {
            return "Filter";
        }
        public String getShortDesc() {
            return "Filter() - creates Filter object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueObj(new ObjFilter());
        }
    }
    

    class FunctionGrep extends Function {
        public String getName() {
            return "Grep";
        }
        public String getShortDesc() {
            return "Grep() or Grep(a,b,...) or Grep(list) - create Grep object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size()==1 && params.get(0) instanceof ValueList) {
                params=((ValueList) params.get(0)).getVal();
            }
            List<String> matchParts=new ArrayList<String>();
            for (Value v:params) {
                matchParts.add(v.getValAsString());
            }
            return new ValueObj(new ObjGrep(matchParts));
        }
    }
    
    
    class FunctionObjInput extends Function {
        public String getName() {
            return "Input";
        }
        public String getShortDesc() {
            return "Input(label) - create Input object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() == 1 && params.get(0) instanceof ValueString) {
                String label=((ValueString) params.get(0)).getVal();
                ObjInput x=new ObjInput(label);
                return ctx.getObjGlobal().getOrAddPersistentObject(x);
            } 

            throw new Exception("Expected label (string) parameter");
        }
    }
    
    class FunctionDateSort extends Function {
        public String getName() {
            return "DateSort";
        }
        public String getShortDesc() {
            return "DateSort() - DateSort object, for sorting lines starting with date/time";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() == 0) {
                ObjDateSort x=new ObjDateSort();
                return new ValueObj(x);
            } 

            throw new Exception("Expected no parameters");
        }
    }
    
    

    class FunctionInt extends Function {
        public String getName() {
            return "Int";
        }
        public String getShortDesc() {
            return "Int(value,data) - create Int object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size()==2) {
                if (!(params.get(0) instanceof ValueInt)) throw new Exception("Expected value parameter to be int");
                long val=((ValueInt) params.get(0)).getVal();
                Value data=params.get(1);
                return new ValueObjInt(val, data);
            } else {
                throw new Exception("Expected parameters value and optional data associated with value");
            }
        }
    }
    
    class FunctionFloat extends Function {
        public String getName() {
            return "Float";
        }
        public String getShortDesc() {
            return "Float(value,data) - create Float object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size()==2) {
                if (!(params.get(0) instanceof ValueFloat)) throw new Exception("Expected value parameter to be float");
                double val=((ValueFloat) params.get(0)).getVal();
                Value data=params.get(1);
                return new ValueObjFloat(val, data);
            } else {
                throw new Exception("Expected parameters value and optional data associated with value");
            }
        }
    }
    

    class FunctionStr extends Function {
        public String getName() {
            return "Str";
        }
        public String getShortDesc() {
            return "Str(value,data) - create Str object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size()==2) {
                if (!(params.get(0) instanceof ValueString)) throw new Exception("Expected value parameter to be string");
                String val=((ValueString) params.get(0)).getVal();
                Value data=params.get(1);
                return new ValueObjStr(val, data);
            } else {
                throw new Exception("Expected parameters value and optional data associated with value");
            }
        }
    }
    
    class FunctionCurrentTime extends Function {
        public String getName() {
            return "currentTimeMillis";
        }
        public String getShortDesc() {
            return "currentTimeMillis() - return current time as millis";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueInt(System.currentTimeMillis());
        }
    }
    
    class FunctionDict extends Function {
        public String getName() {
            return "Dict";
        }
        public String getShortDesc() {
            return "Dict() - create Dict object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("No parameters expected");
            ObjDict x=new ObjDict();
            return new ValueObj(x);
        }
    }
    
    class FunctionDate extends Function {
        public String getName() {
            return "Date";
        }
        public String getShortDesc() {
            return "Date(int?) - create Date object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() == 0) {
                return new ValueObj(new ObjDate());
            } else if (params.size()==1) {
                long val=getInt("int", params, 0);
                return new ValueObj(new ObjDate(val));
            } else {
                throw new Exception("Expected no parameters or single int value");
            }
        }
    }
    
    
    class FunctionRegex extends Function {
        public String getName() {
            return "Regex";
        }
        public String getShortDesc() {
            return "Regex(str) - creates Regex object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected 1 parameter");
            String regex=getString("regex", params, 0);
            return new ValueObj(new ObjRegex(regex));
        }
    }
    
    class FunctionGlob extends Function {
        public String getName() {
            return "Glob";
        }
        public String getShortDesc() {
            return "Glob(pattern) - creates Glob object for file name matching, such as '*.txt'";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected 1 parameter");
            String pattern=getString("pattern", params, 0);
            return new ValueObj(new ObjGlob(pattern));
        }
    }
    


    class FunctionLib extends Function {
        public String getName() {
            return "Lib";
        }
        public String getShortDesc() {
            return "Lib() - create Lib object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueObj(new ObjLib());
        }
    }
    
    class FunctionDataFile extends Function {
        public String getName() {
            return "DataFile";
        }
        public String getShortDesc() {
            return "DataFile(file,prefix) - create DataFile object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 2) throw new Exception("Expected two parameters: file, prefix");
            Obj obj=getObj("file",params,0);
            String prefix=getString("prefix", params, 1);
            
            if (!(obj instanceof ObjFile)) throw new Exception("Expected two parameters: file, prefix");
            
            ObjDataFile x=new ObjDataFile((ObjFile) obj, prefix);
            return new ValueObj(x);
        }
    }
    
    class FunctionEval extends Function {
        public String getName() {
            return "eval";
        }
        public String getShortDesc() {
            return "eval(str) - execute program line and return result";
        }
        @Override
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected parameter str");
            String str=getString("str",params,0);
            SourceLocation loc=new SourceLocation("<eval>", 0, 0);
            return ctx.getObjGlobal().getRuntime().processCodeLines(new CodeLines(str, loc),null);
        }
    }

    class FunctionSyn extends Function {
        public String getName() {
            return "syn";
        }
        public String getShortDesc() {
            return "syn(value) - get value as syntesized string, or exception if it can not be synthesized";
        }
        @Override
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected one parameter of any synthesizable type");
            String s=params.get(0).synthesize();
            return new ValueString(s);
        }
    }


    class FunctionVal extends Function {
        public String getName() {
            return "Val";
        }
        public String getShortDesc() {
            return "Val(name) - get session value created with ValDef";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() == 1) {
                String name=getString("name", params, 0);
                return ctx.getObjGlobal().getPersistentValue(name);
            } else {
                throw new Exception("Expected parameters name, defaultValue");
            }
        }
    } 


    class FunctionValDef extends Function {
        public String getName() {
            return "ValDef";
        }
        public String getShortDesc() {
            return "ValDef(name,value) - set session value";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() == 2) {
                String name=getString("name", params, 0);
                Value value=params.get(1);
                ctx.getObjGlobal().setPersistentValue(name, value);
                return value;
            } else {
                throw new Exception("Expected parameters name, value");
            }
        }
    } 

    class FunctionReadLines extends Function {
        public String getName() {
            return "readLines";
        }
        public String getShortDesc() {
            return "readLines(endmarker) - read input until label on separate line, returns list of strings";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() == 1 && params.get(0) instanceof ValueString) {
                String endMarker=getString("endmarker", params, 0);
                
                List<Value> list=new ArrayList<Value>();
                for (;;) {
                    String s=stdio.getInputLine();
                    if (s.trim().equals(endMarker)) {
                        break;
                    }
                    list.add(new ValueString(s));
                }
                return new ValueList(list);
            } 

            throw new Exception("Expected endmarker string parameter");
        }
    }
    

    class FunctionReadLine extends Function {
        public String getName() {
            return "readLine";
        }
        public String getShortDesc() {
            return "readLine(prompt) - read single input line";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() == 1) {
                String prompt=getString("prompt", params, 0);
                
                boolean silent=ctx.getStdio().hasBufferedInputLines();
                if (!silent) {
                    stdio.print(prompt);
                }
                return new ValueString(stdio.getInputLine());
            } 

            throw new Exception("Expected prompt string parameter");
        }
    }
    

    class FunctionCfg extends Function {
        public String getName() {
            return "Cfg";
        }
        public String getShortDesc() {
            return "Cfg - get config object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueObj(ctx.getObjGlobal().getObjCfg());
        }
    } 

    
    class FunctionIsWindows extends Function {
        public String getName() {
            return "isWindows";
        }
        public String getShortDesc() {
            return "isWindows() - returns boolean";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueBoolean(runningOnWindows());
        }
    } 
    class FunctionSavefile extends Function {
        public String getName() {
            return "savefile";
        }
        public String getShortDesc() {
            return "savefile() - returns File object for savefile";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            File f=getSavefile();
            return new ValueObj(new ObjFile(f.getCanonicalPath()));
        }
    } 

    class FunctionPrintln extends Function {
        public String getName() {
            return "println";
        }
        public String getShortDesc() {
            return "println(str) - print string";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() == 1) {
                String s=params.get(0).getValAsString();
                stdio.println(s);
                return new ValueString(s);
            } else if (params.size() == 0) {
                stdio.println();
		return new ValueString("");
            }

            throw new Exception("Expected no parameter or string parameter");
        }
    }
    
    class FunctionFileLine extends Function {
        public String getName() {
            return "FileLine";
        }
        // (String line, Integer lineNo, ObjFile file)
        public String getShortDesc() {
            return "FileLine(str, lineNo, File) - create FileLine object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 3) {
                throw new Exception("Expected parameters str, lineNo, File");
            }
            String str=getString("str", params, 0);
            Long lineNo=getInt("lineNo", params, 1);
            Obj obj=getObj("File", params, 2);
            if (!(obj instanceof ObjFile)) {
                throw new Exception("Invalid File object");
            }
            ObjFile file=(ObjFile) obj;
            return new ValueObj(new ValueObjFileLine(str,lineNo,file));
        }
    }
    

    class FunctionError extends Function {
        public String getName() {
            return "error";
        }
        public String getShortDesc() {
            return "error(value) - throws exception, terminating code execution";
        }
        @Override
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected one parameter");
            String s=params.get(0).getValAsString();
            throw new Exception(s);
        }
    }

    class FunctionCodeDirs extends Function {
        public String getName() {
            return "codeDirs";
        }
        public String getShortDesc() {
            return "codeDirs - returns list of code dirs (see " + PropsFile.PROPS_FILE + ")";
        }
        @Override
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            List<Value> list=new ArrayList<Value>();
            for (String s:props.getCodeDirs()) {
            	ObjDir dir=new ObjDir(s);
            	list.add(new ValueObj(dir));
            }
            return new ValueList(list);
        }
    }


}
