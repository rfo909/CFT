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

package rf.configtool.main.runtime.lib;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.*;


public class ObjArray extends Obj implements IsSynthesizable {
    private List<Value> data;

    public ObjArray (ValueList list) {
        List<Value> values = list.getVal();
        data=new ArrayList<Value>();
        for (int i=0; i<list.getVal().size(); i++) data.add(values.get(i));

        this.add(new FunctionLength());
        this.add(new FunctionSet());
        this.add(new FunctionGet());
        this.add(new FunctionList());
        this.add(new FunctionAdd());
    }

    private ObjArray theObj() {
        return this;
    }

    @Override
    public boolean eq(Obj x) {
        if (x==this) return true;
        return false;
    }

    @Override
    public String getTypeName() {
        return "Array";
    }

    @Override
    public ColList getContentDescription() {
        return ColList.list().regular("[" + data.size() + "]");
    }

    @Override
    public String createCode() throws Exception {
        if (data==null) return "Std.Array(List)";
        StringBuffer sb = new StringBuffer();
        sb.append("Std.Array(List(");
        boolean comma=false;
        for (Value v : data) {
            if (comma) sb.append(",");
            sb.append(v.synthesize());
            comma=true;
        }
        sb.append("))");
        return sb.toString();
    }

    class FunctionLength extends Function {
        public String getName() {
            return "length";
        }
        public String getShortDesc() {
            return "length() - return length of array";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueInt(data.size());
        }
    }

    class FunctionGet extends Function {
        public String getName() {
            return "get";
        }
        public String getShortDesc() {
            return "get(pos) - get array value";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected pos parameter (int)");
            int pos=(int) getInt("pos", params, 0);
            return data.get(pos);
        }
    }

    class FunctionSet extends Function {
        public String getName() {
            return "set";
        }
        public String getShortDesc() {
            return "set(pos, value) - set array value, returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 2) throw new Exception("Expected pos parameter (int) and value");
            int pos=(int) getInt("pos", params, 0);
            Value value=params.get(1);
            data.set(pos,value);
            return new ValueObj(theObj());
        }
    }

    class FunctionList extends Function {
        public String getName() {
            return "list";
        }
        public String getShortDesc() {
            return "list() - get array as list";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            List<Value> list=new ArrayList<Value>();
            for (int i=0; i<data.size(); i++) list.add(data.get(i));
            return new ValueList(list);
        }
    }

    class FunctionAdd extends Function {
        public String getName() {
            return "add";
        }
        public String getShortDesc() {
            return "add(value) - returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected value parameter");
            int pos=(int) getInt("pos", params, 0);
            Value value=params.get(1);
            data.add(value);
            return new ValueObj(theObj());
        }
    }

}
