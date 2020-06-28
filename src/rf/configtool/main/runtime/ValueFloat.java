package rf.configtool.main.runtime;

import java.util.List;

import rf.configtool.main.Ctx;

public class ValueFloat extends Value {
    
    private double val;
    
    public ValueFloat (double val) {
        this.val=val;
        add(new FunctionRound());
        add(new FunctionF());
        add(new FunctionI());
        add(new FunctionFloor());
        add(new FunctionLog());
        add(new FunctionLog10());
        add(new FunctionAbs());
    }
    
    protected ValueFloat theObj() {
        return this;
    }
    
    public double getVal() {
        return val;
    }
    
    @Override
    public String getTypeName() {
        return "float";
    }

    @Override
    public String getValAsString() {
        return new java.math.BigDecimal(val).toPlainString();
    }
    
    @Override 
    public String synthesize() throws Exception {
        return getValAsString();
    }
    

    
    @Override
    public boolean eq(Obj v) {
        if (v==this) return true; // same instance
        if (v instanceof ValueFloat) {
            return (((ValueFloat) v).getVal()==val);
        }
        if (v instanceof ValueInt) {
            return (((ValueInt) v).getVal()==val);
        }
        return false; // otherwise, two floats are never quite equal
    }

    @Override
    public boolean getValAsBoolean() {
        return true;
    }

    class FunctionRound extends Function {
        public String getName() {
            return "round";
        }
        public String getShortDesc() {
            return "round() - returns rounded int value";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size()!=0) throw new Exception("Expected no parameters");
            return new ValueInt((long) Math.round(val));
        }

    }
    
    class FunctionF extends Function {
        public String getName() {
            return "f";
        }
        public String getShortDesc() {
            return "f() - returns value as float (unchanged)";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            return theObj();
        }

    }

    class FunctionI extends Function {
        public String getName() {
            return "i";
        }
        public String getShortDesc() {
            return "i() - returns rounded int value";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size()!=0) throw new Exception("Expected no parameters");
            return new ValueInt((long) Math.round(val));
        }

    }
    
    class FunctionFloor extends Function {
        public String getName() {
            return "floor";
        }
        public String getShortDesc() {
            return "floor() - returns floored int value";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size()!=0) throw new Exception("Expected no parameters");
            return new ValueInt((long) Math.floor(val));
        }

    }
    
    class FunctionLog extends Function {
        public String getName() {
            return "log";
        }
        public String getShortDesc() {
            return "log()";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size()!=0) throw new Exception("Expected no parameters");
            return new ValueFloat(Math.log(val));
        }

    }
    
    class FunctionLog10 extends Function {
        public String getName() {
            return "log10";
        }
        public String getShortDesc() {
            return "log10()";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size()!=0) throw new Exception("Expected no parameters");
            return new ValueFloat(Math.log10(val));
        }

    }
    
    class FunctionAbs extends Function {
        public String getName() {
            return "abs";
        }
        public String getShortDesc() {
            return "abs() - returns abs value";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size()!=0) throw new Exception("Expected no parameters");
            return new ValueFloat(Math.abs(val));
        }

    }
    


}
