/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2022 Roar Foshaug

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rf.configtool.lexer.Lexer;
import rf.configtool.lexer.SourceLocation;
import rf.configtool.lexer.Token;
import rf.configtool.lexer.TokenStream;
import rf.configtool.main.CodeLine;
import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBlock;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueNull;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;

/**
 * Non-persistent object to be populated with key-value pairs.
 */
public class ObjDict extends Obj {
    
    private String name;
    private Map<String,Value> values=new HashMap<String,Value>();
    private List<String> keySequence=new ArrayList<String>();
    
    public Value getValue(String key) {
        return values.get(key);
    }
    
    public Iterator<String> getKeys() {
        return keySequence.iterator();
    }
    
    private final List<Function> baseFunctions;
    private boolean refreshInnerFunctions = false;
    private Set<String> allFunctionNames = new HashSet<String>();
    

    public ObjDict () {
        this(null);
    }

    public ObjDict (String name) {
        super();
        this.name=name;
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
        baseFunctions.add(new FunctionCopyFrom());
        baseFunctions.add(new FunctionSubset());
        baseFunctions.add(new FunctionGetName());
        baseFunctions.add(new FunctionSetName());
        
        refreshInnerFunctions=true;
    }
    
    @Override
    public Function getFunction (String name) {
        verifyInnerFunctions();
        return super.getFunction(name);
    }
    
    
    @Override
    public void generateHelp(Ctx ctx) {
        verifyInnerFunctions();
        super.generateHelp(ctx);
    } 
    
    public void set (String key, Value value) {
        if (values.get(key) == null) {
            keySequence.add(key);
        }

        // 2020-10: special processing for Lambda's and Closures - the point is that storing a
        // Lambda in a Dict, we wrap the Lambda in a Closure, so that the Lambda becomes a
        // "member function" of the Dict, referring back to it via its "self" variable. When
        // storing a Closure, we unwrap the Lambda and create a new Closure, as described above.
        //
        // This means doing Dict.get("somefunc").call(...) invokes a closure instead of a lambda.
        // 2022-04 RFO Also, have moved the detection of Dict.ident to DottedCall.java, which
        // implements doing auto-invoke of Closures from Dict. The Dict.get("xxx") does not aut-invoke,
        // only Dict.xxx
        //
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

    }
    
    
    public String getName() {
    	return name;
    }
    
    private synchronized void verifyInnerFunctions() {
        if (!refreshInnerFunctions) return;
        refreshInnerFunctions=false;
        
        clearFunctions();
        allFunctionNames.clear();
        
        Set<String> allFunctionNames=new HashSet<String>();
        List<Function> allFunctions=new ArrayList<Function>();
        
        for (Function f:baseFunctions) {
            allFunctionNames.add(f.getName());
            allFunctions.add(f);
        }

        // convert to array and call setFunctions()
        Function[] arr=new Function[allFunctions.size()];
        for (int i=0; i<allFunctions.size(); i++) arr[i]=allFunctions.get(i);
        
        setFunctions(arr);
    }
    
    private ObjDict self() {
        return this;
    }
    
//    private boolean isIdentifier(String name) {
//        try {
//            Lexer p=new Lexer();
//            CodeLine cl=new CodeLine(new SourceLocation("xxx",0,0), name);
//            p.processLine (cl);
//            TokenStream ts=p.getTokenStream();
//            if (ts.getTokenCount() != 2) return false;  // identifier + EOF
//            if (!ts.peekType(Token.TOK_IDENTIFIER)) return false;
//        } catch (Exception ex) {
//            return false;
//        }
//        return true;
//        
//    }
    
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
        if (name != null) {
            sb.append("(" + new ValueString(name).synthesize() + ")");
        }

