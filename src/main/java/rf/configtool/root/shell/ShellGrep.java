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

	public ShellGrep(List<String> parts) throws Exception {
		super(parts);
	}

	public Value execute(Ctx ctx) throws Exception {

		String name=getName();
		
		String currentDir = ctx.getObjGlobal().getCurrDir();
		List<ShellCommandArg> args = getArgs();
		
		if (args.size() < 2) throw new Exception(name + ": expected pattern, file ...");
		
		ShellCommandArg str=args.get(0);
		if (str.isExpr()) throw new Exception(name + ": invalid pattern");
		
		FileSet fs=new FileSet(name,false,true);  // files only
		fs.setIsSafeOperation();

		for (int i=1; i<args.size(); i++) {
			ShellCommandArg arg=args.get(i);
			fs.processArg(currentDir, ctx, arg);
		}
		
		List<String> files=fs.getFiles();
		if (files.size()==0) throw new Exception("Expected one or more files");
		
		List<Value> fileList=new ArrayList<Value>();
		for (String f:files) {
			fileList.add(new ValueObj(new ObjFile(f,Protection.NoProtection)));
		}
		
		return callMacro(ctx, str.getString(), new ValueList(fileList) );
	}

	  private Value callMacro (Ctx ctx, String strExpr, Value fileList) throws Exception {

 		PropsFile propsFile=ctx.getObjGlobal().getRoot().getPropsFile();
        String lambda=propsFile.getMGrep();
		Value[] lambdaArgs= {new ValueString(strExpr), fileList};

		return callConfiguredLambda(getName(), ctx, lambda, lambdaArgs);
    }

	

}
