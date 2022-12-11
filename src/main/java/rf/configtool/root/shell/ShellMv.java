package rf.configtool.root.shell;

import java.io.File;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;


public class ShellMv extends ShellCommand {
	
	public ShellMv(List<String> parts) throws Exception {
		super(parts);
	}

	public Value execute(Ctx ctx) throws Exception {
		
		final String name=getName(); 

		final String currentDir = ctx.getObjGlobal().getCurrDir();
		List<ShellCommandArg> args=getArgs();
		if (args.size() < 2) throw new Exception(name + ": requires at least two args");
		
		FileSet fsSource = new FileSet(true,true);
		for (int i=0; i<args.size()-1; i++) {
			ShellCommandArg arg=args.get(i);
			fsSource.processArg(currentDir, ctx, arg);
		}
		
		List<String> dirs=fsSource.getDirectories();
		List<String> files=fsSource.getFiles();
		
		ShellCommandArg lastArg=args.get(args.size()-1);

		// special cases, where target may depend on src (two args only, with one src element)
		if (args.size() == 2) {
			
			// mv existingDir -> newDir ### rename
			// mv existingDir -> otherExistingDir ### mv into other directory
			if (dirs.size()==1 && files.size()==0) {
				File src=new File(dirs.get(0));
				
				FileSet fsTarget=new FileSet(true,false);
				fsTarget.processArg(currentDir, ctx, lastArg, true, false); // allow new dir
				List<String> targetDirs=fsTarget.getDirectories();

				if (targetDirs.size() != 1) throw new Exception(name + ": expected single target dir");
				File target=new File(targetDirs.get(0));
				
				if (target.exists()) {
					target=new File(target.getCanonicalPath() + File.separator + src.getName());
				}
				
				verifySourceTargetDirsIndependent(name, src, target);
				
				boolean ok = src.renameTo(target);
				ctx.addSystemMessage((ok?"OK   ":"FAIL ") + name + ": " + src.getCanonicalPath() + " -> " + target.getCanonicalPath());
				return new ValueBoolean(ok);		
			}

			// mv existingFile -> newFile  ### rename
			// mv existingFile -> otherExistingFile ### overwrite
			// mv existingFile -> existingDir ### move into dir
			if (files.size()==1 && dirs.size()==0) {
				File src=new File(files.get(0));
				
				FileSet fsTarget=new FileSet(true,true);
				fsTarget.processArg(currentDir, ctx, lastArg, false, true); // allow new files
				
				if (fsTarget.getDirectories().size() + fsTarget.getFiles().size() != 1) {
					throw new Exception(name + ": expected single target dir or file");
				}
	
				File target;
				if (fsTarget.getDirectories().size()==1) {
					target=new File(fsTarget.getDirectories().get(0) + File.separator + src.getName());
				} else {
					target=new File(fsTarget.getFiles().get(0));
				}
				boolean ok = src.renameTo(target);
				ctx.addSystemMessage((ok?"OK   ":"FAIL ") + name + ": " + src.getCanonicalPath() + " -> " + target.getCanonicalPath());
				
				return new ValueBoolean(ok);		
			}
			
		}
		
		// ---------------------------------------------------------------------
		// The general case, where args.size > 2 or number of src elements > 1 (globbing)
		// -> this means target must always be existing dir
		// ---------------------------------------------------------------------
		{
			FileSet fsTarget=new FileSet(true,false);
			fsTarget.processArg(currentDir, ctx, lastArg); // no new anything allowed
			
			if (fsTarget.getDirectories().size() != 1) {
				throw new Exception(name + ": expected single target dir");
			}
			File target = new File(fsTarget.getDirectories().get(0));
			if (!target.exists() && !target.isDirectory()) throw new Exception(name + ": internal error");
			
			boolean allOk=true;
			
			for (String f:fsSource.getDirectories()) {
				verifySourceTargetDirsIndependent(name, new File(f), target);
			}
			
			// iterate over files and directories in args
			for (String f:fsSource.getFiles()) {
				File src=new File(f);
				File fTarget=new File(target.getCanonicalPath() + File.separator + src.getName());
				boolean ok=src.renameTo(fTarget);
				ctx.addSystemMessage((ok?"OK   ":"FAIL ") + name + ": " + src.getCanonicalPath() + " -> " + fTarget.getCanonicalPath());
				if (!ok) allOk=false;
			}
			for (String f:fsSource.getDirectories()) {
				File src=new File(f);
				File dTarget=new File(target.getCanonicalPath() + File.separator + src.getName());
				boolean ok=src.renameTo(dTarget);
				ctx.addSystemMessage((ok?"OK   ":"FAIL ") + name + ": " + src.getCanonicalPath() + " -> " + dTarget.getCanonicalPath());
				if (!ok) allOk=false;
			}
			return new ValueBoolean(allOk);
		}

	}


}
