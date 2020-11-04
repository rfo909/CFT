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

import rf.configtool.data.ProgramLine;
import rf.configtool.main.CodeLine;
import rf.configtool.main.Ctx;
import rf.configtool.main.ObjGlobal;
import rf.configtool.main.OutText;
import rf.configtool.main.Version;
import rf.configtool.main.runtime.Obj.FunctionClone;
import rf.configtool.parser.Parser;
import rf.configtool.parser.SourceLocation;
import rf.configtool.parser.TokenStream;

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
    
    public Obj() {
        add (new FunctionClone());
    }
    
    /**
     * Dict needs to clear and re-add functions when content changes, to enable
     * field values as functions, for direct dotted lookup
     */
    protected void clearFunctions() {
    	functions=new HashMap<String,Function>();
        add (new FunctionClone());
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
    
    
    
     /**
     * Create a safe copy of this value, by means of running it through an eval(syn()) pipeline,
     * which ensures the new value is completely independent. Throws exception if value not synthesizable.
     */
	public Value createClone (Ctx callCtx) throws Exception {
    	Obj obj=this;
		try {
			String s=obj.synthesize();

			Parser p=new Parser();
			p.processLine(new CodeLine(new SourceLocation(), s));
			TokenStream ts = p.getTokenStream();
			ProgramLine progLine=new ProgramLine(ts,false);
			
			Ctx ctx=callCtx.sub();
			progLine.execute(ctx);
			return ctx.pop();
		} catch (Exception ex) {
			throw new Exception("Value could not be run through eval(syn()) - must be synthesizable");
		}
	}

	
	
    
    
	   class FunctionClone extends Function {
	        public String getName() {
	            return "clone";
	        }
	        public String getShortDesc() {
	            return "clone() - creates clone via eval(syn()) - error if value not synthesizable";
	        }
	        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
	            if (params.size() != 0) throw new Exception("Expected no parameters");
	            return createClone(ctx);
	        }

	    }
	    

   
    
    
    
    public void generateHelp(Ctx ctx) {
    	ObjGlobal objGlobal=ctx.getObjGlobal();
        OutText outText=ctx.getOutText();
        if (this instanceof ObjGlobal) {
        	objGlobal.addSystemMessage("-------------------------------------------------");
        	objGlobal.addSystemMessage("Please read the full documentation: doc/Doc.html");
        	objGlobal.addSystemMessage("-------------------------------------------------");
            objGlobal.addSystemMessage(new Version().getVersion());
        	objGlobal.addSystemMessage("");
            objGlobal.addSystemMessage("Global functions");
            objGlobal.addSystemMessage("");
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

        for (String name:fNames) {
        	objGlobal.addSystemMessage(functions.get(name).getShortDesc());
        }
    }

    public String getDescription() {
        return getContentDescription().toString();
        
    }
}
