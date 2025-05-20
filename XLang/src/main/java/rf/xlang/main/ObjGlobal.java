
package rf.xlang.main;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rf.xlang.main.runtime.*;
import rf.xlang.main.runtime.lib.*;

import rf.xlang.parsetree.TupleType;
import rf.xlang.parsetree.CodeFunction;


public class ObjGlobal extends Obj {

    private List<String> systemMessages=new ArrayList<String>();

    private HashMap<String, TupleType> tupleTypes = new HashMap<>();
    private HashMap<String, CodeFunction> codeFunctions = new HashMap<>();
    

    public ObjGlobal() throws Exception {
        add(new FunctionList());
        add(new FunctionDir());
        add(new FunctionFile());
        add(new FunctionInt());
        add(new FunctionFloat());
        add(new FunctionStr());
        add(new FunctionDate());
        add(new FunctionRegex());
        add(new FunctionMath());
        add(new FunctionSys());
        add(new FunctionPrintln());
        add(new FunctionHelp());

    }

    public void addTupleType (TupleType type) {
        this.tupleTypes.put(type.getTypeName(),type);
    }

    public void addCodeFunction (CodeFunction function) {
        this.codeFunctions.put(function.getFunctionName(),function);
    }

    /**
     * Look up named tuple type, return null if not found
     */
    public TupleType getTupleType (String name) {
        return tupleTypes.get(name);
    }

    /**
     * Look up named script code function, return null if not found
     */
    public CodeFunction getCodeFunction (String name) {
        return codeFunctions.get(name);
    }

    public void addSystemMessage (String line) {
        systemMessages.add(line);
    }
    
    public void debug (String line) {
        systemMessages.add("[debug] " + line);
    }
    
    public List<String> getSystemMessages() {
        return systemMessages;
    }
    
    public void clearSystemMessages() {
        systemMessages.clear();
    }

    public boolean runningOnWindows() {
        return (File.separatorChar=='\\');
    }

    @Override
    public boolean eq(Obj x) {
        return false;
    }

    public void cleanupOnExit() {
    }
    
    public String getTypeName() {
        return "<GLOBAL>";
    }

    private Obj self() {
        return this;
    }



    class FunctionList extends Function {
        public String getName() {
            return "List";
        }
        public String getShortDesc() {
            return "List(a,b,c,...,x) - creates list object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            return new ValueList(params);
        }
    }
    
    class FunctionDir extends Function {
        public String getName() {
            return "Dir";
        }
        public String getShortDesc() {
            return "Dir(str) - creates Dir object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size()==1) {
                String s=getString("str", params, 0);
                return new ValueObj(new ObjDir(s));
            } else {
                throw new Exception("Expected string parameter");
            }
        }
    }
    
    class FunctionFile extends Function {
        public String getName() {
            return "File";
        }
        public String getShortDesc() {
            return "File(str) - creates File object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected 1 parameter");
            if (!(params.get(0) instanceof ValueString)) throw new Exception("Expected String parameter");
            return new ValueObj(new ObjFile( ((ValueString) params.get(0)).getVal()));
        }
    }
    


    class FunctionInt extends Function {
        public String getName() {
            return "Int";
        }
        public String getShortDesc() {
            return "Int(value,data) - create Int object - for sorting";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size()==2) {
                if (!(params.get(0) instanceof ValueInt)) throw new Exception("Expected value parameter to be int");
                long val=((ValueInt) params.get(0)).getVal();
                Value data=params.get(1);
                return new ValueObjInt(val, data);
            } else {
                throw new Exception("Expected parameters value and optional data associated with value");
            }
        }
    }
    
    class FunctionFloat extends Function {
        public String getName() {
            return "Float";
        }
        public String getShortDesc() {
            return "Float(value,data) - create Float object - for sorting";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size()==2) {
                if (!(params.get(0) instanceof ValueFloat)) throw new Exception("Expected value parameter to be float");
                double val=((ValueFloat) params.get(0)).getVal();
                Value data=params.get(1);
                return new ValueObjFloat(val, data);
            } else {
                throw new Exception("Expected parameters value and optional data associated with value");
            }
        }
    }
    

    class FunctionStr extends Function {
        public String getName() {
            return "Str";
        }
        public String getShortDesc() {
            return "Str(value,data) - create Str object - for sorting";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size()==2) {
                if (!(params.get(0) instanceof ValueString)) throw new Exception("Expected value parameter to be string");
                String val=((ValueString) params.get(0)).getVal();
                Value data=params.get(1);
                return new ValueObjStr(val, data);
            } else {
                throw new Exception("Expected parameters value and optional data associated with value");
            }
        }
    }

    class FunctionDate extends Function {
        public String getName() {
            return "Date";
        }
        public String getShortDesc() {
            return "Date(int?) - create Date and time object - uses current time if no parameter";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() == 0) {
                return new ValueObj(new ObjDate());
            } else if (params.size()==1) {
                long val=getInt("int", params, 0);
                return new ValueObj(new ObjDate(val));
            } else {
                throw new Exception("Expected no parameters or single int value");
            }
        }
    }
    
    
    class FunctionRegex extends Function {
        public String getName() {
            return "Regex";
        }
        public String getShortDesc() {
            return "Regex(str) - creates Regex object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected 1 parameter");
            String regex=getString("regex", params, 0);
            return new ValueObj(new ObjRegex(regex));
        }
    }

    


    private static ValueObj staticSys=new ValueObj(new ObjSys());
   
    class FunctionSys extends Function {
        
        public String getName() {
            return "Sys";
        }
        public String getShortDesc() {
            return "Sys() - create Sys object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return staticSys;
            //return new ValueObj(new ObjSys());
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

    class FunctionPrintln extends Function {
        public String getName() {
            return "println";
        }
        public String getShortDesc() {
            return "println(...) - print to System.out returns true";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            StringBuffer sb=new StringBuffer();
            boolean sep=false;
            for (Value v:params) {
                if (sep) sb.append(" ");
                sb.append(v.getValAsString());
                sep = true;
            }
            System.out.println(sb.toString());
            return new ValueBoolean(true);
        }
    }

    class FunctionHelp extends Function {
        public String getName() {
            return "help";
        }
        public String getShortDesc() {
            return "help(value) - show help for value";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            Obj obj=null;

            if (params.size() == 1) {
                obj = params.get(0);
                if (obj instanceof ValueObj) obj = ((ValueObj) obj).getVal();
            } else if (params.size() != 0) {
                throw new Exception("Expected optional single value");
            }

            if (obj==null) obj=self();
            obj.generateHelp(ctx);
            return new ValueBoolean(true);
        }
    }

}
