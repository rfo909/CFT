package rf.configtool.root.shell;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.lib.ObjGlob;

public class FileSet {
	
	private HashSet<String> paths=new HashSet<String>();
	
	public static final long LS_DEFAULT_TIMEOUT_MS = 6000;
	// list files and dirs in currDir - default (no parameters) is guarded by a
	// timeout, since
	// working with remote directories, and many files, ls can take very long time.

	public static final int LS_DEFAULT_MAX_ENTRIES = 2000;


	private List<String> directoryList=new ArrayList<String>();
	private List<String> fileList=new ArrayList<String>();;
	private final boolean includeDirs;
	private final boolean includeFiles;
	private final boolean enableLimits;
	
	private boolean isWindows() {
		return File.separatorChar=='\\';
	}

	public FileSet (boolean includeDirs, boolean includeFiles) {
		this(includeDirs,includeFiles, false);
	}

	public FileSet (boolean includeDirs, boolean includeFiles, boolean enableLimits) {
		this.includeDirs=includeDirs;
		this.includeFiles=includeFiles;
		this.enableLimits=enableLimits;
	}
	
	public void addFilePath (String path) {
		if (!includeFiles) return;
		String key=(isWindows() ? path.toLowerCase() : path);
		if (paths.contains(key)) return; // already present
		paths.add(key);
		fileList.add(path);
	}

	public void addDirectoryPath (String path) {
		if (!includeDirs) return;
		String key=(isWindows() ? path.toLowerCase() : path);
		if (paths.contains(key)) return; // already present
		paths.add(key);
		directoryList.add(path);
	}

	public List<String> getFiles() {
		return fileList;
	}
	
	public List<String> getDirectories() {
		return directoryList;
	}
	
	/**
	 * Default processing, when no args. Contains checks on duration and number of files, to
	 * avoid hanging forever. To override, supply arg, for ex '*' or some path.
	 */
	public String addDirContent(String dir, ObjGlob glob) throws Exception {
		File f = new File(dir);

		long startTime = System.currentTimeMillis();

		DirectoryStream<Path> stream = Files.newDirectoryStream(f.toPath());
		Iterator<Path> iter = stream.iterator();

		int totalCount = 0;
		while (iter.hasNext()) {
			Path p = iter.next();

			File x = p.toFile();
			if (!glob.matches(x.getName())) continue;
			
			String path = x.getCanonicalPath();
			if (x.isFile()) {
				addFilePath(path);
			} else if (x.isDirectory()) {
				addDirectoryPath(path);
			}

			totalCount++;

			long duration = System.currentTimeMillis() - startTime;

			if (duration > LS_DEFAULT_TIMEOUT_MS) {
				return "--- directory listing timed out after " + LS_DEFAULT_TIMEOUT_MS + ", use '*' to override";
			}
			if (totalCount >= LS_DEFAULT_MAX_ENTRIES) {
				return "--- directory entry count > " + LS_DEFAULT_MAX_ENTRIES + ", use '*' to override";
			}
		}
		
		return null;

	}

	
	/**
	 * Processing argument to shell function, doing wildcard globbing and
	 * detecting absolute paths.
	 */
	public void processArg (String currentDir, String arg) throws Exception {
		boolean isAbsolute;

		if (isWindows()) {
			isAbsolute=arg.startsWith("\\") || (arg.length() >= 3 && arg.charAt(1)==':' && arg.charAt(2)=='\\');
		} else {
			isAbsolute=arg.startsWith("/");
		}
		
		int lastSeparatorPos = arg.lastIndexOf(File.separatorChar);
		boolean containsSeparators = (lastSeparatorPos >= 0);
		
		int globPos = arg.lastIndexOf('*');
		boolean usesGlobbing = (globPos >= 0);
		
		if (usesGlobbing && containsSeparators && globPos < lastSeparatorPos) {
			throw new Exception("Invalid globbing pattern: " + arg);
		}
		
		if (!usesGlobbing) {
			File f;

			if (isAbsolute) {
				f=new File(arg);
			} else {
				f=new File(currentDir + File.separator + arg);
			}
			
			if (f.exists()) {
				if (f.isDirectory()) {
					this.addDirectoryPath(f.getCanonicalPath());
				} else if (f.isFile()) {
					this.addFilePath(f.getCanonicalPath());
				}
			} else {
				throw new Exception("No such file or directory: " + f.getCanonicalPath());
			}
			
			return;
		}
		
		// Uses globbing!
		
		ObjGlob glob;
		String theDir;
		
		if (!containsSeparators) {
			glob=new ObjGlob(arg);
			theDir=currentDir;
		} else {
	
			glob=new ObjGlob(arg.substring(lastSeparatorPos+1));

			if (isAbsolute) {
				theDir=arg.substring(0,lastSeparatorPos);
			} else {
				theDir=currentDir + File.separator + arg.substring(0,lastSeparatorPos);
			}
		}
		
		this.addDirContent(theDir, glob);
	}

	
}
