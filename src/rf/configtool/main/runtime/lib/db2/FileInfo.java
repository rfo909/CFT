package rf.configtool.main.runtime.lib.db2;

import java.io.File;

public 	class FileInfo {
	private File file;
	
	
	private long size;
	private long lastModified;
	
	public FileInfo (File file) {
		this.file=file;
		if (file==null) throw new RuntimeException("file==null");
		init();
	}
	private void init() {
		if (!file.exists()) {
			this.size=0L;
			this.lastModified=0L;
		} else {
			this.size=file.length();
			this.lastModified=file.lastModified();
		}
	}
	
	public boolean isChanged() {
		long s=(file.exists() ? file.length() : 0L);
		long m=(file.exists() ? file.lastModified() : 0L);
		
		return s != size || m != lastModified;
	}
	
	public void sync() {
		init();
	}
	
	public File getFile() {
		return file;
	}
	
	public void deleteFile() throws Exception {
		if (file.exists()) file.delete();
		if (file.exists()) throw new Exception("Could not delete collection file: " + file.getCanonicalPath());
	}
}
