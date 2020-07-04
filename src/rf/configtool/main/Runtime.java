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

import java.io.File;
import java.io.PrintStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.*;

import rf.configtool.data.ProgramLine;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueNull;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.reporttool.Report;
import rf.configtool.parser.Parser;
import rf.configtool.parser.SourceLocation;
import rf.configtool.parser.Token;
import rf.configtool.parser.TokenStream;

/**
 * Executing one statement at a time, possibly saving last statement in symbol table.
 */
public class Runtime {
    
    public static final String PROGRAM_LINE_SEPARATOR="|"; // separates multiple ProgramLines on same line
    
    private ObjGlobal objGlobal;
    private CodeHistory codeHistory;
    private boolean debugMode=false;
    //private String saveIdentifier=null;
    private Value lastResult=null; 
    
    public Runtime (ObjGlobal objGlobal) {
        this.objGlobal=objGlobal;
        this.codeHistory=objGlobal.getCodeHistory();
    }

    public String getCommandProcessorShortName() {
        return null;
    }


    /**
     * Return true to cancel standard display of CodeHistory
     */
    private boolean processColonCommand(TokenStream ts, CodeHistory codeHistory) throws Exception {
        if (ts.matchStr("quit")) {
            objGlobal.setRuntime(null);
            return true;
        }
        if (ts.matchStr("save")) {
            String ident=ts.matchIdentifier(); // may be null
            objGlobal.saveCode(ident);
            return false;
        } else if (ts.matchStr("load")) {
            String ident=ts.matchIdentifier(); // may be null
            objGlobal.loadCode(ident);
            return false;
        } else if (ts.matchStr("delete")) {
            for (;;) {
                String ident=ts.matchIdentifier("expected identifier to be cleared");
                codeHistory.clear(ident);
                
                if (!ts.matchStr(",")) {
                    break;
                }
            }
            return false;
        } else if (ts.matchStr("copy")) {
            String ident1=ts.matchIdentifier("expected name of codeline to be copied");
            String ident2=ts.matchIdentifier("expected target name");
            codeHistory.copy(ident1,ident2);
            return false;
        } else if (ts.matchStr("debug")) {
            debugMode=!debugMode;
            if (debugMode) {
                objGlobal.outln("DEBUG MODE ON. Repeat :debug command to turn off again.");
            } else {
                objGlobal.outln("DEBUG MODE OFF");
            }
            return true;
        } else if (ts.matchStr("wrap")) {
            boolean wrap=objGlobal.getObjCfg().changeWrap();
            if (wrap) {
                objGlobal.outln("WRAP MODE ON. Repeat :wrap command to turn off again.");
            } else {
                objGlobal.outln("WRAP MODE OFF (default)");
            }
            return true;
        } else if (ts.matchStr("syn")) {
            if (lastResult==null) {
                objGlobal.outln("No current value, can not synthesize");
                return true;
            }
            String s=lastResult.synthesize();
            codeHistory.setCurrLine(s);
            objGlobal.outln("synthesize ok");
            objGlobal.outln("+-----------------------------------------------------");
            objGlobal.outln("| .  : " + s);
            objGlobal.outln("+-----------------------------------------------------");
            objGlobal.outln("Assign to name by /xxx as usual");
            return true; // do not modify codeHistory
        } else if (ts.peekType(Token.TOK_INT)) {
            int pos=Integer.parseInt(ts.matchType(Token.TOK_INT).getStr());
            if (lastResult==null) {
                objGlobal.outln("No current value");
                return true;
            }
            if (!(lastResult instanceof ValueList)) {
                objGlobal.outln("Current value not a list");
                return true;
            }

            List<Value> values=((ValueList) lastResult).getVal();
            
            if (pos < 0 || pos >= values.size()) {
                objGlobal.outln("Invalid index: " + pos);
                return true;
            }
                
            String s=values.get(pos).synthesize();
            codeHistory.setCurrLine(s);
            objGlobal.outln("synthesize ok");
            objGlobal.outln("+-----------------------------------------------------");
            objGlobal.outln("| .  : " + s);
            objGlobal.outln("+-----------------------------------------------------");
            objGlobal.outln("Assign to name by /xxx as usual");
            return true;
        } else {
            throw new Exception("Unknown command, try: quit, save, load, delete, copy, debug, wrap, syn or <int>");
        }
    }
        
    /**
     * Returns value from executing program line. Note may return java null if no return
     * value identified
     */
    public Value processCodeLines (CodeLines lines, FunctionState functionState) throws Exception {

        if (functionState==null) functionState=new FunctionState();
        
        TokenStream ts=lines.getTokenStream();

        // Multiple program lines in single text line, separated by '|'
        // Except for the first one, the others receive the output from the 
        // previous on top of stack, addressable via underscore, or directly
        // assigned to local variable
        
        List<ProgramLine> progLines=new ArrayList<ProgramLine>();
        for(;;) {
            progLines.add(new ProgramLine(ts));
            if (ts.matchStr(PROGRAM_LINE_SEPARATOR)) continue;
            break;
        }

        Value retVal=null;
        
        for (ProgramLine progLine:progLines) {
            Ctx ctx=new Ctx(objGlobal, functionState);
            if (retVal != null) ctx.push(retVal);
            
            progLine.execute(ctx);
            
            OutText outText=ctx.getOutText();
    
            // System messages are written to screen - this applies to help texts etc
            List<String> messages=outText.getSystemMessages();
            for (String s:messages) {
                objGlobal.outln("  # " + s);
            }
            
            // Column data is formatted to text and added to outData as String objects
            List<List<Value>> outData=outText.getData();
            Report report=new Report();
            List<String> formattedText=report.formatDataValues(outData);
            for (String s:formattedText) {
                ctx.getOutData().out(new ValueString(s));
            }
            
            retVal=ctx.getResult();
        }
        return retVal;
    }

    public void processInteractiveInput (String line) {
        line=line.trim();
        TokenStream ts=null;
        try {
            // load code if file has changed in the background, typically with editor
            objGlobal.refreshIfSavefileUpdated();
            
            // pre-processing input
            
            if (line.startsWith(".")) {
                // repeat previous command 
                String currLine=codeHistory.getCurrLine();
                if (currLine==null) {
                    objGlobal.outln("% ERROR: no current line");
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
                    
                    CodeLines codeLines=codeHistory.getNamedLine(str);
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
                processColonCommand(ts,codeHistory);
                return;
            }
            
            // actually execute code line
            if (line.trim().length() > 0) {
                // program line
                codeHistory.setCurrLine(line);
                Value result=processCodeLines(new CodeLines(line,loc), null);
                
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
            }
            
        } catch (Throwable t) {
            objGlobal.outln("% ERROR " + t.getClass().getName() + ": " + t.getMessage());
            if (debugMode) {
                t.printStackTrace();
                try {
                	objGlobal.outln("% INPUT: " + ts.showNextTokens(10));
                } catch (Exception ex) {
                	// ignore
                }
            }
        }
    }

}
