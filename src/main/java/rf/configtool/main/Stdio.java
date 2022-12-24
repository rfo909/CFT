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

package rf.configtool.main;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueString;


/**
 * 2020-11 RFO
 * 
 * In order to virtualize Stdio for multiple threads, created via StmtSpawn, we need
 * to separate real Stdio from virtual Stdio. The real Stdio is available via the
 * ObjGlobal objects, and for all code running in the foreground. 
 * 
 * StmtSpawn creates a Process object, with a virtual Stdio. The code does not know
 * the difference, as the virtual Stdio also received an input stream, it's just
 * that it is a pipe stored in the Thread object. 
 * 
 * This lets us send data to it one line at a time. The virtual Stdio object also buffers
 * output internally. Since care has been taken to only communicate lines (never partial
 * lines) formatting should be good.
 */
public abstract class Stdio {
    

    private List<String> bufferedInputLines=new ArrayList<String>();
    private BufferedReader stdin;
    
     public Stdio (BufferedReader stdin) {
        this.stdin=stdin;
     }
     
     // -------------------------------------
     // The CFT Callstack is maintained here, because the Stdio objects 
     // exactly cover the scope of where we want to collect callstack,
     // as it is instantiated only two places, once for StdioReal in Main and
     // once for StdioVirtual in ObjProcess.
     //
     // Perhaps ideally we would like another name, for an object that contains
     // the Stdio, but for now this will have to do.
     // -------------------------------------
  
     private Stack<CFTCallStackFrame> cftCallStack=new Stack<CFTCallStackFrame>();
     
     /**
      * The idea here is that we let Java methods that represent calling functions, lambdas
      * or closures, require a parameter CFTCallStackFrame, which is the caller representing
      * its' source location. The method that calls CFT functionality manages pushing the
      * caller stack frame on the CFT call stack, doing the CFT function/closure/lambda invocation,
      * then popping the stack frame
      *
      * This narrows down the number of code locations where we need to match push/pop against
      * the CFT call stack, and lets the Java compiler aid us in locating dependencies without
      * adding complexity of tens of locations where we must carefully match push with pop.
      */
     public void pushCFTCallStackFrame (CFTCallStackFrame caller) {
         cftCallStack.push(caller);
     }
     
     /**
      * Doing hard checks to ensure that the caller popped from the stack matches the top element of
      * the stack, to avoid call stack mis-alignment issues, which would be very hard to debug.
      */
     public void popCFTCallStackFrame (CFTCallStackFrame caller) throws Exception {
         if (cftCallStack.isEmpty()) throw new RuntimeException("popCFTCallStackFrame: callStack underflow, expected to pop off " + caller.toString());
         if (cftCallStack.peek() != caller) {
             throw new RuntimeException("popCFTCallStackFrame: expected to pop off " + caller.toString() + " found " + 
                     cftCallStack.peek().toString());
         }
         cftCallStack.pop();
     }
     
     /**
      * Pop CFT call stack frames from top of stack down to but not including
      * target frame (used by tryCatch()). If the aboveTarget is null, it means
      * the stack was empty when getTopCFTCallStackFrame was called, and so the
      * whole stack is returned (and cleared).
      */
     public List<CFTCallStackFrame> getAndClearCFTCallStack (CFTCallStackFrame aboveTarget) {
         List<CFTCallStackFrame> result=new ArrayList<CFTCallStackFrame>();
         while (!cftCallStack.isEmpty()) {
             if (aboveTarget != null && cftCallStack.peek()==aboveTarget) break;
             result.add(cftCallStack.pop());
         }
         return result;
     }
     
     /**
      * Utility method, combining CFT stack trace and any debug lines within each
      * stack frame, as ValueList object (of strings)
      */
     public ValueList getCFTStackTrace (CFTCallStackFrame oldTop) {
      List<Value> result=new ArrayList<Value>();
      List<CFTCallStackFrame> frames = getAndClearCFTCallStack(oldTop);
      for (CFTCallStackFrame frame:frames) {
        result.add(new ValueString(frame.toString()));
        for (String line:frame.getDebugLines()) {
            result.add(new ValueString("   debug: " + line));
        }
      }
      return new ValueList(result);
     }
     

     /**
      * Utility method, combining CFT stack trace and any debug lines within each
      * stack frame, as ValueList object (of strings)
      */
     public ValueList peekFullCFTStackTrace () {
      List<Value> result=new ArrayList<Value>();
      
      for (CFTCallStackFrame frame : cftCallStack) {
        result.add(new ValueString(frame.toString()));
        for (String line:frame.getDebugLines()) {
            result.add(new ValueString("   debug: " + line));
        }
      }
      return new ValueList(result);
     }
     

     public void showAndClearCFTCallStack () {
         while (!cftCallStack.isEmpty()) {
             CFTCallStackFrame x=cftCallStack.pop();
             println("  called from: " + x.toString());
             for (String line:x.getDebugLines()) {
                 println("      debug: " + line);
             }
         }
     }
     

     public void addDebug (String line) {
         if (!cftCallStack.isEmpty()) {
             cftCallStack.peek().addDebugLine(line);
         }
     }
     
 
     public CFTCallStackFrame getTopCFTCallStackFrame () {
         if (cftCallStack.isEmpty()) return null;
         return cftCallStack.peek();
     }
     
     public void clearCFTCallStack() {
         cftCallStack.clear();
     }

     
    
     
     
     // NOTE on synchronized:
     //
     // Using synchronized on input methods is dangerous, as they
     // may block, making the Stdio object incommunicable.
     
     
    
    /**
     * Used by the stdin() statement
     */
    
    public String getInputLine() throws Exception {
        synchronized(bufferedInputLines) {
            if (!bufferedInputLines.isEmpty()) {
                return bufferedInputLines.remove(0);
            }
        }
        return readLine();
    }
    
    private String readLine() throws Exception {
        if (stdin == null) throw new Exception("stdin disconnected - no buffered lines");
        return stdin.readLine();
    }
    
    
    public boolean hasBufferedInputLines() {
        synchronized(bufferedInputLines) {
            return bufferedInputLines.size()>0;
        }
    }

    /**
     * Used by the stdin() statement
     */
    public void addBufferedInputLine (String s) {
        synchronized (bufferedInputLines) {
            bufferedInputLines.add(s);
        }
    }
    
    public void clearBufferedInputLines() {
        synchronized (bufferedInputLines) {
            bufferedInputLines.clear();
        }
    }
    
    public int getCachedInputLineCount() {
        synchronized (bufferedInputLines) {
            return bufferedInputLines.size();
        }
    }
   
    /**
     * Output text
     */
   
    public abstract void println (String s);
    
    public void println () {
        println("");
    }
}
