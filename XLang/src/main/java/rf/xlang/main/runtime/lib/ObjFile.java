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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import rf.xlang.main.Ctx;
import rf.xlang.main.runtime.Function;
import rf.xlang.main.runtime.Obj;
import rf.xlang.main.runtime.Value;
import rf.xlang.main.runtime.ValueBoolean;
import rf.xlang.main.runtime.ValueInt;
import rf.xlang.main.runtime.ValueList;
import rf.xlang.main.runtime.ValueObj;
import rf.xlang.main.runtime.ValueString;
import rf.xlang.util.Hex;
import rf.xlang.util.TabUtil;


public class ObjFile extends Obj {

    static final String DefaultEncoding = "ISO_8859_1";
    
    private String name;
    private String assignedEncoding=null;
    private String customEOL; // for overriding CRLF / LF

    public ObjFile(String name) throws Exception {
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
                new FunctionCopyFrom(),
                new FunctionCopyTo(),
                new FunctionMove(),
                new FunctionEncoding(),
                new FunctionSetWriteLF(),
                new FunctionSetWriteCRLF(),
                new FunctionTouch(),
                new FunctionFile(),
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
            return "read(deTab?) - read text file, returns list of lines (deTab defaults to true, converting TAB to spaces)";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            boolean detab=true;
            if (params.size()==1) {
                detab=getBoolean("deTab", params, 0);
            } else if (params.size() != 0) {
                throw new Exception("Expected optional parameter deTab to control TAB handling");
            }
            List<Value> result=new ArrayList<Value>();
            BufferedReader br=null;
            long lineNo=0;
            try {
                
                br = getBufferedReader();
                for (;;) {
                    String line=br.readLine();
                    lineNo++;
                    if (line==null) break;

                    if (detab) {
                        String deTabbed = TabUtil.substituteTabs(line, 4);
                        result.add(new ValueString(deTabbed));
                        // ObjFileLine is subclass of ValueString
                    } else {
                        result.add(new ValueString(line));
                    }
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
            return new ValueObj(new ObjDir(name.substring(0,pos)));
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


    class FunctionCopyFrom extends Function {
        public String getName() {
            return "copyFrom";
        }
        public String getShortDesc() {
            return "copyFrom(File) - returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected File parameter");


            Obj obj=getObj("File", params, 0);
            if (!(obj instanceof ObjFile)) throw new Exception("Expected File parameter");

            ObjFile srcFile=(ObjFile) obj;
            ObjFile targetFile = self();

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

            Obj obj=getObj("File", params, 0);
            if (!(obj instanceof ObjFile)) throw new Exception("Expected File parameter");

            ObjFile srcFile=self();
            ObjFile targetFile=(ObjFile) obj;

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

            Obj obj=getObj("toFile", params, 0);
            if (!(obj instanceof ObjFile)) throw new Exception("Expected File parameter");

            ObjFile srcFile=self();
            ObjFile targetFile=(ObjFile) obj;
            
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
    



    
    class FunctionTouch extends Function {
        public String getName() {
            return "touch";
        }
        public String getShortDesc() {
            return "touch() - create if not found, then update times to now";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            
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


    class FunctionFile extends Function {
        // To be compatible with  the .file function of the Row object after searching, so when
        // result is list of files instead of list of Row, and we type :N.file, it works as expected
        public String getName() {
            return "file";
        }
        public String getShortDesc() {
            return "file() - returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            return new ValueObj(self());
        }
    }



    private String fmt (int i, int n) {
        String s=""+i;
        while (s.length()<n) s=" "+s;
        return s;
    }

}
