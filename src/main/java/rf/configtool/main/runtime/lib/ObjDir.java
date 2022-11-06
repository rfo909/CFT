/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2022 Roar Foshaug

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

package rf.configtool.main.runtime.lib;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.OutText;
import rf.configtool.main.SoftErrorException;
import rf.configtool.main.Stdio;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueInt;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.util.FileModifiedSort;

public class ObjDir extends Obj {

    private String name;
    private Protection protection;

    public ObjDir(String name, Protection protection) throws Exception {
        this.name=name;
        this.protection=protection;
        
        if (protection==null) {
            throw new Exception("protection==null is invalid, use Protection.NONE");
        }
        
        
        if (isSymlink()) {
            this.protection=new Protection("isSymlink");
        } else {
            try {
                File f=new File(name);
                if (f.exists()) {
                    this.name=f.getCanonicalPath();
                }
            } catch (Exception ex) {
                this.name=name;
            }
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
                new FunctionAllFiles(),
                new FunctionAllDirs(),
                new FunctionFile(),
                new FunctionCopy(),
                new FunctionRun(),
                new FunctionRunDetach(),
                new FunctionRunProcess(),
                new FunctionRunCapture(),
                new FunctionShowTree(),
                new FunctionProtect(),
                new FunctionUnprotect(),
                new FunctionCd(),
                new FunctionVerify(),
                new FunctionNewestFile(),
                new FunctionNewestFiles(),
                new FunctionStats(),
                new FunctionLastModified(),
        };
        setFunctions(arr);

    }
    
    
    public boolean isSymlink() {
        Path path=Paths.get(name);
        return Files.isSymbolicLink(path);
    }


    public Protection getProtection() {
        return protection;
    }
    
    public void validateDestructiveOperation (String op) throws Exception {
        protection.validateDestructiveOperation(op, name);
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
    public String synthesize() throws Exception {
        String prot="";
        if (protection != null && protection.isActive()) {
            prot=".protect(" + (new ValueString(protection.getCode()).synthesize() + ")");
        }
        
        return "Dir(" + (new ValueString(name)).synthesize() + ")" + prot;
    }


    
    @Override
    public String getTypeName() {
        return "Dir";
    }
    
    @Override
    public ColList getContentDescription() {
        
        File f=new File(name);
        if (f.exists()) {
            if (!f.isDirectory()) {
                return ColList.list().regular(fix(f.getAbsolutePath())).status("NOT-A-DIRECTORY");
            }
            return ColList.list().regular(fix(f.getAbsolutePath()));
            // Stopped including file and dir-count, since it may take a lot of time
            // for remote directories
            
//            try { 
//                File[] content=f.listFiles();
//                int fCount=0;
//                int dCount=0;
//                for (File c:content) {
//                    if (c.isDirectory()) dCount++;
//                    else if (c.isFile()) fCount++;
//                }
//                return ColList.list().regular(fix(f.getAbsolutePath())).status("d:"+dCount).status("f:" + fCount);
//            } catch (Exception ex) {
//                return ColList.list().regular(fix(f.getAbsolutePath())).status("d:?").status("f:?").status("NO-ACCESS");
//            }
        }
        return ColList.list().regular(fix(f.getAbsolutePath())).status("DOES-NOT-EXIST");
    }

    private String fix (String name) {
        int pos=name.lastIndexOf(File.separator);
        if (pos > 0) name=name.substring(pos+1);
        return name+File.separator; // to differ from files when presented in common list (presentation only)
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
            return new ValueObj(new ObjDir(name+File.separator+params.get(0).getValAsString(), protection));
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
            return new ValueObj(new ObjFile(fileName, protection));
        }
    }
    
    
    private ObjGlob getObjGlob (List<Value> params, int pos) throws Exception {
        Value objOrString = params.get(pos);
        if (objOrString instanceof ValueString) {
            boolean ignoreCase=(File.separator.equals("\\"));  // ignore case on Windows, otherwise not
            return new ObjGlob( ((ValueString) objOrString).getVal(), ignoreCase);
        }
        Obj obj=getObj("Glob", params, pos);
        if (!(obj instanceof ObjGlob)) throw new Exception("Expected String or Glob parameter");
        return (ObjGlob) obj;
    }
    

