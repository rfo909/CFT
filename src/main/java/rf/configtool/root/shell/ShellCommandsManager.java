/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2023 Roar Foshaug

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

package rf.configtool.root.shell;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;
import rf.configtool.lexer.*;
import rf.configtool.main.Ctx;
import rf.configtool.main.FunctionState;
import rf.configtool.main.ObjGlobal;
import rf.configtool.main.ScriptSourceLine;
import rf.configtool.main.Stdio;
import rf.configtool.main.runtime.Value;

/**
 * Detecting and executing the "shell" commands: ls, cd, pwd ...
 *
 */
public class ShellCommandsManager {
	
	public static final String FORCE_EXTERNAL_COMMAND_PREFIX = "\t";
    
    public static final ShellCommand[] SHELL_COMMANDS = {
    		new ShellShell(),
    		new ShellLs("ls"),
    		new ShellLs("lsd"),
    		new ShellLs("lsf"),
    		new ShellCd(),
    		new ShellPwd(),
    		new ShellCatEditMoreTail("cat"),
    		new ShellCatEditMoreTail("edit"),
    		new ShellCatEditMoreTail("more"),
    		new ShellCatEditMoreTail("tail"),
    		new ShellTouch(),
    		
    		new ShellCp(),
    		new ShellRm(),
    		new ShellMv(),
    		new ShellMkdir(),
    		new ShellGrep(),

    		new ShellDiff(),
    		new ShellShowtree(),
    		new ShellHash(),
    		new ShellHex(),
    		new ShellWhich(),
    };

    
    public List<String> getShellCommandDescriptions() {
    	List<String> lines=new ArrayList<String>();
    	
    	ShellExternalCommand sb=new ShellExternalCommand();
    	lines.add(sb.getName() + " " + sb.getBriefExampleParams());

    	for (ShellCommand x:SHELL_COMMANDS) {
    		String desc=x.getBriefExampleParams();
    		if (desc==null) desc=""; else desc=" " + desc;
    		lines.add(x.getName()+desc);
    	}
    	
    	return lines;
    }
    

    
    /**
     * Execute shell command and return Value, or if the "line" is not a shell command,
     * return null.
     */
    public Value execute (Stdio stdio, ObjGlobal objGlobal, String line) throws Exception {
    	
        boolean forceExternalCommand=false;

        // forcing external command?
        if (line.startsWith(FORCE_EXTERNAL_COMMAND_PREFIX)) {
        	forceExternalCommand=true;
        }
        
        // Strip prefixing space and TAB
        // Can not use trim, as line may well end with TAB (to be replaced with '*') for globbing
        while (line.startsWith(" ") || line.startsWith("\t")) line=line.substring(1);
        
        
        ShellCommand foundCommand=null;
        
        if (!forceExternalCommand) {
        	for (ShellCommand c:SHELL_COMMANDS) {
        		String op=c.getName();
	            if (line.equals(op) || line.startsWith(op+" ") || line.startsWith(op+"(")) {
	            	foundCommand=c;
	            	break;
	            }
	        }
        }
        
        if (!forceExternalCommand && foundCommand==null) {
            return null;
        }
        
        // replace TAB character with '*'
        line=line.replace('\t', '*');
        
        // split the command line
        List<String> parts=parseLineParts(line);
        
        // create Command object from parts
        Command cmd=new Command(parts);
        
        if (forceExternalCommand) {
        	return executeShellCommand(stdio, objGlobal, new ShellExternalCommand(), cmd);
        } else {
        	return executeShellCommand(stdio, objGlobal, foundCommand, cmd);
        }
    }
    
    private Value executeShellCommand (Stdio stdio, ObjGlobal objGlobal, ShellCommand sc, Command cmd) throws Exception {
        FunctionState functionState=new FunctionState("<ShellCommand>"); // no function parameters
        Ctx ctx=new Ctx(stdio, objGlobal, functionState);
        return sc.execute(ctx, cmd);   
    }
    

    /**
     * Group line into parts, and unwrap strings outside ()'s but not inside.
     * Separated by space (outside strings and ()'s)
     */
    private List<String> parseLineParts (String line) throws Exception {
        List<String> parts=new ArrayList<String>();
        
        boolean isExpr=false; // triggered by first character % or : (symbol or Sys.lastResult)
        int parCount=0;
        boolean inString=false;
        char strQuote=' ';
        
        StringBuffer sb=new StringBuffer();
                
        CHARS: for (int pos=0; pos<line.length(); pos++) {
            final char c=line.charAt(pos);
            
            if (inString) {
                if (c==strQuote) {
                    inString=false;
                    if (isExpr) sb.append(c);
                } else {
                    sb.append(c);
                }
                continue CHARS;
            }
            
            // !inString
            
            if (c=='%' || c==':') {
                isExpr=true;
            }
                
            if (parCount==0 && (c=='\'' || c=='"')) {
                inString=true;
                strQuote=c;
                if (isExpr) sb.append(c);
                continue CHARS;
            }
            
            if (parCount > 0 || c=='(') {
                if (parCount==0 && !isExpr && sb.length() > 0) {
                    parts.add(sb.toString());
                    sb=new StringBuffer();
                }
                if (c=='(') { 
                    sb.append(c); 
                    parCount++; 
                } else if (c==')') {
                    sb.append(c); 
                    parCount--; 
                    if (parCount == 0 && !isExpr) {
                        parts.add(sb.toString());
                        sb=new StringBuffer();
                    }
                } else {
                    sb.append(c);
                }
                continue CHARS;
            } 

            // outside strings and ()'s parts are separated by space
            if (c==' ') {
                isExpr=false;
                if (sb.length()>0) {
                    parts.add(sb.toString());
                    sb=new StringBuffer();
                    continue CHARS;
                }
            } else {
                sb.append(c);
            }
        }
        if (parCount > 0) {
            throw new Exception("Unbalanced ()'s");
        }
        if (inString) {
            throw new Exception("Unterminated string");
        }
        if (sb.length()>0) {
            parts.add(sb.toString());
        }
        
        return parts;
    }
    
    

    
    
    

}
