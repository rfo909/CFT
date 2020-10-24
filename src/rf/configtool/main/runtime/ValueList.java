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

import java.util.*;

import rf.configtool.main.Ctx;
import rf.configtool.main.OutText;
import rf.configtool.main.runtime.lib.ValueObjInt;

public class ValueList extends Value {
    
    private List<Value> val;
    
    public ValueList (List<Value> val) {
        this.val=val;
        add(new FunctionLength());
        add(new FunctionNth());
        add(new FunctionSum());
        add(new FunctionConcat());
        add(new FunctionAdd());
        add(new FunctionContains());
        add(new FunctionUnique());
        add(new FunctionKeep());
        add(new FunctionSort());
        add(new FunctionReverse());
        add(new FunctionReplace());
        add(new FunctionRemove());
        add(new FunctionSet());
        add(new FunctionInsert());
        add(new FunctionPush());
        add(new FunctionEmpty());
        add(new FunctionLast());
        add(new FunctionFirst());
    }
    
    protected ValueList self() {
        return this;
    }
    
    public List<Value> getVal() {
        return val;
    }
    
    @Override
    public String getTypeName() {
        return "List";
    }


    @Override
    public String getValAsString() {
        StringBuffer sb=new StringBuffer();
        sb.append("[");
        boolean comma=false;
        for (Value v:val) {
            if (comma) sb.append(", ");
            sb.append(v.getContentDescription());
            comma=true;
        }
        sb.append("]");
        return sb.toString();
    }
    
    @Override 
    public String synthesize() throws Exception {
        StringBuffer sb=new StringBuffer();
        sb.append("List(");
        boolean comma=false;
        for (Value v:val) {
            if (comma) sb.append(",");
            sb.append(v.synthesize());
            comma=true;
        }
        sb.append(")");
        return sb.toString();
    }
    

    
    @Override
    public boolean eq(Obj v) {
        if (v==this) return true;
        return false;
    }

    @Override
    public boolean getValAsBoolean() {
        return val.size() > 0;
    }


    /**
     * Create new list which contains sum of this list and given list
     */
    public ValueList addElements (ValueList x) {
        List<Value> result=new ArrayList<Value>();
        for (Value v:val) result.add(v);
        for (Value v:x.getVal()) result.add(v);
        return new ValueList(result);
    }
    /**
     * Create new list which contains elements of this list that don't exist in given list
     */
    public ValueList removeElements (ValueList x) {
        List<Value> result=new ArrayList<Value>();
        for (Value v:val) {
            if (!x.contains(v)) result.add(v);
        }
        return new ValueList(result);
    }
    
    /**
     * True if list contain value - using eq() implementation of Value classes
     */
    public boolean contains (Value v) {
        for (Value x:val) {
            if (x.eq(v)) return true;
        }
        return false;
    }

