/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2025 Roar Foshaug

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

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import rf.configtool.lexer.Lexer;
import rf.configtool.lexer.SourceLocation;
import rf.configtool.lexer.TokenStream;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueNull;
import rf.configtool.parsetree.Expr;
import rf.configtool.parsetree.CodeSpace;
import rf.configtool.parsetree.Stmt;

/**
 * Ctx means context and is a collection of objects required to execute CFT code.
 */
public class Ctx {
    
    private Ctx parent;
    
    private String loopVariableName;
    private Value loopVariableValue;
    
    private FunctionState functionState;

    private OutData outData;

    private boolean programContainsLooping = false;
    private boolean isInnerBlock=false;

    
    private Stack<Value> stack=new Stack<Value>();
    private Stdio stdio;
    private ObjGlobal objGlobal;
    
    private boolean abortIterationFlag; // "next"
    private boolean breakLoopFlag;  // "break"
    
    private List<CtxCloseHook> ctxCloseHooks=new ArrayList<CtxCloseHook>();
    

    public Ctx(Stdio stdio, ObjGlobal objGlobal, FunctionState functionState) {
        this(null, new OutData(), stdio, objGlobal, functionState);
    }
    
    private Ctx (Ctx parent, OutData outData,  Stdio stdio, ObjGlobal objGlobal, FunctionState functionState) {
        if (functionState==null) functionState=new FunctionState(null,null);
        
        this.parent=parent;
        this.outData=outData;
        this.stdio=stdio;
        this.objGlobal=objGlobal;
        this.functionState=functionState;
        
    }
    
    public Stdio getStdio() {
        return stdio;
    }

    public Ctx sub() {
        return new Ctx(this,outData,stdio,objGlobal,functionState);
    }
              
    public Ctx subNewData(boolean isInnerBlock) {
        Ctx ctx = new Ctx(this,new OutData(),stdio,objGlobal,functionState);
        ctx.isInnerBlock=isInnerBlock;
        return ctx;
    }

    
    public boolean isInnerBlock() {
        return isInnerBlock;
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
    
    public void debug (String s) {
        objGlobal.addSystemMessage("[debug] " + s);
    }
    
    
    public void addSystemMessage (String s) {
        objGlobal.addSystemMessage(s);
    }
    
    public void addCtxCloseHook (CtxCloseHook callback) {
        ctxCloseHooks.add(callback);
    }
    
      
    /**
     * Called from StmtIterate and StmtLoop.
     */
    public void setProgramContainsLooping() {
        Ctx ctx=this;
        for(;;) {
            ctx.programContainsLooping=true;
            if (ctx.isInnerBlock) break;
            if (ctx.parent == null) break;
            ctx=ctx.parent;
        }
        
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
        return new ValueNull();
    }
    
    
    public Value getLocalBlockResult() throws Exception {
        callCtxCloseHooks();
        if (stack.isEmpty()) return new ValueNull(); else return stack.pop();
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
        if (!isInnerBlock) {
            if (!programContainsLooping && parent != null) parent.setAbortIterationFlag();
        }
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
        
        // Lambdas are run on clean Ctx objects, and for local as well as Inner
        // blocks, full lookup is a feature all the way up the the function
        // or inside of lambda, so no special handling here.
        
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
        

    public ObjGlobal getObjGlobal() {
        return objGlobal;
    }
    
    public FunctionState getFunctionState() {
        return functionState;
    }
    
    
    // Utility method
    /**
     * Resolve Code Space (sequence of statements) on string format - does not support PIPE as it 
     * breaks a code body into multiple code spaces.
     */
    public Value resolveCodeSpaceString (String s) throws Exception {
        Lexer p=new Lexer();
        p.processLine(new ScriptSourceLine(new SourceLocation(), s));
        
        TokenStream ts = p.getTokenStream();
        CodeSpace codeSpace=new CodeSpace(ts);
        if (!ts.atEOF()) throw new Exception(ts.error("Invalid token - expected EOF"));
        
        Ctx ctx=this.sub();
        codeSpace.execute(ctx);
        return ctx.getResult();
    }
}
