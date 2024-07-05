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

import java.io.File;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.lib.ObjDir;
import rf.configtool.main.runtime.lib.Protection;

public class ShellMkdir extends ShellCommand {

    @Override
    public String getName() {
        return "mkdir";
    }
    @Override 
    public String getBriefExampleParams() {
        return "<name>";
    }


    public Value execute(Ctx ctx, Command cmd) throws Exception {

        String currentDir = ctx.getObjGlobal().getCurrDir();
        List<Arg> args=cmd.getArgs();
        String name=cmd.getCommandName();
        
        FileSet fs=new FileSet(name,true,false);
        
        for (Arg arg:args) {
            fs.processArg(currentDir, ctx, arg, true, false);
        }
        
        boolean allOk=true;
        List<String> dirs=fs.getDirectories();
        for (String dir:dirs) {
            File f=new File(dir);
            
            if (f.exists()) {
                ctx.addSystemMessage("Directory exists: " + f.getCanonicalPath());
            } else {
                boolean ok = f.mkdirs();
                if (!ok) {
                    ctx.addSystemMessage("Failed to create: " + f.getCanonicalPath());
                    allOk=false;
                }
            }
        }
        return new ValueBoolean(allOk);
    }


    

}
