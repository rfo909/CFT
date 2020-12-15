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
import java.lang.ProcessBuilder.Redirect;
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
import rf.configtool.main.runtime.lib.ObjSys;
import rf.configtool.main.runtime.lib.Protection;
import rf.configtool.main.runtime.lib.RunCaptureOutput;
import rf.configtool.main.runtime.lib.ValueObjFileLine;
import rf.configtool.main.runtime.lib.ValueObjInt;
import rf.configtool.main.runtime.lib.ValueObjFloat;
import rf.configtool.main.runtime.lib.ValueObjStr;
import rf.configtool.parser.Parser;
import rf.configtool.parser.SourceLocation;
import rf.configtool.root.Root;

/**
 * Note that actions without a return value, are statements, and must be parsed
 */
public class ObjGlobal extends Obj {
    
	private Root root;
    private StdioReal stdioReal;

    private String currDir;
    private CodeHistory codeHistory;
    
    private String scriptName;
    
    private HashMap<String,ObjPersistent> sessionObjects=new HashMap<String,ObjPersistent>();
    private HashMap<String,Value> sessionValues=new HashMap<String,Value>();
    private final Runtime runtime;
    private List<String> systemMessages=new ArrayList<String>();


    public Root getRoot() {
    	return root;
    }
    
    public StdioReal getStdioActual() {
        return stdioReal;
    }
    
    public boolean isDebugMode() {
    	return root.isDebugMode();
    }
    
    public ObjGlobal(Root root, StdioReal stdioReal) throws Exception {
    	this.root=root;
        this.stdioReal=stdioReal;
        //props.report(stdio);
        
        
        codeHistory=new CodeHistory(root.getPropsFile(), root.getObjCfg());
        this.runtime=new Runtime(this);
        
        
        add(new FunctionList());
        add(new FunctionDir());
        add(new FunctionFile());
        add(new FunctionFilter());
        add(new FunctionGrep());
        add(new FunctionObjInput());
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
        add(new FunctionReadLines());
        add(new FunctionReadLine());
        add(new FunctionCfg());
        add(new FunctionPrintln());
        add(new FunctionFileLine());
        add(new FunctionError());
        add(new FunctionShell());
        add(new FunctionGetType());

        
        // name spaces
        add(new FunctionSys());
        add(new FunctionLib());
    }
    
    public ObjGlobal objGlobal() {
        return this;
    }
    
    
    public Runtime getRuntime() {
        return runtime;
    }
    
    public void addSystemMessage (String line) {
        systemMessages.add(line);
    }
    
    public void debug (String line) {
    	systemMessages.add("[debug] " + line);
    }
    
    public List<String> getSystemMessages() {
        return systemMessages;
    }
    
    public void clearSystemMessages() {
    	systemMessages.clear();
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
        } else {
        	x.refreshPersistentObj();
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
        if (scriptName==null) return;
        
        String copy=savefileState;
        updateSavefileState();
        if (!copy.equals(savefileState)) {
            //stdout.println("% Refreshing from file");
            loadCode(scriptName);
        }
    }

    public CodeHistory getCodeHistory() {
        return codeHistory;
    }
    
    public void saveCode (String name) throws Exception {
        if (name==null) name=scriptName;
        if (name==null) throw new Exception("No save name defined");
        scriptName=name;
        codeHistory.save(scriptName);

        updateSavefileState();
    }
    
    public void loadCode (String name) throws Exception {
        if (name==null) name=scriptName;
        if (name==null) throw new Exception("No save name defined");
        scriptName=name;
        codeHistory.load(scriptName);

        updateSavefileState();

        
        CodeLines onLoad = codeHistory.getNamedCodeLines("onLoad");
        if (onLoad != null) {
        	this.runtime.processCodeLines(stdioReal, onLoad, new FunctionState());
        }
        
    }

    public String getScriptName() {
        return scriptName;
    }
    
