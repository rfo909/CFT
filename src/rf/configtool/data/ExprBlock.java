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

package rf.configtool.data;

import rf.configtool.main.CodeLines;
import rf.configtool.main.Ctx;
import rf.configtool.main.ObjGlobal;
import rf.configtool.main.Runtime;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBlock;
import rf.configtool.parser.Token;
import rf.configtool.parser.TokenStream;
import java.util.*;

public class ExprBlock extends LexicalElement {

	public static final int MODE_INNER = 0;
	public static final int MODE_LAMBDA = 1;
	public static final int MODE_LOCAL = 2;
	
    private int mode;
    
    private List<ProgramLine> programLines=new ArrayList<ProgramLine>();
    
    // See Runtime.processCodeLines() method to extend to loops and supporting PROGRAM_LINE_SEPARATOR - must in addition 
    // add '}' as terminator character inside ProgramLine

    public ExprBlock (TokenStream ts) throws Exception {
        super(ts);
        if (ts.matchStr("Inner")) {
        	mode=MODE_INNER;
        	ts.matchStr("{","expected '{'");
        } else if (ts.matchStr("Lambda")) {
        	mode=MODE_LAMBDA;
        	ts.matchStr("{","expected '{'");
        } else if (ts.matchStr("{")) {
        	mode=MODE_LOCAL;
        }
	        
        List<ProgramLine> progLines=new ArrayList<ProgramLine>();
        for(;;) {
            progLines.add(new ProgramLine(ts));
            if (ts.matchStr(CodeLines.PROGRAM_LINE_SEPARATOR)) continue;
            break;
        }
    	ts.matchStr("}","expected '}' closing " + getBlockModeName());
    	
        this.programLines=progLines;
    
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
        } else {
        	throw new Exception("Invalid mode: " + mode);
        }
    }


}
