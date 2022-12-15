/*
- an interactive programmable shell for automation 
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

import java.io.File;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import rf.configtool.lexer.SourceLocation;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBinary;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueFloat;
import rf.configtool.main.runtime.ValueInt;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueNull;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.lib.ObjAnnotatedValue;
import rf.configtool.main.runtime.lib.ObjDataFile;
import rf.configtool.main.runtime.lib.ObjDate;
import rf.configtool.main.runtime.lib.ObjDict;
import rf.configtool.main.runtime.lib.ObjDir;
import rf.configtool.main.runtime.lib.ObjFile;
import rf.configtool.main.runtime.lib.ObjGlob;
import rf.configtool.main.runtime.lib.ObjGrep;
import rf.configtool.main.runtime.lib.ObjInput;
import rf.configtool.main.runtime.lib.ObjLib;
import rf.configtool.main.runtime.lib.ObjPersistent;
import rf.configtool.main.runtime.lib.ObjRegex;
import rf.configtool.main.runtime.lib.ObjSys;
import rf.configtool.main.runtime.lib.Protection;
import rf.configtool.main.runtime.lib.ValueObjFileLine;
import rf.configtool.main.runtime.lib.ValueObjFloat;
import rf.configtool.main.runtime.lib.ValueObjInt;
import rf.configtool.main.runtime.lib.ValueObjStr;
import rf.configtool.root.Root;
import rf.configtool.util.TabUtil;

/**
 * ObjGlobal is a global store of state ... per script in memory. The global object managing
 * these is the Root class.
 * 
 * ObjGlobal contains all the global functions.
 * 
 * Note that actions without a return value, are implemented as statements, which are parsed
 * individually according to syntax.
 */
public class ObjGlobal extends Obj {
    
    private Root root;
    private Stdio stdio;

    private String currentDir;
    private ScriptCode currScriptCode;
    
    private String scriptName;
    
    private HashMap<String,ObjPersistent> sessionObjects=new HashMap<String,ObjPersistent>();
    private HashMap<String,Value> sessionValues=new HashMap<String,Value>();
    private final Runtime runtime;
    private List<String> systemMessages=new ArrayList<String>();
    
    private long exprCount=0L;


    public Root getRoot() {
        return root;
    }
    
    public Stdio getStdio() {
        return stdio;
    }
    
    public boolean isDebugMode() {
        return root.isDebugMode();
    }
    
    public ObjGlobal(Root root, String currentDir, Stdio stdio) throws Exception {
        this.root=root;
        this.currentDir=currentDir;
        this.stdio=stdio;
        //props.report(stdio);
        
        if (currentDir==null) throw new Exception("No currentDir");
        
        
        // initialize with empty script
        currScriptCode=new ScriptCode(root.getPropsFile(), root.getObjTerm());
        this.runtime=new Runtime(this);
        
        
        add(new FunctionList());
        add(new FunctionDir());
        add(new FunctionFile());
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
        add(new FunctionTerm());
        add(new FunctionPrintln());
        add(new FunctionFileLine());
        add(new FunctionError());
        add(new FunctionShell());
        add(new FunctionGetType());
        add(new FunctionGetExprCount());
        add(new FunctionBinary());
        add(new FunctionAValue());


        // help
        add(new Function_Stmt());
        add(new Function_Expr());
        add(new Function_Shell());

        // name spaces
        add(new FunctionSys());
        add(new FunctionLib());
    }
    
    public ObjGlobal objGlobal() {
        return this;
    }
    
    public void addExprCount() {
        exprCount++;
    }
    
