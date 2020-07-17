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
 * A macro is by default a local code block that is executed immediately, and is a way of grouping
 * multiple statements and expressions as a single expression. But it can also be created as a
 * stand-alone object, that is invoked via .call(params). It then runs in an independent context.
 */
public class ValueMacro extends Value {
    
    private List<ProgramLine> programLines;
    
    public ValueMacro (List<ProgramLine> programLines) {
        this.programLines=programLines;
        
        add(new FunctionCall());
    }
    
    @Override
    public String getTypeName() {
        return "macro";
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

    
    private Value invoke (Ctx ctxMacro) throws Exception {
    	Value retVal=null;
        
        for (ProgramLine progLine:programLines) {
            Ctx sub=ctxMacro.sub();
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
     * Call local macro ("in line code block"). It runs in sub-context, and inherits
     * lookup of as well parameters and variables. 
     */
    public Value callLocalMacro (Ctx ctx) throws Exception {
        // Execute local macro, which means it has Ctx lookup up the Ctx stack, including
        // parameters to the function, but that the loop flag stops
        Ctx sub=ctx.subContextForCodeBlock(); 
        return invoke(sub);
    }
    
    
    /**
     * Call independent macro which runs in an isolated Ctx
     */
    public Value call (Ctx ctx, List<Value> params) throws Exception {
        Ctx sub=new Ctx(ctx.getObjGlobal(), new FunctionState(params));
        return invoke(sub);
    }
    
    // -----------------------------------------------------------
    
    class FunctionCall extends Function {
        public String getName() {
            return "call";
        }
        public String getShortDesc() {
            return "call(...) - call macro with parameters";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            return call(ctx,params);
        }
    }



}
