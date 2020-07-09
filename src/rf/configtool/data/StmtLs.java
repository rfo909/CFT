/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020 Roar Foshaug

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

package rf.configtool.data;

import java.io.File;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueInt;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.lib.ObjDir;
import rf.configtool.main.runtime.lib.ObjFile;
import rf.configtool.main.runtime.lib.ObjGlob;
import rf.configtool.parser.Token;
import rf.configtool.parser.TokenStream;
import java.util.*;

public class StmtLs extends StmtShellInteractive {

	private boolean showFiles;
	private boolean showDirs;
	
    public StmtLs (TokenStream ts) throws Exception {
        super(ts);
        if (getName().equals("ls")) {
        	showFiles=true;
        	showDirs=true;
        } else if (getName().equals("lsd")) {
        	showFiles=false;
        	showDirs=true;
        } else if (getName().equals("lsf")) {
        	showFiles=true;
        	showDirs=false;
        } else {
        	throw new Exception("Expected ls, lsf or lsd");
        }
    }

    @Override
    protected void processDefault(Ctx ctx) throws Exception {
    	// list files and dirs in currDir
        String currDir=ctx.getObjGlobal().getCurrDir();

        File f=new File(currDir);
        List<String> directories=new ArrayList<String>();
        List<String> files=new ArrayList<String>();

        getDirContent(currDir, directories, files);
        show (ctx, directories, files);
        
    }
    
    
    private void getDirContent (String dir, List<String> directories, List<String> files) throws Exception {
        File f=new File(dir);

        for (String s:f.list()) {
        	String path=dir + File.separator + s;
            File x=new File(path);
            if (x.isFile()) {
                files.add(path);
            } else if (x.isDirectory()) {
                directories.add(path); 
            }
            
        }
    	
    }
    
    
    @Override
    protected void processOne (Ctx ctx, File file) throws Exception {
    	if (!file.exists()) throw new Exception("No such file or directory");
    	if (file.isFile()) {
    		ctx.push(new ValueObj(new ObjFile(file.getCanonicalPath())));
    		return;
    	}
    	if (file.isDirectory()) {
    		List<String> directories=new ArrayList<String>();
    		List<String> files=new ArrayList<String>();
    		getDirContent(file.getCanonicalPath(), directories, files);
			show (ctx, directories, files);
			return;
       	}
    	throw new Exception("No such file or directory");
    	
    }
    
    
    
    @Override
    protected void processSet (Ctx ctx, List<File> elements) throws Exception {
		List<String> directories=new ArrayList<String>();
		List<String> files=new ArrayList<String>();
		for (File f:elements) {
			if (f.isDirectory()) {
				directories.add(f.getCanonicalPath());
			} else if (f.isFile()) {
				files.add(f.getCanonicalPath());
			}
		}
		show (ctx, directories, files);
    }
    
    
    
    
    

    private void show (Ctx ctx, List<String> directories, List<String> files) throws Exception {
        if (showDirs) {
        	sort(directories);
        } else {
            directories.clear();
        }
        if (showFiles) {
            sort(files);
        } else {
            files.clear();
        }
        
        List<Value> result=new ArrayList<Value>();
        for (String x:directories) result.add(new ValueObj(new ObjDir(x)));
        for (String x:files) result.add(new ValueObj(new ObjFile(x)));
                
        ctx.push(new ValueList(result));   	
    	

    }

}
