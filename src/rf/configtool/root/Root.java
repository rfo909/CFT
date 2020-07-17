package rf.configtool.root;

import java.io.*;
import java.util.*;

import rf.configtool.main.CodeHistory;
import rf.configtool.main.CodeLine;
import rf.configtool.main.CodeLines;
import rf.configtool.main.ObjCfg;
import rf.configtool.main.ObjGlobal;
import rf.configtool.main.Stdio;
import rf.configtool.main.Version;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueNull;
import rf.configtool.main.runtime.reporttool.Report;
import rf.configtool.parser.Parser;
import rf.configtool.parser.SourceLocation;
import rf.configtool.parser.Token;
import rf.configtool.parser.TokenStream;

/**
 * EXPERIMENTAL
 * 
 * The Root class manages a set of parallel script contexts. It also handles
 * which stdio to connect to among those, in effect letting the user switch
 * between different objects.
 */
public class Root {

	private static final Version VERSION = new Version();


	private Stdio stdio;
	
	private Map<String, ScriptState> scriptStates = new HashMap<String, ScriptState>();
	private ScriptState currScript;
	private boolean debugMode;
	private Value lastResult;
	private boolean terminationFlag = false;
	
	public Root(Stdio stdio) throws Exception {
		this.stdio = stdio;
		currScript = new ScriptState(new ObjGlobal(this,stdio));
		scriptStates.put(currScript.getScriptName(), currScript);
	}
	
	public void loadScript (String scriptName) throws Exception {
		currScript = getScriptState(scriptName);
	}

	public void setInitialCommands(List<String> initialCommands) {
		for (String line : initialCommands)
			stdio.addBufferedInputLine(line);
	}

