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

import java.util.ArrayList;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.PropsFile;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.lib.conversions.ObjConvert;
import rf.configtool.main.runtime.lib.db.ObjDb;
import rf.configtool.main.runtime.lib.ddd.DDD;
import rf.configtool.main.runtime.lib.integrations.ObjIntegrations;
import rf.configtool.main.runtime.lib.java.ObjJava;
import rf.configtool.main.runtime.lib.math.ObjMath;
import rf.configtool.main.runtime.lib.text.ObjText;
import rf.configtool.main.runtime.lib.web.ObjWeb;

public class ObjLib extends Obj {
    
    public ObjLib() {
        this.add(new FunctionPlot());
        this.add(new FunctionData());
        this.add(new FunctionMath());
        this.add(new FunctionConvert());
        this.add(new FunctionFiles());
        this.add(new FunctionText());
        this.add(new FunctionDb());
        this.add(new FunctionUtil());
        this.add(new FunctionIntegrations());
        this.add(new FunctionJava());
        this.add(new FunctionWeb());
        this.add(new FunctionDDD());
        this.add(new FunctionColor());
        
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
    

    private static final ValueObj staticData = new ValueObj(new ObjData());

    class FunctionData extends Function {
        public String getName() {
            return "Data";
        }
        public String getShortDesc() {
            return "Data() - create Data object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return staticData;
            //return new ValueObj(new ObjData());
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
            for (String s:ctx.getObjGlobal().getRoot().getPropsFile().getCodeDirs()) {
                ObjDir dir=new ObjDir(s, Protection.NoProtection);
                list.add(new ValueObj(dir));
            }
            return new ValueList(list);
        }
    }
    

    class FunctionFiles extends Function {
        public String getName() {
            return "Files";
        }
        public String getShortDesc() {
            return "Files() - create files analysis object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueObj(new ObjFiles());
        }
    } 


   

    class FunctionText extends Function {
        public String getName() {
            return "Text";
        }
        public String getShortDesc() {
            return "Text() - create Text object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueObj(new ObjText());
        }
    } 
    
    class FunctionDb extends Function {
        public String getName() {
            return "Db";
        }
        public String getShortDesc() {
            return "Db() - create Db object for database related functions";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueObj(new ObjDb());
        }
    } 
    
   
    class FunctionUtil extends Function {
        public String getName() {
            return "Util";
        }
        public String getShortDesc() {
            return "Util() - create Util object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueObj(new ObjUtil());
        }
    } 
    
    class FunctionIntegrations extends Function {
        public String getName() {
            return "Integrations";
        }
        public String getShortDesc() {
            return "Integrations() - create Integrations object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueObj(new ObjIntegrations());
        }
    } 
    
   
    
    class FunctionJava extends Function {
        public String getName() {
            return "Java";
        }
        public String getShortDesc() {
            return "Java() - object for interacting with Java classes and objects";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueObj(new ObjJava());
        }
    } 
    
   
    class FunctionWeb extends Function {
        public String getName() {
            return "Web";
        }
        public String getShortDesc() {
            return "Web() - object for setting up Web Server";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueObj(new ObjWeb());
        }
    } 
    
   
    class FunctionDDD extends Function {
        public String getName() {
            return "DDD";
        }
        public String getShortDesc() {
            return "DDD() - object for generating 3D model as image";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueObj(new DDD());
        }
    } 
    
    class FunctionColor extends Function {
        public String getName() {
            return "Color";
        }

        public String getShortDesc() {
            return "Color(r,g,b) - create Color object";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 3) throw new RuntimeException("Expected rgb parameters (ints)");
        	int r=(int) getInt("r", params, 0);
        	int g=(int) getInt("g", params, 1);
        	int b=(int) getInt("b", params, 2);
        	
        	return new ValueObj(new ObjColor(r,g,b));
        }
    }

}
