package rf.configtool.main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.reporttool.Report;

/**
 * This object buffers two types of text output from code:
 * - plain text in the form of lines
 * - structured data in the form of columns
 * 
 * The Runtime class implements presentation of these, in method processProgramLine,
 * just sending the plain text to stdout, while formatting the structured data into 
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
    private List<String> lines=new ArrayList<String>();
    
    public void addPlainText (String line) {
        lines.add(line);
    }
    
    public void addReportData (List<Value> values) {
        data.add(values);
    }
    
    public void addReportData (String... values) {
    	List<Value> vList=new ArrayList<Value>();
    	for (String s:values) vList.add(new ValueString(s));
    	addReportData(vList);
    }
    
    public List<String> getPlainText() {
        return lines;
    }
    
    public List<List<Value>> getData() {
        return data;
    }

    
    public void clear() {
        lines.clear();
    }

}
