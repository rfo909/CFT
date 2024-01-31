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

package rf.configtool.main.runtime.lib;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.OutText;
import rf.configtool.main.SoftErrorException;
import rf.configtool.main.Stdio;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.IsSynthesizable;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBinary;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueInt;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.root.shell.FileSet;
import rf.configtool.root.shell.Arg;
import rf.configtool.util.Encrypt;
import rf.configtool.util.FileInfo;
import rf.configtool.util.Hex;
import rf.configtool.util.TabUtil;
import rf.configtool.util.DateTimeDurationFormatter;


public class ObjFile extends Obj implements IsSynthesizable {

    static final String DefaultEncoding = "ISO_8859_1";
    
    private String name;
    private Protection protection;
    private String assignedEncoding=null;
    private String customEOL; // for overriding CRLF / LF

    public ObjFile(String name, Protection protection) throws Exception {
        this.name=name;
        this.protection=protection;
        
        if (protection==null) {
            throw new Exception("protection==null is invalid, use Protection.NoProtection");
        }

        if (!isSymlink()) {
            try {
                File f=new File(name);
                if (f.exists()) {
                    this.name=f.getCanonicalPath();
                }
            } catch (Exception ex) {
                this.name=name;
            }
        }
        
        if (isSymlink()) {
            this.protection=new Protection("isSymlink");
        }
        
        Function[] arr={
                new FunctionExists(),
                new FunctionName(),
                new FunctionPath(),
                new FunctionDir(),
                new FunctionLength(),
                new FunctionDelete(),
                new FunctionCreate(),
                new FunctionAppend(),
                new FunctionRead(),
                new FunctionLastModified(),
                new FunctionHash(),
                new FunctionMore(),
                new FunctionUncompress(),
                new FunctionCopyFrom(),
                new FunctionCopyTo(),
                new FunctionMove(),
                new FunctionHex(),
                new FunctionReadBytes(),
                new FunctionEncoding(),
                new FunctionProtect(),
                new FunctionUnprotect(),
                new FunctionTail(),
                new FunctionHead(),
                new FunctionSetWriteLF(),
                new FunctionSetWriteCRLF(),
                new FunctionConvertCompressed(),
                new FunctionReadBinary(),
                new FunctionBinaryCreate(),
                new FunctionVerify(),
                new FunctionEncrypt(),
                new FunctionDecrypt(),
                new FunctionGetTimes(),
                new FunctionSetTimes(),
                new FunctionTouch(),
        };
        setFunctions(arr);

    }
    
    
    
    public String getEncoding () {
        if (assignedEncoding != null) return assignedEncoding;
        File f=getFile();
        if (f.exists() && f.isFile()) {
            try {
                FileInputStream fis=null;
                try {
                    fis = new FileInputStream(f);

                    byte[] bom=new byte[4];
                    int count=fis.read(bom);
                    if (count != 4) return DefaultEncoding;

                    String str=Hex.toHex(bom,4);

                    // https://en.wikipedia.org/wiki/Byte_order_mark

                    if (str.equals("0000FEFF")) return "UTF-32BE";
                    if (str.equals("FFFE0000")) return "UTF-32LE";

                    if (str.startsWith("FEFF")) return "UTF-16BE";
                    if (str.startsWith("FFFE")) return "UTF-16LE";

                    if (str.startsWith("EFBBBF")) return "UTF-8";

                } finally {
                    if (fis != null) try {fis.close();} catch (Exception ex) {};
                }
            } catch (Exception ex) {
                // ignore
            }
        }
        return DefaultEncoding;
    }
    
    /**
     * Get BOM byte sequence for encoding by (Java) name
     */
    private byte[] getBOM () {
        String encoding=getEncoding();
        String s;
        
        if (encoding.equals("UTF-32BE")) s="0000FEFF";
        else if(encoding.equals("UTF-32LE")) s="FFFE0000";
        else if(encoding.equals("UTF-16BE")) s="FEFF";
        else if(encoding.equals("UTF-16LE")) s="FFFE";
        else if(encoding.equals("UTF-8")) s="EFBBBF";
        else s=""; // no BOM
        
        return Hex.fromHex(s);
    }
    
    
    /**
     * Get BufferedReader for file, correctly initialized with encoding and advanced past potential byte-order-mark (BOM) for unicode
     */
    public BufferedReader getBufferedReader () throws Exception {
        File f=new File(name);
        if (!f.exists() || !f.isFile()) {
            throw new Exception("File does not exist");
        }
        FileInputStream fis=new FileInputStream(f);
        
        // read past BOM if there is one
        int bomBytes = getBOM().length;
        while (bomBytes>0) {
            fis.read();
            bomBytes--;
        }

        return new BufferedReader(new InputStreamReader(fis, getEncoding()));

    }
    
