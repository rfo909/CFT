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
import rf.configtool.main.runtime.ValueMacro;
import rf.configtool.parser.Token;
import rf.configtool.parser.TokenStream;
import java.util.*;

public class ExprMacro extends LexicalElement {

    private boolean localCodeBlock;
    
    private List<ProgramLine> programLines=new ArrayList<ProgramLine>();
    
    // See Runtime.processCodeLines() method to extend to loops and supporting PROGRAM_LINE_SEPARATOR - must in addition 
    // add '}' as terminator character inside ProgramLine

    public ExprMacro (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("{", "expected '{'");
        
        localCodeBlock=true;
        
        if (ts.matchStr("*")) {  // indicates it can run "anywhere"
            localCodeBlock=false;
        }
        
        List<ProgramLine> progLines=new ArrayList<ProgramLine>();
        for(;;) {
            progLines.add(new ProgramLine(ts));
            if (ts.matchStr(CodeLines.PROGRAM_LINE_SEPARATOR)) continue;
            break;
        }
    	ts.matchStr("}","expected '}' closing " + (localCodeBlock ? "code block" : "macro"));
    	
        this.programLines=progLines;
//
//        
//        while (!ts.matchStr("}")) {
//            statements.add(Stmt.parse(ts));
//        }
//        
    }
    
    
    public Value resolve (Ctx ctx) throws Exception {
        ValueMacro m=new ValueMacro(programLines);
        if (localCodeBlock) {
            return m.callLocalMacro(ctx);
        }
        return m;
    }


}