    public long getExprCount() {
        return exprCount;
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
        if (currentDir==null) {
            File f=new File(".");
            try {
                return f.getCanonicalPath();
            } catch (Exception ex) {
                return ".";
            }
        }
        return currentDir;
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
        this.currentDir=currDir;
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
        return "<GLOBAL>";
    }
    public ColList getContentDescription() {
        return ColList.list().status("<GLOBAL>");
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

    public ScriptCode getCurrScriptCode() {
        return currScriptCode;
    }
    
    public void saveCode (String name) throws Exception {
        if (name==null) name=scriptName;
        if (name==null) throw new Exception("No save name defined");
        scriptName=name;
        currScriptCode.save(scriptName, new File(currentDir));

        updateSavefileState();
    }
    
    public void loadCode (String name) throws Exception {
        if (name==null) name=scriptName;
        if (name==null) throw new Exception("No save name defined");
        scriptName=name;
        currScriptCode.load(scriptName, new File(currentDir));

        updateSavefileState();
 
        FunctionBody onLoad = currScriptCode.getFunctionBody("onLoad");
        if (onLoad != null) {
            try {
            	CFTCallStackFrame caller=new CFTCallStackFrame("Script " + scriptName+":onLoad");
                this.runtime.processFunction(stdio, caller, onLoad, new FunctionState("onLoad"));
            } catch (Exception ex) {
                stdio.println("onLoad function failed with exception");
            }
        }
        
    }

    public String getScriptName() {
        return scriptName;
    }
    
     public File getSavefile() throws Exception {
        if (scriptName==null) throw new Exception("No save name defined");
        return currScriptCode.getSaveFile(scriptName, new File(currentDir));
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
            return "Int(value,data) - create Int object - for sorting";
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
            return "Float(value,data) - create Float object - for sorting";
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
            return "Str(value,data) - create Str object - for sorting";
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
            return "Dict(name?) - create Dict object with optional string name";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() == 0) return new ValueObj(new ObjDict());
            if (params.size() == 1) {
                String name=getString("name", params, 0);
                return new ValueObj(new ObjDict(name));
            }
            throw new Exception("Expected optional name parameter");
        }
    }
    
    class FunctionDate extends Function {
        public String getName() {
            return "Date";
        }
        public String getShortDesc() {
            return "Date(int?) - create Date and time object - uses current time if no parameter";
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
            return "Glob(pattern,ignoreCase?) - creates Glob object for file name matching, such as '*.txt' - ignoreCase defaults to true on windows, otherwise false";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() == 1) {
                String pattern=getString("pattern", params, 0);
                boolean ignoreCase=(File.separator.equals("\\"));  // ignore case on Windows, otherwise not
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
    

    private static ValueObj staticLib = new ValueObj(new ObjLib());

    class FunctionLib extends Function {
        public String getName() {
            return "Lib";
        }
        public String getShortDesc() {
            return "Lib() - create Lib object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return staticLib;
            //return new ValueObj(new ObjLib());
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
        	CFTCallStackFrame caller=new CFTCallStackFrame("eval");
            return runtime.processFunction(ctx.getStdio(), caller, new FunctionBody(str, loc),new FunctionState(null,null));
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
    

    class FunctionTerm extends Function {
        public String getName() {
            return "Term";
        }
        public String getShortDesc() {
            return "Term - get terminal config object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueObj(ctx.getObjGlobal().getRoot().getObjTerm());
        }
    } 

 
    
    class FunctionPrintln extends Function {
        public String getName() {
            return "println";
        }
        public String getShortDesc() {
            return "println([str[,...]]?) - print string";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            Stdio stdio=ctx.getStdio();
            StringBuffer sb=new StringBuffer();
            for (int i=0; i<params.size(); i++) {
            	if (i>0) sb.append(" ");
            	sb.append(params.get(i).getValAsString());
            }
            String s=sb.toString();

            s=TabUtil.substituteTabs(s, 4);
            int w=ctx.getObjGlobal().getRoot().getObjTerm().getScreenWidth();
            if (s.length() >= w-1) {
                s=s.substring(0, w-2)+"+";
            }
            stdio.println(s);
            return new ValueString(s);
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

    
    private static ValueObj staticSys=new ValueObj(new ObjSys());
   
    class FunctionSys extends Function {
        
        public String getName() {
            return "Sys";
        }
        public String getShortDesc() {
            return "Sys() - create Sys object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return staticSys;
            //return new ValueObj(new ObjSys());
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
    



    
    class FunctionGetExprCount extends Function {
        public String getName() {
            return "getExprCount";
        }
        public String getShortDesc() {
            return "getExprCount() - get number of expressions resolved";
        }
        @Override
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueInt(exprCount);
        }
    }
    


    class FunctionAValue extends Function {
        public String getName() {
            return "AValue";
        }
        public String getShortDesc() {
            return "AValue(str,any,metaDict?) - created AValue (annotated value) object";
        }
        @Override
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() == 2 || params.size() == 3) { 
                String str=getString("typeStr", params, 0);
                Value v=params.get(1);
                ObjDict dict=null;
                if (params.size()==3) {
                    Obj obj=getObj("metaDict", params, 2);
                    if (obj instanceof ObjDict) {
                        dict=(ObjDict) obj;
                    } else {
                        throw new Exception("Expected parameters typeStr,value,metaDict?");
                    }
                }
                if (dict==null) dict=new ObjDict();
                return new ValueObj(new ObjAnnotatedValue(str,v,dict));
            }
            throw new Exception("Expected parameters typeStr,value,metaDict?");
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

    
    
    
    class FunctionBinary extends Function {
        public String getName() {
            return "Binary";
        }
        public String getShortDesc() {
            return "Binary(hexString) - convert hex string to Binary value";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected hexString parameter");
            String hex=getString("hexString",params,0);
            int len=hex.length();
            byte[] data=new byte[len/2];
            for (int i=0; i<data.length; i++) {
                char c1=hex.charAt(i*2);
                char c2=hex.charAt(i*2+1);
                data[i]=(byte) Integer.parseInt(""+c1+c2,16);
            }
            return new ValueBinary(data);
        }
        
    } 

    
    class Function_Stmt extends Function {
        public String getName() {
            return "_Stmt";
        }
        public String getShortDesc() {
            return "_Stmt() - information about Statements in CFT";
        }
        private String[] data= {
            "",
            "Statements",
            "----------",
            "",
            "Looping and iteration over lists:", 
            "   loop ... break(cond)",
            "   list -> variable ...",
            "",
            "Loop control:",
            "   assert (boolExpr)",
            "   reject (boolExpr)",
            "   break (boolExpr)",
            "   break",
            "",
            "Loop output:",
            "   out (expr)",
            "   condOut (boolExpr,expr)",
            "   report (expr,expr,...)",
            "   reportList (listExpr)",
            "",
            "The help command shows available functions in objects, and takes two forms:", 
            "   help           : show global functions",
            "   <value> help   : help about top value on stack",
            "",
            "",
            "addDebug (stringExpr)",
            "setBreakPoint (stringExpr)",
            "timeExpr (expr)",
            "",
            "Expressions are also statements",
            "",
            "More system commands are implemented as functions in the Sys object:",
            "",
            "Sys help",
        };
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            for (String line:data) {
                ctx.getObjGlobal().addSystemMessage(line);
            }
            return new ValueBoolean(true);
        }        
    } 
    
    class Function_Expr extends Function {
        public String getName() {
            return "_Expr";
        }
        public String getShortDesc() {
            return "_Expr() - information about expressions in CFT";
        }
        private String[] data= {
            "",
            "Expressions",
            "-----------",
            "",
            "Logical",
            "   bool || bool",
            "   bool && bool",
            "",
            "Compare",
            "   >  <  >=  <= == !=",
            "",
            "Calculate",
            "",
            "   + - * / % div",
            "",
            "Assign local variable",
            "   ident = Expr",
            "   expr => ident",
            "",
            "Blocks",
            "   {...}",
            "   Inner{...}",
            "   Lambda{...}",
            "",
            "Exception handling",
            "   tryCatch(Expr)",
            "   tryCatchSoft(Expr)",
            "",
            "Various",
            "   ( expr )",
            "   !expr",
            "   -expr",
            "   _     # underscore = top value on data stack",
            "",
            "   if(expr,expr,expr)",
            "   if(expr) Stmt [else Stmt]",
            "       (note that Expr is also a valid Stmt)",
            "",
            "   pwd",
            "   null",
            "   ScriptName:func(...)",
            "   Sequence(Expr ...)",
            "   CondSequence(BoolExpr Expr ...)",
            "   SymDict(ident,...)",
            "   P(N[,expr])",
            "   PDict(Str,...)",
            "   SpawnProcess(Dict,expr[,lambda])",
            "",
            "Type checking",
            "   These throw error if failing, mostly for type checking P(N)",
            "      expr as String?",
            "      expr as int",
            "      expr as (List('int','String'))",
            "   Using as? instead of as, means return boolean instead",
            "      2 as? String   # returns false",
            "      2 as String    # error!",
            "",
            "Background processes",
            "   The SpawnProcess() expression has an interactive, simpler syntax:",
            "      & expr",
            "      & expr , name",
            "         The name is a string-expression or an identifier",
            "         Use Sys.Jobs or Jobs script to manage (@J* shortcuts)",
            "",
            "Symbol lookup",
            "   %name             # See _Shell for more on symbols",
            "",
            "Value tokens",
            "   int, string, float",
            "   true",
            "   false",
            "",
            "Function calls",
            "   func",
            "   func(...)",
            "",
            "Dotted lookup",
            "   a.b.c(...).d.e",
            "",
            "Raw string",
            "   @ ...",
            "",
        };
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            for (String line:data) {
                ctx.getObjGlobal().addSystemMessage(line);
            }
            return new ValueBoolean(true);
        }
        
        
    } 
    
    
    class Function_Shell extends Function {
        public String getName() {
            return "_Shell";
        }
        public String getShortDesc() {
            return "_Shell() - CFT shell-like commands";
        }
        private String[] data= {
                "CFT shell-like (interactive) commands",
                "-------------------------------------",
                "",
                "   ls / lsd / lsf ...",
        		"   cd <dir>",
        		"   cat / edit / more / tail <file>",
        		"   touch <file> ...",
        		"   mv ...",
        		"   cp ...",
        		"   rm ...",
        		"   mkdir <dir>",
        		"   diff <file1> <file2>",
        		"   showtree <dir>?",
        		"   hash <file> ...",
        		"   hex <file>",
        		"   grep <str> <file> ...",
                "",
                "- The 'lsd' lists directories only, and 'lsf' files only.",
                "",
                "- The 'edit' command opens a file in editor.",
                "",
                "- Note that 'rm' deletes both files and directories, and asks",
                "  for confirm when non-empty directories.",
                "",
                "- Also note that 'cp' copies both files and directories.",
                "",
                "- All commands allow both globbing, local and absolute paths,",
                "  on both Windows and Linux, symbol references and CFT expressions,",
                "  and may refer Sys.lastResult or numbered element when list:",
                "",
                "   ls a*.txt                - globbing",
                "   ls /some/path            - absolute path",
                "   ls c:\\someDir            - absolute path (windows)",
                "   ls \\\\some-server\\d$\\xxx  - absolute network path (Windows)",
                "",
                "   cd %someSymbol           - symbol resolving to dir or file",
                "   cd (dirExpr)             - dirExpr is a CFT function",
                "",
                "   cat :                    - The ':' corresponds to (Sys.lastResult)",
                "   cd :N                    - The ':N' corresponds to (Sys.lastResult(N))",
                "",
                "Symbols are defined entering %%name which stores lastResult under",
                "that name, usually some Dir or File. A warning will be issued when",
                "storing any other type of value as a symbol.",
                "",
        };
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            for (String line:data) {
                ctx.getObjGlobal().addSystemMessage(line);
            }
            return new ValueBoolean(true);
        }        
    } 
    




}
