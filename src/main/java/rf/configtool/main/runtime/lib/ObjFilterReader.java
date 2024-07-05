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

package rf.configtool.main.runtime.lib;

import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;

/**
 * A simple reader that outputs filtered lines only
 */
public class ObjFilterReader extends Obj {
    
    private ObjLineReader lineReader;
    private ObjGrep grep;
    
    public ObjFilterReader(ObjLineReader lineReader, ObjGrep grep) {
        this.lineReader=lineReader;
        this.grep=grep;
        
        add(new FunctionRead());
    }

    
    public Obj self() {
        return this;
    }
    
        
    @Override
    public boolean eq(Obj x) {
        return x==this;
    }


    @Override
    public String getTypeName() {
        return "FilterReader";
    }
    

    @Override
    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    
    private String getDesc() {
        return "FilterReader";
    }
    
    
 
    
    class FunctionRead extends Function {
        public String getName() {
            return "read";
        }
        public String getShortDesc() {
            return "read() - get next filtered line or null if end of data";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return lineReader.readLine(grep);
        }
    }
 
    
    
    
}
