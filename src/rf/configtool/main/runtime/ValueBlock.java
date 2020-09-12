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

import rf.configtool.data.ProgramLine;
import rf.configtool.data.Stmt;
import rf.configtool.main.Ctx;
import rf.configtool.main.FunctionState;
import rf.configtool.main.ObjGlobal;
import rf.configtool.main.OutData;
import rf.configtool.main.OutText;
import rf.configtool.main.runtime.lib.ObjGrep;
import rf.configtool.main.runtime.reporttool.Report;

/**
 * Block of code
 */
public class ValueBlock extends Value {
    
    private List<ProgramLine> programLines;
    
    public ValueBlock (List<ProgramLine> programLines) {
        this.programLines=programLines;
        
        add(new FunctionCall());
        add(new FunctionIsLambda());
    }
    
    @Override
    public String getTypeName() {
        return "Lambda";
    }


    @Override
    public String getValAsString() {
        return "{}";
    }
    
    
    @Override
    public boolean eq(Obj v) {
        if (v==this) return true;
        return false;
    }

    @Override
    public boolean getValAsBoolean() {
        return true;
    }

    
    private Value execute (Ctx ctx) throws Exception {
    	Value retVal=null;
        
        for (ProgramLine progLine:programLines) {
            Ctx sub=ctx.sub();
            
            if (retVal != null) sub.push(retVal);
            
            progLine.execute(sub);
            
            OutText outText=sub.getOutText();

            // Column data is formatted to text and added to outData as String objects
            List<List<Value>> outData=outText.getData();
            Report report=new Report();
            List<String> formattedText=report.formatDataValues(outData);
            for (String s:formattedText) {
                sub.getOutData().out(new ValueString(s));
            }

            
            
            retVal=sub.getResult();
        }
        return retVal;
    }

    /**
     * Call Inner code block. It runs in sub-context, and inherits
     * lookup of as well parameters and variables. 
     */
    public Value callInnerBlock (Ctx ctx) throws Exception {
        // Execute as Inner code block, which means it has Ctx lookup up the Ctx stack, including
        // parameters to the function, but that the loop flag stops
        Ctx sub=ctx.subContextForInnerBlock(); 
        return execute(sub);
    }
    
    
    
    /**
     * Call Local code block.
     */
    public Value callLocalBlock (Ctx ctx) throws Exception {
    	// Execute code as if it were statements in same context as outside the block. 
        return execute(ctx);
    }
    
    
    /**
     * Call independent macro which runs in an isolated Ctx
     */
    public Value callLambda (Ctx ctx, List<Value> params) throws Exception {
    	if (params==null) params=new ArrayList<Value>();
        Ctx independentCtx=new Ctx(ctx.getObjGlobal(), new FunctionState(params));
        return execute(independentCtx);
    }
    
    // -----------------------------------------------------------
    
    class FunctionCall extends Function {
        public String getName() {
            return "call";
        }
        public String getShortDesc() {
            return "call(...) - call lambda with parameters";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            return callLambda(ctx,params);
        }
    }


    class FunctionIsLambda extends Function {
        public String getName() {
            return "isLambda";
        }
        public String getShortDesc() {
            return "isLambda() - returns true";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            return new ValueBoolean(true);
        }
    }



}
