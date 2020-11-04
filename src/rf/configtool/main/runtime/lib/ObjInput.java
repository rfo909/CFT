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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.Stdio;
import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.*;

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
        	stdio.println("" + (i<10?" ":"") + i + "  : " + uniqueValues.get(i));
        }
    }
    
    private synchronized void addUnique (String s) {
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
                if (currValue.trim().length()>0) stdio.println("    enter for '" + currValue + "'");
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
                    showOptions(ctx);
                    stdio.println("--");
                    stdio.println("Use ':N' for numbered value. Use '::' for input text starting with colonl");
                    stdio.println("--");
                } else if (line.startsWith(":")) {
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


}
