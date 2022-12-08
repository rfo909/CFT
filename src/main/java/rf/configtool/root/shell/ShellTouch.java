package rf.configtool.root.shell;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.lib.ObjFile;
import rf.configtool.main.runtime.lib.Protection;

public class ShellTouch extends ShellCommand {

	public ShellTouch(List<String> parts) throws Exception {
		super(parts);
	}

	public Value execute(Ctx ctx) throws Exception {

		String currDir = ctx.getObjGlobal().getCurrDir();
		boolean noArgs=getArgs().isEmpty();
		
		if (noArgs) throw new Exception("touch: expected file(s)");
		
		List<ShellCommandArg> args=getArgs();
		
		FileSet fs=new FileSet(false,true);  // files only
		for (ShellCommandArg arg:args) {
			fs.processArg(currDir, ctx, arg, false, true);  // allow unknown files
		}
		
		List<String> files=fs.getFiles();
		List<Value> results=new ArrayList<Value>();
		
		for (String filename:files) {
			File file=new File(filename);
	        if (!file.exists()) {
	            file.createNewFile();
	        } else {
	            Path path=file.toPath();
	            FileTime ft = FileTime.fromMillis(System.currentTimeMillis());
	            Files.setLastModifiedTime(path, ft);
	        }
	        Value result=new ValueObj(new ObjFile(file.getCanonicalPath(), Protection.NoProtection));
	        results.add(result);
		}
		if (results.size()==1) return results.get(0);
		return new ValueList(results);
	}


	

}
