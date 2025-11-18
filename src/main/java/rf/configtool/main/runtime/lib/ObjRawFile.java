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
import rf.configtool.main.SoftErrorException;
import rf.configtool.main.Stdio;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.IsSynthesizable;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBinary;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueNull;
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


public class ObjRawFile extends Obj implements IsSynthesizable {

    private String name;

    public ObjRawFile(String name) throws Exception {
        this.name=name;
        this.add(new FunctionWrite());
    }   

    
    
    @Override
    public boolean eq(Obj x) {
        if (x instanceof ObjRawFile) {
            ObjRawFile f=(ObjRawFile) x;
            return f.name.equals(name); // always canonical when possible
        }
        return false;
    }

    @Override
    public String createCode() throws Exception {
        return "RawDevice(" + (new ValueString(name)).synthesize() + ")";
    }


    @Override
    public String getTypeName() {
        return "RawFile";
    }

    
    public ColList getContentDescription() {
        return ColList.list().regular(name);
    }
    

    class FunctionWrite extends Function {
        public String getName() {
            return "write";
        }
        public String getShortDesc() {
            return "write(byte) - write byte";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            int val=(int) getInt("byte", params, 0);
            File f=new File(name);
            FileOutputStream fos=new FileOutputStream(f);
            fos.write(val);
            fos.close();
            return new ValueNull();
        }
    }

}
