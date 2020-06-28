package rf.configtool.util;

public class TabUtil {

    public static String substituteTabs(String s, int count) {
        StringBuffer sb=new StringBuffer();
        for (int pos=0; pos<s.length(); pos++) {
            char c=s.charAt(pos);
            if (c=='\t') {
                int fill=(sb.length())%count;
                if (fill==0) fill=count;
                for (int i=0; i<fill; i++) sb.append(" ");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

}
