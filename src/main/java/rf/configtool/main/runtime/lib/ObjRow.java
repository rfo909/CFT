
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

import java.util.*;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.IsSynthesizable;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueInt;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.ValueList;

import rf.configtool.util.DateTimeDurationFormatter;

/**
 * Report data row
 */
public class ObjRow extends Obj implements IsSynthesizable {

    static final String reportTypes = "|String|FileLine|int|float|boolean|null|Date|Duration|";

    private List<Value> rowData;


    public ObjRow(List<Value> rowData) {
        this.rowData=rowData;
        add(new FunctionGet());
        add(new FunctionShow());
    }

    public ObjRow () {
        this(new ArrayList<Value>());
    }


    private Obj theObj() {
        return this;
    }

    @Override
    public boolean eq(Obj x) {
        if (x==this) return true;
        return false;
    }

    @Override
    public String createCode() throws Exception {
        StringBuffer sb=new StringBuffer();
        sb.append("Sys.Row(");
        boolean comma=false;
        for (Value v:rowData) {
            if(comma) sb.append(",");
            if (v instanceof IsSynthesizable) {
                sb.append(((IsSynthesizable) v).createCode());
                comma=true;
            } else {
                throw new Exception("Value not synthesizable");
            }
        }
        sb.append(")");
        return sb.toString();

    }


    @Override
    public String getTypeName() {
        return "Row";
    }

    @Override
    public ColList getContentDescription() {
        ColList list = ColList.list();
        for (Value v:rowData) {
            String type;
            if (v instanceof ValueObj) {
                type=((ValueObj) v).getVal().getTypeName();
                //System.out.println("objType: " + type);
            } else {
                type=v.getTypeName();
            }
            if(reportTypes.contains("|" + type + "|")) {
                list.regular(v.getValAsString());
            }
        }
        return list;
    }


    class FunctionGet extends Function {
        public String getName() {
            return "get";
        }
        public String getShortDesc() {
            return "get(n=) - return value of column n, defaults to 0";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            int n=0;
            if (params.size() == 1) {
                n = (int) getInt("n", params, 0);
            }
            return rowData.get(n);
        }
    }


    class FunctionShow extends Function {
        public String getName() {
            return "show";
        }
        public String getShortDesc() {
            return "show() - display all columns";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            List<Value> rows=new ArrayList<Value>();

            for (Value v:rowData) {
                String type;
                String value="";
                if (v instanceof ValueObj) {
                    Obj obj=((ValueObj) v).getVal();
                    type="<obj: " + obj.getTypeName() + ">";
                } else {
                    type="<"+v.getTypeName()+">";
                }
                if(reportTypes.contains(v.getTypeName())) {
                    value=v.getValAsString();
                }

                // Using Row to return data, as it gets properly formatted
                List<Value> rowData=new ArrayList<Value>();
                rowData.add(new ValueString(type));
                rowData.add(new ValueString(value));

                ObjRow row=new ObjRow(rowData);
                rows.add(new ValueObj(row));
            }
            return new ValueList(rows);
        }
    }



}
