package rf.configtool.main.runtime;

import java.io.File;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.OutText;

public class ValueBoolean extends Value {
    
    private boolean val;
    
    public ValueBoolean (boolean val) {
        this.val=val;
        add (new FunctionNot());
    }
    
    public boolean getVal() {
        return val;
    }
    
    @Override
    public String getTypeName() {
        return "boolean";
    }

    @Override
    public String getValAsString() {
        return ""+val;
    }
    
    @Override
    public String synthesize() throws Exception {
        return val ? "true" : "false";
    }

    
    @Override
    public boolean eq(Obj v) {
        if (v==this) return true;
        return (v instanceof ValueBoolean) && ((ValueBoolean) v).getVal()==val;
    }
    
    @Override
    public boolean getValAsBoolean() {
        return val;
    }


    
    class FunctionNot extends Function {
        public String getName() {
            return "not";
        }
        public String getShortDesc() {
            return "not() - Inverts boolean value";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueBoolean(!val);
        }

    }
    
    
}
