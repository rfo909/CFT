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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.lib.ObjFile;
import rf.configtool.main.runtime.lib.Protection;

public class ShellTouch extends ShellCommand {

    @Override
    public String getName() {
        return "touch";
    }
    @Override 
    public String getBriefExampleParams() {
        return "<file> ...";
    }


    public Value execute(Ctx ctx, Command cmd) throws Exception {

        String name=cmd.getCommandName();
        
        String currDir = ctx.getObjGlobal().getCurrDir();
        boolean noArgs=cmd.getArgs().isEmpty();
        
        if (noArgs) throw new Exception("touch: expected file(s)");
        
        List<Arg> args=cmd.getArgs();
        
        FileSet fs=new FileSet(name,false,true);  // files only
        
        for (Arg arg:args) {
            fs.processArg(currDir, ctx, arg, false, true);  // allow unknown files
        }
        
        List<String> files=fs.getFiles();
        List<Value> results=new ArrayList<Value>();
        
        for (String filename:files) {
            File file=new File(filename);
            if (!file.exists()) {
                file.createNewFile();
            } else {
                Path path=file.toPath();
                FileTime ft = FileTime.fromMillis(System.currentTimeMillis());
                Files.setLastModifiedTime(path, ft);
            }
            Value result=new ValueObj(new ObjFile(file.getCanonicalPath(), Protection.NoProtection));
            results.add(result);
        }
        if (results.size()==1) return results.get(0);
        return new ValueList(results);
    }


    

}
