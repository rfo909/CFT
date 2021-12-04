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

package rf.configtool.parsetree;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import rf.configtool.lexer.TokenStream;
import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.lib.ObjDir;
import rf.configtool.main.runtime.lib.ObjFile;
import rf.configtool.main.runtime.lib.Protection;

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
    	
        String currDir=ctx.getObjGlobal().getCurrDir();

        List<String> directories=new ArrayList<String>();
        List<String> files=new ArrayList<String>();

        getDirContent(ctx, currDir, directories, files);
        show (ctx, directories, files);
        
    }
    
    public static final long LS_DEFAULT_TIMEOUT_MS = 6000;
    // list files and dirs in currDir - default (no parameters) is guarded by a timeout, since
	// working with remote directories, and many files, ls can take very long time.

    public static final int LS_DEFAULT_MAX_ENTRIES = 2000;


    private void getDirContent (Ctx ctx, String dir, List<String> directories, List<String> files) throws Exception {
        File f=new File(dir);
        
        long startTime=System.currentTimeMillis();
        
        DirectoryStream<Path> stream = Files.newDirectoryStream(f.toPath());
        Iterator<Path> iter = stream.iterator();
        
        int totalCount=0;
        while (iter.hasNext()) {
        	Path p=iter.next();
        	
        	File x=p.toFile();
        	String path=x.getCanonicalPath();
            if (x.isFile()) {
                files.add(path);
            } else if (x.isDirectory()) {
                directories.add(path); 
            }
            
            totalCount++;
            
            long duration = System.currentTimeMillis()-startTime;

            if (duration > LS_DEFAULT_TIMEOUT_MS) {
            	ctx.addSystemMessage("--- directory listing timed out after " + LS_DEFAULT_TIMEOUT_MS + ", use '*' to override");
            	return;
            }
            if (totalCount >= LS_DEFAULT_MAX_ENTRIES) {
            	ctx.addSystemMessage("--- directory entry count > " + LS_DEFAULT_MAX_ENTRIES + ", use '*' to override");
            	return;
            }
        }
        
//        
//        for (String s:f.list()) {
//            String path=dir + File.separator + s;
//            File x=new File(path);
//;            if (x.isFile()) {
//                files.add(path);
//            } else if (x.isDirectory()) {
//                directories.add(path); 
//            }
//            
//        }
        
    }
    
    
    @Override
    protected void processOne (Ctx ctx, File file) throws Exception {
        if (!file.exists()) throw new Exception("No such file or directory");
        if (file.isFile()) {
            ctx.push(new ValueObj(new ObjFile(file.getCanonicalPath(), Protection.NoProtection)));
            return;
        }
        if (file.isDirectory()) {
            List<String> directories=new ArrayList<String>();
            List<String> files=new ArrayList<String>();
            getDirContent(ctx, file.getCanonicalPath(), directories, files);
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
    
    
    
    @Override
    protected boolean processUnknown (Ctx ctx, File file) throws Exception {
        return false;
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
        for (String x:directories) result.add(new ValueObj(new ObjDir(x, Protection.NoProtection)));
        for (String x:files) result.add(new ValueObj(new ObjFile(x, Protection.NoProtection)));
                
        ctx.push(new ValueList(result));      
        

    }

}
