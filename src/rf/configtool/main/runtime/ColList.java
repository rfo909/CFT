package rf.configtool.main.runtime;

import java.util.*;

public class ColList {
    
    public static final ColList list() {
        return new ColList();
    }
    
    private List<String> list=new ArrayList<String>();

    private ColList() {
        
    }
    
    public ColList regular(String value) {
        list.add(value);
        return this;
    }

    public ColList regular() {
        list.add("");
        return this;
    }

    public ColList status(String value) {
        list.add(value);
        return this;
    }

    public ColList status() {
        list.add("");
        return this;
    }

    public List<String> getCols() {
        return list;
    }
    
//  public String getSimpleDisplay() {
//      StringBuffer sb=new StringBuffer();
//      for (String s:list) {
//          s=s+" ";
//          while (s.length() % 8 != 0) s=s + " ";
//          sb.append(s);
//      }
//      return sb.toString();
//  }
//
    public String getCompactDisplay() {
        StringBuffer sb=new StringBuffer();
        for (String s:list) {
            sb.append(s+" ");
        }
        return sb.toString();
    }
    
    public String toString() {
        return getCompactDisplay();
    }
}
