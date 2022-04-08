/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2022 Roar Foshaug

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
import rf.configtool.main.CodeLines;
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
    public static final int MODE_CLASS = 3;
    
    private int mode;
    private String className;
    
    private List<ProgramLine> programLines=new ArrayList<ProgramLine>();
    private String synString;
    
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
        } else if (ts.matchStr("class")) {
        	mode=MODE_CLASS;
        	if (ts.peekType(Token.TOK_IDENTIFIER)) {
        		className=ts.matchIdentifier("internal error");
        	}
        	ts.matchStr("{","expected '{'");
        }
        
        List<ProgramLine> progLines=new ArrayList<ProgramLine>();
        if (mode==MODE_LOCAL) {
            // only one program line as "PIPE" not allowed
            progLines.add(new ProgramLine(ts));
            
            if (ts.matchStr(CodeLines.PIPE_SYMBOL)) {
                // specific exception for this case
                throw new SourceException(getSourceLocation(),"Local block can not contain the PIPE '" + CodeLines.PIPE_SYMBOL + "' character");
            }
            ts.matchStr("}","expected '}' closing " + getBlockModeName() + " starting at " + this.getSourceLocation());
        } else {
            // INNER, LAMBDA and CLASS
            for(;;) {
                progLines.add(new ProgramLine(ts));
                if (ts.matchStr(CodeLines.PIPE_SYMBOL)) continue;
                break;
            }
            ts.matchStr("}","expected '}' closing " + getBlockModeName() + " starting at " + this.getSourceLocation());
        }
        this.programLines=progLines;
        
        int tsEnd=ts.getCurrPos();
        
        StringBuffer sb=new StringBuffer();
        for (int i=tsStart; i<tsEnd; i++) {
            sb.append(" ");
            sb.append(ts.getTokenAtPos(i).getOriginalStringRep());
        }
        this.synString=sb.toString();
    }
    
    private String getBlockModeName() {
        if (mode==MODE_INNER) return "inner block";
        if (mode==MODE_LAMBDA) return "lambda block";
        if (mode==MODE_LOCAL) return "local block";
        if (mode==MODE_CLASS) return "class block";
        throw new RuntimeException("Unknown mode: " + mode);
    }
    
    
    public Value resolve (Ctx ctx) throws Exception {
        ValueBlock b=new ValueBlock(programLines, synString);
        if (mode==MODE_LAMBDA) {
            return b;  // ValueBlock
        }
        
        // directly executing alternatives
        
        if (mode==MODE_INNER) {
            return b.callInnerBlock(ctx);
        } else if (mode==MODE_LOCAL) {
            return b.callLocalBlock(ctx);
        } else if (mode==MODE_CLASS) {
        	if (className == null) className=ctx.getFunctionState().getScriptFunctionName();
        	if (className == null) throw new Exception("Could not identify script function name for class name");
        	ObjDict self=new ObjDict(className);
        	List<Value> params=ctx.getFunctionState().getParams(); // inherit params from surroundings
        	
        	CFTCallStackFrame caller=new CFTCallStackFrame(getSourceLocation(),"Calling lambda");
        	b.callLambda(ctx,caller,self,params);
        	
        	return new ValueObj(self);
        } else {
            throw new Exception("Invalid mode: " + mode);
        }
    }


}
