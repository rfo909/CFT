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

package rf.configtool.main.runtime.reporttool;

import java.util.ArrayList;
import java.util.List;

import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueObj;

public class Report {
    
    private String fmt(String val, int len) {
        StringBuffer sb=new StringBuffer();
        
        if (val.length() > len) {
            val=val.substring(0,len);
        }
        sb.append(val);
        while(sb.length()<len) sb.append(" ");
        sb.append(" | ");
        return sb.toString();
    }
    
    public List<String> formatData (List<List<String>> data, int maxColWidth) {
        List<Integer> colWidth=new ArrayList<Integer>();
        for (List<String> line:data) {
            for (int col=0; col<line.size(); col++) {
                String colVal=line.get(col);
                if (colVal==null) colVal="null";
                
                while(colWidth.size()<col+1) colWidth.add(0);
                if (colVal.length() > colWidth.get(col)) {
                    int width=colVal.length();
                    if (maxColWidth > 0 && maxColWidth < width) width=maxColWidth;
                    colWidth.set(col, width);
                }
            }
        }
        List<String> result=new ArrayList<String>();
        for (List<String> line:data) {
            StringBuffer sb=new StringBuffer();
            for (int col=0; col<line.size(); col++) {
                if (col==line.size()-1) {
                    // last col is not padded, but may be truncated
                    String s=line.get(col);
                    if (maxColWidth>0 && s.length()>maxColWidth) {
                        s=s.substring(0,maxColWidth);
                    }
                    sb.append(s);
                } else {
                    sb.append(fmt(line.get(col), colWidth.get(col)));
                }
            }
            result.add(sb.toString());
        }
        
        return result;
    }
    
    
    public List<String> formatDataValues (List<List<Value>> data) {
        return formatDataValues(data,0);
    }
    
    public List<String> formatDataValues (List<List<Value>> data, int maxColWidth) {
        List<List<String>> strData=new ArrayList<List<String>>();
        for (List<Value> line:data) {
            List<String> strLine=new ArrayList<String>();
            for (Value v:line) {
                strLine.add(v.getValAsString());
            }
            strData.add(strLine);
        }
        return formatData(strData, maxColWidth);
    }
    
    
    public List<String> displayValueLines (Value value) {
        List<String> list=new ArrayList<String>();
        if (!(value instanceof ValueList)) {
            if (value instanceof ValueObj) {
                list.add("  <obj: " + ((ValueObj) value).getVal().getTypeName() + ">");
                list.add("  " + ((ValueObj) value).getVal().getContentDescription().getCompactDisplay());
            } else {
                list.add("  <" + value.getTypeName() + ">");
                list.add("  " + value.getContentDescription().getCompactDisplay());
            }
        } else {
            
            list.add("  <List>");
            List<Value> values=((ValueList) value).getVal();
            if (values.size()==0) {
                list.add("  (empty)");
                return list;
            }
            
            List<List<String>> txtData=new ArrayList<List<String>>();

            for (Value v:values) {
                if (v instanceof ValueObj) {
                    txtData.add( ((ValueObj) v).getVal().getContentDescription().getCols() );
                } else {
                    txtData.add( v.getContentDescription().getCols() );
                }
            }
            
            List<String> lines = formatData(txtData, 0);
            for (int i=0; i<lines.size(); i++) {
                String line=lines.get(i);
                list.add(" " + fmt(i,3) + ": " + line);
            }
            
        }
        
        return list;
        
    }
    
    private String fmt (int i, int n) {
        String s=""+i;
        while (s.length()<n) s=" "+s;
        return s;
    }

}
