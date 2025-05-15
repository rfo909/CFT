
package rf.xlang.main.runtime;

import java.util.HashMap;
import java.util.List;

/**
 * Superclass of all object types, including Value hierarchy
 */
public abstract class Obj {
    
    private Function[] functionArr=null;
    private HashMap<String,Function> functions=new HashMap<String,Function>();
    
    /**
     * Letting ValueString and ValueInt call this instead of adding member functions
     * via add() saves us up to 25% run time, since Obj.add() consumed 65% of the time,
     * according to profiling, and strings and ints are often just compared, which
     * are expressions, not involving member functions. 
     * 
     * After optimizing getFunction(), we've started using setFunctions() instead of add() for
     * many Value and Obj classes.
     * 
     * 2021-05-08 RFO v2.5.4
     */
    protected void setFunctions (Function[] functionArr) {
        this.functionArr=functionArr;
    }
    
    protected void add (Function function) {
        String name=function.getName();
        // Uncommenting the single following line saves up to 10% of run time ...
        //if (functions.containsKey(name)) throw new RuntimeException("Duplicate function " + name);
        this.functions.put(name, function);
    }
    
    /**
     * Dict needs to clear and re-add functions when content changes, to enable
     * field values as functions, for direct dotted lookup
     */
    protected void clearFunctions() {
        functionArr=null;
        functions=new HashMap<String,Function>();
    }
    
    
    protected boolean argCount (List<Value> args, int count) throws Exception {
        return (args != null && args.size()==count);
    }
    
    protected boolean isNull (List<Value> args, int pos) throws Exception {
        return (args.get(pos) instanceof ValueNull);
    }
    
    protected boolean isString (List<Value> args, int pos) throws Exception {
        return (args.get(pos) instanceof ValueString);
    }
    
    protected boolean isInt (List<Value> args, int pos) throws Exception {
        return (args.get(pos) instanceof ValueInt);
    }
    
    protected boolean isBoolean (List<Value> args, int pos) throws Exception {
        return (args.get(pos) instanceof ValueBoolean);
    }
    
    protected boolean isList (List<Value> args, int pos) throws Exception {
        return (args.get(pos) instanceof ValueList);
    }
    
    protected boolean isObj (List<Value> args, int pos) throws Exception {
        return (args.get(pos) instanceof ValueObj);
    }
    
    protected String getString(String name, List<Value> args, int pos) throws Exception {
        Value v=args.get(pos);
        if (!(v instanceof ValueString)) throw new Exception(name + ": type error, expected String, got " + v.getTypeName());
        return ((ValueString) v).getVal();
    }
    
    protected long getInt(String name, List<Value> args, int pos) throws Exception {
        Value v=args.get(pos);
        if (!(v instanceof ValueInt)) throw new Exception(name + ": type error, expected int, got " + v.getTypeName());
        return ((ValueInt) v).getVal();
    }
    
    protected double getFloat(String name, List<Value> args, int pos) throws Exception {
        Value v=args.get(pos);
        if (v instanceof ValueInt) return ((ValueInt) v).getVal();
        if (v instanceof ValueFloat) return ((ValueFloat) v).getVal(); 
        throw new Exception(name + ": type error, expected int or float, got " + v.getTypeName());
    }
    
    protected boolean getBoolean(String name, List<Value> args, int pos) throws Exception {
        Value v=args.get(pos);
        if (!(v instanceof ValueBoolean)) throw new Exception(name + ": type error, expected boolean, got " + v.getTypeName());
        return ((ValueBoolean) v).getVal();
    }
    
    protected List<Value> getList(String name, List<Value> args, int pos) throws Exception {
        Value v=args.get(pos);
        if (!(v instanceof ValueList)) throw new Exception(name + ": type error, expected list, got " + v.getTypeName());
        return ((ValueList) v).getVal();
    }

    protected Obj getObj(String name, List<Value> args, int pos) throws Exception {
        Value v=args.get(pos);
        if (!(v instanceof ValueObj)) throw new Exception(name + ": type error, expected obj, got " + v.getTypeName());
        return ((ValueObj) v).getVal();
    }
    
    
    
    /**
     * Lookup member function by name. 
     */
    public Function getFunction (String name) {
        Function x=functions.get(name);
        if (x != null) return x;
        
        if (functionArr != null) {
            for (Function f:functionArr) {
                if (f.getName().equals(name)) {
                    functions.put(name, f);
                    return f;
                }
            }
            return null;
        }
        return null;
    }
    
    public abstract String getTypeName();
    public abstract boolean eq(Obj x);
        
    
}
