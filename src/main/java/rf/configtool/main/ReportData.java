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

package rf.configtool.main;

import java.util.ArrayList;
import java.util.List;

import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.util.ReportFormattingTool;

/**
 * This object buffers data for creating formatted reports
 */
public class ReportData {
    
    
	private List<List<Value>> presentation=new ArrayList<List<Value>>();

    public void addReportData (List<Value> presentation) {
        this.presentation.add(presentation);
    }
    
    public int getRowCount() {
    	return presentation.size();
    }
    
   
    public List<List<Value>> getReportPresentationValues() {
        return presentation;
    }
    
    public List<String> getPresentation() {
        ReportFormattingTool report=new ReportFormattingTool();
        List<String> formattedText=report.formatDataValues(presentation);
        return formattedText;
    }

   
}
