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

import java.util.ArrayList;
import java.util.List;

import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueString;

import rf.configtool.main.Stdio;

/**
 * Used by Dir.runCapture() function
 */
public class RunCaptureOutput {
    private List<String> lines=new ArrayList<String>();

    private Stdio stdio;

    public RunCaptureOutput() {
        // default constructor
    }

    public RunCaptureOutput (Stdio stdio) {
        this.stdio=stdio;
    }
    public void addLine (String line) {
        lines.add(line);
        if (stdio != null) stdio.println(line);
        while(lines.size()>5000) {
            lines.remove(0);
        }
    }
    
    public Value getCapturedLines() {
        List<Value> stdoutLines=new ArrayList<Value>();
        for (String s:lines) stdoutLines.add(new ValueString(s));
        
        return new ValueList(stdoutLines);
    }
}

