/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2023 Roar Foshaug

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

import java.util.*;

import rf.configtool.lexer.TokenStream;
import rf.configtool.main.Ctx;
import rf.configtool.main.SourceException;
import rf.configtool.main.runtime.*;
import rf.configtool.main.runtime.lib.ObjDict;

public class ExprAs extends ExprCommon {

    private boolean testMode=false;
    private boolean isDictName=false;
    private String typeName;
    private Expr typeNameExpr;
    private boolean orNull=false;
    
    public ExprAs (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("as","expected 'as'");
        testMode=ts.matchStr("?");
        isDictName = ts.matchStr("&");
        if (ts.matchStr("(")) {
            typeNameExpr=new Expr(ts);
            ts.matchStr(")", "expected ')' following as-expression");
        } else {
            typeName=ts.matchIdentifier("expected variable name");
        }
        if (ts.matchStr("?")) {
            orNull=true;
        }
    }
    
    private String showValue (Value v) throws Exception {
        String s=v.getValAsString();
        if (s.length() > 85) s=s.substring(0,80);
        return s;
    }

    public Value resolve (Ctx ctx) throws Exception {
        final Value stackValue=ctx.pop();
        
        Obj theValue=stackValue;
        if (theValue instanceof ValueObj) {
            theValue=((ValueObj) theValue).getVal(); // unwrap Obj 
        }
        
        final List<String> typeNames=new ArrayList<String>();
        
        // either got a typename identifier or an Expr
        if (typeName != null) {
            typeNames.add(typeName);
        } else {
            Value exprValue=typeNameExpr.resolve(ctx);
            if (exprValue instanceof ValueList) {
                List<Value> list=((ValueList) exprValue).getVal();
                for (Value e:list) {
                    typeNames.add(e.getValAsString());
                }
            } else {
                typeNames.add(exprValue.getValAsString());
            }
        }
        // and optional '?' allowing for null
        if (orNull) typeNames.add("null");
        
        
        final String valueTypeName;
        
        if (isDictName) {
            if (!(theValue instanceof ObjDict)) {
                if (testMode) {
                    return new ValueBoolean(false);
                } else {
                    throw new SourceException(getSourceLocation(),
                        "Expected value of type Dict with name - got " + theValue.getTypeName() + ": " + showValue(stackValue));
                } 
            }
            valueTypeName=((ObjDict) theValue).getName();
            if (valueTypeName==null) {
                if (testMode) {
                    return new ValueBoolean(false);
                } else {
                    throw new SourceException(getSourceLocation(),
                        "Expected value of type Dict with name - got " + showValue(stackValue));
                } 
            }
        } else {
            valueTypeName=theValue.getTypeName();
        }
        
//      System.out.println("Got valueTypeName=" + valueTypeName);
        
        boolean ok=false;
        for (String type:typeNames) {
            if (valueTypeName.equals(type)) {
                ok=true;
                break;
            }
        }
        
        if (!ok) {
            if (testMode) return new ValueBoolean(false);
            
            StringBuffer sb=new StringBuffer();
            boolean first=true;
            for (String type:typeNames) {
                if (!first) sb.append("|");
                first=false;
                sb.append(type);
            }

            throw new SourceException(getSourceLocation(),
                (isDictName 
                        ? "Expected Dict of type(s) [" + sb.toString() + "]" 
                        : "Expected value as type(s) [" + sb.toString() + "]"
                ) + " - got " + showValue(stackValue));
        }
        
        // return value
        if (testMode) return new ValueBoolean(true);
        
        return stackValue;
    }

}
