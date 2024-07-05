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

import rf.configtool.lexer.TokenStream;
import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBlock;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.lib.ObjClosure;
import rf.configtool.main.runtime.lib.ObjDict;
import rf.configtool.main.runtime.lib.ObjProcess;

public class ExprSpawnProcess extends ExprCommon {

    private Expr expr;
    private Expr exprDict;
    private Expr onChangeLambdaOrClosure;

    public ExprSpawnProcess(TokenStream ts) throws Exception {
        super(ts);

        ts.matchStr("SpawnProcess", "expected 'spawn'");
        
        ts.matchStr("(", "expected '('");
        exprDict = new Expr(ts);
        ts.matchStr(",","Expected ','");
        expr = new Expr(ts);
        if (ts.matchStr(",")) {
            onChangeLambdaOrClosure = new Expr(ts);
        }
        ts.matchStr(")", "expected ')' closing spawn statement");
    }

    public Value resolve (Ctx ctx) throws Exception {
        Value d = exprDict.resolve(ctx);
        Value oc = null;
        if (onChangeLambdaOrClosure != null) oc=onChangeLambdaOrClosure.resolve(ctx);
             
        ObjDict dict=null;
        ObjClosure closure=null;

        boolean dictOk=false;
        boolean closOk=false;
        
        if (d instanceof ValueObj) {
            Obj obj=((ValueObj) d).getVal();
            if (obj instanceof ObjDict) {
                dict=(ObjDict) obj;
                dictOk=true;
            }
        }
               
        if (oc==null) {
            closOk=true;
        } else {
            if (oc instanceof ValueBlock) {
                closure=new ObjClosure(new ObjDict(), (ValueBlock) oc);
                closOk=true;
            } else if (oc instanceof ValueObj) {
                Obj x=((ValueObj) oc).getVal();
                if (x instanceof ObjClosure) {
                    closure=(ObjClosure) x;
                    closOk=true;
                }
            }
        }
 
        if (!dictOk || !closOk) throw new Exception("Expected parameters Dict, Expr [,lambdaOrClosure]");
        
        ObjProcess process = new ObjProcess(dict, expr, closure);
        process.start(ctx);
        return (new ValueObj(process));
        
    }
}
