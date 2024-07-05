/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2024 Roar Foshaug

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
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.lib.ObjDir;
import rf.configtool.main.runtime.lib.Protection;

public class ShellCd extends ShellCommand {

    @Override
    public String getName() {
        return "cd";
    }
    @Override 
    public String getBriefExampleParams() {
        return "<dir>?";
    }

     public Value execute(Ctx ctx, Command cmd) throws Exception {

        String currDir = ctx.getObjGlobal().getCurrDir();
        
        if (cmd.noArgs()) {
            ctx.getObjGlobal().setCurrDir(null);
            ctx.getObjGlobal().addSystemMessage(ctx.getObjGlobal().getCurrDir());
            return new ValueObj(new ObjDir(ctx.getObjGlobal().getCurrDir(), Protection.NoProtection));
        }
        
        List<Arg> args=cmd.getArgs();
        if (args.size() != 1) throw new Exception("cd: expected zero or one arguments");
        
        FileSet fs=new FileSet(cmd.getCommandName(), true, false);  // directories only
        fs.setIsSafeOperation();
        
        Arg arg=args.get(0);
        fs.processArg(currDir, ctx, args.get(0));
        
        List<String> dirs=fs.getDirectories();
        if (dirs.size() != 1) throw new Exception("cd: got " + dirs.size() + " matches, should be one");
        
        ctx.getObjGlobal().setCurrDir(dirs.get(0));
        ctx.getObjGlobal().addSystemMessage(ctx.getObjGlobal().getCurrDir());
        return new ValueObj(new ObjDir(ctx.getObjGlobal().getCurrDir(), Protection.NoProtection));
    }


    

}
