/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2022 Roar Foshaug

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

import java.util.ArrayList;
import java.util.List;

import rf.configtool.lexer.TokenStream;
import rf.configtool.main.Ctx;
import rf.configtool.main.SourceException;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.lib.ObjClosure;
import rf.configtool.main.runtime.lib.ObjDict;

/**
 * Lookup of identifier or call of function
 */
public class DottedCall extends LexicalElement {

    private String ident;
    private List<Expr> params=new ArrayList<Expr>();
    private boolean checkMode=false;
    private String code;
    
    public DottedCall (TokenStream ts) throws Exception {
        super(ts);
        code=ts.showNextTokens(10);
        //System.out.println("DottedCall: " + code);
        ts.matchStr(".","Expected .identifier");
        if (ts.matchStr("?")) {
            checkMode=true;
        }
        ident=ts.matchIdentifier("expected .identifier");
        if (ts.matchStr("(")) {
            boolean comma=false;
            while (!ts.peekStr(")")) {
                if (comma) {
                    ts.matchStr(",", "expected comma");
                }
                params.add(new Expr(ts));
                comma=true;
            }
            ts.matchStr(")", "expected ')' closing param list");
        }
    }
    
    public Value resolve (Ctx ctx, Obj obj) throws Exception {
//      System.out.println("DottedCall.resolve: " + code);
//      System.out.println("DottedCall.resolve obj=" + obj.getTypeName());
        // parameters
        List<Value> values=new ArrayList<Value>();
        for (Expr e:params) values.add(e.resolve(ctx.sub()));
        
        if (obj instanceof ValueObj) {
        	// unwrap Obj
            obj=((ValueObj) obj).getVal();
        }
        
        if (obj instanceof ObjDict) {
        	ObjDict dict=(ObjDict) obj;
        	Value v=dict.getValue(ident);
        	if (v != null) {
        		// as a funny side effect from moving Dict.ident lookup of values here from Dict, is that
        		// Dict.?xxx supports same functionality as Dict.has("xxx") :-)
        		
        		if (checkMode) return new ValueBoolean(true);
        		
        		//System.out.println("DottedCall: found dict value for '" + ident + "' " + v.getDescription());
        		// Check if closure
        		if (v instanceof ValueObj) {
	        		Obj x=((ValueObj) v).getVal();
	        		if (x instanceof ObjClosure) {
	        			return ((ObjClosure) x).callClosure(ctx, values);
	        		}
        		}
        		// not closure, just return value
        		return v;
        	}
        }
        
        Function f=obj.getFunction(ident);
        if (f==null) {
            if (checkMode) return new ValueBoolean(false);
            
            String msg=getSourceLocation() + " " + obj.getDescription() + " no function '" + ident + "'";
            throw new Exception(msg);
        }
        
        try {
            Value result=f.callFunction(ctx, values);
            //System.out.println("DottedCall result=" + result.getClass().getName());
            if (!checkMode) return result;
            return new ValueBoolean(true);
        } catch (Exception ex) {
            if (checkMode) {
                return new ValueBoolean(false);
            } else {
                if (!(ex instanceof SourceException)) {
                    throw new SourceException(getSourceLocation(), ex);
                } else {
                    throw ex;
                }
            }
        }
    }

}
