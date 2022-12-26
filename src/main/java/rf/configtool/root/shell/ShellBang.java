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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import rf.configtool.lexer.SourceLocation;
import rf.configtool.main.CFTCallStackFrame;
import rf.configtool.main.Ctx;
import rf.configtool.main.FunctionBody;
import rf.configtool.main.FunctionState;
import rf.configtool.main.PropsFile;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueNull;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.lib.ObjDir;
import rf.configtool.main.runtime.lib.Protection;

public class ShellBang extends ShellCommand {

    public ShellBang(List<String> parts) throws Exception {
        super(parts);
    }

    public Value execute(Ctx ctx) throws Exception {

        String currentDir = ctx.getObjGlobal().getCurrDir();
        List<ShellCommandArg> args=getArgs();

        String commandName=getName();

        List<String> command=new ArrayList<String>();
        command.add(commandName);
        
        
        boolean isLinux = File.separator.equals("/");
        
        NEXT_ARG: for (ShellCommandArg arg:args) {
        	if (arg.isExpr()) {
    			FileSet fs=new FileSet("!"+commandName, true, true); // directories and files
    			fs.processArg(currentDir, ctx, arg);
				for (String s : fs.getDirectories()) command.add(s);
				for (String s : fs.getFiles()) command.add(s);
        	} else {
        		String str=arg.getString();
    			if (isLinux) {
        			FileSet fs=new FileSet("!"+commandName, true, true); // directories and files
        			
        			boolean failed=false;
        			try {
        				fs.processArg(currentDir, ctx, arg);
        			} catch (Exception ex) {
        				failed=true;
        				// ignore this here
        			}
        			if (!failed) {
        				for (String s : fs.getDirectories()) command.add(s);
        				for (String s : fs.getFiles()) command.add(s);
        				if (fs.getDirectories().size() + fs.getFiles().size() > 0) continue NEXT_ARG;
        			}
    			} else {
    				// windows
    				command.add(str);
    				continue NEXT_ARG;
    			}
        		// otherwise
        		command.add(str);
        	}
        	
        }
    	return callLambda (ctx, command);
    }
    
    private Value callLambda (Ctx ctx, List<String> command) throws Exception {
    	List<Value> valueList=new ArrayList<Value>();
    	for (String s:command) valueList.add(new ValueString(s));
    	
        PropsFile propsFile=ctx.getObjGlobal().getRoot().getPropsFile();
    	
        String code = propsFile.getBangCommand();
        SourceLocation loc = new SourceLocation("bangCommand", 0, 0);
        FunctionBody codeLines = new FunctionBody(code, loc);

        List<Value> params=new ArrayList<Value>();
        params.add(new ValueList(valueList));
        CFTCallStackFrame caller=new CFTCallStackFrame("<bang-command>");
        return ctx.getObjGlobal().getRuntime().processFunction(ctx.getStdio(), caller, codeLines, new FunctionState(null,params));
        
        
//        String lambda=propsFile.getBangCommand();
//        
//        Value[] lambdaArgs= { new ValueObj(new ValueList(valueList)) };
//        return callConfiguredLambda("bangCommand", ctx, lambda, lambdaArgs);
    }            

    /*
    // Bang command
    
    if (line.startsWith("!")) {
        String str=line;
        if (str.startsWith("!")) {
            str = line.substring(1).trim();
        } else if (str.endsWith("!")) {
            str = line.substring(0,line.length()-1);
        }

        // Run the shell command parser
        String code = propsFile.getBangCommand();
        SourceLocation loc = new SourceLocation("bangCommand", 0, 0);
        FunctionBody codeLines = new FunctionBody(code, loc);

        List<Value> params=new ArrayList<Value>();
        params.add(new ValueString(str));
        CFTCallStackFrame caller=new CFTCallStackFrame("<bang-command>");
        objGlobal.getRuntime().processFunction(stdio, caller, codeLines, new FunctionState(null,params));
        return;
    } 
    */
    

}
