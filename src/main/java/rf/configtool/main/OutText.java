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

package rf.configtool.main;

import java.util.ArrayList;
import java.util.List;

import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueString;

/**
 * This object buffers data for creating formatted reports
 */
public class OutText {
    
    
    private List<List<Value>> data=new ArrayList<List<Value>>();

    public void addReportData (List<Value> values) {
        data.add(values);
    }
    
    public void addReportData (String... values) {
        List<Value> vList=new ArrayList<Value>();
        for (String s:values) vList.add(new ValueString(s));
        addReportData(vList);
    }
    
    
    public List<List<Value>> getData() {
        return data;
    }

   
}
