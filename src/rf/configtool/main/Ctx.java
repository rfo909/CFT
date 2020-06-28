package rf.configtool.main;

import java.util.*;

import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueList;

public class Ctx {
    
    private Ctx parent;
    //private HashMap<String,Value> localVariables=new HashMap<String,Value>();
    private String loopVariableName;
    private Value loopVariableValue;
    
    private FunctionState functionState;

    private OutData outData;
    private OutText outText;
    
    private Stack<Value> stack=new Stack<Value>();
    private ObjGlobal objGlobal;
    
    private boolean abortIterationFlag; // "next"
    private boolean breakLoopFlag;  // "break"
        

    public Ctx(ObjGlobal objGlobal, FunctionState functionState) {
        this(null, new OutData(), new OutText(), objGlobal, functionState);
    }
    
    private Ctx (Ctx parent, OutData outData,  OutText outText, ObjGlobal objGlobal, FunctionState functionState) {
        if (functionState==null) functionState=new FunctionState();
        
        this.parent=parent;
        this.outData=outData;
        this.outText=outText;
        this.objGlobal=objGlobal;
        this.functionState=functionState;
        
    }
    
    public Stdio getStdio() {
        return objGlobal.getStdio();
    }

    public Ctx sub() {
        return new Ctx(this,outData,outText,objGlobal,functionState);
    }
       
    public Ctx sub(List<Value> params) {
        return new Ctx(this,outData,outText,objGlobal,functionState.sub(params));
    }
       
    public void outln (String s) {
        objGlobal.outln(s);
    }
    
    public void outln () {
        objGlobal.outln();
    }
    
   public Value getResult() {
        // if program contains looping, then always return data from out(), even
        // if empty
        if (outData.programContainsLooping()) {
            return new ValueList(outData.getOutData());
        }
        
        // otherwise return top element on stack
        if (!stack.isEmpty()) {
            return stack.pop();
        }
        // no return value
        return null;
    }
    

    
    /**
     * Aborts current Ctx
     */
    public void setAbortIterationFlag() {
        abortIterationFlag=true;
    }
    
    public boolean hasAbortIterationFlag() {
        return abortIterationFlag;
    }
    
    public void setBreakLoopFlag() {
        breakLoopFlag=true;
    }
    
    public boolean hasBreakLoopFlag() {
        return breakLoopFlag;
    }
    
    public void setLoopVariable (String name, Value value) {
    	this.loopVariableName=name;
        this.loopVariableValue=value;
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