    public Protection getProtection() {
        return protection;
    }
    
    public void validateDestructiveOperation (String op) throws Exception {
        protection.validateDestructiveOperation(op, name);
   }
    
      
    public boolean isSymlink() {
        Path path=Paths.get(name);
        return Files.isSymbolicLink(path);
    }

    protected ObjFile self() {
        return this;
    }
    
    public boolean fileExists() {
        File f=new File(name);
        return (f.isFile() && f.exists());
    }
    
    public File getFile() {
        return new File(name);
    }

    @Override
    public boolean eq(Obj x) {
        if (x instanceof ObjFile) {
            ObjFile f=(ObjFile) x;
            return f.name.equals(name); // always canonical when possible
        }
        return false;
    }

    @Override
    public String createCode() throws Exception {
        String enc="";
        String prot="";
        String encoding=getEncoding();
        if (!encoding.equals(DefaultEncoding)) {
            // setEncoding returns self!
            enc=".encoding(" + (new ValueString(encoding)).synthesize() + ")";
        }
        if (protection != null && protection.isActive()) {
            prot=".protect(" + (new ValueString(protection.getCode()).synthesize() + ")");
        }
        return "File(" + (new ValueString(name)).synthesize() + ")" + enc + prot;
    }


    
    public String getPath() {
        return name;
    }
    
    public String getName() {
        int pos=name.lastIndexOf(File.separator);
        if (pos >= 0) return name.substring(pos+1);
        return name;
    }

    public String getTypeName() {
        return "File";
    }
    public ColList getContentDescription() {
        File f=new File(name);
        if (!f.exists()) {
            return ColList.list().regular(fix(name)).status("").status("").status("DOES-NOT-EXIST");
        }
        if (!f.isFile()) {
            return ColList.list().regular(fix(name)).status("").status("").status("NOT-A-FILE");
        }
        long millisSinceModify=System.currentTimeMillis()-f.lastModified();
        String ageString=new DateTimeDurationFormatter(millisSinceModify).fmt();
        return ColList.list().regular(fix(name)).status(fmtSize(f.length())).status(""+f.length()).status(ageString).status(fmtDate(f.lastModified()));
    }
    
    private String fix (String name) {
        int pos=name.lastIndexOf(File.separator);
        if (pos > 0) name=name.substring(pos+1);
        return name;
    }
    
    private String dateFmt="yyyy-MM-dd HH:mm:ss";

    private String fmtDate (long millis) {
        SimpleDateFormat sdf=new SimpleDateFormat(dateFmt);
        return sdf.format(new Date(millis));
    }
    
    private String fmtSize (long count) {
        long k=count/1024;
        return k+"k";
    }
    
    // Implenting customEOL override
    
    private void outln(PrintStream ps, String line) throws Exception {
        if (customEOL==null) {
            ps.println(line);
        } else {
            ps.print(line);
            ps.print(customEOL);
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
            return new ValueBoolean(fileExists());
        }
    }

    class FunctionName extends Function {
        public String getName() {
            return "name";
        }
        public String getShortDesc() {
            return "name() - returns string (last part)";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            File f=new File(name);
            return new ValueString(f.getName());
        }
    }

    class FunctionPath extends Function {
        public String getName() {
            return "path";
        }
        public String getShortDesc() {
            return "path() - returns full path as string";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            File f=new File(name);
            return new ValueString(f.getCanonicalPath());
        }
    }

    class FunctionLength extends Function {
        public String getName() {
            return "length";
        }
        public String getShortDesc() {
            return "length() - returns length of files as int";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueInt(new File(name).length());
        }
    }

    class FunctionDelete extends Function {
        public String getName() {
            return "delete";
        }
        public String getShortDesc() {
            return "delete() - delete file (if it exists), returns true if file existed and was deleted, otherwise false";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            
            validateDestructiveOperation("delete");

            OutText outText=ctx.getOutText();
            File f=new File(name);
            if (f.exists()) {
                if (f.isFile()) {
                    boolean ok=f.delete();
                    if (!ok) {
                        ctx.getObjGlobal().addSystemMessage("Delete failed : " + f.getCanonicalPath());
                    }
                    return new ValueBoolean(ok);
                } else {
                    ctx.getObjGlobal().addSystemMessage("Not a file    : " + f.getCanonicalPath());
                    return new ValueBoolean(false);
                }
            } 
            ctx.getObjGlobal().addSystemMessage("No such file  : " + f.getCanonicalPath());
            return new ValueBoolean(false);
        }
    }



