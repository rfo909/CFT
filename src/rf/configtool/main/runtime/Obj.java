/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020 Roar Foshaug

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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

import rf.configtool.main.Ctx;
import rf.configtool.main.ObjGlobal;
import rf.configtool.main.OutText;

/**
 * Super class of all object types.
 */
public abstract class Obj {
    
    private HashMap<String,Function> functions=new HashMap<String,Function>();
    
    protected void add (Function function) {
        String name=function.getName();
        if (functions.containsKey(name)) throw new RuntimeException("Duplicate function " + name);
        this.functions.put(name, function);
    }
    
    /**
     * Dict needs to clear and re-add functions when content changes, to enable
     * field values as functions, for direct dotted lookup
     */
    protected void clearFunctions() {
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
        if (!(v instanceof ValueString)) throw new Exception(name + ": type errror, expected String, got " + v.getTypeName());
        return ((ValueString) v).getVal();
    }
    
    protected long getInt(String name, List<Value> args, int pos) throws Exception {
        Value v=args.get(pos);
        if (!(v instanceof ValueInt)) throw new Exception(name + ": type errror, expected int, got " + v.getTypeName());
        return ((ValueInt) v).getVal();
    }
    
    protected double getFloat(String name, List<Value> args, int pos) throws Exception {
        Value v=args.get(pos);
        if (v instanceof ValueInt) return ((ValueInt) v).getVal();
        if (v instanceof ValueFloat) return ((ValueFloat) v).getVal(); 
        throw new Exception(name + ": type errror, expected int or float, got " + v.getTypeName());
    }
    
    protected boolean getBoolean(String name, List<Value> args, int pos) throws Exception {
        Value v=args.get(pos);
        if (!(v instanceof ValueBoolean)) throw new Exception(name + ": type errror, expected boolean, got " + v.getTypeName());
        return ((ValueBoolean) v).getVal();
    }
    
    protected List<Value> getList(String name, List<Value> args, int pos) throws Exception {
        Value v=args.get(pos);
        if (!(v instanceof ValueList)) throw new Exception(name + ": type errror, expected list, got " + v.getTypeName());
        return ((ValueList) v).getVal();
    }
    
    protected Obj getObj(String name, List<Value> args, int pos) throws Exception {
        Value v=args.get(pos);
        if (!(v instanceof ValueObj)) throw new Exception(name + ": type errror, expected obj, got " + v.getTypeName());
        return ((ValueObj) v).getVal();
    }
    
    public Function getFunction (String name) {
        return functions.get(name);
    }
    
    public abstract String getTypeName();
    public abstract ColList getContentDescription();
    public abstract boolean eq(Obj x);
    
    /**
     * Values and objects that support synthesizing code from inner state, should override
     * this method, returning valid code that recreates the object. This feature is
     * needed to decouple values from the code.
     */
    public String synthesize() throws Exception {
        throw new Exception("Object " + getTypeName() + " can not be synthesized");
    }
    
    public void generateHelp(Ctx ctx) {
        OutText outText=ctx.getOutText();
        if (this instanceof ObjGlobal) {
            outText.addSystemMessage("---");
            outText.addSystemMessage("For help on data types, create an instance on the stack, then invoke 'help' statement");
            outText.addSystemMessage("Example:");
            outText.addSystemMessage("    List(1,2,3) help     - shows methods on lists");
            outText.addSystemMessage("    'x' help             - shows methods on strings");
            outText.addSystemMessage("    help(3)              - alternative notation - shows help on ints");
            outText.addSystemMessage("");
            outText.addSystemMessage("- Note that for functions with no arguments, the () are optional");
            outText.addSystemMessage("- Looping is done with the '-> var' construct");
            outText.addSystemMessage("     Example: List(1,2,3)->m out('xxx'+m)");
            
            outText.addSystemMessage("");
            outText.addSystemMessage("- The out(expr) statement adds data to the output list");
            outText.addSystemMessage("- In-line variables are assigned with '=ident' after the expression (stack)");
            outText.addSystemMessage("- Type ':' for overview over colon commands");
            outText.addSystemMessage("");;
            outText.addSystemMessage("- The current program line can be given a name, for example '/Test");
            outText.addSystemMessage("- To run it again, just enter 'Test'");
            outText.addSystemMessage("- Enter '?Test' to get info about symbol Test, or just '?' for all");
            outText.addSystemMessage("---");
            outText.addSystemMessage("- Parameters are accessed with P(1), P(1,defaultVal) or just P() for param-list.");
            outText.addSystemMessage("- The current value on stack is accessed via assignment or the '_' expr");
            
            outText.addSystemMessage("");
        }
        
        outText.addSystemMessage("(" + getTypeName() + ")");
        
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

        for (String name:fNames) {
            outText.addSystemMessage(functions.get(name).getShortDesc());
        }
    }

    public String getDescription() {
        return getContentDescription().toString();
        
    }
}
