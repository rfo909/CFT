package rf.xlang.main.runtime.lib.app;

public interface Value {

    public boolean isNull();
    public boolean isList();
    public boolean isObjRef();
    
    public boolean isString();
    public boolean isInteger();
    public boolean isLong();
    public boolean isBoolean();
    public boolean isFloat();
    public boolean isDouble();
    
}
