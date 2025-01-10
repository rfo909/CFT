/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2025 Roar Foshaug

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

import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;

public class ObjText extends Obj {
    
    public ObjText() {
        add(new FunctionLexer());
        add(new FunctionFilter());
        add(new FunctionCRLF());
        add(new FunctionLF());
        add(new FunctionTAB());
    }
    
    @Override
    public boolean eq(Obj x) {
        return x==this;
    }


    @Override
    public String getTypeName() {
        return "Text";
    }
    

    @Override
    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    
    private String getDesc() {
        return "Text";
    }
    
    private Obj theObj () {
        return this;
    }
    

    
    class FunctionLexer extends Function {
        public String getName() {
            return "Lexer";
        }
        public String getShortDesc() {
            return "Lexer() - create object for lexical analysis";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueObj(new ObjLexer());
        }
    }
    
    class FunctionFilter extends Function {
        public String getName() {
            return "Filter";
        }
        public String getShortDesc() {
            return "Filter() - creates Filter object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueObj(new ObjFilter());
        }
    }
    
    class FunctionCRLF extends Function {
        public String getName() {
            return "CRLF";
        }

        public String getShortDesc() {
            return "CRLF() - returns CRLF string";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueString("\r\n");
        }

    }
    
    class FunctionLF extends Function {
        public String getName() {
            return "LF";
        }

        public String getShortDesc() {
            return "LF() - returns LF string";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueString("\n");
        }

    }
    
    class FunctionTAB extends Function {
        public String getName() {
            return "TAB";
        }

        public String getShortDesc() {
            return "TAB() - returns TAB string";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueString("\t");
        }

    }

}
