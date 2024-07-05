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

package rf.configtool.root.shell;

import java.util.ArrayList;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.PropsFile;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.lib.ObjFile;
import rf.configtool.main.runtime.lib.Protection;



public class ShellGrep extends ShellCommand {

    @Override
    public String getName() {
        return "grep";
    }
    @Override 
    public String getBriefExampleParams() {
        return "<word|str> <file> ... - ex: grep test *.txt";
    }
    

     public Value execute(Ctx ctx, Command cmd) throws Exception {

        String name=cmd.getCommandName();
        
        String currentDir = ctx.getObjGlobal().getCurrDir();
        List<Arg> args = cmd.getArgs();
        
        if (args.size() < 2) throw new Exception(name + ": expected pattern, file ...");
        
        Arg str=args.get(0);
        if (str.isExpr()) throw new Exception(name + ": invalid pattern");
        
        FileSet fs=new FileSet(name,false,true);  // files only
        fs.setIsSafeOperation();

        for (int i=1; i<args.size(); i++) {
            Arg arg=args.get(i);
            fs.processArg(currentDir, ctx, arg);
        }
        
        List<String> files=fs.getFiles();
        if (files.size()==0) throw new Exception("Expected one or more files");
        
        List<Value> fileList=new ArrayList<Value>();
        for (String f:files) {
            fileList.add(new ValueObj(new ObjFile(f,Protection.NoProtection)));
        }
        
        return callMacro(ctx, name, str.getString(), new ValueList(fileList) );
    }

     
    private Value callMacro (Ctx ctx, String name, String strExpr, Value fileList) throws Exception {

        PropsFile propsFile=ctx.getObjGlobal().getRoot().getPropsFile();
        String lambda=propsFile.getMGrep();
        Value[] lambdaArgs= {new ValueString(strExpr), fileList};

        return callConfiguredLambda(name, ctx, lambda, lambdaArgs);
    }

    

}
