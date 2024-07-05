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

import java.io.*;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;


public class ShellCp extends ShellCommand {

    private boolean confirmed=false;
    
    @Override
    public String getName() {
        return "cp";
    }
    @Override 
    public String getBriefExampleParams() {
        return "<src> ... <target>";
    }


    private boolean copyFile (Ctx ctx, File src, File target) throws Exception {
        FileInputStream fis=null;
        FileOutputStream fos=null;
        try {
            fis=new FileInputStream(src);
            fos=new FileOutputStream(target);
            byte[] buf=new byte[64*1024];
            for (;;) {
                int count=fis.read(buf);
                if (count <= 0) break;
                fos.write(buf,0,count);
            }
            return true;
        } catch (Exception ex) {
            return false;
        } finally {
            if (fis != null) try {fis.close();} catch (Exception ex) {};
            if (fos != null) try {fos.close();} catch (Exception ex) {};
        }
    }
    
    private boolean copyTree (Ctx ctx, String name, File srcDir, File targetDir) throws Exception {
        
        if (targetDir.exists() && !targetDir.isDirectory()) {
            throw new Exception(name + ": target not a directory");
        }
        
        verifySourceTargetDirsIndependent(name, srcDir, targetDir);

        if (!confirmed) {
            ctx.getStdio().println("Confirm copy " + srcDir.getCanonicalPath() + " -> " + targetDir.getCanonicalPath());
            ctx.getStdio().println("Type 'yes' to continue");
            String line = ctx.getStdio().getInputLine();
            if (!line.equals("yes")) {
                return false;
            }
            confirmed=true;
        }
        
        if (!targetDir.exists()) {
            boolean ok = targetDir.mkdir();
            if (!ok) return false;
        }

        boolean allOk=true;
        for (File f : srcDir.listFiles()) {
            if (f.isDirectory()) {
                File t=new File(targetDir.getCanonicalPath() + File.separator + f.getName());
                if (!copyTree(ctx, name, f, t)) allOk=false;
            } else if (f.isFile()) {
                File t=new File(targetDir.getCanonicalPath() + File.separator + f.getName());
                if (!copyFile(ctx, f, t)) allOk=false;
            }
        }
        return allOk;
    }
    
    
    public Value execute(Ctx ctx, Command cmd) throws Exception {
        
        final String name=cmd.getCommandName(); 

        final String currentDir = ctx.getObjGlobal().getCurrDir();
        List<Arg> args=cmd.getArgs();
        if (args.size() < 2) throw new Exception(name + ": requires at least two args");
        
        FileSet fsSource = new FileSet(name,true,true);
        fsSource.setIsSafeOperation();  // copy source is non destructive
        
        for (int i=0; i<args.size()-1; i++) {
            Arg arg=args.get(i);
            fsSource.processArg(currentDir, ctx, arg);
        }
        
        List<String> dirs=fsSource.getDirectories();
        List<String> files=fsSource.getFiles();
        
        Arg lastArg=args.get(args.size()-1);

        // special cases, where target may depend on src (two args only, with one src element)
        if (args.size() == 2) {
            
            // cp existingDir -> newDir ### copy
            // cp existingDir -> otherExistingDir ### cp into other directory
            if (dirs.size()==1 && files.size()==0) {
                File src=new File(dirs.get(0));
                
                FileSet fsTarget=new FileSet(name,true,false); // directories only
                
                fsTarget.processArg(currentDir, ctx, lastArg, true, false); // allow new dir
                List<String> targetDirs=fsTarget.getDirectories();

                if (targetDirs.size() != 1) throw new Exception(name + ": expected single target dir");
                File target=new File(targetDirs.get(0));
                
                if (target.exists()) {
                    target=new File(target.getCanonicalPath() + File.separator + src.getName());
                } 
                
                boolean ok = copyTree(ctx,name,src,target);
                ctx.addSystemMessage((ok?"OK   ":"FAIL ") + name + ": " + src.getCanonicalPath() + " -> " + target.getCanonicalPath());
                return new ValueBoolean(ok);        
            }
            
            // cp existingFile -> newFile  ### file copy
            // cp existingFile -> otherExistingFile ### copy + overwrite
            // cp existingFile -> existingDir ### copy into dir
            if (files.size()==1 && dirs.size()==0) {
                File src=new File(files.get(0));
                
                FileSet fsTarget=new FileSet(name,true,true); // directory or file
                fsTarget.processArg(currentDir, ctx, lastArg, false, true); // allow new files, but not new directories
                
                if (fsTarget.getDirectories().size() + fsTarget.getFiles().size() != 1) {
                    throw new Exception(name + ": expected single target dir or file");
                }
    
                File target;
                if (fsTarget.getDirectories().size()==1) {
                    target=new File(fsTarget.getDirectories().get(0) + File.separator + src.getName());
                } else {
                    target=new File(fsTarget.getFiles().get(0));
                }
                boolean ok = copyFile(ctx,src,target);
                ctx.addSystemMessage((ok?"OK   ":"FAIL ") + name + ": " + src.getCanonicalPath() + " -> " + target.getCanonicalPath());
                
                return new ValueBoolean(ok);        
            }
            
        }
        
        // ---------------------------------------------------------------------
        // The general case, where args.size >= 2 or number of src elements > 1 (globbing)
        // -> this means target must always be existing dir
        // ---------------------------------------------------------------------
        {
            FileSet fsTarget=new FileSet(name,true,false); // directories only
            fsTarget.processArg(currentDir, ctx, lastArg); // no new anything allowed
            
            if (fsTarget.getDirectories().size() != 1) {
                throw new Exception(name + ": expected single target dir");
            }
            
            File target = new File(fsTarget.getDirectories().get(0));
            if (!target.exists() || !target.isDirectory()) throw new Exception(name + ": internal error");
            
            boolean allOk=true;
            
            // iterate over files and directories in args
            for (String f:fsSource.getFiles()) {
                File src=new File(f);
                File fTarget=new File(target.getCanonicalPath() + File.separator + src.getName());
                boolean ok=copyFile(ctx,src,fTarget);
                ctx.addSystemMessage((ok?"OK   ":"FAIL ") + name + ": " + src.getCanonicalPath() + " -> " + fTarget.getCanonicalPath());
                if (!ok) allOk=false;
            }
            for (String f:fsSource.getDirectories()) {
                File src=new File(f);
                File dTarget=new File(target.getCanonicalPath() + File.separator + src.getName());
                boolean ok=copyTree(ctx,name,src,dTarget);
                ctx.addSystemMessage((ok?"OK   ":"FAIL ") + name + ": " + src.getCanonicalPath() + " -> " + dTarget.getCanonicalPath());
                if (!ok) allOk=false;
            }
            return new ValueBoolean(allOk);
        }

    }


}
