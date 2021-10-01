package rf.configtool.main.runtime.lib;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import rf.configtool.lexer.CharSource;
import rf.configtool.lexer.Lexer;
import rf.configtool.lexer.SourceLocation;
import rf.configtool.lexer.TokenStream;
import rf.configtool.main.CodeLine;
import rf.configtool.main.Ctx;
import rf.configtool.main.FunctionState;
import rf.configtool.main.Stdio;
import rf.configtool.main.StdioVirtual;
import rf.configtool.main.runtime.*;
import rf.configtool.main.runtime.lib.ObjExtProcess.FunctionDestroy;
import rf.configtool.main.runtime.lib.ObjExtProcess.FunctionExitCode;
import rf.configtool.main.runtime.lib.ObjExtProcess.FunctionIsAlive;
import rf.configtool.parsetree.Expr;
import rf.configtool.parsetree.ProgramLine;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

/**
 * A process represents a thread executing CFT code with virtualized Stdio, available via 
 * functions in this Process object.
 */
public class ObjProcess extends Obj {

    // Ensure exitValue is not read and updated at the same time
    private final Object EXIT_LOCK = "EXIT_LOCK";
    
    private ObjDict dict;
    private Expr expr;
    
    private StdioVirtual stdioVirtual;
    private PrintStream processInput;
    
    private Value exitValue;

    public ObjProcess(ObjDict dict, Expr expr) {
        this.dict=dict;
        this.expr = expr;
        
        this.add(new FunctionOutput());
        this.add(new FunctionSendLine());
        this.add(new FunctionClose());
        this.add(new FunctionIsAlive());
        this.add(new FunctionIsDone());
        this.add(new FunctionExitValue());
        this.add(new FunctionWait());
        this.add(new FunctionData());

    }

    protected void setExitValue (Value exitValue) {
        synchronized(EXIT_LOCK) {
            this.exitValue=exitValue;
        }
    }
    
    public void start(Ctx ctx) throws Exception {
        PipedOutputStream out;
        PipedInputStream in;

        in = new PipedInputStream();
        out = new PipedOutputStream(in);
        
        BufferedReader br=new BufferedReader(new InputStreamReader(in));
        this.stdioVirtual = new StdioVirtual(br);
        this.processInput=new PrintStream(out);
        
        // Creating empty function state to be populated with local variables matching
        // the Dictionary content
        FunctionState functionState = new FunctionState();
        
        // Each Dict value must be run through an eval(syn(value)) pipeline, to make them
        // guaranteed independent of all other internal structures in CFT. 
        Iterator<String> keys = dict.getKeys();
        while (keys.hasNext()) {
            String key=keys.next();
            Value value=dict.getValue(key);
            
            // Make copies of all context values from dict parameter
            Value transformedValue=value.createClone(ctx);
            functionState.set(key, transformedValue);
        }

        Ctx processCtx = new Ctx(stdioVirtual, ctx.getObjGlobal(), functionState);

        Runner runner = new Runner(processCtx, expr, this);
        (new Thread(runner)).start();

    }

    @Override
    public boolean eq(Obj x) {
        return x == this;
    }

    public String getTypeName() {
        return "Process";
    }

    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    private String getDesc() {
        return "Process";
    }
    
    public List<String> getAndClearOutput() {
        return stdioVirtual.getAndClearOutputBuffer();
    }
    
    public void sendLine (String line) throws Exception {
        processInput.println(line);
    }

    public void close() throws Exception {
        processInput.close();
    }
    

    private Obj self() {
        return this;
    }

    class Runner implements Runnable {
        private Ctx ctx;
        private Expr expr;
        private ObjProcess process;

        public Runner(Ctx ctx, Expr expr, ObjProcess process) {
            this.ctx = ctx;
            this.expr = expr;
            this.process = process;
        }

        public void run() {
            try {
                Value v = expr.resolve(ctx);
                process.setExitValue(v);
            } catch (Exception ex) {
                // Generating full exception log to virtual stdout if process fails
                Throwable t=ex;
                Stdio stdio = ctx.getStdio();
                stdio.println("Process fails with Exception: " + t.getMessage());
//              while (t != null) {
//                  stdio.println("Process fails with Exception: " + t.getMessage());
//                  for (StackTraceElement line : t.getStackTrace()) {
//                      stdio.println("   " + line.toString());
//                  }
//                  t=t.getCause();
//                  if (t != null ) {
//                      stdio.println("Caused by:");
//                  }
//              }
                process.setExitValue(new ValueNull());
            }
        }
    }

    
    class FunctionOutput extends Function {
        public String getName() {
            return "output";
        }

        public String getShortDesc() {
            return "output() - get buffered output lines";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            List<String> output = stdioVirtual.getAndClearOutputBuffer();
            List<Value> result = new ArrayList<Value>();
            for (String s : output)
                result.add(new ValueString(s));
            return new ValueList(result);
        }
    }

    class FunctionSendLine extends Function {
        public String getName() {
            return "sendLine";
        }

        public String getShortDesc() {
            return "sendLine(line) - send input line to process";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected line string parameter");
            String line=getString("line",params,0);
            processInput.println(line);
            return new ValueBoolean(true);
        }
    }
    
    class FunctionClose extends Function {
        public String getName() {
            return "close";
        }

        public String getShortDesc() {
            return "close() - close stdin for process";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            processInput.close();
            return new ValueBoolean(true);
        }
    }
    

    class FunctionIsAlive extends Function {
        public String getName() {
            return "isAlive";
        }

        public String getShortDesc() {
            return "isAlive() - true if process running";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            synchronized(EXIT_LOCK) {
                return new ValueBoolean(exitValue==null);
            }
        }
    }
    

    class FunctionIsDone extends Function {
        public String getName() {
            return "isDone";
        }

        public String getShortDesc() {
            return "isDone() - true if process completed running";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            synchronized(EXIT_LOCK) {
                return new ValueBoolean(exitValue!=null);
            }
        }
    }
    

    class FunctionExitValue extends Function {
        public String getName() {
            return "exitValue";
        }

        public String getShortDesc() {
            return "exitValue() - returns exit value or null if still running";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            synchronized(EXIT_LOCK) {
                if (exitValue==null) return new ValueNull();
                return exitValue;
            }
        }
    }
    
    class FunctionWait extends Function {
        public String getName() {
            return "wait";
        }

        public String getShortDesc() {
            return "wait() - wait for process to terminate - returns self";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            for (;;) {
                synchronized(EXIT_LOCK) {
                    if (exitValue!=null) break;
                }
                Thread.sleep(5);
            }
            return new ValueObj(self());
        }
    }
    
    class FunctionData extends Function {
        public String getName() {
            return "data";
        }

        public String getShortDesc() {
            return "data() - returns the (original) context dictionary";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueObj(dict);
        }
    }
    

}
