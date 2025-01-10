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

import java.util.ArrayList;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.PropsFile;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueNull;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.lib.ObjFile;
import rf.configtool.main.runtime.lib.Protection;



public class ShellHash extends ShellCommand {

    @Override
    public String getName() {
        return "hash";
    }
    @Override 
    public String getBriefExampleParams() {
        return "<file>? ...";
    }


    public Value execute(Ctx ctx, Command cmd) throws Exception {

        String name=cmd.getCommandName();
        
        String currentDir = ctx.getObjGlobal().getCurrDir();
        boolean noArgs=cmd.getArgs().isEmpty();
        
        if (noArgs) {
            return callMacro(ctx, name, new ValueList(new ArrayList<Value>())); // empty list
        }
        
        List<Arg> args=cmd.getArgs();
        
        FileSet fs=new FileSet(name,false,true);  // files only
        fs.setIsSafeOperation();
        
        for (Arg arg:args) {
            fs.processArg(currentDir, ctx, arg);  // no unknown files or dirs
        }
        
        List<String> files=fs.getFiles();
        if (files.size() < 1) throw new Exception(name + ": Expected one or more files");
        
        List<Value> list = new ArrayList<Value>();
        
        for (String filename:files) {
            list.add(new ValueObj(new ObjFile(filename, Protection.NoProtection)));
        }
        
        return callMacro(ctx, name, new ValueList(list));
    }

    private Value callMacro(Ctx ctx, String name, Value fileList) throws Exception {

        PropsFile propsFile = ctx.getObjGlobal().getRoot().getPropsFile();
        String lambda = propsFile.getMHash();
        Value[] lambdaArgs = { fileList };

        return callConfiguredLambda(name, ctx, lambda, lambdaArgs);
    }

    

}
