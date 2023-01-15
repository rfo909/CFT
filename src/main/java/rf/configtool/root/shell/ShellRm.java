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
import rf.configtool.main.runtime.ValueBlock;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.lib.ObjDir;
import rf.configtool.main.runtime.lib.ObjFile;
import rf.configtool.main.runtime.lib.Protection;


public class ShellRm extends ShellCommand {

	@Override
	public String getName() {
		return "rm";
	}
	@Override 
	public String getBriefExampleParams() {
		return "<file/dir> ...";
	}



    public Value execute(Ctx ctx, Command cmd) throws Exception {
        
        final String currentDir = ctx.getObjGlobal().getCurrDir();
        String name=cmd.getCommandName();
        List<Arg> args=cmd.getArgs();
        
        FileSet fs=new FileSet(name,true,true); // files and directories
        for (Arg arg:args) {
            fs.processArg(currentDir, ctx, arg);
        }
        
        List<Value> result=new ArrayList<Value>();
        for (String fname : fs.getFiles()) {
            result.add(new ValueObj(new ObjFile(fname, Protection.NoProtection)));
        }
        for (String dname : fs.getDirectories()) {
            result.add(new ValueObj(new ObjDir(dname, Protection.NoProtection)));
        }
        
        
        PropsFile propsFile=ctx.getObjGlobal().getRoot().getPropsFile();
        String lambda=propsFile.getMRm();
        Value[] lambdaArgs= {new ValueList(result)};

        return callConfiguredLambda(name, ctx, lambda, lambdaArgs);
    }


}
