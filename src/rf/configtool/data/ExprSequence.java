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

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.SourceException;
import rf.configtool.main.runtime.*;
import rf.configtool.main.runtime.lib.ObjDict;
import rf.configtool.main.runtime.lib.ObjDir;
import rf.configtool.main.runtime.lib.ObjFile;
import rf.configtool.main.runtime.lib.ObjGlob;
import rf.configtool.main.runtime.lib.Protection;
import rf.configtool.parser.TokenStream;

/**
 * Create List from sequence of expressions not (necessarily) separated by comma. 
 * 
 * Could have moved List() here, but that would mean removing List() from the help
 * which is generated from functions defined inside ObjGlobal.
 * 
 * The guarded=true variant is called CondSequence, and uses boolean value of first
 * parameter to decide if including the following data in the list or not.
 * 
 */
public class ExprSequence extends ExprCommon {
    
    private boolean guarded;
    private List<Expr> expr = new ArrayList<Expr>();

    public ExprSequence (boolean guarded, TokenStream ts) throws Exception {
        super(ts);
        this.guarded=guarded;
        final String name = guarded ? "CondSequence" : "Sequence";
        
        ts.matchStr(name,"expected '" + name + "'");
        ts.matchStr("(", "expected '(' following " + name);
        boolean allowComma=false;
        while (!ts.matchStr(")")) {
            if (allowComma) ts.matchStr(",");
            expr.add(new Expr(ts));
            allowComma=true;
        }
    }
    
    public Value resolve (Ctx ctx) throws Exception {
        if (expr.size()==0) return new ValueList(new ArrayList<Value>());
        if (guarded) {
            Value v=expr.get(0).resolve(ctx);
            if (!(v instanceof ValueBoolean)) throw new Exception("CondSequence: first parameter not a boolean");
            boolean ok = v.getValAsBoolean();
            if (!ok) return new ValueList(new ArrayList<Value>()); // empty list
        }
        boolean isFirst=true;
        
        List<Value> result=new ArrayList<Value>();
        for (Expr e:expr) {
            if (guarded && isFirst) {
                isFirst = false;
                continue;
            }
            result.add(e.resolve(ctx));
        }
        return new ValueList(result);
     }
    
}
