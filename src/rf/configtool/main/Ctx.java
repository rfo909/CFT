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

package rf.configtool.main;

import java.util.*;

import rf.configtool.data.Expr;
import rf.configtool.data.Stmt;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.parser.SourceLocation;

/**
 * Ctx means context and is a collection of objects required to execute CFT code.
 */
public class Ctx {
    
    private Ctx parent;
    private String loopVariableName;
    private Value loopVariableValue;
    
    private FunctionState functionState;

    private OutData outData;
    private OutText outText;
    
    // These two needed to support inline code-blocks ("macros")
    
    private boolean programContainsLooping = false;
    private boolean programContainsLoopingInfoStopsHere=false;

    
    private Stack<Value> stack=new Stack<Value>();
    private Stdio stdio;
    private ObjGlobal objGlobal;
    
    private boolean abortIterationFlag; // "next"
    private boolean breakLoopFlag;  // "break"
    
    private List<CtxCloseHook> ctxCloseHooks=new ArrayList<CtxCloseHook>();
    

    public Ctx(Stdio stdio, ObjGlobal objGlobal, FunctionState functionState) {
        this(null, new OutData(), new OutText(), stdio, objGlobal, functionState);
    }
    
    private Ctx (Ctx parent, OutData outData,  OutText outText, Stdio stdio, ObjGlobal objGlobal, FunctionState functionState) {
        if (functionState==null) functionState=new FunctionState();
        
        this.parent=parent;
        this.outData=outData;
        this.outText=outText;
        this.stdio=stdio;
        this.objGlobal=objGlobal;
        this.functionState=functionState;
        
    }
    
    public Stdio getStdio() {
        return stdio;
    }

    public Ctx sub() {
        return new Ctx(this,outData,outText,stdio,objGlobal,functionState);
    }
              
    /**
     * An Inner code block is a code block that is invoked immediately, and that has access up the
     * Ctx stack, but with separate OutData object, to avoid mixing result data from environment.
     * 
     * It also setting the programmingContainsLoopingInfoStopsHere flag, which means propagation up the
     * chain of ctx objects when a loop is executed, stops here. That propagation is the one which
     * in turn decides the output result being the list from out() or report() - if there is a loop,
     * or the topmost element - if no loop.
     * 
     * This makes an Inner block something between local blocks, and Lambdas. Local blocks run all 
     * statements in a simple sub-context from the Ctx outside the block, while lambdas run in
     * a separate environment unrelated to the caller, in the same way as functions do, with function
     * parameters as well.
     */
    public Ctx subContextForInnerBlock () {
        Ctx ctx = new Ctx(this,new OutData(),outText,stdio,objGlobal,functionState);
        ctx.programContainsLoopingInfoStopsHere=true;
        return ctx;
    }
    
    
    public boolean isDebugMode() {
    	return objGlobal.isDebugMode();
    }
    
    public void debug (Stmt stmt) {
    	if (isDebugMode()) {
    		objGlobal.debug("stmt " + stmt.getSourceLocation().toString());
    	}
    }
    
    public void debug (Expr expr) {
    	if (isDebugMode()) {
    		objGlobal.debug("expr " + expr.getSourceLocation().toString());
    	}
    }
    
    public void addSystemMessage (String s) {
    	objGlobal.addSystemMessage(s);
    }
    
    public void addCtxCloseHook (CtxCloseHook callback) {
    	ctxCloseHooks.add(callback);
    }
    
      
    /**
     * Called from StmtIterate and StmtLoop. Could have used the occurrence of loop variables (which are
     * stored in the Ctx instances) if it wasn't for the "loop" statement, which has no loop variable.
     */
    public void setProgramContainsLooping() {
        // Previously this was implemented as part of the OutData object which made it in effect
        // global.
        Ctx ctx=this;
        for(;;) {
            ctx.programContainsLooping=true;
            if (ctx.programContainsLoopingInfoStopsHere) break;
            if (ctx.parent == null) break;
            ctx=ctx.parent;
        }
        
    }
    

              
    public void outln (String s) {
        objGlobal.outln(s);
    }
    
    public void outln () {
        objGlobal.outln();
    }
    
    public Value getResult() throws Exception {
    	callCtxCloseHooks();
    	
        // if program contains looping, then always return data from out(), even
        // if empty
        if (programContainsLooping) {
            return new ValueList(outData.getOutData());
        }
        
        // otherwise return top element on stack
        if (!stack.isEmpty()) {
            return stack.pop();
        }
        // no return value
        return null;
    }
    
    
    private void callCtxCloseHooks() throws Exception {
    	for (CtxCloseHook x: ctxCloseHooks) {
    		x.ctxClosing(this);
    	}
    	ctxCloseHooks.clear();
    }

    
    /**
     * Aborts current Ctx
     */
    public void setAbortIterationFlag() {
        abortIterationFlag=true;
        if (!programContainsLooping && parent != null) parent.setAbortIterationFlag();
    }
    
    public boolean hasAbortIterationFlag() {
        return abortIterationFlag;
    }
    
    public void setBreakLoopFlag() {
        breakLoopFlag=true;
        if (!programContainsLooping && parent != null) parent.setBreakLoopFlag();
    }
    
    public boolean hasBreakLoopFlag() {
        return breakLoopFlag;
    }
    
    public void setLoopVariable (String name, Value value) {
        this.loopVariableName=name;
        this.loopVariableValue=value;
    }
    
    public boolean isLoopVariable (String name) {
    	return getLoopVariable(name) != null;
    }
    
    public Value getVariable (String name) {
        Value v=getLoopVariable(name);
        if (v != null) return v;
        
        return functionState.get(name);
    }
    
    private Value getLoopVariable (String name) {
        // first we traverse localVariables up all Ctx, since these will
        // contain loop variables, then we check with FunctionState, where
        // all assigned variables ("=x") are stored in shared scope for the
        // function. 
        
        // This also means one can not redefine loop variables.
        
        if (loopVariableName != null && name.equals(loopVariableName)) {
            return loopVariableValue;
        }
        if (parent != null) return parent.getLoopVariable(name);
        return null;
    }
    
    public Value pop() {
        if (stack.isEmpty()) return null;
        return stack.pop();
    }
    
    public void push (Value v) {
        stack.push(v);
    }

    public OutData getOutData() {
        return outData;
    }
        
    public OutText getOutText() {
        return outText;
    }
    
    public ObjGlobal getObjGlobal() {
        return objGlobal;
    }
    
    public FunctionState getFunctionState() {
        return functionState;
    }
}
