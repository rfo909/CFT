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

package rf.configtool.main.runtime.lib;

import java.io.*;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.Ctx;
import rf.configtool.main.OutText;
import rf.configtool.main.runtime.*;
import rf.configtool.util.FileInfo;
import rf.configtool.util.TabUtil;

import java.nio.charset.Charset;
import java.nio.file.Path;

public class ObjFile extends Obj {

	static final String DefaultEncoding = "ISO_8859_1";
	
    private String name;
    private String encoding=DefaultEncoding;

    public ObjFile(String name) {
        this.name=name;

        try {
            File f=new File(name);
            if (f.exists()) {
                this.name=f.getCanonicalPath();
            }
        } catch (Exception ex) {
            this.name=name;
        }
        
        add(new FunctionExists());
        add(new FunctionName());
        add(new FunctionPath());
        add(new FunctionDir());
        add(new FunctionLength());
        add(new FunctionDelete());
        add(new FunctionCreate());
        add(new FunctionAppend());
        add(new FunctionRead());
        add(new FunctionLastModified());
        add(new FunctionHash());
        add(new FunctionMore());
        add(new FunctionUncompress());
        add(new FunctionCopyFrom());
        add(new FunctionCopyTo());
        add(new FunctionMove());
        add(new FunctionHex());
        add(new FunctionReadBytes());
        add(new FunctionEncoding());
        add(new FunctionSetEncoding());
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
    public String synthesize() throws Exception {
    	String enc="";
    	if (!encoding.equals(DefaultEncoding)) {
    		// setEncoding returns self!
    		enc=".setEncoding(" + (new ValueString(encoding)).synthesize() + ")";
    	}
        return "File(" + (new ValueString(name)).synthesize() + ")" + enc;
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
        long secondsSinceModify=(System.currentTimeMillis()-f.lastModified())/1000L;
        return ColList.list().regular(fix(name)).status(fmtSize(f.length())).status(""+f.length()).status(fmtDuration(secondsSinceModify)).status(fmtDate(f.lastModified()));
    }
    
    private String fix (String name) {
        int pos=name.lastIndexOf(File.separator);
        if (pos > 0) name=name.substring(pos+1);
        return name;
    }
    
    private String fmtDuration (long numSeconds) {
        long x=numSeconds;
        long seconds=x % 60;   x/=60;   // x is now minutes
        long minutes=x % 60;   x/=60;   // x is now hours
        long hours=x%24;       x/=24;   // x is now days
        long days=x;
        
        if (days >= 1) {
            return "d:"+days;
        }
        return "d:<1";
    }
    
    private String fmt (long x, int digits) {
        String s="" + x;
        while(s.length()<digits) s="0"+s;
        return s;
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
            OutText outText=ctx.getOutText();
            File f=new File(name);
            if (f.exists()) {
                if (f.isFile()) {
                    boolean ok=f.delete();
                    if (!ok) {
                        outText.addSystemMessage("Delete failed : " + f.getCanonicalPath());
                    }
                    return new ValueBoolean(ok);
                } else {
                    outText.addSystemMessage("Not a file    : " + f.getCanonicalPath());
                    return new ValueBoolean(false);
                }
            } 
            outText.addSystemMessage("No such file  : " + f.getCanonicalPath());
            return new ValueBoolean(false);
        }
    }



