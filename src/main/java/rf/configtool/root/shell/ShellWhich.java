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

import java.util.ArrayList;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.PropsFile;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueNull;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.lib.ObjFile;
import rf.configtool.main.runtime.lib.Protection;



public class ShellWhich extends ShellCommand {

    public ShellWhich(List<String> parts) throws Exception {
        super(parts);
    }

    public Value execute(Ctx ctx) throws Exception {

        String name=getName();
        
        boolean noArgs=getArgs().isEmpty();
        
        if (noArgs) {
        	throw new Exception(name + ": expected program name");
        }        
        
        List<ShellCommandArg> args=getArgs();
        if (args.size()>1) throw new Exception(name + ": expected single program name");
        
        ShellCommandArg arg=args.get(0);
        if (arg.isExpr()) throw new Exception(name + ": expected single name (string)");
        
        return callMacro(ctx, new ValueString(arg.getString()));
    }

    
      private Value callMacro (Ctx ctx, ValueString str) throws Exception {

	    PropsFile propsFile=ctx.getObjGlobal().getRoot().getPropsFile();
        String lambda=propsFile.getMWhich();
        Value[] lambdaArgs= {str};

        return callConfiguredLambda(getName(), ctx, lambda, lambdaArgs);
    }

    

}
