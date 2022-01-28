/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2022 Roar Foshaug

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 3 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>
*/

package rf.configtool.main.runtime.lib.db;

import java.io.File;

public    class FileInfo {
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
