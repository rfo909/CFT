package rf.configtool.main.runtime.lib;

import java.io.*;
import java.util.*;

import rf.configtool.main.Ctx;
import rf.configtool.main.OutText;
import rf.configtool.main.Version;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueFloat;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import java.awt.Color;

public class ObjSys extends Obj {
    
    public ObjSys() {
        this.add(new FunctionVersion());
        this.add(new FunctionFunctions());
    }
    
    @Override
    public boolean eq(Obj x) {
        return x==this;
    }


    public String getTypeName() {
        return "Sys";
    }
    

    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    
    private String getDesc() {
        return "Sys";
    }
    
    private Obj theObj () {
        return this;
    }
    

    class FunctionVersion extends Function {
        public String getName() {
            return "version";
        }
        public String getShortDesc() {
            return "version() - returns CFT version string";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueString(new Version().getVersion());
        }
    }
    
    class FunctionFunctions extends Function {
        public String getName() {
            return "functions";
        }
        public String getShortDesc() {
            return "functions() - returns list of custom functions";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            List<Value> values=new ArrayList<Value>();
            List<String> names = ctx.getObjGlobal().getCodeHistory().getNames();
            for (String name:names) values.add(new ValueString(name));
            return new ValueList(values);
        }
    }
    
    
}
