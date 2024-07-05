/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2024 Roar Foshaug

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

package rf.configtool.root;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import rf.configtool.lexer.Lexer;
import rf.configtool.lexer.SourceLocation;
import rf.configtool.lexer.Token;
import rf.configtool.lexer.TokenStream;
import rf.configtool.main.CFTCallStackFrame;
import rf.configtool.main.Ctx;
import rf.configtool.main.ScriptSourceLine;
import rf.configtool.main.FunctionBody;
import rf.configtool.main.FunctionState;
import rf.configtool.main.ObjGlobal;
import rf.configtool.main.ObjTerm;
import rf.configtool.main.PropsFile;
import rf.configtool.main.ScriptCode;
import rf.configtool.main.SourceException;
import rf.configtool.main.Stdio;
import rf.configtool.main.StdioReal;
import rf.configtool.main.Version;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueNull;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.lib.ObjDir;
import rf.configtool.main.runtime.lib.Protection;
import rf.configtool.root.shell.ShellCommand;
import rf.configtool.root.shell.ShellCommandsManager;
import rf.configtool.util.ReportFormattingTool;

/**
 * The Root class manages a set of parallel script contexts.
 */
public class Root {
    
    private final String sessionUUID;
    private final byte[] secureSessionID; 


    private StdioReal stdio;
    private PropsFile propsFile;
    private ObjTerm objTerm;

    private Map<String, ScriptState> scriptStates = new HashMap<String, ScriptState>();
    private ScriptState currScript;
    private boolean debugMode;
    private Value lastResult;
    private final long startTime;
    private boolean terminationFlag = false;
    
    private BackgroundProcesses backgroundProcesses=new BackgroundProcesses();
    
    /**
     * Unique value per CFT session, available via Sys.sessionUUID CFT function
     */
    public String getSessionUUID() {
        return sessionUUID;
    } 

    public byte[] getSecureSessionID() {
        return secureSessionID;
    } 

    public Root(StdioReal stdio, String customScriptDir, boolean noTerminal) throws Exception {
        this.sessionUUID = UUID.randomUUID().toString();
        this.secureSessionID = (UUID.randomUUID().toString()+":"+System.currentTimeMillis()).getBytes("UTF-8");
        this.startTime=System.currentTimeMillis();
        this.stdio = stdio;
        propsFile=new PropsFile(customScriptDir);
        objTerm=new ObjTerm(noTerminal);
        

        String globalOnLoad = propsFile.getGlobalOnLoad();
        if (globalOnLoad != null) {
            addInitialCommand(globalOnLoad);
        }
        


        createNewScript();
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public PropsFile getPropsFile() {
        return propsFile;
    }

    
    public ObjTerm getObjTerm() {
        return objTerm;
    }
    
    public boolean isDebugMode() {
        return debugMode;
    }

    public void loadScript(String scriptName) throws Exception {
        currScript = getScriptState(scriptName, true);
    }

    public void addInitialCommand(String line) {
            stdio.addBufferedInputLine(line);
    }

    private void refreshIfSavefileUpdated() throws Exception {
        Iterator<String> keys = scriptStates.keySet().iterator();
        List<String> keysToDelete = new ArrayList<String>();
        while (keys.hasNext()) {
            String key = keys.next();
            ScriptState x = scriptStates.get(key);
            try {
                x.getObjGlobal().refreshIfSavefileUpdated();
            } catch (Exception ex) {
                stdio.println("ERROR: could not reload script " + key + " - removing from cache");
                keysToDelete.add(key);
            }
        }
        for (String key : keysToDelete) {
            if (!currScript.getObjGlobal().equals(key)) {
                scriptStates.remove(key);
            }
        }
    }
    
    
    private String getCurrDirOrDot() throws Exception {
        if (currScript != null) return currScript.getObjGlobal().getCurrDir();
        return (new File(".")).getCanonicalPath();
    }
    

    public ScriptState getScriptState(String name, boolean isLoad) throws Exception {
        if (name == null || name.equals(currScript.getScriptName())) {
            if (isLoad)
                currScript.getObjGlobal().loadCode(null); // reloads code - overwrite any local changes
            return currScript;
        }
        // script already loaded (but not current)?
        ScriptState otherScript = scriptStates.get(name);
        if (otherScript != null) {
            if (isLoad)
                otherScript.getObjGlobal().loadCode(otherScript.getScriptName());
            return otherScript;
        }
        // script not loaded, create new ScriptState
        ScriptState newScript = new ScriptState(name, new ObjGlobal(this, getCurrDirOrDot(), stdio)); // throws exception if there is
                                                                                    // trouble
        scriptStates.put(newScript.getScriptName(), newScript);
        return newScript;
    }
    
    public BackgroundProcesses getBackgroundProcesses() {
        return backgroundProcesses;
    }

    public void createNewScript() throws Exception {
        currScript = new ScriptState(new ObjGlobal(this, getCurrDirOrDot(), stdio));
        scriptStates.put(currScript.getScriptName(), currScript);
    }

    private void cleanupOnExit() throws Exception {
        Iterator<String> keys = scriptStates.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            ScriptState x = scriptStates.get(key);
            x.getObjGlobal().cleanupOnExit();
        }
    }

