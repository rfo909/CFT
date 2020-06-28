package rf.configtool.main.runtime;

public class ValueObj extends Value {
    
    private Obj val;
    
    public ValueObj (Obj val) {
        this.val=val;
    }
    
    public Obj getVal() {
        return val;
    }
    
    @Override
    public String getTypeName() {
        return "<obj>";
    }

    @Override
    public String getValAsString() {
        return val.getDescription();
    }
    
    @Override
    public String synthesize() throws Exception {
        return val.synthesize();
    }

    @Override
    public boolean getValAsBoolean() {
        return true;
    }


    @Override
    public boolean eq(Obj v) {
        if (!(v instanceof ValueObj)) return false;
        ValueObj obj=(ValueObj) v;
        if (obj.getVal()==val) return true;
        return obj.getVal().eq(val);
    }


}
