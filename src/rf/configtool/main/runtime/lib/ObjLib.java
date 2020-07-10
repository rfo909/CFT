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
import rf.configtool.main.PropsFile;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueFloat;
import rf.configtool.main.runtime.ValueInt;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.lib.conversions.ObjConvert;

import java.awt.Color;

public class ObjLib extends Obj {
    
    public ObjLib() {
        this.add(new FunctionPlot());
        this.add(new FunctionData());
        this.add(new FunctionMath());
        this.add(new FunctionConvert());
    }
    
    @Override
    public boolean eq(Obj x) {
        return x==this;
    }


    public String getTypeName() {
        return "Lib";
    }
    

    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    
    private String getDesc() {
        return "Lib";
    }
    
    private Obj theObj () {
        return this;
    }
    
    class FunctionPlot extends Function {
        public String getName() {
            return "Plot";
        }
        public String getShortDesc() {
            return "Plot() - create Plot object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueObj(new ObjPlot());
        }
    }
    


    class FunctionData extends Function {
        public String getName() {
            return "Data";
        }
        public String getShortDesc() {
            return "Data() - create Data object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueObj(new ObjData());
        }
    }
    

    class FunctionMath extends Function {
        public String getName() {
            return "Math";
        }
        public String getShortDesc() {
            return "Math() - create Math object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueObj(new ObjMath());
        }
    } 

    class FunctionConvert extends Function {
        public String getName() {
            return "Convert";
        }
        public String getShortDesc() {
            return "Convert() - create Convert object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueObj(new ObjConvert());
        }
    } 

    class FunctionCodeDirs extends Function {
        public String getName() {
            return "codeDirs";
        }
        public String getShortDesc() {
            return "codeDirs() - returns list of code dirs (see " + PropsFile.PROPS_FILE + ")";
        }
        @Override
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            List<Value> list=new ArrayList<Value>();
            for (String s:ctx.getObjGlobal().getPropsFile().getCodeDirs()) {
            	ObjDir dir=new ObjDir(s, Protection.NoProtection);
            	list.add(new ValueObj(dir));
            }
            return new ValueList(list);
        }
    }
    


}
