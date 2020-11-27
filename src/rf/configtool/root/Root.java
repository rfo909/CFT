package rf.configtool.root;

import java.io.*;
import java.util.*;

import rf.configtool.main.CodeHistory;
import rf.configtool.main.CodeLine;
import rf.configtool.main.CodeLines;
import rf.configtool.main.Ctx;
import rf.configtool.main.FunctionState;
import rf.configtool.main.ObjCfg;
import rf.configtool.main.ObjGlobal;
import rf.configtool.main.PropsFile;
import rf.configtool.main.SourceException;
import rf.configtool.main.Stdio;
import rf.configtool.main.StdioReal;
import rf.configtool.main.Version;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueBlock;
import rf.configtool.main.runtime.ValueNull;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.reporttool.Report;
import rf.configtool.parser.Parser;
import rf.configtool.parser.SourceLocation;
import rf.configtool.parser.Token;
import rf.configtool.parser.TokenStream;

/**
 * The Root class manages a set of parallel script contexts.
 */
public class Root {
	
	private final String sessionUUID;
    


	private static final Version VERSION = new Version();

	private StdioReal stdio;
	private PropsFile propsFile;
    private ObjCfg objCfg;

	private Map<String, ScriptState> scriptStates = new HashMap<String, ScriptState>();
	private ScriptState currScript;
	private boolean debugMode;
	private Value lastResult;
	private final long startTime;
	private boolean terminationFlag = false;
	
    /**
     * Unique value per CFT session, available via Sys.sessionUUID CFT function
     */
    public String getSessionUUID() {
    	return sessionUUID;
    } 

	public Root(StdioReal stdio, String customScriptDir) throws Exception {
    	this.sessionUUID = UUID.randomUUID().toString();
		this.startTime=System.currentTimeMillis();
		this.stdio = stdio;
    	propsFile=new PropsFile(customScriptDir);
        objCfg=new ObjCfg();

        createNewScript();
	}
	
	public long getStartTime() {
		return startTime;
	}
	
	public PropsFile getPropsFile() {
		return propsFile;
	}

    
    public ObjCfg getObjCfg() {
        return objCfg;
    }
    
    public boolean isDebugMode() {
    	return debugMode;
    }

	public void loadScript(String scriptName) throws Exception {
		currScript = getScriptState(scriptName, true);
	}

