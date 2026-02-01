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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

import rf.configtool.main.Ctx;

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
import java.io.RandomAccessFile;


public class ObjRawFile extends Obj implements IsSynthesizable {

    private String name;
    private RandomAccessFile raf;

    public ObjRawFile(String name) throws Exception {
        this.name=name;
        this.raf=new RandomAccessFile(new File(name), "rw");
        this.add(new FunctionWrite());
        this.add(new FunctionRead());
        this.add(new FunctionClose());
        this.add(new FunctionName());
    }   

    
    
    @Override
    public boolean eq(Obj x) {
        if (x instanceof ObjRawFile) {
            ObjRawFile f=(ObjRawFile) x;
            return f.name.equals(name);
        }
        return false;
    }

    @Override
    public String createCode() throws Exception {
        return "RawFile(" + (new ValueString(name)).synthesize() + ")";
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
            raf.write(val); 
            return new ValueNull();       
        }
    }

    class FunctionRead extends Function {
        public String getName() {
            return "read";
        }
        public String getShortDesc() {
            return "read() - read byte, may block";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            return new ValueInt(raf.read());
        }
    }

    class FunctionClose extends Function {
        public String getName() {
            return "close";
        }
        public String getShortDesc() {
            return "close() - close raw file";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            raf.close();
            return new ValueNull();
        }
    }

    class FunctionName extends Function {
        public String getName() {
            return "name";
        }
        public String getShortDesc() {
            return "name() - return name";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            return new ValueString(name);
        }
    }   
}