    class FunctionFiles extends Function {
        public String getName() {
            return "files";
        }
        public String getShortDesc() {
            return "files(Glob?) - returns list of File objects";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            ObjGlob glob=null;
            if (params.size()==1) {
                glob=getObjGlob(params,0);
            } else if (params.size() != 0) {
                throw new Exception("Expected optional Glob parameter only");
            }


            File f=new File(name);
            List<Value> result=new ArrayList<Value>();
            for (String s:f.list()) {
                File x=new File(name + File.separator + s);
                if (x.isFile()) {
                    if (glob != null && !glob.matches(s)) continue;
                    result.add(new ValueObj(new ObjFile(x.getCanonicalPath(), protection)));
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
            ObjGlob glob=null;
            if (params.size()==1) {
                glob=getObjGlob(params,0);
            } else if (params.size() != 0) {
                throw new Exception("Expected optional Glob parameter only");
            }


            File f=new File(name);
            List<Value> result=new ArrayList<Value>();
            for (String s:f.list()) {
                File x=new File(name + File.separator + s);
                if (x.isDirectory()) {
                    if (glob != null && !glob.matches(s)) continue;
                    result.add(new ValueObj(new ObjDir(x.getCanonicalPath(), protection)));
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
            
            validateDestructiveOperation("create");

            OutText outText=ctx.getOutText();
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
            
            validateDestructiveOperation("delete");

            OutText outText=ctx.getOutText();
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

            validateDestructiveOperation("copy target dir");

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

    
    class FunctionAllFiles extends Function {
        public String getName() {
            return "allFiles";
        }
        public String getShortDesc() {
            return "allFiles(Glob?) - returns list of all File objects under this directory";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            OutText outText=ctx.getOutText();
            ObjGlob glob=null;
            if (params.size()==1) {
                glob=getObjGlob(params,0);
            } else if (params.size() != 0) {
                throw new Exception("Expected optional Glob parameter only");
            }
            List<File> allFiles=new ArrayList<File>();
            traverse(ctx.getStdio(), new File(name), glob, null, allFiles);
            
            
            List<Value> result=new ArrayList<Value>();
            for (File f:allFiles) {
                result.add(new ValueObj(new ObjFile(f.getCanonicalPath(), protection)));
            }
            return new ValueList(result);
        }
    }

    class FunctionAllDirs extends Function {
        public String getName() {
            return "allDirs";
        }
        public String getShortDesc() {
            return "allDirs(Glob?) - returns list of Dir objects under this directory";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            ObjGlob glob=null;
            if (params.size()==1) {
                glob=getObjGlob(params,0);
            } else if (params.size() != 0) {
                throw new Exception("Expected optional Glob parameter only");
            }

            List<File> allDirs=new ArrayList<File>();
            traverse(ctx.getStdio(), new File(name), glob, allDirs, null);
            
            
            List<Value> result=new ArrayList<Value>();
            for (File f:allDirs) {
                result.add(new ValueObj(new ObjDir(f.getCanonicalPath(), protection)));
            }
            return new ValueList(result);
        }
    }

    
    private void traverse (Stdio stdio, File currDir, ObjGlob glob, List<File> dirs, List<File> files) throws Exception {
        File[] list=currDir.listFiles();
        for (File f:list) {
            if (f.getName().equals(".") || f.getName().equals("..")) continue;
            if (f.isDirectory()) {
                // search for sub-dirs before adding dir, this makes it safe
                // to delete dioutTextrectories in the order presented
                try {
                    traverse(stdio, f, glob, dirs, files);
                } catch(Exception ex) {
                    stdio.println("Traversing dir " + f + " " + ex.getMessage());
                }
                if (dirs != null) {
                    if (glob==null || glob.matches(f.getName())) dirs.add(f);
                }
            } else {
                if (files != null)  {
                    if (glob==null || glob.matches(f.getName())) files.add(f);
                }
            }
        }
        
    }
    
   private void callExternalProgram (Ctx ctx, boolean foreground, Stdio stdio, OutText outText, RunCaptureOutput capture, List<Value> params) throws Exception {
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
        if (capture==null) processBuilder.redirectOutput(Redirect.INHERIT);
        
        // set current directory
        processBuilder.directory(new File(name));
        
        long startTime=System.currentTimeMillis();
        Process process = processBuilder.start();
        
        if (capture != null) {
            BufferedReader br=null;
            try {
                br=new BufferedReader(new InputStreamReader(process.getInputStream()));
                for (;;) {
                    String line=br.readLine();
                    if (line==null) break;
                    capture.addLine(line);
                    //stdio.println(line);
                }
            } finally {
                if (br != null) br.close();
            }
        }
        
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
            callExternalProgram(ctx, true, ctx.getStdio(), ctx.getOutText(), null, params);
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
            callExternalProgram(ctx, false, ctx.getStdio(), ctx.getOutText(), null, params);
            return new ValueObj(self());

        }
    }
    
    class FunctionRunCapture extends Function {
        public String getName() {
            return "runCapture";
        }
        public String getShortDesc() {
            return "runCapture(list|...) - execute external program in foreground, but capture output, and return list of stdout lines";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            RunCaptureOutput capt=new RunCaptureOutput();
            callExternalProgram(ctx, true, ctx.getStdio(), ctx.getOutText(), capt, params);
            return capt.getCapturedLines();
        }
    }
    

    private Process startProcess (Ctx ctx, File input, File output, File stderr, List<Value> params) throws Exception {

        int exitCode=-999;
        
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
        
        processBuilder.redirectInput(input);
        processBuilder.redirectOutput(output);
        processBuilder.redirectError(stderr);
        
        // set current directory
        processBuilder.directory(new File(name));
        
        Process process = processBuilder.start();

        return process;
        
    }



    class FunctionRunProcess extends Function {
        public String getName() {
            return "runProcess";
        }
        public String getShortDesc() {
            return "runProcess(stdinFile, stdoutFile, stdErrFile, list|...) - start external program - returns Process object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            ObjFile stdin = (ObjFile) getObj("stdinFile", params, 0);
            ObjFile stdout = (ObjFile) getObj("stdoutFile", params, 1);
            ObjFile stderr = (ObjFile) getObj("stderrFile", params, 2);
            
		      List<Value> cmd=new ArrayList<Value>();
		      for (int i=3; i<params.size(); i++) cmd.add(params.get(i));
            
            
            Process process = startProcess(ctx, stdin.getFile(), stdout.getFile(), stderr.getFile(), cmd);

            return new ValueObj(new ObjExtProcess(process));
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



    class FunctionProtect extends Function {
        public String getName() {
            return "protect";
        }
        public String getShortDesc() {
            return "protect(desc?) - set protection status, returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            String desc="-";
            if (params.size() > 1) throw new Exception("Expected optional string parameter");
            if (params.size()==1) {
                desc=getString("desc", params, 0);
            }
            protection = new Protection(desc);
            return new ValueObj(self());
        }
    }
    

    class FunctionUnprotect extends Function {
        public String getName() {
            return "unprotect";
        }
        public String getShortDesc() {
            return "unprotect() - unprotect protected directory - error if not protected - returns self";
                // Because if we unprotect something and it isn't protected, we don't have
                // control of what we are doing, and the protection mechanism is all 
                // about ensuring control
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (!protection.isActive()) throw new Exception("unprotect: " + name + " - not protected.");
            protection=Protection.NoProtection;
            return new ValueObj(self());
        }
    }
    

  
    class FunctionCd extends Function {
        public String getName() {
            return "cd";
        }
        public String getShortDesc() {
            return "cd() - use this Dir as current work dir - returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            ctx.getObjGlobal().setCurrDir(name);
            return new ValueObj(self());
        }
    }
    

    class FunctionVerify extends Function {
        public String getName() {
            return "verify";
        }
        public String getShortDesc() {
            return "verify(str) - verify exists, and return self, or throw soft error with str";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected str parameter");
            String str=getString("str", params, 0);
            
            File f=new File(name);
            boolean ok=f.exists() && f.isDirectory();
            if (!ok) throw new SoftErrorException(str + ": " + name); 

            return new ValueObj(self());
        }
    }
    

    class FunctionNewestFile extends Function {
        public String getName() {
            return "newestFile";
        }
        public String getShortDesc() {
            return "newestFile(Glob?) - return file last modified";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            OutText outText=ctx.getOutText();
            ObjGlob glob=null;
            if (params.size()==1) {
                glob = getObjGlob(params,0);
            } else if (params.size() != 0) {
                throw new Exception("Expected optional Glob parameter only");
            }
            
            File[] list=getDir().listFiles();
            long newestTime=0L;
            File newestFile=null;
            
            for (File f:list) {
                if (!f.isFile()) continue;
                if (glob != null && !glob.matches(f.getName())) continue;
                if (f.lastModified() > newestTime) {
                    newestTime=f.lastModified();
                    newestFile=f;
                }
            }
            
            if (newestFile==null) {
                throw new SoftErrorException("No such file");
            }
            return new ValueObj(new ObjFile(newestFile.getCanonicalPath(), self().protection));
        }
    }

    
    class FunctionNewestFiles extends Function {
        public String getName() {
            return "newestFiles";
        }
        public String getShortDesc() {
            return "newestFiles(Glob?,count) - return sorted list (newest first) of newest files";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            ObjGlob glob=null;
            int count=0;
            if (params.size() == 2) {
        		glob=getObjGlob(params,0);
            	count=(int) getInt("count",params,1);
        	} else if (params.size()==1) {
            	count=(int) getInt("count",params,0);
        	} else {
            	throw new Exception("Expected parameter glob?, count");
        	}
            
            File f=new File(name);
            File[] content = f.listFiles();

            List<File> fileList=new ArrayList<File>();
            for (File x:content) {
                try {
                    if (x.isFile()) {
                    	if (glob == null || glob.matches(x.getName())) fileList.add(x); 
                    } 
                } catch (Exception ex) {
                    // ignore
                }
            }

//            
//            Comparator<File> comp=new Comparator<File>() {
//                public int compare(File a, File b) {
//                    long ia;
//                    try {
//                        ia=a.lastModified();
//                    } catch (Exception ex) {
//                        ia=0L;
//                    }
//                    long ib;
//                    try {
//                        ib=b.lastModified();
//                    } catch (Exception ex) {
//                        ib=0L;
//                    }
//                    if (ia>ib) return -1;
//                    if (ia==ib) return 0;
//                    return 1;
//                    
//                }
//            };
//          
            
            FileModifiedSort.sort(fileList);
            List<Value> valList=new ArrayList<Value>();
            if (count > fileList.size()) count=fileList.size();
            CREATE_RESULT: for (File file:fileList) {
                try {
                    valList.add(new ValueObj(new ObjFile(file.getAbsolutePath(), Protection.NoProtection)));
                } catch (Exception ex) {
                    // ignore
                }
                if (valList.size()==count) break CREATE_RESULT;
            }
            
            return new ValueList(valList);
        }
    }
  
    

    
    class FunctionStats extends Function {
        public String getName() {
            return "stats";
        }
        public String getShortDesc() {
            return "stats() - return dictionary with stats for directory";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            File f=new File(name);
            
            int fileCount=0;
            int dirCount=0;

            File[] content = f.listFiles();
            for (File x:content) {
                try {
                    if (x.isFile()) { fileCount++; } 
                    else if (x.isDirectory()) { dirCount++; }
                } catch (Exception ex) {
                    // ignore
                }
            }
            ObjDict dict=new ObjDict();
            dict.set("fileCount", new ValueInt(fileCount));
            dict.set("dirCount", new ValueInt(dirCount));
            
            return new ValueObj(dict);
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
