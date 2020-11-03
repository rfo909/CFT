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

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.lib.ObjProcess;
import rf.configtool.parser.TokenStream;

public class StmtSpawn extends Stmt {

	private Expr expr;

	public StmtSpawn(TokenStream ts) throws Exception {
		super(ts);

		ts.matchStr("spawn", "expected 'spawn'");
		ts.matchStr("(", "expected '('");
		expr = new Expr(ts);
		ts.matchStr(")", "expected ')' closing spawn statement");
	}

	public void execute(Ctx ctx) throws Exception {
		ObjProcess process = new ObjProcess(expr);
		process.start(ctx);
		ctx.push(new ValueObj(process));
		;
	}

}
