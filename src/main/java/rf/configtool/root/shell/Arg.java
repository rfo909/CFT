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

package rf.configtool.root.shell;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Value;
import rf.configtool.parsetree.Expr;
import java.io.File;
import java.util.List;

public class Arg {
    
    private String str;
    private Expr expr;
    
    public Arg (String str) {
        this.str=str;
    }
    
    public Arg (Expr expr) {
        this.expr=expr;
    }
    
    public boolean isExpr() {
        return expr != null;
    }
    
    public Value resolveExpr (Ctx ctx) throws Exception {
        return this.expr.resolve(ctx);
    }
    
    public String getString () throws Exception {
        return str;
    }
}
