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
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import rf.configtool.lexer.TokenStream;
import rf.configtool.main.Ctx;
import rf.configtool.main.FunctionBody;
import rf.configtool.main.FunctionState;
import rf.configtool.main.ObjGlobal;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueNull;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.lib.ObjDir;
import rf.configtool.main.runtime.lib.ObjFile;
import rf.configtool.main.runtime.lib.ObjGlob;
import rf.configtool.main.runtime.lib.Protection;

public class ShellLs extends ShellCommand {

	private final String name;
    private final boolean showFiles;
    private final boolean showDirs;

    public ShellLs(String name) {
    	this.name=name;
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
		return name;
	}
	@Override 
	public String getBriefExampleParams() {
		return "...";
	}



    public Value execute(Ctx ctx, Command cmd) throws Exception {

        String currDir = ctx.getObjGlobal().getCurrDir();

        boolean noArgs=cmd.getArgs().isEmpty();
        
        FileSet fs=new FileSet(name,showDirs, showFiles);
        fs.setIsSafeOperation();
        
        if (noArgs) {
            ObjGlob glob=new ObjGlob("*");
            String errMsg = fs.addDirContent(currDir, glob);
            if (errMsg != null) {
                ctx.addSystemMessage(errMsg);
                return new ValueNull();
            }
            
            return generateResultList(fs);
        } 
        
        // process args, some of which may be expressions

        List<Arg> args=cmd.getArgs();
        
        for (Arg arg:args) {
            fs.processArg(currDir, ctx, arg);
        }
        
        if (fs.getFiles().size()==0 && fs.getDirectories().size()==1 && !fs.argsContainGlobbing()) {
            // ls someDir ---> list content inside that dir
            String singleDir=fs.getDirectories().get(0);
            
            fs=new FileSet(name,showDirs,showFiles);
            fs.setIsSafeOperation();
            
            fs.addDirContent(singleDir, new ObjGlob("*"));
        }

        return generateResultList(fs);

    }


    
    private Value generateResultList(FileSet fs) throws Exception {
        List<Value> result = new ArrayList<Value>();
        
        if (showDirs) {
            List<String> dirList=fs.getDirectories();
            sort(dirList);
            for (String x : dirList) {
                result.add(new ValueObj(new ObjDir(x, Protection.NoProtection)));
            }
        }
        if (showFiles) {
            List<String> fileList=fs.getFiles();
            sort(fileList);
            for (String x : fileList) {
                result.add(new ValueObj(new ObjFile(x, Protection.NoProtection)));
            }

        }

        return new ValueList(result);

    }

}
