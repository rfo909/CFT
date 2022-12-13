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



public class ShellHex extends ShellCommand {

	public ShellHex(List<String> parts) throws Exception {
		super(parts);
	}

	public Value execute(Ctx ctx) throws Exception {

		String name=getName();
		
		String currentDir = ctx.getObjGlobal().getCurrDir();
		boolean noArgs=getArgs().isEmpty();
		
		List<ShellCommandArg> args=getArgs();
		if (noArgs || args.size()>1) throw new Exception(name + ": expected single file");
		
		FileSet fs=new FileSet(name,false,true);  // files only
		fs.setIsSafeOperation();
		
		for (ShellCommandArg arg:args) {
			fs.processArg(currentDir, ctx, arg);  // no unknown files or dirs
		}
		
		List<String> files=fs.getFiles();
		if (files.size() != 1) throw new Exception(name + ": expected single file, got " + files.size());
		
		Value file=new ValueObj(new ObjFile(files.get(0), Protection.NoProtection));
		
		return callMacro(ctx, file);
	}

	
	  private Value callMacro (Ctx ctx, Value file) throws Exception {

 		PropsFile propsFile=ctx.getObjGlobal().getRoot().getPropsFile();
        String lambda=propsFile.getMHex();
		Value[] lambdaArgs= {file};

		return callConfiguredLambda(getName(), ctx, lambda, lambdaArgs);
    }

	

}
