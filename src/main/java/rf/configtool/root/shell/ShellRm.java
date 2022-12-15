package rf.configtool.root.shell;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import rf.configtool.lexer.SourceLocation;
import rf.configtool.main.CFTCallStackFrame;
import rf.configtool.main.Ctx;
import rf.configtool.main.FunctionBody;
import rf.configtool.main.FunctionState;
import rf.configtool.main.PropsFile;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBlock;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.lib.ObjDir;
import rf.configtool.main.runtime.lib.ObjFile;
import rf.configtool.main.runtime.lib.Protection;


public class ShellRm extends ShellCommand {

	public ShellRm(List<String> parts) throws Exception {
		super(parts);
	}

	public Value execute(Ctx ctx) throws Exception {
		
		final String currentDir = ctx.getObjGlobal().getCurrDir();
		String name=getName();
		List<ShellCommandArg> args=getArgs();
		
		FileSet fs=new FileSet(name,true,true); // files and directories
		for (ShellCommandArg arg:args) {
			fs.processArg(currentDir, ctx, arg);
		}
		
		List<Value> result=new ArrayList<Value>();
		for (String fname : fs.getFiles()) {
			result.add(new ValueObj(new ObjFile(fname, Protection.NoProtection)));
		}
		for (String dname : fs.getDirectories()) {
			result.add(new ValueObj(new ObjDir(dname, Protection.NoProtection)));
		}
		
		
        PropsFile propsFile=ctx.getObjGlobal().getRoot().getPropsFile();
        String lambda=propsFile.getMRm();
		Value[] lambdaArgs= {new ValueList(result)};

		return callConfiguredLambda(getName(), ctx, lambda, lambdaArgs);
	}


}