    class FunctionCreate extends Function {
        public String getName() {
            return "create";
        }
        public String getShortDesc() {
            return "create(expr) - create file if it doesn't exist, with content as given";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected one parameter any type (file data)");
            OutText outText=ctx.getOutText();
            File f=new File(name);

            PrintStream ps=null;
            try {
                ps=new PrintStream(new FileOutputStream(f),false, encoding);
                Value content=params.get(0);
                if (content instanceof ValueList) {
                    List<Value> lines=((ValueList)content).getVal();
                    for (Value line:lines) {
                        // process as lines
                        ps.println(line.getValAsString());
                    }
                } else {
                    ps.println(content.getValAsString());
                }
            } finally {
                if (ps != null) try {ps.close();} catch (Exception ex) {};
            }
            return new ValueBoolean(true);
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
//          if (!f.exists()) {
//              throw new Exception("File does not exist");
//          }
            PrintStream ps=null;
            try {
                ps=new PrintStream(new FileOutputStream(f,true), false, encoding);
                Value content=params.get(0);
                if (content instanceof ValueList) {
                    List<Value> lines=((ValueList)content).getVal();
                    for (Value line:lines) {
                        // process as lines
                        ps.println(line.getValAsString());
                    }
                } else {
                    ps.println(content.getValAsString());
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
            File f=new File(name);
            if (!f.exists()) {
                throw new Exception("File does not exist");
            }
            List<Value> result=new ArrayList<Value>();
            BufferedReader br=null;
            long lineNo=0;
            try {
                //br=new BufferedReader(new FileReader(f));
            	br = new BufferedReader(
         			   new InputStreamReader(
         	                      new FileInputStream(f), encoding));

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

    class FunctionMore extends Function {
        public String getName() {
            return "more";
        }
        public String getShortDesc() {
            return "more() - page through a file";
        }


        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            final int lines=ctx.getObjGlobal().getObjCfg().getScreenHeight()-2; // room for info line + input line
            final int width=ctx.getObjGlobal().getObjCfg().getScreenWidth()-2; // a little space to the right

            File f=new File(name);
            if (!f.exists() || !f.isFile()) throw new Exception("Invalid file");
            
            BufferedReader br=null;
            try {
                //br=new BufferedReader(new InputStreamReader(new FileInputStream(f)));
            	br = new BufferedReader(
          			   new InputStreamReader(
          	                      new FileInputStream(f), encoding));

            	int lineNo=0;
                int linesDisplayed=0;
                for (;;) {
                    String line=br.readLine();
                    if (line==null) {
                        ctx.outln("[" + f.getName() + " | EOF]");
                        break;
                    }
                    
                    line=TabUtil.substituteTabs(line,4);
                    
                    lineNo++;
                    String pre=fmt(lineNo,3) + " | ";
                    line=pre+line;
                    if (line.length()>width-1) {
                        line=line.substring(0,width-1) + "+";
                    }
                    ctx.outln(line);
                    linesDisplayed++;
                    
                    
                    
                    if (linesDisplayed >= lines) {
                        ctx.outln("[" + f.getName() + " | line:" + lineNo + "] ENTER to continue, 'q' to quit");
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
            return new ValueObj(new ObjFile(targetPath));
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

            File src=new File(srcFile.getPath());
            File target=new File(self().getPath());

            if (!src.isFile()) throw new Exception("Source '" + src.getCanonicalPath() + "' is not a file");
            if (target.exists()) {
                if (!target.isFile()) throw new Exception("Target '" + target.getCanonicalPath() + "' exists, but is not a file");
                outText.addSystemMessage("Overwriting file: " + target.getCanonicalPath());
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

            ObjFile srcFile=(ObjFile) obj;

            File src=new File(self().getPath());
            File target=new File(srcFile.getPath());

            if (!src.isFile()) throw new Exception("Source '" + src.getCanonicalPath() + "' is not a file");
            if (target.exists()) {
                if (!target.isFile()) throw new Exception("Target '" + target.getCanonicalPath() + "' exists, but is not a file");
                outText.addSystemMessage("Overwriting file: " + target.getCanonicalPath());
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

            ObjFile srcFile=(ObjFile) obj;

            File src=new File(self().getPath());
            File target=new File(srcFile.getPath());

            if (!src.isFile()) throw new Exception("Source '" + src.getCanonicalPath() + "' is not a file");
            if (target.exists()) {
                if (!target.isFile()) throw new Exception("Target '" + target.getCanonicalPath() + "' exists, but is not a file");
                outText.addSystemMessage("Overwriting file: " + target.getCanonicalPath());
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
            return "hex - page through file content in hex";
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
            
            final int lines=ctx.getObjGlobal().getObjCfg().getScreenHeight()-2; // room for info line + input line
            
            
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
                        ctx.outln(createLine(lineNumber++, sb1,sb2));
                        sb1=new StringBuffer();
                        sb2=new StringBuffer();
                        countInLine=0;

                        linesDisplayed++;
                        if (linesDisplayed >= lines) {
                            ctx.outln("[" + f.getName() + "] ENTER to continue, 'q' to quit");
                            String s=ctx.getStdio().getInputLine();
                            if (s.trim().equals("q")) break;
                            linesDisplayed=0;
                        }
                    }
                }
                if (countInLine>0) {
                    ctx.outln(createLine(lineNumber,sb1,sb2));
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
            return "encoding() - get encoding (string)";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueString(encoding);
        }
    }

    class FunctionSetEncoding extends Function {
        public String getName() {
            return "setEncoding";
        }
        public String getShortDesc() {
            return "setEncoding(encoding) - set encoding, returns self)";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected encoding parameter (String)");
            encoding=getString("encoding", params, 0);
            if (!Charset.isSupported(encoding)) throw new Exception("Charset '" + encoding + "' not supported");
            return new ValueObj(self());
        }
    }

 
    private String fmt (int i, int n) {
        String s=""+i;
        while (s.length()<n) s=" "+s;
        return s;
    }

}
