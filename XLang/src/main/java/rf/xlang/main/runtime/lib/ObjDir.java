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

package rf.xlang.main.runtime.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import rf.xlang.main.Ctx;
import rf.xlang.main.runtime.*;

public class ObjDir extends Obj {

    private String name;

    public ObjDir(String name) throws Exception {
        this.name=name;

        try {
            File f=new File(name);
            if (f.exists()) {
                this.name=f.getCanonicalPath();
            }
        } catch (Exception ex) {
            this.name=name;
        }
        Function[] arr={
                new FunctionName(),
                new FunctionPath(),
                new FunctionExists(),
                new FunctionSub(),
                new FunctionFiles(),
                new FunctionDirs(),
                new FunctionCreate(),
                new FunctionDelete(),
                new FunctionFile(),
                new FunctionCopy(),
                new FunctionRun(),
                new FunctionRunDetach(),
                new FunctionShowTree(),
                new FunctionLastModified(),
        };
        setFunctions(arr);

    }
    
    
    public boolean isSymlink() {
        Path path=Paths.get(name);
        return Files.isSymbolicLink(path);
    }

  public boolean dirExists() {
        try {
            File f=new File(name);
            if (f.exists() && f.isDirectory()) return true;
        } catch (Exception ex) {
            // ignore
        }
        return false;
    }
    
    public File getDir() {
        return new File(name);
    }
    
    public ObjDir self() {
        return this;
    }
    
     
    public String getName() {
        return name;
    }
    
    @Override
    public boolean eq(Obj x) {
        if (x instanceof ObjDir) {
            ObjDir d=(ObjDir) x;
            return d.name.equals(name); // always canonical when possible
        }
        return false;
    }

    @Override
    public String getTypeName() {
        return "Dir";
    }
    

    private String fix (String name) {
        int pos=name.lastIndexOf(File.separator);
        if (pos > 0) name=name.substring(pos+1);
        if (!name.endsWith(File.separator)) {
            return name+File.separator; // to differ from files when presented in common list (presentation only)
        } else {
            return name;
        }
    }

    class FunctionSub extends Function {
        public String getName() {
            return "sub";
        }
        public String getShortDesc() {
            return "sub(str) - returns Dir object for sub directory";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected 1 parameter");
            return new ValueObj(new ObjDir(name+File.separator+params.get(0).getValAsString()));
        }
    }

    class FunctionName extends Function {
        public String getName() {
            return "name";
        }
        public String getShortDesc() {
            return "name() - returns name (last part)";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            int pos=name.lastIndexOf(File.separatorChar);
            if (pos >= 0) return new ValueString(name.substring(pos+1));
            return new ValueString(name);
        }
    }

    class FunctionPath extends Function {
        public String getName() {
            return "path";
        }
        public String getShortDesc() {
            return "path() - returns full path";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueString(name);
        }
    }

    class FunctionExists extends Function {
        public String getName() {
            return "exists";
        }
        public String getShortDesc() {
            return "exists() - returns true or false";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            File f=new File(name);
            boolean result=f.exists() && f.isDirectory();
            return new ValueBoolean(result);
        }
    }

    class FunctionFile extends Function {
        public String getName() {
            return "file";
        }
        public String getShortDesc() {
            return "file(name) - create File object relative to directory";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected string parameter");
            if (!(params.get(0) instanceof ValueString)) throw new Exception("Expected string parameter");
            String s=((ValueString) params.get(0)).getVal();
            
            String fileName=name+File.separator+s;
            return new ValueObj(new ObjFile(fileName));
        }
    }
    

    class FunctionFiles extends Function {
        public String getName() {
            return "files";
        }
        public String getShortDesc() {
            return "files(Glob?) - returns list of File objects";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            File f=new File(name);
            if (!f.isDirectory()) throw new Exception("No such directory: " + name);
            List<Value> result=new ArrayList<Value>();
            for (String s:f.list()) {
                File x=new File(name + File.separator + s);
                if (x.isFile()) {
                    result.add(new ValueObj(new ObjFile(x.getCanonicalPath())));
                }
            }
            return new ValueList(result);
        }
    }


    class FunctionDirs extends Function {
        public String getName() {
            return "dirs";
        }
        public String getShortDesc() {
            return "dirs(Glob?) - returns list of Dir objects";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            File f=new File(name);
            if (!f.isDirectory()) throw new Exception("No such directory: " + name);
            List<Value> result=new ArrayList<Value>();
            for (String s:f.list()) {
                File x=new File(name + File.separator + s);
                if (x.isDirectory()) {
                    result.add(new ValueObj(new ObjDir(x.getCanonicalPath())));
                }
            }
            return new ValueList(result);
        }
    }

    class FunctionCreate extends Function {
        public String getName() {
            return "create";
        }
        public String getShortDesc() {
            return "create() - returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            
            File f=new File(name);
            if (!f.exists()) {
                boolean ok = f.mkdirs();
                if (!ok) throw new Exception("Could not create dir " + f.getCanonicalPath());
                return new ValueObj(self());
            } else if (!f.isDirectory()) {
                throw new Exception ("Exists, but not a dir " + f.getCanonicalPath());
            }
            return new ValueObj(self());
        }
    }

