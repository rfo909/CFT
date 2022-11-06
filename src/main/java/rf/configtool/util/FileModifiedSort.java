package rf.configtool.util;

import java.util.Comparator;
import java.util.List;
import java.io.File;

public class FileModifiedSort {

	public static void sort (List<File> files) {
		files.sort(getLastModifiedComparator());
	}

	public static Comparator<File> getLastModifiedComparator() {
	    return new Comparator<File>() {
	        public int compare(File a, File b) {
	            long ia;
	            try {
	                ia=a.lastModified();
	            } catch (Exception ex) {
	                ia=0L;
	            }
	            long ib;
	            try {
	                ib=b.lastModified();
	            } catch (Exception ex) {
	                ib=0L;
	            }
	            if (ia>ib) return -1;
	            if (ia==ib) return 0;
	            return 1;
	            
	        }
	    };
	}

}