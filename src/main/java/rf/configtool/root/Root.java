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
import rf.configtool.main.CodeLine;
import rf.configtool.main.CodeLines;
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
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.reporttool.Report;

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
                    CodeLines promptCodeLines = new CodeLines(promptCode, loc);
    
                    String pre;
                    try {
                    	CFTCallStackFrame caller=new CFTCallStackFrame("<interactive-input>");

                        Value ret = objGlobal.getRuntime().processCodeLines(stdio, caller, promptCodeLines, new FunctionState(null,null));
                        pre=ret.getValAsString();
                    } catch (Exception ex) {
                        if (debugMode) {
                            pre="ERROR";
                            ex.printStackTrace();
                        } else {
                            pre="$";
                        }
                    }
    
                    // Stdio can only do line output, so using System.out directly
                    stdio.print(pre);
                }
                String line = null;
                try {
                    line = stdio.getInputLine().trim();
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
    public void processInteractiveInput(String line) throws Exception {
        line = line.trim();
        TokenStream ts = null;
        ObjGlobal objGlobal = currScript.getObjGlobal();
        ScriptCode currScriptCode = objGlobal.getCodeHistory();

        stdio.clearCallStack();
        
        try {
            // Shortcuts

            String shortcutPrefix = propsFile.getShortcutPrefix();
            if (line.startsWith(shortcutPrefix)) {
                String shortcutName = line.substring(shortcutPrefix.length()).trim();
                String shortcutCode = propsFile.getShortcutCode(shortcutName);
                SourceLocation loc = new SourceLocation("shortcut:" + shortcutName, 0, 0);

                CodeLines codeLines = new CodeLines(shortcutCode, loc);

            	CFTCallStackFrame caller=new CFTCallStackFrame("<interactive-input>");
                Value ret = objGlobal.getRuntime().processCodeLines(stdio, caller, codeLines, new FunctionState(null,null));
                postProcessResult(ret);
                showSystemLog();

                return;
            }
            
            // Bang command
            
            if (line.startsWith("!")) {
                String str=line.substring(1).trim();

                // Run the shell command parser
                String code = propsFile.getBangCommand();
                SourceLocation loc = new SourceLocation("bangCommand", 0, 0);
                CodeLines codeLines = new CodeLines(code, loc);

                List<Value> params=new ArrayList<Value>();
                params.add(new ValueString(str));
            	CFTCallStackFrame caller=new CFTCallStackFrame("<bang-command>");
                objGlobal.getRuntime().processCodeLines(stdio, caller, codeLines, new FunctionState(null,params));
                return;
            } 


            // pre-processing input

            if (line.startsWith(".")) {
                // repeat previous command
                String currLine = currScriptCode.getCurrLine();
                if (currLine == null) {
                    stdio.println("ERROR: no current line");
                    return;
                }
                line = currLine + line.substring(1);
                stdio.println("$ " + line);
            } 
            // identify input tokens
            Lexer p = new Lexer();
            SourceLocation loc = new SourceLocation("input", 0, 0);
            p.processLine(new CodeLine(loc, line));
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
                        
                        ScriptCode hist = sstate.getObjGlobal().getCodeHistory();

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
                processColonCommand(ts);
                return;
            }

            // actually execute code line
            if (line.trim().length() > 0) {
                // program line
                currScriptCode.setCurrLine(line);
            	CFTCallStackFrame caller=new CFTCallStackFrame("<interactive-input>");

                Value result = objGlobal.getRuntime().processCodeLines(stdio, caller, new CodeLines(line, loc), new FunctionState(null,null));

                postProcessResult(result);
                showSystemLog();
            }

        } catch (Throwable t) {
            try {
                showSystemLog(); 
            } catch (Exception ex) {
                // ignore
            }
            stdio.println("ERROR: " + t.getMessage());
            stdio.showCFTCallStack();
            if (debugMode) {
                if (t instanceof SourceException) {
                    SourceException se=(SourceException) t;
                    if (se.getOriginalException() != null) {
                        // show original exception stack trace!
                        t=se.getOriginalException();
                    }
                }
                t.printStackTrace();
//              try {
//                  objGlobal.outln("INPUT: " + ts.showNextTokens(10));
//              } catch (Exception ex) {
//                  // ignore
//              }
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
        Report report = new Report();
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

    private void processColonCommand(TokenStream ts) throws Exception {
        ObjGlobal objGlobal = currScript.getObjGlobal();
        ScriptCode codeHistory = objGlobal.getCodeHistory();

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
                    + " (C) 2020-2022"
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
