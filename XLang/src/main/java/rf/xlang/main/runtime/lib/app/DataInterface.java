package rf.xlang.main.runtime.lib.app;

public interface DataInterface {

    // single value fields
    public Value getValue (ObjRef obj, String name);
    public void setValue (ObjRef obj, String name, Value value);

    // list/array position
    public Value getValue (ObjRef obj, String name, int index);
    public void setValue (ObjRef obj, String name, int index, Value value);
    
    // list/array other operations
    public void listClear (ObjRef obj, String name);
    public void listAppend (ObjRef obj, String name, Value value);
    
    
}