    public Value getLastResult() {
        if (lastResult == null)
            return new ValueNull();
        return lastResult;
    }

    // Interactive input loop
    public void inputLoop() {
        copyrightNotice();
        stdio.println(Version.getVersion());

        try {
            INPUT_LOOP: for (;;) {
                
                if (terminationFlag) break INPUT_LOOP;

                ObjGlobal objGlobal = currScript.getObjGlobal();


                if (!stdio.hasBufferedInputLines()) {
                    // Only produce prompt when non-buffered lines
                    
                    // Run the prompt code line to produce possibly dynamic prompt
                    String promptCode = propsFile.getPromptCode();
                    SourceLocation loc = new SourceLocation("prompt", 0, 0);
                    FunctionBody promptCodeLines = new FunctionBody(promptCode, loc);
    
                    String promptLine;
                    try {
                        CFTCallStackFrame caller=new CFTCallStackFrame("<interactive-input>");

                        Value ret = objGlobal.getRuntime().processFunction(stdio, caller, promptCodeLines, new FunctionState(null,null));
                        promptLine=ret.getValAsString();
                    } catch (Exception ex) {
                        if (debugMode) {
                            promptLine="ERROR";
                            ex.printStackTrace();
                        } else {
                            promptLine="$";
                        }
                    }
    
                    // Stdio can only do line output, so using System.out directly
                    stdio.println(promptLine);
                }
                String line = null;
                try {
                    line = stdio.getInputLine();
                } catch (Exception ex) {
                    stdio.println("inputLoop(): read failed");
                    break INPUT_LOOP;
                }

                refreshIfSavefileUpdated();
                propsFile.refreshFromFile();

                
                processInteractiveInput(line);

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        // Clean up and terminate
        // ----------------------
        stdio.println("Shutting down lock manager");
        LockManager.setShuttingDown();
        stdio.println("Runtime exit, cleaning up");
        try {
            cleanupOnExit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Moved here from Runtime
     */
    public void processInteractiveInput(final String inputLine) throws Exception {
        TokenStream ts = null;
        ObjGlobal objGlobal = currScript.getObjGlobal();
        ScriptCode currScriptCode = objGlobal.getCurrScriptCode();

        stdio.clearCFTCallStack();
               
        String line=inputLine;
        
        try {
            // Shortcuts

            String shortcutPrefix = propsFile.getShortcutPrefix();
            if (line.startsWith(shortcutPrefix)) {
                String shortcutName = line.substring(shortcutPrefix.length()).trim();
                String shortcutCode = propsFile.getShortcutCode(shortcutName);
                SourceLocation loc = new SourceLocation("shortcut:" + shortcutName, 0, 0);

                FunctionBody codeLines = new FunctionBody(shortcutCode, loc);

                CFTCallStackFrame caller=new CFTCallStackFrame("<interactive-input>");
                Value ret = objGlobal.getRuntime().processFunction(stdio, caller, codeLines, new FunctionState(null,null));
                postProcessResult(ret);
                showSystemLog();

                return;
            }

            
            
            // History management - not bothering with shortcuts (processed above)
            try {
                Value currDir=new ValueObj(new ObjDir(objGlobal.getCurrDir(),Protection.NoProtection));
                Value command=new ValueString(line);
                
                SourceLocation loc=new SourceLocation("history", 0, 0);
                String code = propsFile.getHistoryCommand();

                FunctionBody codeLines = new FunctionBody(code, loc);

                CFTCallStackFrame caller=new CFTCallStackFrame("<interactive-input>");
                List<Value> args=new ArrayList<Value>();
                args.add(currDir);
                args.add(command);
                
                objGlobal.getRuntime().processFunction(stdio, caller, codeLines, new FunctionState(null,args));
            } catch (Exception ex) {
                stdio.addDebug("Failed calling historyCommand (CFT.props)");
            }
            
            
            
            // pre-processing input
            final boolean forceCftCode = line.startsWith(ShellCommandsManager.FORCE_CFT_CODE_PREFIX);
            if (forceCftCode) {
                line=line.substring(ShellCommandsManager.FORCE_CFT_CODE_PREFIX.length()).trim();
            } 
            
            
            if (!forceCftCode) {
                // check for interactive shell commands?
                
                Value v = (new ShellCommandsManager()).execute(stdio, objGlobal, line);
                if (v != null) {
                    postProcessResult(v);
                    showSystemLog();
                    return;
                }
            }
            
            if (line.startsWith(".") && !line.startsWith("."+File.separatorChar)) {
                // repeat previous command
                // ("./xxx" and ".\xxx" is assumed to be local programs to be run, see below)
                String currLine = currScriptCode.getCurrLine();
                if (currLine == null) {
                    stdio.println("ERROR: no current line");
                    return;
                }
                line = currLine + line.substring(1);
                stdio.println(line);
            } 
            
  
            
            
            
            // identify input tokens
            Lexer p = new Lexer();
            SourceLocation loc = new SourceLocation("input", 0, 0);
            p.processLine(new ScriptSourceLine(loc, line));
            ts = p.getTokenStream();

            // execute input

            if (ts.matchStr("/")) {
                boolean isPrivate=false;
                if (ts.matchStr("/")) {
                    isPrivate=true;
                }
                String ident = ts.matchIdentifier("expected name following '/' - for naming current program line");
                boolean force = ts.matchStr("!");
                if (!ts.atEOF())
                    throw new Exception("Expected '/ident' to save previous program line");
                boolean success;
                if (isPrivate) {
                    success = currScriptCode.assignPrivateName(ident, force);
                } else {
                    success = currScriptCode.assignPublicName(ident, force);
                }
                if (!success) {
                    stdio.println("ERROR: Symbol exists. Use /" + ident + "! to override");
                }
                return;
            }
            if (ts.matchStr("?")) {

                String ident = ts.matchIdentifier();
                
                if (ident != null) {
                    boolean colon = ts.matchStr(":");
                    if (colon) {
                        // colon means ident is a script name
                        ScriptState sstate=null;
                        try {
                            // try the "switch" functionality first
                            sstate = getScriptState(ident, false);
                        } catch (Exception ex) {
                            sstate=null;
                        }
                        
                        if (sstate==null) try {
                            // use "load"
                            sstate = getScriptState(ident, true);
                        } catch (Exception ex) {
                            sstate=null;
                        }
                        if (sstate==null) {
                            throw new Exception("No such script: " + ident);
                        }
                        
                        ScriptCode hist = sstate.getObjGlobal().getCurrScriptCode();

                        // script: may in turn be followed by another identifier for partial or
                        // complete match, as before
                        String ident2=ts.matchIdentifier();
                        if (ident2 != null) {
                            hist.report(stdio, ident2, false);
                        } else {
                            hist.reportAll(stdio, true);
                        }
                    } else {
                        // no colon
                        currScriptCode.report(stdio, ident, false);
                    }
                } else {
                    boolean publicOnly=true;
                    if (ts.matchStr("?")) {
                        publicOnly=false;
                    }
                    currScriptCode.reportAll(stdio,publicOnly);
                }
                String scriptName = objGlobal.getScriptName();
                if (scriptName != null) {
                    stdio.println("Current script name: " + scriptName);
                }
                return; // abort further processing
            }
            
            if (ts.matchStr(":")) {
                processColonCommand(ts,line);
                return;
            }

            // actually execute code line
            if (line.trim().length() > 0) {
                // program line
                currScriptCode.setCurrLine(line);
                CFTCallStackFrame caller=new CFTCallStackFrame("<interactive-input>");
                
                // ## Note: (v3.8.3) somewhat of a hack to differentiate between valid CFT input, 
                // CFT errors in script code, and OS functionality without the bang ("!")
                // Also modified the "." command so that "." + File.separator is NOT seen as
                // an attempt to repeat last
            
                boolean isCFTInput = true;

                // detect ./command syntax directly?
                if (line.trim().startsWith("."+File.separator)) {
                    isCFTInput=false;
                }
                
                // try parsing command line?
                if (isCFTInput) {
                    FunctionBody fbody=new FunctionBody(line,loc);
                    try {
                        fbody.getCodeSpaces();
                    } catch (Exception ex) {
                        isCFTInput=false;
                    }
                }
                
                // try executing CFT command?
                if (isCFTInput) try {
                    Value result = objGlobal.getRuntime().processFunction(stdio, caller, new FunctionBody(line, loc), new FunctionState(null,null));
                    postProcessResult(result);
                    showSystemLog();
                } catch (Exception ex) {
                    if (forceCftCode) throw ex;
                        // log below
                    
                    //System.out.println("---> " + ex.getMessage());
                    if (ex.getMessage().contains("<script>") 
                            || ex.getMessage().contains("[eof]"))  // parse problem in called code 
                    {
                        throw ex; // CFT script code error, log below
                    }
                    isCFTInput=false;
                }
                
                // try process as CFT bang command?
                if (!forceCftCode && !isCFTInput) {
                    final String shellCommandLine=ShellCommandsManager.FORCE_EXTERNAL_COMMAND_PREFIX+line;
                    //System.out.println(shellCommandLine);

                    Value x = (new ShellCommandsManager()).execute(stdio, objGlobal, shellCommandLine);
                    if (x != null) {
                        postProcessResult(x);
                        showSystemLog();
                        return;
                    }
                }
            
            }

        } catch (Throwable t) {
            stdio.println("ERROR: " + t.getMessage());
            stdio.showAndClearCFTCallStack();
            if (debugMode) {
                if (t instanceof SourceException) {
                    SourceException se=(SourceException) t;
                    if (se.getOriginalException() != null) {
                        // show original exception stack trace!
                        t=se.getOriginalException();
                    }
                }
                t.printStackTrace();
            }
        }
    }

    private void postProcessResult(Value result) throws Exception {
        if (result == null) {
            result = new ValueNull();
        }
        ObjGlobal objGlobal = currScript.getObjGlobal();

        // update lastResult
        lastResult = result;

        // present result
        ReportFormattingTool report = new ReportFormattingTool();
        List<String> lines = report.displayValueLines(result);
        int width = objTerm.getScreenWidth();

        Stdio stdio = objGlobal.getStdio();

        // Display lines cut off at screenWidth, for readability
        for (String s : lines) {
            if (s.length() > width - 1) {
                s = s.substring(0, width - 2) + "+";
            }
            stdio.println(s);
        }

    }

    public void showSystemLog() {
        ObjGlobal objGlobal = currScript.getObjGlobal();
        // System messages are written to screen - this applies to help texts etc
        List<String> messages = objGlobal.getSystemMessages();
        int limit = objTerm.getScreenWidth()-1;
        for (String s : messages) {
            String x="  # " + s;
            if (x.length() > limit) {
                x=x.substring(0,limit-1) + "+";
            }
            stdio.println(x);
        }

        objGlobal.clearSystemMessages();
    }

    private void processColonCommand(TokenStream ts, String inputLine) throws Exception {
        ObjGlobal objGlobal = currScript.getObjGlobal();
        ScriptCode codeHistory = objGlobal.getCurrScriptCode();

        if (ts.matchStr("quit")) {
            terminationFlag = true;
            return;
        }

        final int screenWidth = objTerm.getScreenWidth();

        if (ts.matchStr("save")) {
            String ident = ts.matchIdentifier(); // may be null
            if (ident == null) {
                ident = currScript.getScriptName();
            }
            if (ident == null) {
                throw new SourceException(ts.getSourceLocation(), "No save name");
            }
            
            currScript.getObjGlobal().saveCode(ident);

            String currName = currScript.getScriptName();
            if (!currName.equals(ident)) {
                // saving current script with new name
                scriptStates.remove(currName);
                    // remove currScript reference for old name 
                currScript.updateName(ident);
                scriptStates.put(ident, currScript);
            }
            return;
        } else if (ts.matchStr("load")) {
            String ident = ts.matchIdentifier(); // may be null
            currScript = getScriptState(ident, true);
            return;
        } else if (ts.matchStr("sw")) {
            String ident=null;
            if (ts.peekType(Token.TOK_IDENTIFIER)) {
                ident=ts.matchIdentifier("internal error");
            }
        
            Iterator<String> keys = scriptStates.keySet().iterator();
            boolean foundAny=false;

            String partialMatch=null;
            
            while (keys.hasNext()) {
                String scriptName=keys.next();
                if (scriptName.trim().length()==0) {
                    // the empty script
                    continue;
                }
                if (ident != null) {
                    if (scriptName.equals(ident)) {
                        // got an exact match, switching to it
                        currScript=getScriptState(scriptName, false);
                        return;
                    } else if (scriptName.contains(ident)) {
                        // got a match, switch to it
                        partialMatch=scriptName;
                    }
                } else {
                    stdio.println("- " + scriptName);
                    foundAny=true;
                }
            }
            if (partialMatch != null) {
                currScript=getScriptState(partialMatch, false);
                return;

            }
            if (!foundAny) {
                stdio.println("(no scripts loaded)");
            }
            return;

        } else if (ts.matchStr("delete")) {
            for (;;) {
                String ident = ts.matchIdentifier("expected identifier to be cleared");
                codeHistory.clear(ident);

                if (!ts.matchStr(",")) {
                    break;
                }
            }
            return;
        } else if (ts.matchStr("new")) {
            createNewScript();
            return;
        } else if (ts.matchStr("copy")) {
            String ident1 = ts.matchIdentifier("expected name of codeline to be copied");
            String ident2 = ts.matchIdentifier("expected target name");
            codeHistory.copy(ident1, ident2);
            return;
        } else if (ts.matchStr("debug")) {
            debugMode = !debugMode;
            if (debugMode) {
                stdio.println("DEBUG MODE ON. Repeat :debug command to turn off again.");
            } else {
                stdio.println("DEBUG MODE OFF");
            }
            return;
        } else if (ts.matchStr("wrap")) {
            boolean wrap = objTerm.changeWrap();
            if (wrap) {
                stdio.println("WRAP MODE ON. Repeat :wrap command to turn off again.");
            } else {
                stdio.println("WRAP MODE OFF (default)");
            }
            return;
        } else if (ts.matchStr("syn")) {
            if (lastResult == null) {
                stdio.println("No current value, can not synthesize");
                return;
            }
            String s = lastResult.synthesize();
            codeHistory.setCurrLine(s);
            stdio.println("synthesize ok");
            stdio.println("+-----------------------------------------------------");
            String line = "| .  : " + s;
            if (line.length() > screenWidth) {
                line = line.substring(0, screenWidth - 1) + "+";
            }
            stdio.println(line);
            stdio.println("+-----------------------------------------------------");
            stdio.println("Assign to name by /xxx as usual");
            return; // do not modify codeHistory
        } else if (ts.peekType(Token.TOK_INT)) {
            int pos = Integer.parseInt(ts.matchType(Token.TOK_INT).getStr());
            if (lastResult == null) {
                stdio.println("No current value");
                return;
            }
            if (!(lastResult instanceof ValueList)) {
                stdio.println("Current value not a list");
                return;
            }
            
            if (!ts.atEOF()) {
                // :N.expr
                String str=inputLine.trim().substring(1).trim(); // points at first digit
                int restPos=0;
                while (restPos < str.length() && "0123456789".indexOf(str.charAt(restPos)) >= 0) restPos++;
                String command="Sys.lastResult("+pos+")" + str.substring(restPos);
                
                SourceLocation loc = new SourceLocation(":N-expr", 0);

                FunctionBody codeLines = new FunctionBody(command, loc);

                CFTCallStackFrame caller=new CFTCallStackFrame("<interactive-input>");
                Value ret = objGlobal.getRuntime().processFunction(stdio, caller, codeLines, new FunctionState(null,null));
                postProcessResult(ret);
                showSystemLog();
                return;

            }

            List<Value> values = ((ValueList) lastResult).getVal();

            if (pos < 0 || pos >= values.size()) {
                stdio.println("Invalid index: " + pos);
                return;
            }

            Value theValue=values.get(pos);
            lastResult=theValue;
            
            String s = theValue.synthesize();
            codeHistory.setCurrLine(s);
            stdio.println("synthesize ok");
            stdio.println("+-----------------------------------------------------");
            String line = "| .  : " + s;
            if (line.length() > screenWidth) {
                line = line.substring(0, screenWidth - 1) + "+";
            }
            stdio.println(line);
            stdio.println("+-----------------------------------------------------");
            stdio.println("Assign to name by /xxx as usual");
            return;
        } else {
            stdio.println();
            stdio.println("Colon commands");
            stdio.println("--------------");
            stdio.println(":save [ident]?           - save script");
            stdio.println(":load [ident]?           - load script");
            stdio.println(":new                     - create new empty script");
            stdio.println(":sw [ident]?             - switch between loaded scripts");
            stdio.println(":delete ident [, ident]* - delete function(s)");
            stdio.println(":copy ident ident        - copy function");
            stdio.println(":wrap                    - line wrap on/off");
            stdio.println(":debug                   - enter or leave debug mode");
            stdio.println(":syn                     - synthesize last result");
            stdio.println(":<int>                   - synthesize a row from last result (must be list)");
            stdio.println(":quit                    - terminate CFT");
            stdio.println();
            return;
        }
    }

    private void copyrightNotice() {
        // avoid replacement by Copyright script
        String cop="Copyright" 
                    + " (C) 2020-2023"
                    + " Roar Foshaug";
        
        stdio.println("");
        stdio.println(cop);
        stdio.println("This program comes with ABSOLUTELY NO WARRANTY. See GNU GPL3.");
        stdio.println("This is free software, and you are welcome to redistribute it");
        stdio.println("under certain conditions. See GNU GPL3.");
        stdio.println("");
        stdio.println("You should have received a copy of the GNU General Public License");
        stdio.println("along with this program.  If not, see <https://www.gnu.org/licenses/>");
        stdio.println("");
        stdio.println("https://github.com/rfo909/CFT.git");
        stdio.println("");
    }

}
