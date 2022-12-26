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
import java.util.List;

import rf.configtool.lexer.SourceLocation;
import rf.configtool.main.CFTCallStackFrame;
import rf.configtool.main.Ctx;
import rf.configtool.main.FunctionBody;
import rf.configtool.main.FunctionState;
import rf.configtool.main.PropsFile;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBlock;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.lib.ObjDir;
import rf.configtool.main.runtime.lib.ObjFile;
import rf.configtool.main.runtime.lib.Protection;

public class ShellCatEditMoreTail  extends ShellCommand {

    public ShellCatEditMoreTail(List<String> parts) throws Exception {
        super(parts);
    }

    public Value execute(Ctx ctx) throws Exception {

        String currDir = ctx.getObjGlobal().getCurrDir();
        boolean noArgs=getArgs().isEmpty();
        
        String name=getName();
        
        if (noArgs) {
            return callMacro(ctx,null);
        }
        FileSet fs=new FileSet(name,false, true); // files only
        fs.setIsSafeOperation();
        
        for (ShellCommandArg arg : getArgs()) fs.processArg(currDir, ctx, arg);
        
        List<String> files=fs.getFiles();
        if (files.isEmpty()) throw new Exception(name + ": no match");
        
        if (files.size() > 1) throw new Exception(name + ": matched " + files.size() + " elements");
        
        ObjFile f=new ObjFile(files.get(0), Protection.NoProtection);
        
        return callMacro(ctx, f);
    }

    private Value callMacro (Ctx ctx, ObjFile file) throws Exception {
        String name=getName();
        
        PropsFile propsFile=ctx.getObjGlobal().getRoot().getPropsFile();
        
        String lambda;

        if (name.equals("cat")) {
            lambda=propsFile.getMCat();
        } else if (name.equals("edit")) {
            lambda=propsFile.getMEdit(); 
        } else if (name.equals("more")) {
            lambda=propsFile.getMMore();
        } else if (name.equals("tail")) {
            lambda=propsFile.getMTail();
        } else {
            throw new Exception("Invalid statement name, expected cat, edit or more: " + name);
        }

            
        if (file != null) {
            Value[] lambdaArgs= {new ValueObj(file)};
            return callConfiguredLambda(name, ctx, lambda, lambdaArgs);
        } else {
            Value[] lambdaArgs= {};
            return callConfiguredLambda(name, ctx, lambda, lambdaArgs);
        }

    }


    

}
