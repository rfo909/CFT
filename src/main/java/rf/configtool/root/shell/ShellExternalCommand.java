/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2025 Roar Foshaug

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

public class ShellExternalCommand extends ShellCommand {
    
    @Override
    public String getName() {
        // Must correspond to ShellCommandsManager.FORCE_EXTERNAL_COMMAND_PREFIX
        return "<TAB>";
    }
    @Override 
    public String getBriefExampleParams() {
        return "... - run operating system command or program";
    }

    public Value execute(Ctx ctx, Command cmd) throws Exception {

        String currentDir = ctx.getObjGlobal().getCurrDir();
        List<Arg> args=cmd.getArgs();

        String commandName=cmd.getCommandName();

        List<String> command=new ArrayList<String>();
        command.add(commandName);
        
        
        boolean isLinux = File.separator.equals("/");
        
        NEXT_ARG: for (Arg arg:args) {
            if (arg.isExpr()) {
                FileSet fs=new FileSet(commandName, true, true); // directories and files
                fs.setIsSafeOperation();
                fs.processArg(currentDir, ctx, arg);
                for (String dir : fs.getDirectories()) command.add(dir);
                for (String file : fs.getFiles()) command.add(file);
            } else {
                String str=arg.getString();
                if (isLinux) {
                    // linux shells perform globbing expansion
                    FileSet fs=new FileSet(commandName, true, true); // directories and files
                    fs.setIsSafeOperation();
                    
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
                        if (fs.getDirectories().size() + fs.getFiles().size() > 0) {
                            continue NEXT_ARG;
                        }
                        // else: default processing is to add string as-is to command
                    }
                    // String failed to match files and directories: add string as-is to command 
                    command.add(str);
                } else {
                    // windows: always add string as-is, no glob-expansion here
                    command.add(str);
                    continue NEXT_ARG;
                }
            }
            
        }


        //StringBuffer sb=new StringBuffer();
        //for (String s:command) sb.append(s+" ");
        //System.out.println("*** ShellExternalCommand: " + sb.toString().trim());

        return callLambda (ctx, command);
    }

    /**
     * Call the bangCommand from the properties file, with parameters as single
     * List parameter.
     */
    private Value callLambda (Ctx ctx, List<String> command) throws Exception {
        List<Value> valueList=new ArrayList<Value>();
        for (String s:command) valueList.add(new ValueString(s));
        
        PropsFile propsFile=ctx.getObjGlobal().getRoot().getPropsFile();
        
        String code = propsFile.getRunExtCommand();
        SourceLocation loc = new SourceLocation("runExtCommand", 0, 0);
        FunctionBody codeLines = new FunctionBody(code, loc);

        List<Value> params=new ArrayList<Value>();
        params.add(new ValueList(valueList));
        CFTCallStackFrame caller=new CFTCallStackFrame("<runExtCommand>");
        return ctx.getObjGlobal().getRuntime().processFunction(ctx.getStdio(), caller, codeLines, new FunctionState(null,params));
        
    }            


}
