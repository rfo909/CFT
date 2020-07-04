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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.reporttool.Report;

/**
 * This object buffers two types of text output from code:
 * - plain text system messages in the form of lines
 * - structured data in the form of columns
 * 
 * The Runtime class implements presentation of these, in method processProgramLine,
 * just sending the plain text to stdout, (prefixed with '#'), while formatting the structured data into 
 * lines that are added to the OutData (same as out(x) in code). This is
 * so that report data can be easily written to file, as well as 
 * processed further (counting, sorting etc)
 * 
 * Example:
 * 
+-----------------------------------------------------
| data  : "a b c d e".split
|
| file  : File("test.txt")
|
| rep   : data->d report(1,2,3,d)
|
| update: file.delete file.create(rep)
+-----------------------------------------------------
 *
 */
public class OutText {
    
    
    private List<List<Value>> data=new ArrayList<List<Value>>();

    // simple text output
    private List<String> systemMessages=new ArrayList<String>();
    
    public void addSystemMessage (String line) {
        systemMessages.add(line);
    }
    
    public void addReportData (List<Value> values) {
        data.add(values);
    }
    
    public void addReportData (String... values) {
        List<Value> vList=new ArrayList<Value>();
        for (String s:values) vList.add(new ValueString(s));
        addReportData(vList);
    }
    
    public List<String> getSystemMessages() {
        return systemMessages;
    }
    
    public List<List<Value>> getData() {
        return data;
    }

   
}
