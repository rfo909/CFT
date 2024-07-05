/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2024 Roar Foshaug

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 3 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>
*/

package rf.configtool.main.runtime;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import rf.configtool.lexer.Lexer;
import rf.configtool.lexer.SourceLocation;
import rf.configtool.lexer.TokenStream;
import rf.configtool.main.ScriptSourceLine;
import rf.configtool.main.Ctx;
import rf.configtool.main.ObjGlobal;
import rf.configtool.main.ReportData;
import rf.configtool.main.Version;
import rf.configtool.main.runtime.lib.ObjClosure;
import rf.configtool.main.runtime.lib.ObjDict;
import rf.configtool.parsetree.CodeSpace;

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
    
    protected ValueBinary getBinary(String name, List<Value> args, int pos) throws Exception {
        Value v=args.get(pos);
        if (!(v instanceof ValueBinary)) throw new Exception(name + ": type error, expected Binary, got " + v.getTypeName());
        return (ValueBinary) v;
    }

    protected Obj getObj(String name, List<Value> args, int pos) throws Exception {
        Value v=args.get(pos);
        if (!(v instanceof ValueObj)) throw new Exception(name + ": type error, expected obj, got " + v.getTypeName());
        return ((ValueObj) v).getVal();
    }
    
    /**
     * Match ObjClosure directly, or if lambda (ValueBlock) create Closure wrapping it
     */
    protected ObjClosure getClosure (String name, List<Value> args, int pos) throws Exception {
        Value v=args.get(pos);

        if (v instanceof ValueBlock) {
            return new ObjClosure(new ObjDict(), (ValueBlock) v);
        }
        
        if (v instanceof ValueObj) {
            Obj obj=((ValueObj) v).getVal();
            if (obj instanceof ObjClosure) {
                return (ObjClosure) obj; 
            }
        }
        throw new Exception(name + ": expected Lambda or Closure, got " + v.getTypeName());
    }
    
    
    /**
     * Lookup member function by name. 
     */
    public Function getFunction (String name) {
//      // first implementation : 5600ms
//      if (functionArr != null) {
//          for (Function f:functionArr) functions.put(f.getName(),f);
//          functionArr=null;
//      }
//      return functions.get(name);
        
        // 4200ms
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
    public abstract ColList getContentDescription();
    public abstract boolean eq(Obj x);
    
    public String synthesize() throws Exception {
        if (this instanceof IsSynthesizable) {
            return ((IsSynthesizable) this).createCode();
        }
        throw new Exception("Object " + getTypeName() + " can not be synthesized");
    }
    
    
    
     /**
     * Create a safe copy of this value, by means of running it through an eval(syn()) pipeline,
     * which ensures the new value is completely independent. Throws exception if value not synthesizable.
     */
    public Value createClone (Ctx callCtx) throws Exception {
        Obj obj=this;
        if (!(obj instanceof IsSynthesizable)) throw new Exception("Not synthesizable");
        IsSynthesizable x=(IsSynthesizable) obj;
        try {
            String s=x.createCode();

            Lexer p=new Lexer();
            p.processLine(new ScriptSourceLine(new SourceLocation(), s));
            TokenStream ts = p.getTokenStream();
            CodeSpace progLine=new CodeSpace(ts);
            
            Ctx ctx=callCtx.sub();
            progLine.execute(ctx);
            Value retVal=ctx.pop();
            if (retVal==null) retVal=new ValueNull();
            return retVal;
        } catch (Exception ex) {
            throw new Exception("Value could not be run through eval(syn()) - must be synthesizable");
        }
    }
   
    
    
    
    public void generateHelp(Ctx ctx) {
        ObjGlobal objGlobal=ctx.getObjGlobal();
        if (this instanceof ObjGlobal) {
            objGlobal.addSystemMessage(Version.getVersion());
            objGlobal.addSystemMessage("");
        } 
        
        
        // populate functions map completely for help
        if (functionArr != null) {
            for (Function f:functionArr) functions.put(f.getName(), f);
            functionArr=null;
        }
        
        List<String> fNames=new ArrayList<String>();        
        Iterator<String> names=functions.keySet().iterator();
        
        while(names.hasNext()) {
            String name=names.next();
            fNames.add(name);
        }
        fNames.sort(new Comparator<String>() {
            public int compare (String a, String b) {
                return a.compareTo(b);
            }
        });
        
        
        boolean foundSpecial=false;
        for (String name:fNames) {
            if (name.startsWith("_")) {
                objGlobal.addSystemMessage(functions.get(name).getShortDesc());
                foundSpecial=true;
            }
        }
        if (foundSpecial) {
            objGlobal.addSystemMessage("");
        }
        for (String name:fNames) {
            if (!name.startsWith("_")) objGlobal.addSystemMessage(functions.get(name).getShortDesc());
        }
    }

    public String getDescription() {
        return getContentDescription().toString();
        
    }
}
