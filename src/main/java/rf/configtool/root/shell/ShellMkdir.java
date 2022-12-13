package rf.configtool.root.shell;

import java.io.File;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.lib.ObjDir;
import rf.configtool.main.runtime.lib.Protection;

public class ShellMkdir extends ShellCommand {

	public ShellMkdir(List<String> parts) throws Exception {
		super(parts);
	}

	public Value execute(Ctx ctx) throws Exception {

		String currentDir = ctx.getObjGlobal().getCurrDir();
		List<ShellCommandArg> args=getArgs();
		String name=getName();
		
		FileSet fs=new FileSet(name,true,false);
		
		for (ShellCommandArg arg:args) {
			fs.processArg(currentDir, ctx, arg, true, false);
		}
		
		boolean allOk=true;
		List<String> dirs=fs.getDirectories();
		for (String dir:dirs) {
			File f=new File(dir);
			
			if (f.exists()) {
				ctx.addSystemMessage("Directory exists: " + f.getCanonicalPath());
			} else {
				boolean ok = f.mkdir();
				if (!ok) {
					ctx.addSystemMessage("Failed to create: " + f.getCanonicalPath());
					allOk=false;
				}
			}
		}
		return new ValueBoolean(allOk);
	}


	

}
