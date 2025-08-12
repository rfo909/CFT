
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

import java.io.File;
import java.io.BufferedReader;
import java.util.*;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.*;

import rf.configtool.util.DateTimeDurationFormatter;
import rf.configtool.util.TabUtil;

/**
 * Report data row
 */
public class ObjRow extends Obj implements IsSynthesizable {

    static final String reportTypes = "|String|FileLine|int|float|boolean|null|Date|Duration|";

    private List<Value> rowData;


    public ObjRow(List<Value> rowData) {
        this.rowData=rowData;
        add(new FunctionGet());
        add(new FunctionFile());
        add(new FunctionDir());
        add(new FunctionDate());
        add(new FunctionDuration());
        add(new FunctionShow());
        add(new FunctionAsStringsRow());
        add(new FunctionAsList());
        add(new FunctionContains());
        add(new FunctionLineNo());
        add(new FunctionLines());
    }


    private String getValueType (Value v) {
        if (v instanceof ValueObj) {
            return ((ValueObj) v).getVal().getTypeName();
        }
        return v.getTypeName();
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
            return "get(n) - return value of column n, defaults to 0";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            int n=0;
            if (params.size() == 1) {
                n = (int) getInt("n", params, 0);
            }
            return rowData.get(n);
        }
    }


    class FunctionFile extends Function {
        public String getName() {
            return "file";
        }
        public String getShortDesc() {
            return "file() - return first File object or null if none";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            for (Value v : rowData) {
                if (getValueType(v).equals("File")) return v;
            }
            for (Value v : rowData) {
                if (getValueType(v).equals("FileLine")) {
                    ValueObjFileLine fl=(ValueObjFileLine) v;
                    return new ValueObj(fl.getFile());

                }
            }
            return new ValueNull();
        }
    }

    class FunctionDir extends Function {
        public String getName() {
            return "dir";
        }
        public String getShortDesc() {
            return "dir() - return first Dir or dir of first File, or null if none";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            for (Value v : rowData) {
                if (getValueType(v).equals("Dir")) return v;
            }
            ObjFile file=null;
            for (Value v : rowData) {
                if (getValueType(v).equals("File")) {
                    file = (ObjFile) ((ValueObj) v).getVal();
                    break;
                }
            }
            if (file==null) {
                for (Value v : rowData) {
                    if (getValueType(v).equals("FileLine")) {
                        ValueObjFileLine fl=(ValueObjFileLine) v;
                        file=fl.getFile();
                        break;
                    }
                }
    
            }
            if (file != null) {
                String path = file.getPath();
                int i=path.lastIndexOf(File.separator);
                if (i>=0) path=path.substring(0,i);
                if (path.equals("")) path=File.separator;
                return new ValueObj(new ObjDir(path, file.getProtection()));
            }

            return new ValueNull();
        }
    }

    class FunctionDate extends Function {
        public String getName() {
            return "date";
        }
        public String getShortDesc() {
            return "date() - return first Date object or null if none";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            for (Value v : rowData) {
                if (getValueType(v).equals("Date")) return v;
            }
            return new ValueNull();
        }
    }


    class FunctionDuration extends Function {
        public String getName() {
            return "duration";
        }
        public String getShortDesc() {
            return "duration() - return first Duration object or null if none";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            for (Value v : rowData) {
                if (getValueType(v).equals("Duration")) return v;
            }
            return new ValueNull();
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


    class FunctionAsStringsRow extends Function {
        public String getName() {
            return "asStringsRow";
        }
        public String getShortDesc() {
            return "asStringsRow() - returns Row with printable columns as strings";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");

            List<Value> newRowData=new ArrayList<Value>();
            for (Value v:rowData) {

                String type;
                if (v instanceof ValueObj) {
                    type=((ValueObj) v).getVal().getTypeName();
                } else {
                    type=v.getTypeName();
                }
                if(reportTypes.contains("|" + type + "|")) {
                    newRowData.add(new ValueString(v.getValAsString()));
                }
            }
            return new ValueObj(new ObjRow(newRowData));
        }
    }


    class FunctionAsList extends Function {
        public String getName() {
            return "asList";
        }
        public String getShortDesc() {
            return "asList() - return column values as List";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            return new ValueList(rowData);
        }
    }



    class FunctionContains extends Function {
        public String getName() {
            return "contains";
        }
        public String getShortDesc() {
            return "contains(String) - checks against printable columns, returns boolean";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected search string parameter");
            String str = getString("String", params, 0);

            for (Value v : rowData) {
                String type;
                if (v instanceof ValueObj) {
                    type = ((ValueObj) v).getVal().getTypeName();
                } else {
                    type = v.getTypeName();
                }
                if (reportTypes.contains("|" + type + "|")) {
                    if (v.getValAsString().contains(str)) return new ValueBoolean(true);
                }
            }
            return new ValueBoolean(false);
        }
    }


    class FunctionLineNo extends Function {
        public String getName() {
            return "lineNo";
        }
        public String getShortDesc() {
            return "lineNo() - find first FileLine object and return lineNo from it, or null if none";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            for (Value v : rowData) {
                if (getValueType(v).equals("FileLine")) {
                    ValueObjFileLine fl=(ValueObjFileLine) v;
                    return new ValueInt(fl.getLineNo());
                }
            }
            return new ValueNull();
        }
    }


    class FunctionLines extends Function {
        public String getName() {
            return "lines";
        }
        public String getShortDesc() {
            return "lines([[backCount]?,fwdcount]?) - if row contains a FileLine, display content accordingly";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            long backCount=0;
            long fwdCount=20;

            if (params.size()==1) {
                fwdCount = getInt("fwdCount", params, 0);
            } else if (params.size()==2) {
                backCount = getInt("fwdCount", params, 0);
                fwdCount = getInt("fwdCount", params, 1);
            } else if (params.size() > 2) {
                throw new Exception("Expected parameters [[backCount]?, fwdCount]?");
            }

            for (Value v : rowData) {
                if (getValueType(v).equals("FileLine")) {
                    ValueObjFileLine fl=(ValueObjFileLine) v;
                    long lineNo=fl.getLineNo();
                    long startLine=lineNo-backCount;
                    if (startLine < 1) startLine=1;
                    long endLine=lineNo + fwdCount;
                    ObjFile f=fl.getFile();
                    displayLines(ctx,f,startLine,endLine);
                    return new ValueString(f.getPath() + " : " + lineNo);
                }
            }
            // not found
            return new ValueNull();
        }

        private void displayLines (Ctx ctx, ObjFile f, long startLine, long endLine) throws Exception{
            BufferedReader br=null;
            try {
                br=f.getBufferedReader();
                long lineNo=0;
                for (;;) {
                    String line=br.readLine();
                    lineNo++;
                    if (line==null) break;

                    if (lineNo >= startLine && lineNo <= endLine) {
                        String deTabbed = TabUtil.substituteTabs(line, 4);
                        ctx.getStdio().println(deTabbed);
                    }
                    if (lineNo >= endLine) break;
                }
            } finally {
                if (br != null) try {br.close();} catch (Exception ex) {};
            }
        }
    }
}