    // -------------------------------------------------------------------------------------
    
    
    class FunctionLength extends Function {
        public String getName() {
            return "length";
        }
        public String getShortDesc() {
            return "length() - returns list length";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) {
                throw new Exception("Expected no parameters");
            }
            return new ValueInt(val.size());
        }
    }

    class FunctionAdd extends Function {
        public String getName() {
            return "add";
        }
        public String getShortDesc() {
            return "add(val) - add element, returns self (modifies same instance)";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (!argCount(params,1)) {
                throw new Exception("Expected val parameter");
            }
            val.add(params.get(0));
            return self();
        }
    }


    
    class FunctionContains extends Function {
        public String getName() {
            return "contains";
        }
        public String getShortDesc() {
            return "contains(any) - returns boolean";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) {
                throw new Exception("Expected one parameter");
            }
            return new ValueBoolean(contains(params.get(0)));
        }
    }



    class FunctionSum extends Function {
        public String getName() {
            return "sum";
        }
        public String getShortDesc() {
            return "sum() - returns sum of list of numbers";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) {
                throw new Exception("Expected no parameters");
            }
            long sum=0L;
            for (Value v:val) {
                if (!(v instanceof ValueInt)) throw new Exception("Can only sum integers");
                sum += ((ValueInt) v).getVal();
            }
            return new ValueInt(sum);
        }
    }
    

    class FunctionConcat extends Function {
        public String getName() {
            return "concat";
        }
        public String getShortDesc() {
            return "concat() or concat(str) - returns string concatenation, optionally with separator string";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            String separatorString="";
            if (params.size() == 1) {
                separatorString=params.get(0).getValAsString();
            }
            StringBuffer sb=new StringBuffer();
            boolean sep=false;
            for (Value v:val) {
                if (sep) sb.append(separatorString);
                sb.append(v.getValAsString());
                sep=true;
            }
            return new ValueString(sb.toString());
        }
    }


    class FunctionNth extends Function {
        public String getName() {
            return "nth";
        }
        public String getShortDesc() {
            return "nth() or nth(pos) - returns nth element, defaults to 0, use negative to index from end";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() == 0) {
                return val.get(0);
            }
            if (params.size() != 1) {
                throw new Exception("Expected int parameter");
            }

            if (params.get(0) instanceof ValueInt) {
                long pos=((ValueInt) params.get(0)).getVal();
                if (pos < 0) {
                    // negatives index from end
                    pos=val.size()+pos;
                }
                return val.get((int) pos);
            } else {
                throw new Exception("Expected int parameter");
            }
        }
    }
    

    
    class FunctionUnique extends Function {
        public String getName() {
            return "unique";
        }
        public String getShortDesc() {
            return "unique() - remove duplicates";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) {
                throw new Exception("Expected no parameters");
            }
            List<Value> newList=new ArrayList<Value>();
            ValueList result=new ValueList(newList);
            
            for (Value v:val) {
                if (!result.contains(v)) newList.add(v);
            }
            return result;
        }
    }
    
    class FunctionKeep extends Function {
        public String getName() {
            return "keep";
        }
        public String getShortDesc() {
            return "keep(from,to?) - returns sub-list";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 1 && params.size() != 2) throw new Exception("Expected in parameter(s) from, to?");
        	
        	int from=(int) getInt("from", params, 0);
        	if (from < 0) from=0;
        	
        	int to;
        	if (params.size() == 2) {
        		to=(int) getInt("to", params, 1);
        		if (to >= val.size()) to=val.size()-1;
        	} else {
        		to=val.size()-1;
        	}
       

            List<Value> newVal=new ArrayList<Value>();
            for (int i=from; i<=to; i++) {
                newVal.add(val.get(i));
            }
            
            return new ValueList(newVal);
        }
    }


    class FunctionSort extends Function {
        public String getName() {
            return "sort";
        }
        public String getShortDesc() {
            return "sort - sorts list and returns it";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (!argCount(params,0)) {
                throw new Exception("Expected no parameters");
            }
            if (val.size() <= 1) return self();
            
            boolean intSort=true;
            for (Value v:val) {
                if (!(v instanceof ValueInt)) {
                    intSort=false;
                    break;
                }
            }
            
            Comparator<Value> c;
            if (intSort) {
                c=new Comparator<Value>() {
                    public int compare(Value a, Value b) {
                        long ia=((ValueInt) a).getVal();
                        long ib=((ValueInt) b).getVal();
                        if (ia<ib) return -1;
                        if (ia==ib) return 0;
                        return 1;
                        
                    }
                };
            } else {
                c=new Comparator<Value>() {
                    public int compare(Value a, Value b) {
                        String sa=a.getValAsString();
                        String sb=b.getValAsString();
                        return sa.compareTo(sb);
                    }
                };

            }
            
            val.sort(c);
            return self();
        }
    }

    class FunctionReverse extends Function {
        public String getName() {
            return "reverse";
        }
        public String getShortDesc() {
            return "reverse - returns reversed list";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (!argCount(params,0)) {
                throw new Exception("Expected no parameters");
            }
            List<Value> newVal=new ArrayList<Value>();
            for (Value v:val) {
                newVal.add(0,v);
            }
            return new ValueList(newVal);
        }
    }

    class FunctionReplace extends Function {
        public String getName() {
            return "replace";
        }
        public String getShortDesc() {
            return "replace(str1,str2) - returns new list";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            List<Value> newVal=new ArrayList<Value>();
            if (!argCount(params,2)) {
                throw new Exception("Expected two parameters: str1, str2");
            }
            String str1=getString("str1",params,0);
            String str2=getString("str2",params,1);
            
            for (Value v:val) {
                String s=v.getValAsString().replace(str1, str2);
                newVal.add(new ValueString(s));
            }
            return new ValueList(newVal);
        }
    }



    class FunctionRemove extends Function {
        public String getName() {
            return "remove";
        }
        public String getShortDesc() {
            return "remove(pos) - delete element, return self (same list instance)";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) {
                throw new Exception("Expected pos parameter (int)");
            }
            int pos=(int) getInt("pos",params,0);
            val.remove(pos);
            
            return self();
        }
    }

    class FunctionSet extends Function {
        public String getName() {
            return "set";
        }
        public String getShortDesc() {
            return "set(pos,val) - insert element, return self (same list instance)";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 2) {
                throw new Exception("Expected pos,val parameters");
            }
            int pos=(int) getInt("pos",params,0);
            val.set(pos,params.get(1));
            
            return self();
        }
    }

    class FunctionInsert extends Function {
        public String getName() {
            return "insert";
        }
        public String getShortDesc() {
            return "insert(pos,val) - insert element, return self (same list instance)";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 2) {
                throw new Exception("Expected pos,val parameters");
            }
            int pos=(int) getInt("pos",params,0);
            val.add(pos,params.get(1));
            
            return self();
        }
    }

    class FunctionPush extends Function {
        public String getName() {
            return "push";
        }
        public String getShortDesc() {
            return "push(count,default?) - push values on stack, to be assigned in natural order";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() < 1 || params.size() > 2) {
                throw new Exception("Expected parameter count, default?");
            }
            int count=(int) getInt("count",params,0);
            
            Value defaultValue;
            if (params.size()==2) {
                defaultValue=params.get(1);
            } else {
                defaultValue=new ValueNull();
            }
            
            for (int i=count-1; i>=1; i--) {
                if (val.size() > i) {
                    ctx.push(val.get(i));
                } else {
                    ctx.push(defaultValue);
                }
            }
            // return first value
            if (val.size() > 0) {
                return val.get(0);
            } else {
                return defaultValue;
            }
        }
    }
    
    class FunctionEmpty extends Function {
        public String getName() {
            return "empty";
        }
        public String getShortDesc() {
            return "empty() - returns boolean";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (!argCount(params,0)) {
                throw new Exception("Expected no parameters");
            }
            return new ValueBoolean(val.size()==0);
        }
    }

    
    /*
     * The last() and first() functions differ from nth, int that return null if no elements
     */

    class FunctionLast extends Function {
        public String getName() {
            return "last";
        }
        public String getShortDesc() {
            return "last(count?) - returns last element or null if empty, or if count given: list of N last elements";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	
            if (params.size() == 0) {
            	if(val.size()==0) return new ValueNull();
            	return val.get(val.size()-1);
            } else if (params.size() == 1) {
            	int count=(int) getInt("count", params, 0);
            	if (val.size()==0) return new ValueList(new ArrayList<Value>());
            	
            	List<Value> result=new ArrayList<Value>();
            	int from=val.size()-count;
            	if (from < 0) from=0;
            	
            	for (int i=from; i<val.size(); i++) {
            		result.add(val.get(i));
            	}
            	return new ValueList(result);
            	
            } else {
            	throw new Exception("Expected optional count parameter");
            }
        }
    }
    

    


    class FunctionFirst extends Function {
        public String getName() {
            return "first";
        }
        public String getShortDesc() {
            return "first(count?) - returns first element or null if empty, or if count given: list of N first elements";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	
            if (params.size() == 0) {
            	if(val.size()==0) return new ValueNull();
            	return val.get(0);
            } else if (params.size() == 1) {
            	int count=(int) getInt("count", params, 0);
            	if (val.size()==0) return new ValueList(new ArrayList<Value>());
            	
            	List<Value> result=new ArrayList<Value>();
            	int to=count;
            	if (to>val.size()) to=val.size();
            	
            	for (int i=0; i<to; i++) {
            		result.add(val.get(i));
            	}
            	return new ValueList(result);
            	
            } else {
            	throw new Exception("Expected optional count parameter");
            }
        }
    }
    

    


}