        for (String propName:keySequence) {
            Value value=values.get(propName);
            
            // NOTE: closures are not synthesizable, but lambdas are, and setting a field in a Dict to 
            // a lambda, recreates a closure, so that's how this is handled.
            if (value instanceof ValueObj) {
                Obj obj=((ValueObj) value).getVal();
                if (obj instanceof ObjClosure) {
                    ValueBlock lambda=((ObjClosure) obj).getLambda();
                    value = lambda;
                } 
            } else if (value instanceof ValueBlock) {
                // simple sanity check
                throw new Exception("Internal errors: Dict should contain ref to Lambda as it should be auto-wrapped as Closure");
            }
            sb.append(".set(" + new ValueString(propName).synthesize() + "," + value.synthesize() + ")");
        }
        return sb.toString();
    }
    

    @Override
    public ColList getContentDescription() {
        StringBuffer sb=new StringBuffer();
        for (String key:keySequence) {
            sb.append(" "+key);
        }
        String str=sb.toString().trim();
        if (str.length() > 60) str=str.substring(0,55) + "+";
        
        String sn="";
        if (name != null) sn=":"+name;
        return ColList.list().regular("Dict" + sn + " [" + str + "]");
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
            set(key, defaultValue);
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
            
            set(key, value);
            
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
            for (String key:keySequence) {
                result.add(new ValueString(key));
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
                keySequence.remove(key);
            } 
            
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
            for (String key:keySequence) {
                String line=key + ": " + values.get(key).getValAsString();
                ctx.getObjGlobal().addSystemMessage(line);
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

            set(key,new ValueString(value));
            
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
        
            ObjDict x=new ObjDict();
           
            for (String key:keySequence) {
                x.set(pre + key + post, values.get(key));
            }
            
            return new ValueObj(x);
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
            for (String key:keySequence) {
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

     class FunctionCopyFrom extends Function {
         public String getName() {
             return "copyFrom";
         }
         public String getShortDesc() {
             return "copyFrom(Dict) - add and replace values in current Dict with values from parameter Dict - returns self";
         }
         public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
             if (params.size() != 1) throw new Exception("Expected Dict parameter");
             Obj obj=getObj("Dict",params,0);
             if (obj instanceof ObjDict) {
                 ObjDict d=(ObjDict) obj;
                 for (String key:d.keySequence) { 
                     Value value=d.getValue(key);
                     set(key, value);
                 }
             } else {
                 throw new Exception("Expected Dict parameter");
             }
             return new ValueObj(self());
         }
     }


     class FunctionSubset extends Function {
         public String getName() {
             return "subset";
         }
         public String getShortDesc() {
             return "subset(keyList,defaultValue) - create new Dict with keys from keyList, using defaultValue for missing keys";
         }
         public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
             if (params.size() != 2) throw new Exception("Expected parameters keyList, defaultValue");
             List<Value> list=getList("keyList",params,0);
             Value defaultValue=params.get(1);
             
             ObjDict x=new ObjDict();

             Map<String,Value> data=new HashMap<String,Value>();
             for (Value v:list) {
                 if (!(v instanceof ValueString)) throw new Exception("Expected keyList parameter to contain strings");
                 String key=((ValueString)v).getVal();
                 if (self().values.containsKey(key)) {
                     x.set(key, self().values.get(key));
                 } else {
                     x.set(key, defaultValue);
                 }
             }
             return new ValueObj(x);
         }
     }
     
     class FunctionGetName extends Function {
         public String getName() {
            return "getName";
        }
        public String getShortDesc() {
            return "getName() - return name string or null if not set";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            if (name==null) return new ValueNull();
            return new ValueString(name);
         }
    }


     class FunctionSetName extends Function {
         public String getName() {
            return "setName";
        }
        public String getShortDesc() {
            return "setName(str|null) - set name, return self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected name string|null parameter");
            if (params.get(0) instanceof ValueNull) {
                name=null;
            } else {
                String str=getString("name",params,0);
                name=str;
            }
            return new ValueObj(self());
         }
    }

}
