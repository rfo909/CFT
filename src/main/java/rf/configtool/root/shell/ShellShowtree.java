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

import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.PropsFile;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.lib.ObjDir;
import rf.configtool.main.runtime.lib.Protection;

public class ShellShowtree  extends ShellCommand {

    public ShellShowtree(List<String> parts) throws Exception {
        super(parts);
    }

    public Value execute(Ctx ctx) throws Exception {

        String currentDir = ctx.getObjGlobal().getCurrDir();
        List<ShellCommandArg> args=getArgs();
        
        String name=getName();
        
        if (args.size()==0) {
            return callMacro(ctx,null);
        }
        FileSet fs=new FileSet(name,true, false); // directories only
        fs.setIsSafeOperation();
        
        if (args.size() > 1) throw new Exception(name + ": Expected one optional parameter only - a directory");
        
        fs.processArg(currentDir, ctx, args.get(0));
        
        List<String> directories=fs.getDirectories();
        if (directories.size() != 1) throw new Exception(name + ": Expected one directory");
        
        ObjDir dir=new ObjDir(directories.get(0), Protection.NoProtection);
        
        return callMacro(ctx, dir);
    }

    private Value callMacro (Ctx ctx, ObjDir dir) throws Exception {

        if (dir==null) {
            dir=new ObjDir(ctx.getObjGlobal().getCurrDir(), Protection.NoProtection);
        }
        
    PropsFile propsFile=ctx.getObjGlobal().getRoot().getPropsFile();
        String lambda=propsFile.getMShowtree();
        Value[] lambdaArgs= {new ValueObj(dir)};

        return callConfiguredLambda(getName(), ctx, lambda, lambdaArgs);
    }


    

}
