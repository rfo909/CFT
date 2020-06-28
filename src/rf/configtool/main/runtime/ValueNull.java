package rf.configtool.main.runtime;

public class ValueNull extends Value {

    @Override
    public String getTypeName() {
        return "<null-type>";
    }


    @Override
    public String getValAsString() {
        return "null";
    }
    
    @Override
    public String synthesize() {
        return "null";
    }
    
    @Override
    public boolean eq(Obj v) {
        return (v instanceof ValueNull);
    }

    @Override
    public boolean getValAsBoolean() {
        return false;
    }



}