	public void setInitialCommands(List<String> initialCommands) {
		for (String line : initialCommands)
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

	private void processSave(String newName) throws Exception {
		// user has typed :save name
		currScript.getObjGlobal().saveCode(newName);

		String currName = currScript.getScriptName();
		if (!currName.equals(newName)) {
			scriptStates.remove(currName);
			currScript.updateName(newName);
			scriptStates.put(newName, currScript);
		}
	}

	public ScriptState getScriptState(String name, boolean isLoad) throws Exception {
		if (name == null || name.equals(currScript.getScriptName())) {
			if (isLoad)
				currScript.getObjGlobal().loadCode(null); // reloads code - overwrite any local changes
			return currScript;
		}
		ScriptState otherScript = scriptStates.get(name);
		if (otherScript != null) {
			if (isLoad)
				otherScript.getObjGlobal().loadCode(otherScript.getScriptName());
			return otherScript;
		}
		ScriptState newScript = new ScriptState(name, new ObjGlobal(this, stdio)); // throws exception if there is
																					// trouble
		scriptStates.put(newScript.getScriptName(), newScript);
		return newScript;
	}

	public void createNewScript() throws Exception {
		currScript = new ScriptState(new ObjGlobal(this, stdio));
		scriptStates.put(currScript.getScriptName(), currScript);
	}

	private String getScriptStateNames() {
		StringBuffer sb = new StringBuffer();
		Iterator<String> keys = scriptStates.keySet().iterator();
		while (keys.hasNext()) {
			sb.append(" '" + keys.next() + "'");
		}
		return sb.toString().trim();
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

	// Moved here from Main
	public void inputLoop() {
		copyrightNotice();
		stdio.println(VERSION.getVersion());


		try {
			for (;;) {


				if (terminationFlag) {
					stdio.println("Runtime exit, cleaning up");
					cleanupOnExit();
					return;
				}
				
				ObjGlobal objGlobal = currScript.getObjGlobal();


				if (!stdio.hasBufferedInputLines()) {
					// Only produce prompt when non-buffered lines
					
					// Run the prompt code line to produce possibly dynamic prompt
					String promptCode = propsFile.getPromptCode();
					SourceLocation loc = new SourceLocation("prompt", 0, 0);
					CodeLines codeLines = new CodeLines(promptCode, loc);
	
					String pre;
					try {
						Value ret = objGlobal.getRuntime().processCodeLines(stdio, codeLines, new FunctionState());
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
				String line = stdio.getInputLine().trim();

				refreshIfSavefileUpdated();
				propsFile.refreshFromFile();

				
				processInteractiveInput(line);

			}
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
		CodeHistory codeHistory = objGlobal.getCodeHistory();

		try {
			// Shortcuts

			String shortcutPrefix = propsFile.getShortcutPrefix();
			if (line.startsWith(shortcutPrefix)) {
				String shortcutName = line.substring(shortcutPrefix.length()).trim();
				String shortcutCode = propsFile.getShortcutCode(shortcutName);
				SourceLocation loc = new SourceLocation("shortcut:" + shortcutName, 0, 0);

				CodeLines codeLines = new CodeLines(shortcutCode, loc);

				Value ret = objGlobal.getRuntime().processCodeLines(stdio, codeLines, new FunctionState());
				postProcessResult(ret);
				showSystemLog();

				return;
			}

			// pre-processing input

			if (line.startsWith(".")) {
				// repeat previous command
				String currLine = codeHistory.getCurrLine();
				if (currLine == null) {
					stdio.println("ERROR: no current line");
					return;
				}
				line = currLine + line.substring(1);
				stdio.println("$ " + line);
			} else if (line.startsWith("!")) {
				int pos = line.indexOf("!", 1);
				if (pos > 0) {
					String str = line.substring(1, pos);

					// Look for inner pattern
					String pattern = null;
					int colon = str.indexOf(':');
					if (colon > 0) {
						pattern = str.substring(colon + 1);
						str = str.substring(0, colon);
					}

					CodeLines codeLines = codeHistory.getNamedCodeLines(str);
					if (codeLines != null) {
						if (codeLines.hasMultipleCodeLines()) {
							stdio.println("Function '" + str + "' is not a single line of code");
							return;
						}
						String codeLine = codeLines.getFirstNonBlankLine();
						if (pattern != null) {
							int cutoffPos = codeLine.indexOf(pattern);
							if (cutoffPos > 0) {
								codeLine = codeLine.substring(0, cutoffPos);
							}
						}
						line = codeLine + line.substring(pos + 1);
						stdio.println("----> " + line);
					} else {
						stdio.println("No function '" + str + "' - Usage: !ident! or !ident:pattern!...");
						return;
					}

				}
			}

			// identify input tokens
			Parser p = new Parser();
			SourceLocation loc = new SourceLocation("input", 0, 0);
			p.processLine(new CodeLine(loc, line));
			ts = p.getTokenStream();

			// execute input

			if (ts.matchStr("/")) {
				String ident = ts.matchIdentifier("expected name following '/' - for naming current program line");
				boolean force = ts.matchStr("!");
				if (!ts.atEOF())
					throw new Exception("Expected '/ident' to save previous program line");
				boolean success = codeHistory.assignName(ident, force);
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
						
						CodeHistory hist = sstate.getObjGlobal().getCodeHistory();

						// script: may in turn be followed by another identifier for partial or
						// complete match, as before
						String ident2=ts.matchIdentifier();
						if (ident2 != null) {
							hist.report(stdio, ident2);
						} else {
							hist.reportAll(stdio);
						}
					} else {
						// no colon
						codeHistory.report(stdio, ident);
					}
				} else {
					codeHistory.reportAll(stdio);
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
				codeHistory.setCurrLine(line);
				Value result = objGlobal.getRuntime().processCodeLines(stdio, new CodeLines(line, loc), null);

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
			if (debugMode) {
				if (t instanceof SourceException) {
					SourceException se=(SourceException) t;
					if (se.getOriginalException() != null) {
						// show original exception stack trace!
						t=se.getOriginalException();
					}
				}
				t.printStackTrace();
//				try {
//					objGlobal.outln("INPUT: " + ts.showNextTokens(10));
//				} catch (Exception ex) {
//					// ignore
//				}
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
		int width = objCfg.getScreenWidth();

		Stdio stdio = objGlobal.getStdioActual();

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
		int limit = objCfg.getScreenWidth()-1;
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
		CodeHistory codeHistory = objGlobal.getCodeHistory();

		if (ts.matchStr("quit")) {
			terminationFlag = true;
			return;
		}

		final int screenWidth = objCfg.getScreenWidth();

		if (ts.matchStr("save")) {
			String ident = ts.matchIdentifier(); // may be null
			if (ident == null)
				ident = currScript.getScriptName();
			if (ident == null) {
				throw new SourceException(ts.getSourceLocation(), "No save name");
			}
			processSave(ident); // maintain map
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
			//objGlobal.outln("Loaded scripts: " + getScriptStateNames());
			return;
		} else if (ts.matchStr("wrap")) {
			boolean wrap = objCfg.changeWrap();
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

			String s = values.get(pos).synthesize();
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
		stdio.println("");
		stdio.println("CFT (\"ConfigTool\") Copyright (c) 2020 Roar Foshaug");
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
