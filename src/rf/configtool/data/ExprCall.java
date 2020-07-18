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

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintStream;

import rf.configtool.main.Ctx;
import rf.configtool.main.ObjGlobal;
import rf.configtool.main.Stdio;
import rf.configtool.main.runtime.*;
import rf.configtool.main.runtime.lib.ObjDict;
import rf.configtool.main.runtime.lib.ObjDir;
import rf.configtool.main.runtime.lib.ObjFile;
import rf.configtool.main.runtime.lib.ObjGlob;
import rf.configtool.parser.SourceLocation;
import rf.configtool.parser.TokenStream;
import rf.configtool.root.ScriptState;

import java.util.*;

/**
 * Return directory object for current directory
 */
public class ExprCall extends LexicalElement {

    private Expr target;
    private List<Expr> params;
    
    // call "savefile:name" with Data (...)
    
    public ExprCall (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("call","expected 'call'");
        target=new Expr(ts); // "savefile:func"
        
        params=new ArrayList<Expr>();
        if (ts.matchStr("(")) {
            boolean comma=false;
            while (!ts.matchStr(")")) {
                if(comma) ts.matchStr(",", "expected comma");
                params.add(new Expr(ts));
                comma=true;
            }
        }

    }
    
    private String asString(Ctx ctx, Expr expr, String name) throws Exception {
        Value v=expr.resolve(ctx);
        if (v==null || !(v instanceof ValueString)) throw new Exception(name + " - expected string parameter");
        return ((ValueString) v).getVal();
    }
    

    
    public Value resolve (Ctx ctx) throws Exception {
        String t=asString(ctx,target,"script:function").trim();
        int pos=t.indexOf(":");
        if (pos < 0) throw new Exception("Expected script:function");
        
        String script=t.substring(0,pos);
        
        String func=t.substring(pos+1);
          
        List<Value> args=new ArrayList<Value>();
        for (Expr expr:params) args.add(expr.resolve(ctx));
        
        ObjGlobal objGlobal=ctx.getObjGlobal();
        Stdio stdio=objGlobal.getStdio();
        
        //return objGlobal.getRoot().invokeScriptFunction(script, func, args);
        ScriptState x=objGlobal.getRoot().getScriptState(script, false);
        Value retVal=x.invokeFunction (func, args);
        return retVal;
    }
}
