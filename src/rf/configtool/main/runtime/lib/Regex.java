package rf.configtool.main.runtime.lib;

public class Regex {
    
    public static String createRegex(String pattern, boolean caseSensitive) {
        StringBuffer sb=new StringBuffer();
        for (int i=0; i<pattern.length(); i++) {
            Character c=pattern.charAt(i);
            if (c=='*') {
                sb.append(".*");
            } else if (c=='?') {
                sb.append(".");
            } else if (c=='\\') {
                sb.append("[\\\\]");
            } else if (".^?[]{}()+-".indexOf(c) >= 0) {
                   sb.append("[\\"+c+"]");
            } else {
                if (caseSensitive) {
                    sb.append(c);
                } else {
                    String s=""+c;
                    sb.append("["+s.toLowerCase()+s.toUpperCase()+"]");
                }
            }
            
        }
        return sb.toString();
    }
    
    public static String createGlobRegex (String pattern) {
        return "^" + Regex.createRegex(pattern, false) + "$";
    }
    
}