	private void refreshIfSavefileUpdated() throws Exception {
		Iterator<String> keys = scriptStates.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			ScriptState x = scriptStates.get(key);
			x.getObjGlobal().refreshIfSavefileUpdated();
		}
	}
	
	private void processSave (String newName) throws Exception {
		// user has typed :save name
		currScript.getObjGlobal().saveCode(newName);

		String currName=currScript.getScriptName();
		if (!currName.equals(newName)) {
			scriptStates.remove(currName);
			currScript.updateName(newName);
			scriptStates.put(newName, currScript);
		}
	}
	
	public ScriptState getScriptState (String name) throws Exception {
		ObjGlobal objGlobal = currScript.getObjGlobal();
		if (name == null || name.equals(currScript.getScriptName())) {
			//objGlobal.loadCode(null); // reloads code - pointless as code is reloaded automatically when changed, but okay
			return currScript;
		}
		ScriptState otherScript=scriptStates.get(name);
		if (otherScript != null) {
			return otherScript;
//			currScript=otherScript;
//			return;
		}
		ScriptState newScript=new ScriptState(name, new ObjGlobal(this,stdio));  // throws exception if there is trouble
		scriptStates.put(newScript.getScriptName(), newScript);
		return newScript;
	}
	
	private void cleanupOnExit () throws Exception {
		Iterator<String> keys = scriptStates.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			ScriptState x = scriptStates.get(key);
			x.getObjGlobal().cleanupOnExit();
		}
	}
	
	// Moved here from Main
	public void inputLoop() {
		copyrightNotice();
		stdio.println(VERSION.getVersion());

		ObjGlobal objGlobal = currScript.getObjGlobal();

		try {
			for (;;) {

				if (terminationFlag) {
					stdio.println("Runtime exit, cleaning up");
					cleanupOnExit();
					return;
				}

				String pre = "$";

				// Stdio can only do line output, so using System.out directly
				System.out.print(pre + " ");
				String line = stdio.getInputLine().trim();
				
				processInteractiveInput(line);

				//objGlobal.getRuntime().processInteractiveInput(line);

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Moved here from Runtime
	 */
	public void processInteractiveInput (String line) throws Exception {
        line=line.trim();
        TokenStream ts=null;
        ObjGlobal objGlobal=currScript.getObjGlobal();
    	CodeHistory codeHistory=objGlobal.getCodeHistory();
    	
    	refreshIfSavefileUpdated();

        try {
            // load code if file has changed in the background, typically with editor
            // pre-processing input
            
            if (line.startsWith(".")) {
                // repeat previous command 
                String currLine=codeHistory.getCurrLine();
                if (currLine==null) {
                    objGlobal.outln("ERROR: no current line");
                    return;
                }
                line=currLine + line.substring(1); 
                objGlobal.outln("$ " + line);
            } 
            else if (line.startsWith("!")) {
                int pos=line.indexOf("!",1);
                if (pos > 0) {
                    String str=line.substring(1, pos);
                    
                    // Look for inner pattern
                    String pattern=null;
                    int colon=str.indexOf(':');
                    if (colon>0) {
                        pattern=str.substring(colon+1);
                        str=str.substring(0,colon);
                    }
                    
                    CodeLines codeLines=codeHistory.getNamedCodeLines(str);
                    if (codeLines != null) {
                        if (codeLines.hasMultipleCodeLines()) {
                            objGlobal.outln("Function '" + str + "' is not a single line of code");
                            return;
                        }
                        String codeLine=codeLines.getFirstNonBlankLine();
                        if (pattern != null) {
                            int cutoffPos=codeLine.indexOf(pattern);
                            if (cutoffPos > 0) {
                                codeLine=codeLine.substring(0,cutoffPos);
                            }
                        }
                        line=codeLine+line.substring(pos+1);
                        objGlobal.outln("----> " + line);
                    } else {
                        objGlobal.outln("No function '" + str + "' - Usage: !ident! or !ident:pattern!...");
                        return;
                    }
                    
                }
            }
              
            // identify input tokens
            Parser p=new Parser();
            SourceLocation loc=new SourceLocation("input",0,0);
            p.processLine(new CodeLine(loc, line));
            ts=p.getTokenStream();
            
            // execute input
            
            if (ts.matchStr("/")) {
                String ident=ts.matchIdentifier("expected name following '/' - for naming current program line");
                boolean force=ts.matchStr("!");
                if (!ts.atEOF()) throw new Exception("Expected '/ident' to save previous program line");
                boolean success=codeHistory.assignName(ident, force);
                if (!success) {
                    objGlobal.outln("ERROR: Symbol exists. Use /" + ident + "! to override");
                }
                return;
            } 
            if (ts.matchStr("?")) {
                
                String ident=ts.matchIdentifier();
                if (ident != null) {
                    codeHistory.report(ident);
                } else {
                    codeHistory.reportAll();
                }
                String savename=objGlobal.getSavename();
                if (savename != null) objGlobal.outln("Current save name: " + savename);
                return; // abort further processing
            }
            if (ts.matchStr(":")) {
                processColonCommand(ts);
                return;
            }
            
            // actually execute code line
            if (line.trim().length() > 0) {
                // program line
                codeHistory.setCurrLine(line);
                Value result=objGlobal.getRuntime().processCodeLines(new CodeLines(line,loc), null);
                
                if (result==null) result=new ValueNull();
                
                lastResult=result;

                Report report=new Report();
                List<String> lines=report.displayValueLines(result);
                ObjCfg cfg = objGlobal.getObjCfg();
                int width=cfg.getScreenWidth();
                
                Stdio stdio=objGlobal.getStdio();
    
                // Display lines cut off at screenWidth, for readability
                for (String s:lines) {
                    if (s.length() > width-1) {
                        s=s.substring(0,width-2)+"+";
                    }
                    stdio.println(s);
                }
                
                
                
                // System messages are written to screen - this applies to help texts etc
                List<String> messages=objGlobal.getSystemMessages();
                for (String s:messages) {
                    objGlobal.outln("  # " + s);
                }
                
                objGlobal.clearSystemMessages();
                

            }
            
        } catch (Throwable t) {
            objGlobal.outln("ERROR " + t.getClass().getName() + ": " + t.getMessage());
            if (debugMode) {
                t.printStackTrace();
                try {
                	objGlobal.outln("INPUT: " + ts.showNextTokens(10));
                } catch (Exception ex) {
                	// ignore
                }
            }
        }
    }

	private void processColonCommand(TokenStream ts) throws Exception {
		ObjGlobal objGlobal=currScript.getObjGlobal();
		CodeHistory codeHistory=objGlobal.getCodeHistory();
		
		if (ts.matchStr("quit")) {
			terminationFlag=true;
			return;
		}
		
		final int screenWidth=objGlobal.getObjCfg().getScreenWidth();

		if (ts.matchStr("save")) {
			String ident = ts.matchIdentifier(); // may be null
			if (ident==null) ident=currScript.getScriptName();
			if (ident==null) {
				throw new Exception("No save name");
			}
			processSave(ident); // maintain map
			return;
		} else if (ts.matchStr("load")) {
			String ident = ts.matchIdentifier(); // may be null
			currScript = getScriptState(ident);
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
		} else if (ts.matchStr("copy")) {
			String ident1 = ts.matchIdentifier("expected name of codeline to be copied");
			String ident2 = ts.matchIdentifier("expected target name");
			codeHistory.copy(ident1, ident2);
			return;
		} else if (ts.matchStr("debug")) {
			debugMode = !debugMode;
			if (debugMode) {
				objGlobal.outln("DEBUG MODE ON. Repeat :debug command to turn off again.");
			} else {
				objGlobal.outln("DEBUG MODE OFF");
			}
			return;
		} else if (ts.matchStr("wrap")) {
			boolean wrap = objGlobal.getObjCfg().changeWrap();
			if (wrap) {
				objGlobal.outln("WRAP MODE ON. Repeat :wrap command to turn off again.");
			} else {
				objGlobal.outln("WRAP MODE OFF (default)");
			}
			return;
		} else if (ts.matchStr("syn")) {
			if (lastResult == null) {
				objGlobal.outln("No current value, can not synthesize");
				return;
			}
			String s = lastResult.synthesize();
			codeHistory.setCurrLine(s);
			objGlobal.outln("synthesize ok");
			objGlobal.outln("+-----------------------------------------------------");
			String line="| .  : " + s;
			if (line.length() > screenWidth) {
				line=line.substring(0,screenWidth-1)+"+";
			}
			objGlobal.outln(line);
			objGlobal.outln("+-----------------------------------------------------");
			objGlobal.outln("Assign to name by /xxx as usual");
			return; // do not modify codeHistory
		} else if (ts.peekType(Token.TOK_INT)) {
			int pos = Integer.parseInt(ts.matchType(Token.TOK_INT).getStr());
			if (lastResult == null) {
				objGlobal.outln("No current value");
				return;
			}
			if (!(lastResult instanceof ValueList)) {
				objGlobal.outln("Current value not a list");
				return;
			}

			List<Value> values = ((ValueList) lastResult).getVal();

			if (pos < 0 || pos >= values.size()) {
				objGlobal.outln("Invalid index: " + pos);
				return;
			}

			String s = values.get(pos).synthesize();
			codeHistory.setCurrLine(s);
			objGlobal.outln("synthesize ok");
			objGlobal.outln("+-----------------------------------------------------");
			String line="| .  : " + s;
			if (line.length() > screenWidth) {
				line=line.substring(0,screenWidth-1)+"+";
			}
			objGlobal.outln(line);
			objGlobal.outln("+-----------------------------------------------------");
			objGlobal.outln("Assign to name by /xxx as usual");
			return;
		} else {
			throw new Exception("Unknown command, try: quit, save, load, delete, copy, debug, wrap, syn or <int>");
		}
	}

	
	

	private void copyrightNotice() {
		stdio.println("");
		stdio.println("CFT (\"ConfigTool\") Copyright (c) 2020 Roar Foshaug");
		stdio.println("This program comes with ABSOLUTELY NO WARRANTY. See GNU GPL3.");
		stdio.println("This is free software, and you are welcome to redistribute it");
		stdio.println("under certain conditions. See GNU GPL3.");
		stdio.println("");
		stdio.println("You should have received a copy of the GNU General Public License");
		stdio.println("along with this program.  If not, see <https://www.gnu.org/licenses/>");
		stdio.println("");
	}


}
