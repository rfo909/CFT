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
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueNull;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.lib.ObjDir;
import rf.configtool.main.runtime.lib.ObjFile;
import rf.configtool.main.runtime.lib.ObjGlob;
import rf.configtool.main.runtime.lib.Protection;

public class ShellLs extends ShellCommand {

    private final String commandName;
    private final boolean showFiles;
    private final boolean showDirs;

    public ShellLs(String name) {
        this.commandName = name;
        if (name.equals("ls")) {
            showFiles = true;
            showDirs = true;
        } else if (name.equals("lsd")) {
            showFiles = false;
            showDirs = true;
        } else if (name.equals("lsf")) {
            showFiles = true;
            showDirs = false;
        } else {
            throw new RuntimeException("Expected ls, lsf or lsd");
        }
    }
    
    @Override
    public String getName() {
        return commandName;
    }
    @Override 
    public String getBriefExampleParams() {
        return "...";
    }



    public Value execute(Ctx ctx, Command cmd) throws Exception {

        String currDir = ctx.getObjGlobal().getCurrDir();

        boolean noArgs=cmd.getArgs().isEmpty();
        
        FileSet fs=new FileSet(commandName,showDirs, showFiles);
        fs.setIsSafeOperation();

        if (noArgs) {
            boolean enforceLimits=true;
            ObjGlob glob=new ObjGlob("*");
            String errMsg = fs.addDirContent(currDir, glob,enforceLimits);
            if (errMsg != null) {
                ctx.addSystemMessage(errMsg);
                return new ValueNull();
            }
            
            return generateResultList(fs, enforceLimits);
        } 
        
        // process args, some of which may be expressions

        List<Arg> args=cmd.getArgs();
        boolean enforceLimits=false;
        
        for (Arg arg:args) {
            fs.processArg(currDir, ctx, arg);
        }
        
        if (fs.getFiles().size()==0 && fs.getDirectories().size()==1 && !fs.argsContainGlobbing()) {
            // ls someDir ---> list content inside that dir
            String singleDir=fs.getDirectories().get(0);
            
            fs=new FileSet(commandName,showDirs,showFiles);
            fs.setIsSafeOperation();
            
            fs.addDirContent(singleDir, new ObjGlob("*"), enforceLimits);
        }

        return generateResultList(fs, enforceLimits);

    }


    
    private Value generateResultList(FileSet fs, boolean enforceLimits) throws Exception {
        List<Value> result = new ArrayList<Value>();

        long startTime=System.currentTimeMillis();

        if (showDirs) {
            List<String> dirList=fs.getDirectories();
            sort(dirList);
            //System.out.println("dir sort ok after " + (System.currentTimeMillis()-startTime) + "ms");
            for (String x : dirList) {
                if (enforceLimits) {
                    long duration=System.currentTimeMillis()-startTime;
                    if (duration > FileSet.LS_DEFAULT_TIMEOUT_MS) throw new Exception("directory listing timed out, use 'ls *' to override");
                }
                result.add(new ValueObj(new ObjDir(x, Protection.NoProtection)));
            }
        }
        //System.out.println("dirs ok after " + (System.currentTimeMillis()-startTime) + "ms");

        if (showFiles) {
            List<String> fileList=fs.getFiles();
            sort(fileList);
            //System.out.println("file sort ok after " + (System.currentTimeMillis()-startTime) + "ms");

            for (String x : fileList) {
                if (enforceLimits) {
                    long duration=System.currentTimeMillis()-startTime;
                    if (duration > FileSet.LS_DEFAULT_TIMEOUT_MS) throw new Exception("directory listing with " + fileList.size() + " files timed out, use 'ls *' to override");
                }
                result.add(new ValueObj(new ObjFile(x, Protection.NoProtection)));
            }

        }

        return new ValueList(result);

    }

}
