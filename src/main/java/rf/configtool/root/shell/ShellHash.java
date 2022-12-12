package rf.configtool.root.shell;

import java.util.ArrayList;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.PropsFile;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.lib.ObjFile;
import rf.configtool.main.runtime.lib.Protection;



public class ShellHash extends ShellCommand {

	public ShellHash(List<String> parts) throws Exception {
		super(parts);
	}

	public Value execute(Ctx ctx) throws Exception {

		String name=getName();
		
		String currentDir = ctx.getObjGlobal().getCurrDir();
		boolean noArgs=getArgs().isEmpty();
		
		if (noArgs) throw new Exception(name + ": expected file(s)");
		
		List<ShellCommandArg> args=getArgs();
		
		FileSet fs=new FileSet(false,true);  // files only
		for (ShellCommandArg arg:args) {
			fs.processArg(currentDir, ctx, arg);  // no unknown files or dirs
		}
		
		List<String> files=fs.getFiles();
		if (files.size() < 1) throw new Exception(name + ": Expected one or more files");
		
		List<Value> list = new ArrayList<Value>();
		
		for (String filename:files) {
			list.add(new ValueObj(new ObjFile(filename, Protection.NoProtection)));
		}
		
		return callMacro(ctx, new ValueList(list));
	}

	  private Value callMacro (Ctx ctx, Value fileList) throws Exception {

 		PropsFile propsFile=ctx.getObjGlobal().getRoot().getPropsFile();
        String lambda=propsFile.getMHash();
		Value[] lambdaArgs= {fileList};

		return callConfiguredLambda(getName(), ctx, lambda, lambdaArgs);
    }

	

}
