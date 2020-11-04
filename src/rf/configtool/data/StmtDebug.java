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

import java.util.*;

import rf.configtool.main.Ctx;
import rf.configtool.main.Stdio;
import rf.configtool.main.runtime.Value;
import rf.configtool.parser.TokenStream;

public class StmtDebug extends Stmt {

    private Expr value;
    
    public StmtDebug (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("debug","expected 'debug'");
        ts.matchStr("(", "expected '(' following debug");
        value=new Expr(ts);
        ts.matchStr(")", "expected ')' closing debug stmt");
    }

    public void execute (Ctx ctx) throws Exception {
    	Stdio stdio=ctx.getStdio();
    	
    	stdio.println("%DEBUG% " + value.resolve(ctx).getValAsString());
    }

}