    class FunctionDelete extends Function {
        public String getName() {
            return "delete";
        }
        public String getShortDesc() {
            return "delete() - return boolean true deleted ok, otherwise false";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");

            File f=new File(name);
            if (f.exists()) {
                if (f.isDirectory()) {
                    boolean result=f.delete();
                    return new ValueBoolean(result);
                } else {
                    ctx.getObjGlobal().addSystemMessage("Not a dir   " + f.getCanonicalPath());
                    return new ValueBoolean(false);
                }
            } else {
                ctx.getObjGlobal().addSystemMessage("Not found   " + f.getCanonicalPath());
                return new ValueBoolean(false);
            }
        }
    }

    class FunctionCopy extends Function {
        public String getName() {
            return "copy";
        }
        public String getShortDesc() {
            return "copy(File) - copy file to directory, ok if copied ok, otherwise false";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected File parameter");
            Value p1=params.get(0);
            if (!(p1 instanceof ValueObj)) throw new Exception("Expected File parameter");
            
            Obj obj=((ValueObj) p1).getVal();
            if (!(obj instanceof ObjFile)) throw new Exception("Expected File parameter");
            
            ObjFile objFile=(ObjFile) obj;
            String srcPath=objFile.getPath();
            String srcName=objFile.getName();
            
            File src=new File(srcPath);
            if (!src.exists() || !src.isFile()) throw new Exception("Source file does not exist or is not a file");
            
            File target=new File(name + File.separator + srcName);
            //if (target.exists()) throw new Exception("Target file exists");
            
            InputStream in=new FileInputStream(src);
            OutputStream out=new FileOutputStream(target);
            byte[] buf=new byte[64*1024];
            for (;;) {
                int count=in.read(buf);
                if (count <= 0) break;
                out.write(buf, 0, count);
            }
            in.close();
            out.close();
            return new ValueBoolean(true);
        }
    }

    

   private void callExternalProgram (Ctx ctx, boolean foreground, List<Value> params) throws Exception {
        List<Value> args;
        if (params.size()==1) {
            if (params.get(0) instanceof ValueString) {
                args=new ArrayList<Value>();
                args.add(params.get(0));
            } else {
                // list of strings
                args=getList("list", params, 0);
            }
        } else if (params.size() > 1) {
            // sequence of strings
            args=params;
        } else {
            throw new Exception("Expected single list, or list of strings");
        }
        
        List<String> strArgs=new ArrayList<String>();
        for (Value v:args) strArgs.add(v.getValAsString());
        
        String program=strArgs.get(0);
        
        ProcessBuilder processBuilder = new ProcessBuilder(strArgs);
        
        processBuilder.redirectInput(Redirect.INHERIT); // connect input
        processBuilder.redirectError(Redirect.INHERIT);

        // set current directory
        processBuilder.directory(new File(name));
        
        long startTime=System.currentTimeMillis();
        Process process = processBuilder.start();

        if (foreground) {
            String desc=program;
            int returnCode=process.waitFor();
            long endTime=System.currentTimeMillis();
            long duration=endTime-startTime; 
        }

    }

    class FunctionRun extends Function {
        public String getName() {
            return "run";
        }
        public String getShortDesc() {
            return "run(list|...) - execute external program in foreground, waits for it to terminate";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            callExternalProgram(ctx, true, params);
            return new ValueObj(self());

        }
    }


    class FunctionRunDetach extends Function {
        public String getName() {
            return "runDetach";
        }
        public String getShortDesc() {
            return "runDetach(list|...) - execute external program in background";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            callExternalProgram(ctx, false, params);
            return new ValueObj(self());

        }
    }


    class FunctionShowTree extends Function {
        public String getName() {
            return "showTree";
        }
        public String getShortDesc() {
            return "showTree(limit?) - returns list of directories where the sum of file sizes > 0 / limit";
        }
        private void traverse (String path, long limitKb, List<Value> results) {
            // each directory sums files directly inside, not recursive
            String[] elements = (new File(path)).list();
            long bytecount=0;
            int count=0;
            boolean error=false;
            for (String e:elements) {
                File f=new File(path + File.separator + e);
                try {
                    if (f.exists() && f.isFile()) {
                        long bytes=f.length();
                        bytecount += bytes;
                        count++;
                    }
                } catch (Exception ex) {
                    error=true;
                }
            }
            bytecount /= 1024;
            String unit=" Kb";

            if (count > 0 && bytecount > limitKb) results.add(new ValueString(path + "  | size: "+ bytecount + unit +(error?" (?)":"")));
        
            for (String e:elements) {
                File f=new File(path + File.separator + e);
                try {
                    if (f.exists() && f.isDirectory()) {
                        traverse(path + File.separator + e, limitKb, results);
                    }
                } catch (Exception ex) {
                    // ignore
                }
            }
        }
        
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            long limitKb=0;
            if (params.size() == 1) {
                limitKb=getInt("limitKb",params,0);
            } else if (params.size() > 1) {
                throw new Exception("Exected optional parameter limitKb");
            }
            
            File f=getDir();
            
            // traverse and sum file sizes in MBytes
            List<Value> result=new ArrayList<Value>();
            traverse(f.getCanonicalPath(), limitKb, result);
            return new ValueList(result);
        }
    } 




    class FunctionLastModified extends Function {
        public String getName() {
            return "lastModified";
        }
        public String getShortDesc() {
            return "lastModified() - return time of last modification as int";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            File f=new File(name);
            return new ValueInt(f.lastModified());
        }
    }



}
