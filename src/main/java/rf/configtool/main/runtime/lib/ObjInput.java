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

package rf.configtool.main.runtime.lib;

import java.util.ArrayList;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.Stdio;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueNull;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;

/**
 * Persistent object within a session, the persistence part is managed by the global function instantiating
 * this class, "Input", as defined in ObjGlobal, using the internal method getOrAddPersistentObject().
 * Used to ask for input, and remember this to the next time, so that Enter uses the previous value. 
 */
public class ObjInput extends ObjPersistent {
    
    private String label;
    private String currValue;
    
    
    private List<String> uniqueValues=new ArrayList<String>();
    
    public ObjInput (String label) {
        this.label=label;
        this.currValue="";
        
        add(new FunctionGet());
        add(new FunctionSetCurr());
        add(new FunctionClear());
        add(new FunctionGetHistory());
        add(new FunctionSetHistory());
        add(new FunctionGetCurr());
        add(new FunctionSetCurrCond());
    }
    
    public ObjInput self() {
        return this;
    }
    

    @Override 
    public String getPersistenceId() {
        return "Input: " + label;
    }
    


    @Override
    public boolean eq(Obj x) {
        return x==this;
    }

    @Override
    public String getTypeName() {
        return "Input";
    }
    
    @Override
    public ColList getContentDescription() {
        return ColList.list().regular("Input: " + label);
    }
        
    private synchronized void showOptions(Ctx ctx) {
        Stdio stdio=ctx.getStdio();
        
        for (int i=0; i<uniqueValues.size(); i++) {
            stdio.println("  :" + i + (i<10?" ":"") + "  ---> " + uniqueValues.get(i));
        }
    }
    
    private synchronized void addUnique (String s) {
        if (s=="") return; // ignore empty string, as this is handled via :E
        // add last used value to end of list
        uniqueValues.remove(s);
        uniqueValues.add(s);
        // keep list length under control
        while (uniqueValues.size() > 10) uniqueValues.remove(0);
    }
    
    private synchronized String getOption (String pos) {
        try {
            int x=Integer.parseInt(pos.trim());
            if (x<0 || x>=uniqueValues.size()) return null;
            return uniqueValues.get(x);
        } catch (Exception ex) {
            return null;
        }
    }

    class FunctionGet extends Function {
        public String getName() {
            return "get";
        }
        public String getShortDesc() {
            return "get() - get value for question as given when creating Input object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            Stdio stdio=ctx.getStdio();
            
            if (params.size()!=0) throw new Exception("Expected no parameters");

            boolean silent=ctx.getStdio().hasBufferedInputLines();
            if (silent) {
                String line=ctx.getStdio().getInputLine();
                if (line.trim().length()>0) {
                    currValue=line;
                }
                addUnique(currValue);
                return new ValueString(currValue);
            }

            // ':'  - show options
            // '::sdfsdf' - match text that starts with colon
            // ':xxx' - identify option
            LOOP: for(;;) {
                stdio.println("(?) " + label);
                if (currValue.trim().length()>0) stdio.println("    Enter for '" + currValue + "'");
                if (uniqueValues.size() > 0) stdio.println("    ':' for options");
                String line=ctx.getStdio().getInputLine();
                if (line.trim().length()==0) {
                    if (currValue==null) currValue="";
                    stdio.println(currValue);
                    stdio.println();
                    break LOOP;
                } else if (line.trim().startsWith("::")) {
                    currValue=line.substring(1); 
                    break;
                } else if (line.trim().equals(":")) {
                    stdio.println("--------------------------------------------------------------");
                    stdio.println("Enter ':N' for numbered value, :E for empty string,");
                    stdio.println("or enter text, using '::' for input text starting with colon.");
                    stdio.println();
                    showOptions(ctx);
                    stdio.println("--------------------------------------------------------------");
                } else if (line.startsWith(":")) {
                    if (line.equals(":e") || line.equals(":E")) {
                        currValue="";
                        break LOOP;
                    }
                    String pos=line.substring(1);
                    String x=getOption(pos);
                    if (x==null) {
                        stdio.println("** Invalid reference");
                        continue;
                    }
                    stdio.println(x);  // show selected value
                    stdio.println();
                    currValue=x;
                    break LOOP;
                } else {
                    currValue=line;
                    break LOOP;
                }
            }
            addUnique(currValue);
            return new ValueString(currValue);
        }
    }

    
    class FunctionSetCurr extends Function {
        public String getName() {
            return "setCurr";
        }
        public String getShortDesc() {
            return "setCurr(str) - set current value (which is default when pressing ENTER in get) - returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size()!=1) throw new Exception("Expected String value");
            currValue=getString("str", params, 0);
            return new ValueObj(self());
        }
    }

    class FunctionClear extends Function {
        public String getName() {
            return "clear";
        }
        public String getShortDesc() {
            return "clear() - clear history and curr - returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size()!=0) throw new Exception("Expected no params");
            uniqueValues.clear();
            currValue=null;
            return new ValueObj(self());
        }
    }
    
    
    class FunctionGetHistory extends Function {
        public String getName() {
            return "getHistory";
        }
        public String getShortDesc() {
            return "getHistory() - returns list of string in history";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size()!=0) throw new Exception("Expected no params");
            List<Value> list=new ArrayList<Value>();
            for (String s:uniqueValues) {
                list.add(new ValueString(s));
            }
            return new ValueList(list);
        }
    }

    class FunctionSetHistory extends Function {
        public String getName() {
            return "setHistory";
        }
        public String getShortDesc() {
            return "setHistory(list) - set history - does not affect current value - returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size()!=1) throw new Exception("Expected list parameter");
            List<Value> list=getList("list", params, 0);
            uniqueValues.clear();
            for (Value v:list) {
                uniqueValues.add(v.getValAsString());
            }
            return new ValueObj(self());
        }
    }
    
    class FunctionGetCurr extends Function {
        public String getName() {
            return "getCurr";
        }
        public String getShortDesc() {
            return "getCurr() - get current value or null if not set";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            if (currValue.equals("")) return new ValueNull();
            return new ValueString(currValue);
        }
    }
    
    class FunctionSetCurrCond extends Function {
        public String getName() {
            return "setCurrCond";
        }
        public String getShortDesc() {
            return "setCurrCond(str) - set current value if no value exists - returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size()!=1) throw new Exception("Expected String value");
            if (currValue.equals("")) {
                currValue=getString("str", params, 0);
            }
            return new ValueObj(self());
        }
    }
    

}
