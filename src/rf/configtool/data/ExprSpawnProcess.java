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

import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.*;
import rf.configtool.main.runtime.lib.ObjDict;
import rf.configtool.main.runtime.lib.ObjProcess;
import rf.configtool.parser.TokenStream;

public class ExprSpawnProcess extends LexicalElement {

	private Expr expr;
	private Expr exprDict;

	public ExprSpawnProcess(TokenStream ts) throws Exception {
		super(ts);

		ts.matchStr("SpawnProcess", "expected 'spawn'");
		
		ts.matchStr("(", "expected '('");
		exprDict = new Expr(ts);
		ts.matchStr(",","Expected ','");
		expr = new Expr(ts);
		ts.matchStr(")", "expected ')' closing spawn statement");
	}

    public Value resolve (Ctx ctx) throws Exception {
		Value d = exprDict.resolve(ctx);
		if (d instanceof ValueObj) {
			Obj obj=((ValueObj) d).getVal();
			if (obj instanceof ObjDict) {
				ObjDict dict=(ObjDict) obj;
				
				ObjProcess process = new ObjProcess(dict, expr);
				process.start(ctx);
				return (new ValueObj(process));
			}
		}
		throw new Exception("Expected parameters Dict : Expr");
	}
}
