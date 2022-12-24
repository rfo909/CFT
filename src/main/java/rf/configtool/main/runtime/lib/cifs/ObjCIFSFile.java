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

package rf.configtool.main.runtime.lib.cifs;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import jcifs.CIFSContext;
import jcifs.CloseableIterator;
import jcifs.SmbResource;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;
import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.*;
import rf.configtool.main.runtime.lib.ObjFile;
import rf.configtool.util.TabUtil;

public class ObjCIFSFile extends Obj {
    
    private String url;
    private CIFSContext context;
    private SmbFile smbFile;

    public ObjCIFSFile (String url, CIFSContext context) throws Exception {
        this.url=url;
        this.context=context;
        
        this.smbFile=new SmbFile(url, context);

        this.add(new FunctionLength());
        this.add(new FunctionIsFile());
        this.add(new FunctionIsDir());
        this.add(new FunctionExists());
        this.add(new FunctionList());
        this.add(new FunctionCopyFrom());
        this.add(new FunctionCopyTo());

    }
    
    private ObjCIFSFile self() {
        return this;
    }
    
    @Override
    public boolean eq(Obj x) {
        return false;
    }

    
    @Override
    public String getTypeName() {
        return "CIFSFile";
    }
    
    @Override
    public ColList getContentDescription() {
        return ColList.list().regular("CIFSFile");
    }
  
    class FunctionLength extends Function {
        public String getName() {
            return "length";
        }
        public String getShortDesc() {
            return "length() - return length of file as int";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueInt(smbFile.length());
        }
    }

    class FunctionIsFile extends Function {
        public String getName() {
            return "isFile";
        }
        public String getShortDesc() {
            return "isFile() - boolean";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueBoolean(smbFile.isFile());
        }
    }
    
    class FunctionIsDir extends Function {
        public String getName() {
            return "isDir";
        }
        public String getShortDesc() {
            return "isDir() - boolean";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueBoolean(smbFile.isDirectory());
        }
    }
    
    class FunctionExists extends Function {
        public String getName() {
            return "exists";
        }
        public String getShortDesc() {
            return "exists() - boolean";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueBoolean(smbFile.exists());
        }
    }

    class FunctionList extends Function {
        public String getName() {
            return "list";
        }
        public String getShortDesc() {
            return "list() - returns list of strings for content in directory";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            if (!smbFile.isDirectory()) throw new Exception("Not a directory");

            
            List<Value> result=new ArrayList<Value>();
            SmbFile theFile=smbFile;
            if (!url.endsWith("/")) {
                // querying a directory, requires the path to end with "/" as the
                // returned strings are "the last part of the url", and without 
                // the "/" we get XXXa XXXb and so on, where XXX is current dir, and
                // a,b is content inside.
                theFile=new SmbFile(url+"/", context);
            }
            for (String s:theFile.list()) {
                if (s.endsWith("/")) {
                    // directories end with '/' - stripping it
                    s=s.substring(0,s.length()-1);
                }
                result.add(new ValueString(s));
            }
            return new ValueList(result);
        }
    }

    
    class FunctionCopyFrom extends Function {
        public String getName() {
            return "copyFrom";
        }
        public String getShortDesc() {
            return "copyFrom(srcFile) - returns number of bytes copied";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected srcFile parameter");
            Obj obj=getObj("srcFile",params,0);
            if (obj instanceof ObjFile) {
                File f=((ObjFile) obj).getFile();
                    InputStream in=null;
                    OutputStream out=null;
                try {
                    in=new FileInputStream(f);
                    out=new SmbFileOutputStream(smbFile);
                    long count=copy(in,out);
                    return new ValueInt(count);
                } finally {
                    if (in != null) try {in.close();} catch (Exception ex) {};
                    if (out != null) try {out.close();} catch (Exception ex) {};
                }
            } else {
                throw new Exception("Expected srcFile parameter");
            }
        }
    }

    
    class FunctionCopyTo extends Function {
        public String getName() {
            return "copyTo";
        }
        public String getShortDesc() {
            return "copyTo(targetFile) - returns number of bytes copied";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected targetFile parameter");
            Obj obj=getObj("targetFile",params,0);
            if (obj instanceof ObjFile) {
                ObjFile file=(ObjFile) obj;
                file.validateDestructiveOperation("copyTo");
                File f=file.getFile();
                    InputStream in=null;
                    OutputStream out=null;
                try {
                    in=new SmbFileInputStream(smbFile);
                    out=new FileOutputStream(f);
                    long count=copy(in,out);
                    return new ValueInt(count);
                } finally {
                    if (in != null) try {in.close();} catch (Exception ex) {};
                    if (out != null) try {out.close();} catch (Exception ex) {};
                }
            } else {
                throw new Exception("Expected targetFile parameter");
            }
        }
    }

    private long copy (InputStream in, OutputStream out) throws Exception {
        byte[] buf=new byte[64*1024];
        long count=0L;
        for (;;) {
            int i = in.read(buf);
            if (i<=0) break;
            out.write(buf,0,i);
            count +=i;
        }
        return count;
    }
    
}
