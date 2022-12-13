package rf.configtool.root.shell;

import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.lib.ObjDir;
import rf.configtool.main.runtime.lib.Protection;

public class ShellCd extends ShellCommand {

	public ShellCd(List<String> parts) throws Exception {
		super(parts);
	}

	public Value execute(Ctx ctx) throws Exception {

		String currDir = ctx.getObjGlobal().getCurrDir();
		boolean noArgs=getArgs().isEmpty();
		
		if (noArgs) {
	        ctx.getObjGlobal().setCurrDir(null);
	        ctx.getObjGlobal().addSystemMessage(ctx.getObjGlobal().getCurrDir());
	        return new ValueObj(new ObjDir(ctx.getObjGlobal().getCurrDir(), Protection.NoProtection));
		}
		
		List<ShellCommandArg> args=getArgs();
		if (args.size() != 1) throw new Exception("cd: expected zero or one arguments");
		
		FileSet fs=new FileSet(getName(), true, false);  // directories only
		fs.setIsSafeOperation();
		
		ShellCommandArg arg=args.get(0);
		fs.processArg(currDir, ctx, args.get(0));
		
		List<String> dirs=fs.getDirectories();
		if (dirs.size() != 1) throw new Exception("cd: got " + dirs.size() + " matches, should be one");
		
        ctx.getObjGlobal().setCurrDir(dirs.get(0));
        ctx.getObjGlobal().addSystemMessage(ctx.getObjGlobal().getCurrDir());
        return new ValueObj(new ObjDir(ctx.getObjGlobal().getCurrDir(), Protection.NoProtection));
	}


	

}
