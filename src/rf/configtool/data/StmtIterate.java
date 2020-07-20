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

package rf.configtool.data;

import rf.configtool.main.CodeLines;
import rf.configtool.main.Ctx;
import rf.configtool.main.Runtime;
import rf.configtool.main.runtime.*;
import rf.configtool.parser.TokenStream;
import java.util.*;

public class StmtIterate extends Stmt {
    
    private String loopVariable;
    private List<Stmt> body=new ArrayList<Stmt>();
    
    public StmtIterate (TokenStream ts) throws Exception {
        super(ts);
        
        ts.matchStr("->", "expected '->'");
        loopVariable=ts.matchIdentifier("expected loop variable name");
        
        while (!ts.atEOF() && !ts.peekStr(CodeLines.PROGRAM_LINE_SEPARATOR) && !ts.peekStr("}")) {
            body.add(Stmt.parse(ts));
        }
        
    }
    
    public void execute (Ctx ctx) throws Exception {
        ctx.setProgramContainsLooping();
        
        Value v=ctx.pop();
        if (v==null) return; // No value occurs if a program line executes no out() and leaves nothing on the stack
        
        List<Value> data;
        
        
        if (v instanceof ValueList) {
            // iterate over all values in list
            data=((ValueList) v).getVal();
        } else {
        	if (!v.getValAsBoolean()) {
                // no iteration 
                return;
            }
            // otherwise iterate over that one value only
            data=new ArrayList<Value>();
            data.add(v);
        }
        
    
        OUTER: for (Value currVal:data) {
            Ctx sub=ctx.sub();
            sub.setLoopVariable(loopVariable, currVal);
            for (Stmt stmt:body) {
            	ctx.debug(stmt);
            	
                stmt.execute(sub);
                if (sub.hasBreakLoopFlag()) {
                    break OUTER;
                }
                if (sub.hasAbortIterationFlag()) {
                    // ctx.outln("Aborting '" + loopVariable + "' for value " + currVal.getContentDescription());
                    continue OUTER;
                }
            }

        }
    }

    

}
