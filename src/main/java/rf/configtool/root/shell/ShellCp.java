package rf.configtool.root.shell;

import java.io.*;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;


public class ShellCp extends ShellCommand {

	private boolean confirmed=false;
	
	public ShellCp(List<String> parts) throws Exception {
		super(parts);
	}
	
	private boolean copyFile (Ctx ctx, File src, File target) throws Exception {
		FileInputStream fis=null;
		FileOutputStream fos=null;
		try {
			fis=new FileInputStream(src);
			fos=new FileOutputStream(target);
			byte[] buf=new byte[64*1024];
			for (;;) {
				int count=fis.read(buf);
				if (count <= 0) break;
				fos.write(buf,0,count);
			}
			return true;
		} catch (Exception ex) {
			return false;
		} finally {
			if (fis != null) try {fis.close();} catch (Exception ex) {};
			if (fos != null) try {fos.close();} catch (Exception ex) {};
		}
	}
	
	private boolean copyTree (Ctx ctx, File srcDir, File targetDir) throws Exception {
		
		if (targetDir.exists() && !targetDir.isDirectory()) {
			throw new Exception(getName() + ": target not a directory");
		}
		if (!confirmed) {
			ctx.getStdio().println("Confirm copy " + srcDir.getCanonicalPath() + " -> " + targetDir.getCanonicalPath());
			ctx.getStdio().println("Type 'yes' to continue");
			String line = ctx.getStdio().getInputLine();
			if (!line.equals("yes")) {
				return false;
			}
			confirmed=true;
		}
		
		if (!targetDir.exists()) {
			boolean ok = targetDir.mkdir();
			if (!ok) return false;
		}

		boolean allOk=true;
		for (File f : srcDir.listFiles()) {
			if (f.isDirectory()) {
				File t=new File(targetDir.getCanonicalPath() + File.separator + f.getName());
				if (!copyTree(ctx, f, t)) allOk=false;
			} else if (f.isFile()) {
				File t=new File(targetDir.getCanonicalPath() + File.separator + f.getName());
				if (!copyFile(ctx, f, t)) allOk=false;
			}
		}
		return allOk;
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
			
			// cp existingDir -> newDir ### copy
			// cp existingDir -> otherExistingDir ### cp into other directory
			if (dirs.size()==1 && files.size()==0) {
				File src=new File(dirs.get(0));
				
				FileSet fsTarget=new FileSet(true,false); // directories only
				fsTarget.processArg(currentDir, ctx, lastArg, true, false); // allow new dir
				List<String> targetDirs=fsTarget.getDirectories();

				if (targetDirs.size() != 1) throw new Exception(name + ": expected single target dir");
				File target=new File(targetDirs.get(0));
				
				if (target.exists()) {
					target=new File(target.getCanonicalPath() + File.separator + src.getName());
				} 
				
				boolean ok = copyTree(ctx,src,target);
				ctx.addSystemMessage((ok?"OK   ":"FAIL ") + name + ": " + src.getCanonicalPath() + " -> " + target.getCanonicalPath());
				return new ValueBoolean(ok);		
			}
			
			// cp existingFile -> newFile  ### file copy
			// cp existingFile -> otherExistingFile ### copy + overwrite
			// cp existingFile -> existingDir ### copy into dir
			if (files.size()==1 && dirs.size()==0) {
				File src=new File(files.get(0));
				
				FileSet fsTarget=new FileSet(true,true); // directory or file
				fsTarget.processArg(currentDir, ctx, lastArg, false, true); // allow new files, but not new directories
				
				if (fsTarget.getDirectories().size() + fsTarget.getFiles().size() != 1) {
					throw new Exception(name + ": expected single target dir or file");
				}
	
				File target;
				if (fsTarget.getDirectories().size()==1) {
					target=new File(fsTarget.getDirectories().get(0) + File.separator + src.getName());
				} else {
					target=new File(fsTarget.getFiles().get(0));
				}
				boolean ok = copyFile(ctx,src,target);
				ctx.addSystemMessage((ok?"OK   ":"FAIL ") + name + ": " + src.getCanonicalPath() + " -> " + target.getCanonicalPath());
				
				return new ValueBoolean(ok);		
			}
			
		}
		
		// ---------------------------------------------------------------------
		// The general case, where args.size >= 2 or number of src elements > 1 (globbing)
		// -> this means target must always be existing dir
		// ---------------------------------------------------------------------
		{
			FileSet fsTarget=new FileSet(true,false); // directories only
			fsTarget.processArg(currentDir, ctx, lastArg); // no new anything allowed
			
			if (fsTarget.getDirectories().size() != 1) {
				throw new Exception(name + ": expected single target dir");
			}
			
			File target = new File(fsTarget.getDirectories().get(0));
			if (!target.exists() || !target.isDirectory()) throw new Exception(name + ": internal error");
			
			boolean allOk=true;
			
			// iterate over files and directories in args
			for (String f:fsSource.getFiles()) {
				File src=new File(f);
				File fTarget=new File(target.getCanonicalPath() + File.separator + src.getName());
				boolean ok=copyFile(ctx,src,fTarget);
				ctx.addSystemMessage((ok?"OK   ":"FAIL ") + name + ": " + src.getCanonicalPath() + " -> " + fTarget.getCanonicalPath());
				if (!ok) allOk=false;
			}
			for (String f:fsSource.getDirectories()) {
				File src=new File(f);
				File dTarget=new File(target.getCanonicalPath() + File.separator + src.getName());
				boolean ok=copyTree(ctx,src,dTarget);
				ctx.addSystemMessage((ok?"OK   ":"FAIL ") + name + ": " + src.getCanonicalPath() + " -> " + dTarget.getCanonicalPath());
				if (!ok) allOk=false;
			}
			return new ValueBoolean(allOk);
		}

	}


}
