package rf.configtool.root.shell;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import rf.configtool.lexer.TokenStream;
import rf.configtool.main.Ctx;
import rf.configtool.main.FunctionBody;
import rf.configtool.main.FunctionState;
import rf.configtool.main.ObjGlobal;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueNull;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.lib.ObjDir;
import rf.configtool.main.runtime.lib.ObjFile;
import rf.configtool.main.runtime.lib.ObjGlob;
import rf.configtool.main.runtime.lib.Protection;

public class ShellLs extends ShellCommand {

	private boolean showFiles;
	private boolean showDirs;

	public ShellLs(List<String> parts) throws Exception {
		super(parts);
		
		String name=getName();
		
		if (name.equals("nls")) {
			showFiles = true;
			showDirs = true;
		} else if (name.equals("nlsd")) {
			showFiles = false;
			showDirs = true;
		} else if (name.equals("nlsf")) {
			showFiles = true;
			showDirs = false;
		} else {
			throw new Exception("Expected nls, nlsf or nlsd");
		}
	}

	public Value execute(Ctx ctx) throws Exception {

		String currDir = ctx.getObjGlobal().getCurrDir();
		boolean noArgs=getArgs().isEmpty();
		boolean enableLimits=noArgs;
		
		FileSet fs=new FileSet(showDirs, showFiles, enableLimits);
		
		if (noArgs) {
			ObjGlob glob=new ObjGlob("*");
			String errMsg = fs.addDirContent(currDir, glob);
			if (errMsg != null) {
				ctx.addSystemMessage(errMsg);
				return new ValueNull();
			}
			
			return generateResultList(fs);
		} 
		
		// process args, some of which may be expressions

		List<ShellCommandArg> args=getArgs();
		
		for (ShellCommandArg arg:args) {
			if (arg.isExpr()) {
				Value v=arg.resolveExpr(ctx);
				if (v instanceof ValueString) {
					fs.processArg(currDir, ((ValueString) v).getVal());
				} else if (v instanceof ValueObj) {
					Obj obj=((ValueObj) v).getVal();
					if (obj instanceof ObjFile) {
						fs.addFilePath( ((ObjFile) obj).getFile().getCanonicalPath() );
					} else if (obj instanceof ObjDir) {
						fs.addDirectoryPath ( ((ObjDir) obj).getDir().getCanonicalPath() );
					}
				}
			} else {
				fs.processArg(currDir, arg.getString());
			}

			if (fs.getFiles().size()==0 && fs.getDirectories().size()==1) {
				// ls someDir ---> list content inside that dir
				String singleDir=fs.getDirectories().get(0);
				
				fs=new FileSet(showDirs,showFiles, enableLimits);
				fs.addDirContent(singleDir, new ObjGlob("*"));
			}

		}
		
		return generateResultList(fs);

	}


	
	private Value generateResultList(FileSet fs) throws Exception {
		List<String> dirList=fs.getDirectories();
		List<String> fileList=fs.getFiles();
		
		if (showDirs) {
			sort(dirList);
		} else {
			dirList.clear();
		}
		if (showFiles) {
			sort(fileList);
		} else {
			fileList.clear();
		}

		List<Value> result = new ArrayList<Value>();
		for (String x : dirList)
			result.add(new ValueObj(new ObjDir(x, Protection.NoProtection)));
		for (String x : fileList)
			result.add(new ValueObj(new ObjFile(x, Protection.NoProtection)));

		return new ValueList(result);

	}

}
