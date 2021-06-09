package rf.configtool.main.runtime.lib;

import java.io.*;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.CtxCloseHook;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueBlock;
import rf.configtool.main.runtime.ValueNull;
import rf.configtool.main.runtime.ValueObj;

/**
 * A simple reader that outputs filtered lines only
 */
public class ObjFilterReader extends Obj {
    
    private ObjLineReader lineReader;
    private ObjGrep grep;
    
    public ObjFilterReader(ObjLineReader lineReader, ObjGrep grep) {
        this.lineReader=lineReader;
        this.grep=grep;
        
        add(new FunctionRead());
    }

    
    public Obj self() {
        return this;
    }
    
        
    @Override
    public boolean eq(Obj x) {
        return x==this;
    }


    @Override
    public String getTypeName() {
        return "FilterReader";
    }
    

    @Override
    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    
    private String getDesc() {
        return "FilterReader";
    }
    
    
 
    
    class FunctionRead extends Function {
        public String getName() {
            return "read";
        }
        public String getShortDesc() {
            return "read() - get next filtered line or null if end of data";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return lineReader.readLine(grep);
        }
    }
 
    
    
    
}
