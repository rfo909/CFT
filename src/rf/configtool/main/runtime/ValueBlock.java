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
import rf.configtool.main.runtime.lib.ObjDict;
import rf.configtool.main.runtime.lib.ObjGrep;
import rf.configtool.main.runtime.reporttool.Report;

/**
 * Block of code - takes three different forms, but only one is available as
 * a data object, and that's the Lambda form.
 */
public class ValueBlock extends Value {
    
    private List<ProgramLine> programLines;
    private String synString;
    
    public ValueBlock (List<ProgramLine> programLines, String synString) {
        this.programLines=programLines;
        this.synString=synString;
        
        add(new FunctionCall());
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

    @Override
    public String synthesize() {
        return synString;
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
     * Call lambda, running in an isolated Ctx
     */
    public Value callLambda (Ctx ctx, List<Value> params) throws Exception {
    	return callLambda(ctx, new ObjDict(), params);
    }
    
    /**
     * Call lambda with "self" dictionary - this is a Closure feature. The
     * dictionary is stored in the "self" variable inside the lambda.
     */
    public Value callLambda (Ctx ctx, ObjDict dict, List<Value> params) throws Exception {
    	if (params==null) params=new ArrayList<Value>();
    	FunctionState fs=new FunctionState(params);
    	fs.set("self", new ValueObj(dict));
        Ctx independentCtx=new Ctx(ctx.getStdio(), ctx.getObjGlobal(), fs);
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


 

}
