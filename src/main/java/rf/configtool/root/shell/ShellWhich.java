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

    @Override
    public String getName() {
        return "which";
    }
    @Override 
    public String getBriefExampleParams() {
        return "<command>";
    }


    public Value execute(Ctx ctx, Command cmd) throws Exception {

        String name=cmd.getCommandName();
        
        boolean noArgs=cmd.getArgs().isEmpty();
        
        if (noArgs) {
            throw new Exception(name + ": expected program name");
        }        
        
        List<Arg> args=cmd.getArgs();
        if (args.size()>1) throw new Exception(name + ": expected single program name");
        
        Arg arg=args.get(0);
        if (arg.isExpr()) throw new Exception(name + ": expected single name (string)");
        
        return callMacro(ctx, name, new ValueString(arg.getString()));
    }

    
      private Value callMacro (Ctx ctx, String name, ValueString str) throws Exception {

        PropsFile propsFile=ctx.getObjGlobal().getRoot().getPropsFile();
        String lambda=propsFile.getMWhich();
        Value[] lambdaArgs= {str};

        return callConfiguredLambda(name, ctx, lambda, lambdaArgs);
    }

    

}
