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

package rf.configtool.main.runtime.lib.text;

import java.io.*;
import java.util.*;

import rf.configtool.main.Ctx;
import rf.configtool.main.OutText;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueFloat;
import rf.configtool.main.runtime.ValueInt;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.lib.ValueObjFileLine;
import rf.configtool.parser.CharSource;
import rf.configtool.parser.CharTable;

import java.awt.Color;

public class ObjParser extends Obj {
    
    public ObjParser() {
    	this.add(new FunctionReadme());
    	this.add(new FunctionProduction());
    }
    
    @Override
    public boolean eq(Obj x) {
        return x==this;
    }


    @Override
    public String getTypeName() {
        return "Parser";
    }
    

    @Override
    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    
    private String getDesc() {
        return "Parser";
    }
    
    private Obj theObj () {
        return this;
    }
    

    class FunctionReadme extends Function {
        public String getName() {
            return "readme";
        }
        public String getShortDesc() {
            return "readme() - returns info on the Parser object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() != 0) throw new Exception("Expected no parameters");
        	String[] lines= {
        		"The Parser object",
        		"-----------------",
        		"The Parser is powered by a grammar. It is created from a set of named",
        		"productions, which are based on EBNF (Extended Backus-Naur Form), and then",
        		"extended a bit more.",
        		"",
        		"A production has a name, and a sequence of elements to match. These take",
        		"two forms: either a non-terminal, which refers to other productions by name,",
        		"which is simply a String, or a terminal, which refers to an object that",
        		"implements the TokenMatcher Java Interface.",
        		"",
        		"That currently means a Node object, as created via the Lexer object.",
        		"",
        		"Example:",
        		"--------",
        		"Lib.Text.Lexer.Node.addToken('{').setIsToken(5) =leftCurl",
        		"Lib.Text.Lexer.Node.addToken('}').setIsToken(5) =rightCurl",
        		"Lib.Text.Lexer.Node.addToken(',').setIsToken(5) =comma",
        		"Lib.Text.Lexer.Node.addToken(':').setIsToken(5) =colon",
        		"",
        		"Lib.Text.Parser =parser",
        		"parser.production('object', leftCurl, 'field*sepComma', rightCurl)",
        		"parser.production('sepComma', comma)",
        		"parser.production('field', 'fieldName', colon, 'value')",
        		"   ...",
        		"",
        		"The references to other productions may be expressed as follows:",
        		"  - 'name'    - matches production once",
        		"  - 'name?'   - optional match",
        		"  - 'name*'   - matches zero or more times",
        		"  - 'name+'   - one or more times",
        		"  - 'name*sep - zero or more time, with second production 'sep' between",
        		"  - 'name+sep - one or more times with separator production",
        	};
        	List<Value> list=new ArrayList<Value>();
        	for (String s:lines) {
        		list.add(new ValueString(s));
        	}
        	return new ValueList(list);
        }
    }

    
    class FunctionProduction extends Function {
        public String getName() {
            return "production";
        }
        public String getShortDesc() {
            return "production(name, ...) - add grammar production - see readme() - returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	if (params.size() < 2) throw new Exception("Expected parameters name, ...");
        	String name=getString("name", params, 0);
        	for (int i=1; i<params.size(); i++) {
        		Value v=params.get(i);
        		if (v instanceof ValueString) {
        			// add non-terminal
        			// TODO
        			continue;
        		} else if (v instanceof ValueObj) {
        			Obj obj=((ValueObj) v).getVal();
        			if (obj instanceof ObjLexerNode) {
        				// terminal
        				// TODO
        				continue;
        			}
        		}
        		throw new Exception("Invalid production right-hand-side: expected strings or Lexer.Node instances only");
        	}
        	return new ValueObj(theObj());
        }
    }
    
    
   

}
