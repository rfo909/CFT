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

package rf.configtool.main.runtime.lib;

import java.util.*;

import rf.configtool.main.CodeLine;
import rf.configtool.main.Ctx;
import rf.configtool.main.OutText;
import rf.configtool.main.runtime.*;
import rf.configtool.parser.Parser;
import rf.configtool.parser.SourceLocation;
import rf.configtool.parser.TokenStream;
import rf.configtool.parser.Token;

/**
 * Non-persistent object to be populated with key-value pairs.
 */
public class ObjDict extends Obj {
    
    private Map<String,Value> values=new HashMap<String,Value>();
    
    public Iterator<String> getKeys() {
        return values.keySet().iterator();
    }
    
    public Value getValue(String key) {
        return values.get(key);
    }
    
    private List<Function> baseFunctions;
    
    public ObjDict () {
        baseFunctions=new ArrayList<Function>();
        baseFunctions.add(new FunctionSet());
        baseFunctions.add(new FunctionGet());
        baseFunctions.add(new FunctionKeys());
        baseFunctions.add(new FunctionRemove());
        baseFunctions.add(new FunctionHas());
        baseFunctions.add(new FunctionShow());
        baseFunctions.add(new FunctionSetStr());
        baseFunctions.add(new FunctionMergeCodes());
        baseFunctions.add(new FunctionHasNullValue());
        baseFunctions.add(new FunctionBind());
        baseFunctions.add(new FunctionGetMany());
        
        
        
        init();
    }
    
    public ObjDict(Map<String,Value> values) {
        this();
        this.values=values;
        init();
    }
    
    private void init() {
        clearFunctions();
        
        Set<String> functionNames=new HashSet<String>();
        for (Function f:baseFunctions) {
            functionNames.add(f.getName());
            add(f);
        }
        Iterator<String> names = values.keySet().iterator();
        while (names.hasNext()) {
            String name=names.next();
            if (functionNames.contains(name) || !isIdentifier(name)) continue;
            functionNames.add(name);
            add(new FunctionGetDynamic(name));
        }
    }
    
    private ObjDict self() {
    	return this;
    }
    
    private boolean isIdentifier(String name) {
        try {
            Parser p=new Parser();
            CodeLine cl=new CodeLine(new SourceLocation("xxx",0,0), name);
            p.processLine (cl);
            TokenStream ts=p.getTokenStream();
            if (ts.getTokenCount() != 2) return false;  // identifier + EOF
            if (!ts.peekType(Token.TOK_IDENTIFIER)) return false;
        } catch (Exception ex) {
            return false;
        }
        return true;
        
    }
    
    private ObjDict theDict() {
        return this;
    }
    

    @Override
    public boolean eq(Obj x) {
        return x==this;
    }

    @Override
    public String getTypeName() {
        return "Dict";
    }
    
    
    @Override 
    public String synthesize() throws Exception {
        StringBuffer sb=new StringBuffer();
        sb.append("Dict");
        Iterator<String> names=values.keySet().iterator();
        while (names.hasNext()) {
            String name=names.next();
            sb.append(".set(" + new ValueString(name).synthesize() + "," + values.get(name).synthesize() + ")");
        }
        return sb.toString();
    }
    

    @Override
    public ColList getContentDescription() {
        StringBuffer str=new StringBuffer();
        Iterator<String> x=values.keySet().iterator();
        while (x.hasNext()) {
            str.append(" "+x.next());
        }
        return ColList.list().regular("Dict: " + str.toString().trim());
    }
        

    class FunctionGet extends Function {
        public String getName() {
            return "get";
        }
        public String getShortDesc() {
            return "get(name, defaultValue?) - get value for given property name. If not set, both set it to defaultValue, and return it.";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            String key=null;
            Value defaultValue=null;
            if (params.size() == 1) {
                key=getString("name", params, 0);
            } else if (params.size()==2) {
                key=getString("name", params, 0);
                defaultValue=params.get(1);
            } else {
                throw new Exception("Expected name or name + defaultValue");
            }
            
            if (values != null) {
                Value v=values.get(key);
                if (v != null) return v;
            }
            
            if (defaultValue==null) throw new Exception("No value for key='" + key + "'");
            
            // store defaultValue in dict
            if (values==null) values=new HashMap<String,Value>();
            values.put(key, defaultValue);
            return defaultValue;
        }
    }


    class FunctionSet extends Function {
        public String getName() {
            return "set";
        }
        public String getShortDesc() {
            return "set(name,value) - set value for given property name - returns updated dict";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (values==null) values=new HashMap<String,Value>();
            
            if (params.size() != 2) throw new Exception("Expected name and value parameters");
            String key=getString("name", params, 0);
            Value value=params.get(1);
            // 2020-10: special processing for Lambda's and Closures - the point is that storing a
            // Lambda in a Dict, we wrap the Lambda in a Closure, so that the Lambda becomes a
            // "member function" of the Dict, referring back to it via its "self" variable. When
            // storing a Closure, we unwrap the Lambda and create a new Closure, as described above.
            //
            // This means doing Dict.get("somefunc").call(...) invokes a closure instead of a lambda.
            if (value instanceof ValueBlock) {
            	ValueBlock lambda=(ValueBlock) value;
            	//System.out.println("Wrapping lambda in closure for key " + key);
            	value=new ValueObj(new ObjClosure(self(), lambda));
            } else if (value instanceof ValueObj) {
            	Obj obj=((ValueObj) value).getVal();
            	if (obj instanceof ObjClosure) {
            		ValueBlock lambda=((ObjClosure) obj).getLambda();
            		//System.out.println("Recoding closure for key " + key);
            		value=new ValueObj(new ObjClosure(self(), lambda));
            	}
            }
            values.put(key, value);
            
            init();
            
            return new ValueObj(theDict());
        }
    }


