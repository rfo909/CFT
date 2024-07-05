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

import java.util.ArrayList;
import java.util.List;

import rf.configtool.lexer.TokenStream;
import rf.configtool.main.CFTCallStackFrame;
import rf.configtool.main.Ctx;
import rf.configtool.main.CustomException;
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

        CFTCallStackFrame callStackTarget = null;
        if (checkMode) {
            callStackTarget = ctx.getStdio().getTopCFTCallStackFrame();
        }
        try {
            // parameters
            List<Value> values=new ArrayList<Value>();
            for (Expr e:params) values.add(e.resolve(ctx.sub()));
            
            if (obj instanceof ValueObj) {
                // unwrap Obj
                obj=((ValueObj) obj).getVal();
            }

            if (obj instanceof ObjDict) {
                ObjDict dict=(ObjDict) obj;

                if (dict.getFunction(ident) == null) {
                    // allowing dotted lookup of values only when not named the same as dictionary member functions
                    Value v = dict.getValue(ident);
                    if (v != null) {
                        // as a funny side effect from moving Dict.ident lookup of values here from Dict, is that
                        // Dict.?xxx supports same functionality as Dict.has("xxx") :-)

                        if (checkMode) return new ValueBoolean(true);

                        //System.out.println("DottedCall: found dict value for '" + ident + "' " + v.getDescription());
                        // Check if closure
                        if (v instanceof ValueObj) {
                            Obj x = ((ValueObj) v).getVal();
                            if (x instanceof ObjClosure) {
                                CFTCallStackFrame caller = new CFTCallStackFrame(getSourceLocation(), "Calling Dict.closure " + ident);

                                Value result = ((ObjClosure) x).callClosure(ctx, caller, values);
                                return result;
                            }
                        }
                        // not closure, just return value
                        return v;
                    }
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
                    if (!(ex instanceof CustomException)) {
                        throw new SourceException(getSourceLocation(), ex);
                    } else {
                        throw ex;
                    }
                }
            }
        } finally {
            // if running in checkMode, we stored the top off the callStack
            // in the top of this method, and are now restoring the callstack,
            // because if a x.?y fails, there will have been an exception,
            // which means the callstack is out of sync. Correcting this here!
            //
            // See Tests01:Test52...
            
            if (callStackTarget != null) ctx.getStdio().getAndClearCFTCallStack(callStackTarget);
        }
    }

}
