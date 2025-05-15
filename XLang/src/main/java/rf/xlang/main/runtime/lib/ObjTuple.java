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

package rf.xlang.main.runtime.lib;

import java.util.HashMap;

import rf.xlang.main.Ctx;
import rf.xlang.main.runtime.*;
import rf.xlang.parsetree.TupleType;
import java.util.*;

public class ObjTuple extends Obj {

    private TupleType tupleType;
    private HashMap<String, Value> values=new HashMap<>();

    public ObjTuple(TupleType tupleType) {
        this.tupleType=tupleType;
    }

    public ObjTuple(TupleType tupleType, List<Value> initValues) throws Exception {
        this(tupleType);
        int pos=0;
        for (String field : tupleType.getFieldNames()) {
            if (pos < initValues.size()) {
                values.put(field,initValues.get(pos));
            }
            pos++;
        }
    }

    @Override
    public String getTypeName() {
        return "Tuple:" + tupleType.getTypeName();
    }

    public Value getValue (String name) {
        return values.get(name);
    }

    public String getValAsString() {
        StringBuffer sb=new StringBuffer();

        sb.append("[");

        boolean comma=false;
        for (String field : tupleType.getFieldNames()) {
            sb.append(field);
            sb.append("=");
            Value v=values.get(field);
            if (v==null) sb.append("null"); else sb.append(v.getValAsString());
            if (comma) sb.append(", ");
            comma=true;
        }
        sb.append("]");
        return sb.toString();
    }


    @Override
    public boolean eq(Obj v) {
        return false;
    }

    public boolean getValAsBoolean() {
        return true;
    }

    private Value self() {
        return new ValueObj(this);
    }

}