    class FunctionKeys extends Function {
        public String getName() {
            return "keys";
        }
        public String getShortDesc() {
            return "keys() - return list of keys (strings)";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            List<Value> result=new ArrayList<Value>();
            if (values != null) {
                Iterator<String> keys=values.keySet().iterator();
                while (keys.hasNext()) {
                    result.add(new ValueString(keys.next()));
                }
            }
            return new ValueList(result);
        }
    }

    class FunctionRemove extends Function {
        public String getName() {
            return "remove";
        }
        public String getShortDesc() {
            return "remove(name) - get value for given property name and delete it from dict";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected property name parameter");
            String key=getString("name", params, 0);
            
            if (values != null) {
                values.remove(key);
            } 
            
            init();
            
            return new ValueObj(theDict());
        }
    }



    class FunctionHas extends Function {
        public String getName() {
            return "has";
        }
        public String getShortDesc() {
            return "has(key) - return boolean";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected parameter 'key' as string");
            String key=getString("key", params, 0);
            return new ValueBoolean(values.containsKey(key));
        }
    }


    class FunctionShow extends Function {
        public String getName() {
            return "show";
        }
        public String getShortDesc() {
            return "show() - prints output, returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            if (values != null) {
                Iterator<String> keys=values.keySet().iterator();
                while (keys.hasNext()) {
                    String key=keys.next();
                    String line=key + ": " + values.get(key).getValAsString();
                    ctx.getObjGlobal().addSystemMessage(line);
                }
            }
            return new ValueObj(theDict());
        }
    }



    class FunctionSetStr extends Function {
        public String getName() {
            return "setStr";
        }
        public String getShortDesc() {
            return "setStr(str) - parses string 'xxx [=:] yyy - returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected str parameter");
            String str=getString("str", params, 0);
            
            int pos1=str.indexOf(':');
            int pos2=str.indexOf('=');
            
            if (pos1<0 && pos2<0) {
                throw new Exception("Expected ':' or '=' in '" + str + "'");
            }
            
            int max=Math.max(pos1, pos2);
            int min=Math.min(pos1, pos2);
            

            int pos=max;
            if (min > 0) pos=min;
            
        
            String key=str.substring(0,pos).trim();
            String value=str.substring(pos+1).trim();
            values.put(key,new ValueString(value));
            
            init();
            
            return new ValueObj(theDict());
        }
    }


    class FunctionMergeCodes extends Function {
        public String getName() {
            return "mergeCodes";
        }
        public String getShortDesc() {
            return "mergeCodes([pre,post]?) - create copy where all keys are rewritten as ${key} or <pre>key<post>";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	String pre="${";
        	String post="}";
        	if (params.size()==2) {
        		pre=getString("pre",params,0);
        		post=getString("post",params,1);
        	} else if (params.size() != 0) {
        		throw new Exception("Expected no parameters or two parameters: pre,post");
        	}
        
            Map<String,Value> map=new HashMap<String,Value>();
            
            Iterator<String> keys=values.keySet().iterator();
            while(keys.hasNext()) {
                String key=keys.next();
                map.put(pre + key + post, values.get(key));
            }
            
            return new ValueObj(new ObjDict(map));
        }
    }


    /**
     * Identifier-named fields are available as functions, for dotted lookup, see init()
     *
     */
    class FunctionGetDynamic extends Function {
        private String propertyName;
        
        public FunctionGetDynamic(String varName) {
            this.propertyName=varName;
        }
        
        public String getName() {
            return propertyName;
        }
        public String getShortDesc() {
            return propertyName + "()  - get property value";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return values.get(propertyName);
        }
    }


     class FunctionHasNullValue extends Function {
         public String getName() {
            return "hasNullValue";
        }
        public String getShortDesc() {
            return "hasNullValue() - true if at least one value is null";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            
            boolean found=false;
            Iterator<String> keys=values.keySet().iterator();
            while(keys.hasNext()) {
                String key=keys.next();
                if (values.get(key) instanceof ValueNull) {
                	found=true;
                	break;
                }
            }
            return new ValueBoolean(found);
            
         }
    }


     class FunctionBind extends Function {
         public String getName() {
            return "bind";
        }
        public String getShortDesc() {
            return "bind(lambda) - returns closure";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected lambda parameter");
            if (!(params.get(0) instanceof ValueBlock)) throw new Exception("Expected lambda parameter");
            ValueBlock lambda=(ValueBlock) params.get(0);
            
            return new ValueObj(new ObjClosure(theDict(), lambda));
         }
    }

     

     class FunctionGetMany extends Function {
         public String getName() {
             return "getMany";
         }
         public String getShortDesc() {
             return "getMany(keyList) - return list of values for list of keys";
         }
         public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	 if (params.size() != 1) throw new Exception("Expected single list of keys");
        	 List<Value> keys = getList("keyList",params,0);
        	 List<Value> result=new ArrayList<Value>();
        	 
        	 for (Value key:keys) {
        		 String s=key.getValAsString();
        		 Value value=values.get(s);
        		 if (value==null) result.add(new ValueNull()); else result.add(value);
        	 }
        	 return new ValueList(result);
         }
     }


}
