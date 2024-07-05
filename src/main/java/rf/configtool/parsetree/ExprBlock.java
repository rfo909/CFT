/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2024 Roar Foshaug

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

package rf.configtool.parsetree;

import java.util.ArrayList;
import java.util.List;

import rf.configtool.lexer.Token;
import rf.configtool.lexer.TokenStream;
import rf.configtool.main.CFTCallStackFrame;
import rf.configtool.main.FunctionBody;
import rf.configtool.main.Ctx;
import rf.configtool.main.SourceException;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBlock;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.lib.ObjDict;

public class ExprBlock extends ExprCommon {

    public static final int MODE_INNER = 0;
    public static final int MODE_LAMBDA = 1;
    public static final int MODE_LOCAL = 2;
    
    private int mode;
    
    private List<CodeSpace> programLines=new ArrayList<CodeSpace>();
    
    // See Runtime.processCodeLines() method to extend to loops and supporting PROGRAM_LINE_SEPARATOR - must in addition 
    // add '}' as terminator character inside ProgramLine

    public ExprBlock (TokenStream ts) throws Exception {
        super(ts);
        int tsStart=ts.getCurrPos();
        
        if (ts.matchStr("Inner")) {
            mode=MODE_INNER;
            ts.matchStr("{","expected '{'");
        } else if (ts.matchStr("Lambda")) {
            mode=MODE_LAMBDA;
            ts.matchStr("{","expected '{'");
        } else if (ts.matchStr("{")) {
            mode=MODE_LOCAL;
        }
        
        List<CodeSpace> progLines=new ArrayList<CodeSpace>();
        if (mode==MODE_LOCAL) {
            // only one program line as "PIPE" not allowed
            progLines.add(new CodeSpace(ts));
            
            if (ts.matchStr(FunctionBody.PIPE_SYMBOL)) {
                // specific exception for this case
                throw new SourceException(getSourceLocation(),"Local block can not contain the PIPE '" + FunctionBody.PIPE_SYMBOL + "' character");
            }
            ts.matchStr("}","expected '}' closing " + getBlockModeName() + " starting at " + this.getSourceLocation());
        } else {
            // INNER + LAMBDA
            for(;;) {
                progLines.add(new CodeSpace(ts));
                if (ts.matchStr(FunctionBody.PIPE_SYMBOL)) continue;
                break;
            }
            ts.matchStr("}","expected '}' closing " + getBlockModeName() + " starting at " + this.getSourceLocation());
        }
        this.programLines=progLines;
        
        int tsEnd=ts.getCurrPos();
        
    }
    
    private String getBlockModeName() {
        if (mode==MODE_INNER) return "inner block";
        if (mode==MODE_LAMBDA) return "lambda block";
        if (mode==MODE_LOCAL) return "local block";
        throw new RuntimeException("Unknown mode: " + mode);
    }
    
    
    public Value resolve (Ctx ctx) throws Exception {
        ValueBlock b=new ValueBlock(programLines);
        if (mode==MODE_LAMBDA) {
            return b;  // ValueBlock
        }
        
        // directly executing alternatives
        
        if (mode==MODE_INNER) {
            return b.callInnerBlock(ctx);
        } else if (mode==MODE_LOCAL) {
            return b.callLocalBlock(ctx);
//        } else if (mode==MODE_CLASS) {
//          if (className == null) className=ctx.getFunctionState().getScriptFunctionName();
//          if (className == null) throw new Exception("Could not identify script function name for class name");
//          ObjDict self=new ObjDict(className);
//          List<Value> params=ctx.getFunctionState().getParams(); // inherit params from surroundings
//          
//          CFTCallStackFrame caller=new CFTCallStackFrame(getSourceLocation(),"Creating class " + className);
//          b.callLambda(ctx,caller,self,params);
//          
//          return new ValueObj(self);
        } else {
            throw new Exception("Invalid mode: " + mode);
        }
    }


}
