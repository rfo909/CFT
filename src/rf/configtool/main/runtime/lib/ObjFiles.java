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
import java.util.*;

import rf.configtool.main.Ctx;
import rf.configtool.main.OutText;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueFloat;
import rf.configtool.main.runtime.ValueInt;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueMacro;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import java.awt.Color;

public class ObjFiles extends Obj {
    
    public ObjFiles() {
        this.add(new FunctionDateSort());
        this.add(new FunctionLineReader());
        this.add(new FunctionFilterReader());
    }
    
    @Override
    public boolean eq(Obj x) {
        return x==this;
    }


    @Override
    public String getTypeName() {
        return "Files";
    }
    

    @Override
    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    
    private String getDesc() {
        return "Files";
    }
    
    private Obj self () {
        return this;
    }
    
     
    
    class FunctionDateSort extends Function {
        public String getName() {
            return "DateSort";
        }
        public String getShortDesc() {
            return "DateSort() - DateSort object, for sorting lines starting with date/time";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() == 0) {
                ObjDateSort x=new ObjDateSort();
                return new ValueObj(x);
            } 

            throw new Exception("Expected no parameters");
        }
    }
    

    class FunctionLineReader extends Function {
        public String getName() {
            return "LineReader";
        }
        public String getShortDesc() {
            return "LineReader(file) - create LineReader for file - automatically closed at end of current context";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() == 1) {
            	Obj obj=getObj("file",params,0);
            	if (!(obj instanceof ObjFile)) throw new Exception("Expected file parameter");
            	ObjFile f=(ObjFile) obj;
            	if (!f.getFile().exists() || !f.getFile().isFile()) {
            		throw new Exception("File " + f.getPath() + " not found");
            	}
            	
                ObjLineReader x=new ObjLineReader(f);
                return ctx.getObjGlobal().getOrAddPersistentObject(x);
            } 

            throw new Exception("Expected file parameter");
        }
    }
    
    class FunctionFilterReader extends Function {
        public String getName() {
            return "FilterReader";
        }
        public String getShortDesc() {
            return "FilterReader(LineReader,Grep) - create FilterReader for file - automatically closed at end of current context";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() == 2) {
            	Obj obj=getObj("LineReader",params,0);
            	if (!(obj instanceof ObjLineReader)) throw new Exception("Expected LineReader, Grep parameters");
            	ObjLineReader reader=(ObjLineReader) obj;
            	
            	obj=getObj("Grep",params,1);
            	if (!(obj instanceof ObjGrep)) throw new Exception("Expected LineReader, Grep parameters");
            	ObjGrep grep=(ObjGrep) obj;
            	
            	ObjFilterReader x=new ObjFilterReader(reader, grep);
            	return new ValueObj(x);
            } 

            throw new Exception("Expected LineReader, Grep parameters");
        }
    }
    

}