    class FunctionCreate extends Function {
        public String getName() {
            return "create";
        }
        public String getShortDesc() {
            return "create(expr) - create file if it doesn't exist, with content as given, returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected one parameter any type (file data)");
            
            validateDestructiveOperation("create");

            OutText outText=ctx.getOutText();
            File f=new File(name);

            PrintStream ps=null;
            try {
                FileOutputStream fos=new FileOutputStream(f);
                fos.write(getBOM());
                
                ps=new PrintStream(fos,false, getEncoding());
                Value content=params.get(0);
                if (content instanceof ValueList) {
                    List<Value> lines=((ValueList)content).getVal();
                    for (Value line:lines) {
                        // process as lines
                        outln(ps,line.getValAsString());
                    }
                } else {
                    outln(ps,content.getValAsString());
                }
            } finally {
                if (ps != null) try {ps.close();} catch (Exception ex) {};
            }
            return new ValueObj(self());
        }
    }

    class FunctionAppend extends Function {
        public String getName() {
            return "append";
        }
        public String getShortDesc() {
            return "append(expr) - append content to existing file";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected one parameter any type (file data)");
            
            validateDestructiveOperation("append");

            File f=new File(name);
            
            PrintStream ps=null;
            try {
                FileOutputStream fos;
                if (f.exists() && f.length()>0) {
                    fos=new FileOutputStream(f, true);
                } else {
                    fos=new FileOutputStream(f);
                    fos.write(getBOM());
                }
                
                ps=new PrintStream(fos, false, getEncoding());
                Value content=params.get(0);
                if (content instanceof ValueList) {
                    List<Value> lines=((ValueList)content).getVal();
                    for (Value line:lines) {
                        // process as lines
                        outln(ps,line.getValAsString());
                    }
                } else {
                    outln(ps,content.getValAsString());
                }
            } finally {
                if (ps != null) try {ps.close();} catch (Exception ex) {};
            }
            return new ValueBoolean(true);
        }
    }

    class FunctionRead extends Function {
        public String getName() {
            return "read";
        }
        public String getShortDesc() {
            return "read() - read text file, returns list of lines";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            List<Value> result=new ArrayList<Value>();
            BufferedReader br=null;
            long lineNo=0;
            try {
                
                br = getBufferedReader();
                for (;;) {
                    String line=br.readLine();
                    lineNo++;
                    if (line==null) break;
                    
                    String deTabbed=TabUtil.substituteTabs(line,4);
                    result.add(new ValueObjFileLine(deTabbed, lineNo, self()));  
                        // ObjFileLine is subclass of ValueString
                }
            } finally {
                if (br != null) try {br.close();} catch (Exception ex) {};
            }
            return new ValueList(result);
        }
    }

    class FunctionDir extends Function {
        public String getName() {
            return "dir";
        }
        public String getShortDesc() {
            return "dir() - get Dir object for this file";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            int pos=name.lastIndexOf(File.separatorChar);
            if (pos < 0) throw new Exception("No directory found");
            return new ValueObj(new ObjDir(name.substring(0,pos), protection));
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
    

    class FunctionHash extends Function {
        public String getName() {
            return "hash";
        }
        public String getShortDesc() {
            return "hash() - return hash as hex string";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            File f=new File(name);
            if (!f.exists() || !f.isFile()) throw new Exception("Invalid file");
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");  // 32 bytes hash
            byte[] buf=new byte[4096];
            FileInputStream fis=null;
            try {
                fis=new FileInputStream(f);
                for(;;) {
                    int count=fis.read(buf);
                    if (count <= 0) break;
                    digest.update(buf,0,count);
                }
            } finally {
                if (fis != null) try {fis.close();} catch (Exception ex) {};
            }
            
            byte[] hash=digest.digest();
            
            String digits="0123456789abcdef";
            StringBuffer sb=new StringBuffer();
            for (int i=0; i<hash.length; i++) {
                byte b=hash[i];
                sb.append(digits.charAt( (b>>4) & 0x0F ));
                sb.append(digits.charAt( b & 0x0F ));
            }
            
            
            return new ValueString(sb.toString());
        }
    }

    class FunctionMore extends Function {
        public String getName() {
            return "more";
        }
        public String getShortDesc() {
            return "more() - page through a file";
        }


        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            Stdio stdio=ctx.getStdio();

            final int lines=ctx.getObjGlobal().getRoot().getObjTerm().getScreenHeight()-2; // room for info line + input line
            final int width=ctx.getObjGlobal().getRoot().getObjTerm().getScreenWidth()-2; // a little space to the right

            File f=new File(name);
            if (!f.exists() || !f.isFile()) throw new Exception("Invalid file");
            
            BufferedReader br=null;
            try {

                br = getBufferedReader();

                int lineNo=0;
                int linesDisplayed=0;
                for (;;) {
                    String line=br.readLine();
                    if (line==null) {
                        stdio.println("[" + f.getName() + " | EOF]");
                        break;
                    }
                    
                    line=TabUtil.substituteTabs(line,4);
                    
                    lineNo++;
                    String pre=fmt(lineNo,3) + " | ";
                    line=pre+line;
                    if (line.length()>width-1) {
                        line=line.substring(0,width-1) + "+";
                    }
                    stdio.println(line);
                    linesDisplayed++;
                    
                    
                    
                    if (linesDisplayed >= lines) {
                        stdio.println("[" + f.getName() + " | line:" + lineNo + "] ENTER to continue, 'q' to quit");
                        String s=ctx.getStdio().getInputLine();
                        if (s.trim().equals("q")) break;
                        linesDisplayed=0;
                    }
                }
                return new ValueObj(self());
            } finally {
                if (br != null) try {br.close();} catch (Exception ex) {};
            }
        }
    }
    
    
    class FunctionUncompress extends Function {
        public String getName() {
            return "uncompress";
        }
        public String getShortDesc() {
            return "uncompress(TargetDir) - copy file to target dir, uncompress if compressed, returns target file";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected TargetDir parameter");
            Obj obj=getObj("TargetDir", params, 0);
            if (!(obj instanceof ObjDir)) throw new Exception("Expected TargetDir parameter");
            
            ObjDir targetDir=(ObjDir) obj;
            
            targetDir.validateDestructiveOperation("uncompress target dir");
            
            FileInfo info=new FileInfo(name);
            
            String targetFileName;
            if (info.getCompression() != FileInfo.UNCOMPRESSED) {
                targetFileName=info.getFileStem();
            } else {
                targetFileName=getName();
            }
            String targetPath=targetDir.getName() + File.separator + targetFileName;
            InputStream in=info.getInputStream();
            OutputStream out=new FileOutputStream(targetPath);
            byte[] buf=new byte[8192];
            
            for (;;) {
                int count=in.read(buf);
                if (count <= 0) break;
                out.write(buf, 0, count);
            }
            
            in.close();
            out.close();
            return new ValueObj(new ObjFile(targetPath, targetDir.getProtection()));
        }
    }

    class FunctionConvertCompressed extends Function {
        public String getName() {
            return "convertCompressed";
        }
        public String getShortDesc() {
            return "convertCompressed(TargetFile) - compress or uncompress between GZIP (.z and .gz) or ZIP (.zip) or plain (other ending)";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected TargetFile parameter");
            Obj obj=getObj("TargetFile", params, 0);
            if (!(obj instanceof ObjFile)) throw new Exception("Expected TargetFile parameter");
            
            ObjFile targetFile=(ObjFile) obj;
            
            targetFile.validateDestructiveOperation("file compress target");
            
            FileInfo source=new FileInfo(self().getPath());
            FileInfo target=new FileInfo(targetFile.getPath());
            target.copyFrom(source);
            return new ValueObj(targetFile);
        }
    }

    class FunctionCopyFrom extends Function {
        public String getName() {
            return "copyFrom";
        }
        public String getShortDesc() {
            return "copyFrom(File) - returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected File parameter");


            OutText outText=ctx.getOutText();

            Obj obj=getObj("File", params, 0);
            if (!(obj instanceof ObjFile)) throw new Exception("Expected File parameter");

            ObjFile srcFile=(ObjFile) obj;
            ObjFile targetFile = self();

            targetFile.validateDestructiveOperation ("copyFrom target");

            File src=new File(srcFile.getPath());
            File target=new File(targetFile.getPath());

            if (!src.isFile()) throw new Exception("Source '" + src.getCanonicalPath() + "' is not a file");
            if (target.exists()) {
                if (!target.isFile()) throw new Exception("Target '" + target.getCanonicalPath() + "' exists, but is not a file");
                ctx.getObjGlobal().addSystemMessage("Overwriting file: " + target.getCanonicalPath());
            }
            InputStream in=null;
            OutputStream out=null;
            try {
                in=new FileInputStream(src);
                out=new FileOutputStream(target);
                byte[] buf=new byte[64*1024];
                for (;;) {
                    int count=in.read(buf);
                    if (count <= 0) break;
                    out.write(buf, 0, count);
                }
            } finally {
                if (in != null) try {in.close();} catch (Exception ex) {};
                if (out != null) try {out.close();} catch (Exception ex) {};
            }
            return new ValueObj(self());
        }
    }


    class FunctionCopyTo extends Function {
        public String getName() {
            return "copyTo";
        }
        public String getShortDesc() {
            return "copyTo(File) - returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected File parameter");
            OutText outText=ctx.getOutText();

            Obj obj=getObj("File", params, 0);
            if (!(obj instanceof ObjFile)) throw new Exception("Expected File parameter");

            ObjFile srcFile=self();
            ObjFile targetFile=(ObjFile) obj;


            targetFile.validateDestructiveOperation("copyTo");

            File src=new File(self().getPath());
            File target=new File(targetFile.getPath());
            

            if (!src.isFile()) throw new Exception("Source '" + src.getCanonicalPath() + "' is not a file");
            if (target.exists()) {
                if (!target.isFile()) throw new Exception("Target '" + target.getCanonicalPath() + "' exists, but is not a file");
                ctx.getObjGlobal().addSystemMessage("Overwriting file: " + target.getCanonicalPath());
            }
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

            return new ValueObj(self());
        }
    }
    
    class FunctionMove extends Function {
        public String getName() {
            return "move";
        }
        public String getShortDesc() {
            return "move(toFile) - returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected toFile parameter");
            OutText outText=ctx.getOutText();

            Obj obj=getObj("toFile", params, 0);
            if (!(obj instanceof ObjFile)) throw new Exception("Expected File parameter");

            ObjFile srcFile=self();
            ObjFile targetFile=(ObjFile) obj;
            
            srcFile.validateDestructiveOperation("move source");
            targetFile.validateDestructiveOperation("move target");
 
            File src=new File(srcFile.getPath());
            File target=new File(targetFile.getPath());

            if (!src.isFile()) throw new Exception("Source '" + src.getCanonicalPath() + "' is not a file");
            if (target.exists()) {
                if (!target.isFile()) throw new Exception("Target '" + target.getCanonicalPath() + "' exists, but is not a file");
                ctx.getObjGlobal().addSystemMessage("Overwriting file: " + target.getCanonicalPath());
            }
            boolean ok=src.renameTo(target);
            if (!ok) throw new Exception("mv failed");

            return new ValueObj(self());
        }
    }

    class FunctionHex extends Function {
        public String getName() {
            return "hex";
        }
        public String getShortDesc() {
            return "hex() - page through file content in hex";
        }
        
        private String toHex (int i) {
            final String digits="0123456789abcdef";
            return "" + digits.charAt(i>>4) + digits.charAt(i&0x0F);
        }
        
        private final int ValuesPerLine = 10;

        private String findChar(int i) {
            if (i==9) {
                return "\\t";
            }
            if (i==13) {
                return "\\r";
            }
            if (i==10) {
                return "\\n";
            }

            if (i>=32 && i <= 126) {
                byte[] b = { (byte) i };
                try {
                    String s=new String(b,"ISO-8859-1");
                    if (s.length() != 1) return ". ";
                    return s + " ";
                } catch (Exception ex) {
                    return ". ";
                }
            } else return ". ";
        }
        
        private String createLine (int lineNumber, StringBuffer sb1, StringBuffer sb2) {
            String num=""+(lineNumber*ValuesPerLine);
            while(num.length() < 5) num=" "+num;
            
            String a=sb1.toString();
            while (a.length() < ValuesPerLine*2) a+=" ";
            return num + " " + a + "   " + sb2.toString().trim();
        }
        

        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            Stdio stdio=ctx.getStdio();
            
            final int lines=ctx.getObjGlobal().getRoot().getObjTerm().getScreenHeight()-2; // room for info line + input line
            
            
            File f=getFile();
            //List<Value> result=new ArrayList<Value>();
            
            StringBuffer sb1=new StringBuffer();
            StringBuffer sb2=new StringBuffer();
            FileInputStream fis=null;
            
            int linesDisplayed=0;
            
            int lineNumber=0;
            
            try {
                fis=new FileInputStream(f);
                int countInLine=0;
                for (;;) {
                    int b=fis.read();
                    if (b < 0) break;
                    sb1.append(findChar(b));
                    sb2.append(" " + toHex(b));
                    countInLine++;
                    if (countInLine >= ValuesPerLine) {
                        stdio.println(createLine(lineNumber++, sb1,sb2));
                        sb1=new StringBuffer();
                        sb2=new StringBuffer();
                        countInLine=0;

                        linesDisplayed++;
                        if (linesDisplayed >= lines) {
                            stdio.println("[" + f.getName() + "] ENTER to continue, 'q' to quit");
                            String s=ctx.getStdio().getInputLine();
                            if (s.trim().equals("q")) break;
                            linesDisplayed=0;
                        }
                    }
                }
                if (countInLine>0) {
                    stdio.println(createLine(lineNumber,sb1,sb2));
                }
            } finally {
                if (fis != null) try {fis.close();} catch (Exception ex) {}
            }
            return new ValueObj(self());
        }
    } 

    
    class FunctionReadBytes extends Function {
        public String getName() {
            return "readBytes";
        }
        public String getShortDesc() {
            return "readBytes(count) - returns list of int";
        }
        
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected parameter count");
            long count=getInt("count",params,0);
            
            
            File f=getFile();
            List<Value> result=new ArrayList<Value>();
            
            FileInputStream fis=null;
            try {
                fis=new FileInputStream(f);
                byte[] buf=new byte[(int) count];
                int numRead=fis.read(buf);
                for (int i=0; i<numRead; i++) {
                    result.add(new ValueInt(buf[i]));
                }
            } finally {
                if (fis != null) try {fis.close();} catch (Exception ex) {}
            }
            return new ValueList(result);
        }
    } 

    
    class FunctionEncoding extends Function {
        public String getName() {
            return "encoding";
        }
        public String getShortDesc() {
            return "encoding(encoding?) - get or set encoding (returns self)";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() == 1) {
                String targetEncoding=getString("encoding", params, 0);
                if (!Charset.isSupported(targetEncoding)) throw new Exception("Charset '" + targetEncoding + "' not supported");
                assignedEncoding=targetEncoding;
                return new ValueObj(self()); // important for synthesis
            } else if (params.size() != 0) {
                throw new Exception("Expected single optional parameter encoding");
            }
            return new ValueString(getEncoding());
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
            return "unprotect() - unprotect protected file - error if not protected - returns self";
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
    

    

    class FunctionTail extends Function {
        public String getName() {
            return "tail";
        }
        public String getShortDesc() {
            return "tail(count) - returns list of lines from the tail end of file";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            // Using random access for big files
            final int BYTES_PER_LINE = 256;
            
            if (params.size() != 1) throw new Exception("Expected count parameter");
            final int count=(int) getInt("count", params, 0);
            String[] lines=new String[count];
            
            File f=new File(name);
            if (!f.exists()) {
                // return empty list 
                List<Value> list=new ArrayList<Value>();
                return new ValueList(list);
            }
            long seekPos=f.length()-(count*BYTES_PER_LINE);
            if (seekPos < 0) seekPos=0;

            BufferedReader br=null;
            RandomAccessFile raf=new RandomAccessFile(f,"r");
            int readLines=0;
            String encoding=getEncoding();
            try {
                //System.out.println("Seeking to " + seekPos);
                raf.seek(seekPos);
                br = new BufferedReader(new InputStreamReader(new FileInputStream(raf.getFD()), encoding));

                for (;;) {
                    String line=br.readLine();
                    if (line==null) break;

                    lines[readLines%count]=line;
                    readLines++;
                }
                
                List<Value> result=new ArrayList<Value>();
                for (int i=0; i<count; i++) {
                    int pos=(readLines+i)%count;
                    long lineNo = readLines-count+i;
                    if (lineNo < 0) continue;
                    String s=lines[pos];
                    String deTabbed=TabUtil.substituteTabs(s, 4);
                    result.add(new ValueObjFileLine(deTabbed, seekPos==0 ? lineNo : -1, self()));
                }
                
                return new ValueList(result);
            } finally {
                if (raf != null) try {raf.close();} catch (Exception ex) {};
                if (br != null) try {br.close();} catch (Exception ex) {};
            }
            
        }
    }
    

    
    class FunctionHead extends Function {
        public String getName() {
            return "head";
        }
        public String getShortDesc() {
            return "head(count) - returns list of lines from start of file (empty list if file doesn't exist)";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected count parameter");
            final int count=(int) getInt("count", params, 0);
            
            File f=new File(name);
            if (!f.exists()) {
                // return empty list 
                List<Value> list=new ArrayList<Value>();
                return new ValueList(list);
            }

            List<Value> list=new ArrayList<Value>();
            
            BufferedReader br=null;
            try {
                br = getBufferedReader();
                
                long lineNo=0;
                for (;;) {
                    String line=br.readLine();
                    lineNo++;
                    if (line==null) break;
                    
                    String deTabbed=TabUtil.substituteTabs(line, 4);
                    list.add(new ValueObjFileLine(deTabbed, lineNo, self()));
                    if (list.size() >= count) break;
                }
                return new ValueList(list);

            } finally {
                if (br != null) try {br.close();} catch (Exception ex) {};
            }
            
        }
    }

    

    class FunctionSetWriteLF extends Function {
        public String getName() {
            return "setWriteLF";
        }
        public String getShortDesc() {
            return "setWriteLF() - override default newline / CRLF";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            customEOL="\n";
            return new ValueObj(self());
        }
    }
    

    class FunctionSetWriteCRLF extends Function {
        public String getName() {
            return "setWriteCRLF";
        }
        public String getShortDesc() {
            return "setWriteCRLF() - override default newline / CRLF";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            customEOL="\r\n";
            return new ValueObj(self());
        }
    }
    

    class FunctionReadBinary extends Function {
        public String getName() {
            return "readBinary";
        }
        public String getShortDesc() {
            return "readBinary([offset,count]?) - returns binary value for whole file, or as specified";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() == 0) {
                File f=new File(name);
                byte[] buf=new byte[(int) f.length()];
                InputStream in=null;
                try {
                    in=new FileInputStream(f);
                    int count = in.read(buf);
                    if (count != buf.length) throw new Exception("Invalid read, got " + count + " bytes of " + buf.length); 
                } finally  {
                    if (in != null) try {in.close();} catch (Exception ex) {};
                }
                return new ValueBinary(buf);
            } else if (params.size()==2) {
                int offset=(int) getInt("offset", params, 0);
                int count=(int) getInt("count", params, 1);
                File f=new File(name);
                byte[] buf=new byte[count];
                FileInputStream fis=null;
                try {
                    RandomAccessFile raf=new RandomAccessFile(f,"r");
                    raf.seek(offset);
                    fis=new FileInputStream(raf.getFD());
                    int bytesRead = fis.read(buf);
                    if (bytesRead != buf.length) {
                        // repackage buf to shorter array
                        byte[] newBuf=new byte[bytesRead];
                        for (int i=0; i<bytesRead; i++) newBuf[i]=buf[i];
                        buf=newBuf;
                    }
                    return new ValueBinary(buf);
                } finally {
                    if (fis != null) try {fis.close();} catch (Exception ex) {};
                }
            } else {
                throw new Exception("Expected either no parameters, or two parameters (offset+count)");
            }
        }
    }
    

    class FunctionBinaryCreate extends Function {
        public String getName() {
            return "binaryCreate";
        }
        public String getShortDesc() {
            return "binaryCreate(data) - create file from binary data - returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected binary data parameter");
            ValueBinary data=getBinary("data",params,0);
            
            validateDestructiveOperation("File.binaryCreate");
            data.validateNonSecure("File.binaryCreate");

            
            OutputStream out=null;
            File f=new File(name);
            try {
                out=new FileOutputStream(f);
                out.write(data.getVal());
            } finally  {
                if (out != null) try {out.close();} catch (Exception ex) {};
            }
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
            boolean ok=f.exists() && f.isFile();
            if (!ok) throw new SoftErrorException(str + ": " + name); 

            return new ValueObj(self());
        }
    }
    
    
    private void encryptDecrypt (Ctx ctx, List<Value> params, boolean modeEncrypt) throws Exception {
        if (params.size() != 3) throw new Exception("Expected parameters passwordBinary, saltString, targetFile");
        
        final byte[] ENCRYPT_BLOCKS_MAGIC="usdfocvbi;ObjFile.encryptDecrypt.blocks.v1".getBytes("UTF-8");
        
        ValueBinary password=getBinary("passwordBinary",params,0);
        String salt=getString("saltString", params, 1);
        Obj fileObj=getObj("targetFile", params, 2);
        
        if (!(fileObj instanceof ObjFile)) throw new Exception("Expected parameters passwordBinary, saltString, targetFile");

        
        ObjFile targetFile=(ObjFile) fileObj;
        targetFile.validateDestructiveOperation("encrypt/decrypt target file");
        
        File src=getFile();
        File target=targetFile.getFile();
        
        FileInputStream in=null;
        FileOutputStream out=null;
        
        try {
            in=new FileInputStream(src);
            out=new FileOutputStream(target);
            
            if (modeEncrypt) {
                out.write(ENCRYPT_BLOCKS_MAGIC);
                encryptDecryptBlocks(ctx, password, salt, in, out, modeEncrypt);
            } else {
                // decrypt: check if file starts with magic or not
                byte[] data=new byte[ENCRYPT_BLOCKS_MAGIC.length];
                int count=in.read(data);
                boolean foundMagic=true;
                if (count!=ENCRYPT_BLOCKS_MAGIC.length) {
                    foundMagic=false;
                } else {
                    // compare bytes
                    for (int i=0; i<ENCRYPT_BLOCKS_MAGIC.length; i++) {
                        if (data[i] != ENCRYPT_BLOCKS_MAGIC[i]) {
                            foundMagic=false;
                            break;
                        }
                    }
                }
                if (foundMagic) {
                    encryptDecryptBlocks(ctx,password, salt, in, out, modeEncrypt);
                } else {
                    in.close();
                    in=new FileInputStream(src);
                    // fallback to original implementation
                    encryptDecryptSerial(ctx, password, salt, in, out, modeEncrypt);
                }
            }
        } finally {
            if (in != null) try {in.close();} catch (Exception ex) {};
            if (out != null) try {out.close();} catch (Exception ex) {};
        }
    }
    
    /**
     * New version that encrypts block independently, with different keys, enabling (future) parallel
     * implementation for speed.
     */
    private void encryptDecryptBlocks (Ctx ctx, ValueBinary password, String salt, FileInputStream in, FileOutputStream out, boolean modeEncrypt) throws Exception {
        byte[] buf = new byte[1024 * 16];
        
        for (int blockNo=0;true;blockNo++) {
            int count = in.read(buf);
            if (count <= 0) break;
            
            Encrypt enc = new Encrypt(password.getVal(), ("ObjFile:" + salt+"-blockNo:" + blockNo).getBytes("UTF-8"));

            for (int i = 0; i < count; i++) {
                buf[i] = enc.process(modeEncrypt, buf[i]);
            }
            out.write(buf, 0, count);
        }
    }
    
    /**
     * Original implementation, now only used for decrypt of files without magic marker at start
     */
    private void encryptDecryptSerial (Ctx ctx, ValueBinary password, String salt, FileInputStream in, FileOutputStream out, boolean modeEncrypt) throws Exception {
        Encrypt enc = new Encrypt(password.getVal(), ("ObjFile:" + salt).getBytes("UTF-8"));
        byte[] buf = new byte[1024 * 64];
        for (;;) {
            int count = in.read(buf);
            if (count <= 0)
                break;
            for (int i = 0; i < count; i++) {
                buf[i] = enc.process(modeEncrypt, buf[i]);
            }
            out.write(buf, 0, count);
        }

    }
    
    
    class FunctionEncrypt extends Function {
        public String getName() {
            return "encrypt";
        }
        public String getShortDesc() {
            return "encrypt(passwordBinary,saltString,targetFile) - returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            encryptDecrypt(ctx, params, true);
            return new ValueObj(self());
        }
    }
    

    class FunctionDecrypt extends Function {
        public String getName() {
            return "decrypt";
        }
        public String getShortDesc() {
            return "decrypt(passwordBinary,saltString,targetFile) - returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            encryptDecrypt(ctx, params, false);
            return new ValueObj(self());
        }
    }
    
    class FunctionGetTimes extends Function {
        public String getName() {
            return "getTimes";
        }
        public String getShortDesc() {
            return "getTimes() - return Dict with .created .modified .accessed int values";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            
            Path file = Paths.get(name);
            BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);

            ObjDict dict=new ObjDict();
            
            dict.set("created", new ValueInt(attr.creationTime().toMillis()));
            dict.set("modified", new ValueInt(attr.lastModifiedTime().toMillis()));
            dict.set("accessed", new ValueInt(attr.lastAccessTime().toMillis()));
            
            return new ValueObj(dict);
            
        }
    }
    

    class FunctionSetTimes extends Function {
        public String getName() {
            return "setTimes";
        }
        public String getShortDesc() {
            return "setTimes(timeCreated, timeModified, timeAccessed) - set file times";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 3) throw new Exception("Expected integer parameters timeCreated, timeModified, timeAccessed");
            
            long timeCreated = getInt("timeCreated",params,0);
            long timeModified = getInt("timeModified",params,1);
            long timeAccessed = getInt("timeAccessed",params,2);
            
            FileTime created=FileTime.fromMillis(timeCreated);
            FileTime modified=FileTime.fromMillis(timeModified);
            FileTime accessed=FileTime.fromMillis(timeAccessed);
            
            BasicFileAttributeView attributes = Files.getFileAttributeView(Paths.get(name), BasicFileAttributeView.class);
            attributes.setTimes(modified, accessed, created);

            return new ValueObj(self());
        }
    }
 
    
    class FunctionTouch extends Function {
        public String getName() {
            return "touch";
        }
        public String getShortDesc() {
            return "touch() - create if not found, then update times to now";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            
            validateDestructiveOperation("touch");
            
            File x=new File(name);
            if (!x.exists()) {
                boolean ok = x.createNewFile();
                if (!ok) throw new Exception("Could not create file");
            } else {
                // update time stamp for existing file
                FileTime now=FileTime.fromMillis(System.currentTimeMillis());
    
                BasicFileAttributeView attributes = Files.getFileAttributeView(Paths.get(name), BasicFileAttributeView.class);
                attributes.setTimes(now, now, now);
            }

            return new ValueObj(self());
        }
    }
 

    private String fmt (int i, int n) {
        String s=""+i;
        while (s.length()<n) s=" "+s;
        return s;
    }

}
