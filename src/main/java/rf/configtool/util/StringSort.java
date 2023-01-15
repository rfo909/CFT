package rf.configtool.util;

import java.util.Comparator;
import java.util.List;

public class StringSort {

    public static void sort (List<String> data) {
        Comparator<String> c=new Comparator<String>() {
            public int compare(String a, String b) {
                return a.compareTo(b);
            }
        };
        data.sort(c);
    }
}
