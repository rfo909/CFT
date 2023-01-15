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

package rf.configtool.root.shell;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.lib.ObjFile;
import rf.configtool.main.runtime.lib.Protection;

public class ShellShell extends ShellCommand {

	@Override
	public String getName() {
		return "shell";
	}
	@Override 
	public String getBriefExampleParams() {
		return null;
	}


    public Value execute(Ctx ctx, Command cmd) throws Exception {

    	String shellCommand;
    	
        if (File.separator.equals("\\")) {
            shellCommand=ctx.getObjGlobal().getRoot().getPropsFile().getWinShell();
        } else {
            shellCommand=ctx.getObjGlobal().getRoot().getPropsFile().getShell();
        }
        callExternalProgram(shellCommand, ctx);
        
        return new ValueBoolean(true);

    }


    

}
