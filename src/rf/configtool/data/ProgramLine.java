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
import rf.configtool.main.SourceException;
import rf.configtool.main.runtime.Obj;
import rf.configtool.parser.Token;
import rf.configtool.parser.TokenStream;
import java.util.*;

public class ProgramLine extends LexicalElement {
    
    private List<Stmt> statements=new ArrayList<Stmt>();

    public ProgramLine (TokenStream ts) throws Exception {
    	this(ts,true);
    }

    public ProgramLine (TokenStream ts, boolean loopingAllowed) throws Exception {
        super(ts);
        while (!ts.atEOF() && !ts.peekStr(CodeLines.PROGRAM_LINE_SEPARATOR) && !ts.peekStr("}")) {
        	Stmt stmt=Stmt.parse(ts);
        	if (!loopingAllowed) {
        		if ((stmt instanceof StmtLoop) || (stmt instanceof StmtIterate)) {
        			throw new SourceException(getSourceLocation(), "Loop statements invalid here");
        		}
        	}
            statements.add(stmt);
        }
    }
    
    
    public void execute (Ctx ctx) throws Exception {
        for (Stmt stmt:statements) {
        	ctx.debug(stmt);
        	
            stmt.execute(ctx);
        }
    }


}
