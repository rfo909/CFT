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

import java.util.*;

import rf.configtool.lexer.TokenStream;
import rf.configtool.main.Ctx;
import rf.configtool.main.SourceException;
import rf.configtool.main.runtime.*;

public class ExprAs extends ExprCommon {

    private String typeName;
    private Expr typeNameExpr;
    private boolean orNull=false;
    
    public ExprAs (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("as","expected 'as'");
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
        
        final List<String> typeNames=new ArrayList<String>();
        
        
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
        if (orNull) typeNames.add("null");
        
        boolean ok=false;
        for (String type:typeNames) {
        	if (stackValue.getTypeName().equals(type)) {
        		ok=true;
        		break;
        	}
        }
        
        if (!ok) {
	        StringBuffer sb=new StringBuffer();
	        boolean first=true;
	        for (String type:typeNames) {
	        	if (!first) sb.append("|");
	        	first=false;
	        	sb.append(type);
	        }

    		throw new SourceException(getSourceLocation(), 
    			"Expected value as type [" + sb.toString() + "] - got " + stackValue.getTypeName() + ": " + showValue(stackValue));
    	}
    	
    	// return value
    	return stackValue;
    }

}
