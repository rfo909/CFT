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

package rf.configtool.main.runtime.lib;

import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueInt;
import rf.configtool.main.runtime.ValueNull;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;

public class ValueObjFileLine extends ValueString {
    
    private Long lineNo;
    private ObjFile file;
    
    public ValueObjFileLine (String line, Long lineNo, ObjFile file) {
        super(line);
        this.lineNo=lineNo;
        this.file=file;
        
        add(new FunctionLineNumber());
        add(new FunctionFile());
    }

    @Override
    public String getTypeName() {
        return "FileLine";
    }
    
    @Override
    public String synthesize() throws Exception {
        return "FileLine(" + super.synthesize() + "," + lineNo + "," + file.synthesize() + ")";
    }

    public ObjFile getFile() {
        return file;
    }
    
    public Long getLineNo() {
        return lineNo;
    }
    

    class FunctionLineNumber extends Function {
        public String getName() {
            return "lineNumber";
        }
        public String getShortDesc() {
            return "lineNumber() - returns line number";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) {
                throw new Exception("Expected no parameters");
            }
            if (lineNo==null) return new ValueNull();
            return new ValueInt(lineNo);
        }
    }

    class FunctionFile extends Function {
        public String getName() {
            return "file";
        }
        public String getShortDesc() {
            return "file() - returns File object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) {
                throw new Exception("Expected no parameters");
            }
            return new ValueObj(file);
        }
    }

}