     public File getSavefile() throws Exception {
        if (scriptName==null) throw new Exception("No save name defined");
        return codeHistory.getSaveFile(scriptName);
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
                return new ValueObj(new ObjDir(getCurrDir(), Protection.NoProtection));
            } else if (params.size()==1) {
                String s=getString("str", params, 0);
                return new ValueObj(new ObjDir(s, Protection.NoProtection));
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
            return new ValueObj(new ObjFile( ((ValueString) params.get(0)).getVal(), Protection.NoProtection));
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
            return "Glob(pattern,ignoreCase?) - creates Glob object for file name matching, such as '*.txt' - ignoreCase defaults to true";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() == 1) {
	            String pattern=getString("pattern", params, 0);
	            boolean ignoreCase=true;
	            return new ValueObj(new ObjGlob(pattern,ignoreCase));
            } else if (params.size() == 2) {
	            String pattern=getString("pattern", params, 0);
	            boolean ignoreCase=getBoolean("ignoreCase", params, 1);
	            return new ValueObj(new ObjGlob(pattern,ignoreCase));
            } else {
            	throw new Exception("Expected parameters pattern, ignoreCase?");
            }
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
            return runtime.processCodeLines(ctx.getStdio(), new CodeLines(str, loc),null);
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


    class FunctionReadLines extends Function {
        public String getName() {
            return "readLines";
        }
        public String getShortDesc() {
            return "readLines(endmarker) - read input until label on separate line, returns list of strings";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	Stdio stdio=ctx.getStdio();

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
            return "readLine(prompt?) - read single input line";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	Stdio stdio=ctx.getStdio();

        	if (params.size() == 1) {
                String prompt=getString("prompt", params, 0);
                
                boolean silent=ctx.getStdio().hasBufferedInputLines();
                if (!silent) {
                    stdio.println("(?) " + prompt);
                }
                return new ValueString(stdio.getInputLine());
            } else if (params.size() == 0) {
            	// no prompt
            	return new ValueString(stdio.getInputLine());
            }

            throw new Exception("Expected optional prompt string parameter");
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
            return new ValueObj(ctx.getObjGlobal().getRoot().getObjCfg());
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
        	Stdio stdio=ctx.getStdio();

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
            return "error(cond?, msg) - if expr is true or no expr, throw soft error exception";
        }
        @Override
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() == 2) {
	            boolean cond=params.get(0).getValAsBoolean();
	            String s=params.get(1).getValAsString();
	            if (cond) throw new SoftErrorException(s);
	            return new ValueNull(); 
            } else if (params.size() ==1) {
	            String s=params.get(0).getValAsString();
	            throw new SoftErrorException(s);
            } else {
            	throw new Exception("Expected parameters [cond,] message");
            }
        }
    }

   
    class FunctionSys extends Function {
        public String getName() {
            return "Sys";
        }
        public String getShortDesc() {
            return "Sys() - create Sys object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueObj(new ObjSys());
        }
    } 

   

    class FunctionShell extends Function {
        public String getName() {
            return "shell";
        }
        public String getShortDesc() {
            return "shell() - runs shell as configured in " + PropsFile.PROPS_FILE;
        }
        @Override
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            String shellCommand;
            if (File.separator.equals("\\")) {
            	shellCommand=ctx.getObjGlobal().getRoot().getPropsFile().getWinShell();
            } else {
            	shellCommand=ctx.getObjGlobal().getRoot().getPropsFile().getShell();
            }
            callExternalProgram(shellCommand, ctx);
            return new ValueBoolean(true);
        }
    }
    
    
    class FunctionGetType extends Function {
        public String getName() {
            return "getType";
        }
        public String getShortDesc() {
            return "getType(any) - get value or object type";
        }
        @Override
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected parameter any");
            Value v=params.get(0);
            String s;
            if (v instanceof ValueObj) {
            	s=((ValueObj) v).getVal().getTypeName();
            } else {
            	s=v.getTypeName();
            }
            return new ValueString(s);
        }
    }
    

	
	
	
    
    private void callExternalProgram (String cmd, Ctx ctx) throws Exception {
        List<String> strArgs=new ArrayList<String>();
        strArgs.add(cmd);

        String program=strArgs.get(0);
        
        ProcessBuilder processBuilder = new ProcessBuilder(strArgs);
        
        processBuilder.redirectInput(Redirect.INHERIT); // connect input
        processBuilder.redirectOutput(Redirect.INHERIT);
        processBuilder.redirectError(Redirect.INHERIT);

        // set current directory
        processBuilder.directory(new File(ctx.getObjGlobal().getCurrDir()));
        
        Process process = processBuilder.start();
        process.waitFor();
        ctx.getObjGlobal().addSystemMessage("Running " + program + " completed");
    }

 



}
