/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2023 Roar Foshaug

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

import java.util.ArrayList;
import java.util.List;

import rf.configtool.main.CFTCallStackFrame;
import rf.configtool.main.Ctx;
import rf.configtool.main.FunctionState;
import rf.configtool.main.ReportData;
import rf.configtool.main.runtime.lib.ObjDict;
import rf.configtool.parsetree.CodeSpace;
import rf.configtool.util.ReportFormattingTool;

/**
 * Block of code - takes three different forms, but only one is available as
 * a data object, and that's the Lambda form.
 */
public class ValueBlock extends Value {
    
    private List<CodeSpace> programLines;
    
    public ValueBlock (List<CodeSpace> programLines) {
        this.programLines=programLines;
        
        add(new FunctionCall());
    }
    
    @Override
    public String getTypeName() {
        return "Callable";
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

    private Value executeLocalBlock (Ctx ctx) throws Exception {
        if (programLines.size() != 1) throw new Exception("Internal error: local block should contain ONE ProgramLine");
        programLines.get(0).execute(ctx);
        return ctx.getLocalBlockResult();
    }

    /**
     * Call Inner code block. It runs in sub-context, and inherits
     * lookup of as well parameters and variables. 
     */
    public Value callInnerBlock (Ctx callerCtx) throws Exception {
        // Execute as Inner code block, which means it has Ctx lookup up the Ctx stack, including
        // parameters to the function, but using the isInnerBlock flag for the contexts for each
        // progLine, to block loop trigger-propagation from interfering with the callerCtx.
        
        Value retVal=null;
        
        for (CodeSpace progLine:programLines) {
            
            Ctx sub=callerCtx.subNewData(true);  

            if (retVal != null) sub.push(retVal);
            
            progLine.execute(sub);
            
            ReportData reportData=sub.getReportData();

            // Column data is formatted to text and added to outData as String objects
            List<List<Value>> presentationValues=reportData.getReportPresentationValues();
            ReportFormattingTool report=new ReportFormattingTool();
            List<String> formattedText=report.formatDataValues(presentationValues);
            for (String s:formattedText) {
                sub.getOutData().out(new ValueString(s));
            }

            
            
            retVal=sub.getResult();
        }
        return retVal;
    }
    
    
    
    /**
     * Call Local code block.
     */
    public Value callLocalBlock (Ctx ctx) throws Exception {
        // Execute code as if it were statements in same context as outside the block. 
        return executeLocalBlock(ctx);
    }
    
    /**
     * Call lambda, running in an isolated Ctx
     */
    public Value callLambda (Ctx ctx, List<Value> params) throws Exception {
        CFTCallStackFrame caller=new CFTCallStackFrame("ValueBlock.callLambda()","Calling lambda");

        return callLambda(ctx, caller, new ObjDict(), params);
    }
    
    /**
     * Call lambda with "self" dictionary - this is a Closure feature. The
     * dictionary is stored in the "self" variable inside the lambda.
     */
    public Value callLambda (Ctx ctx, CFTCallStackFrame caller, ObjDict self, List<Value> params) throws Exception {
        if (params==null) params=new ArrayList<Value>();
        FunctionState fs=new FunctionState(null,params);
        fs.set("self", new ValueObj(self));
 
        ctx.getStdio().pushCFTCallStackFrame(caller);
        
        Ctx independentCtx=new Ctx(ctx.getStdio(), ctx.getObjGlobal(), fs);
        
        Value retVal=null;
        
        for (CodeSpace progLine:programLines) {
            // Note that independentCtx is created from scratch, so there is no parent 
            // and therefore no lookup from parent Ctx. 
            
            Ctx sub=independentCtx.subNewData(false);
            
            if (retVal != null) sub.push(retVal);
            
            progLine.execute(sub);
            
            ReportData reportData=sub.getReportData();

            // Column data is formatted to text and added to outData as String objects
            List<List<Value>> outData=reportData.getReportPresentationValues();
            ReportFormattingTool report=new ReportFormattingTool();
            List<String> formattedText=report.formatDataValues(outData);
            for (String s:formattedText) {
                sub.getOutData().out(new ValueString(s));
            }
            retVal=sub.getResult();
        }

        ctx.getStdio().popCFTCallStackFrame(caller);
        return retVal;
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
