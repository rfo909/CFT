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


public class ShellDiff extends ShellCommand {

    public ShellDiff(List<String> parts) throws Exception {
        super(parts);
    }

    public Value execute(Ctx ctx) throws Exception {
        
        final String currentDir = ctx.getObjGlobal().getCurrDir();
        List<ShellCommandArg> args=getArgs();
        String name=getName();

        if (args.size() != 2) throw new Exception(getName() + ": expected two files");
        
        FileSet fs1=new FileSet(name,false, true);
        fs1.setIsSafeOperation();
        
        fs1.processArg(currentDir, ctx, args.get(0));

        FileSet fs2=new FileSet(name, false, true);
        fs2.setIsSafeOperation();
        
        fs2.processArg(currentDir, ctx, args.get(1));

        List<String> list1=fs1.getFiles();
        List<String> list2=fs2.getFiles();
        
        if (list1.size() != 1 || list2.size() != 1) throw new Exception(getName() + ": invalid arguments");
        
        Value p1 = new ValueObj(new ObjFile(list1.get(0),Protection.NoProtection));
        Value p2 = new ValueObj(new ObjFile(list2.get(0),Protection.NoProtection));
        
        
        PropsFile propsFile=ctx.getObjGlobal().getRoot().getPropsFile();
        String lambda=propsFile.getMDiff();
        Value[] lambdaArgs= {p1,p2};

        return callConfiguredLambda(getName(), ctx, lambda, lambdaArgs);
    }


}
