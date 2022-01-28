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

package rf.configtool.main.runtime.lib.text;

import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueObj;

public class ObjText extends Obj {
    
    public ObjText() {
        this.add(new FunctionLexer());
        this.add(new FunctionFilter());
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
    

  

}
