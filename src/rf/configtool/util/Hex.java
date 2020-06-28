package rf.configtool.util;

public class Hex {
    
    static final String HEX_DIGITS = "0123456789ABCDEF";
    
    public static String toHex (byte[] buf, int count) {
        if (count==0) return "";
        StringBuffer sb=new StringBuffer();
        for (int i=0; i<count; i++) {
            byte b=buf[i];
            sb.append(HEX_DIGITS.charAt((b>>4) & 0x0F));
            sb.append(HEX_DIGITS.charAt(b & 0x0F));
        }
        return sb.toString();
    }
    
    public static byte[] fromHex (String s) {
        if (s.length()==0) return new byte[0];
        byte[] buf=new byte[s.length()/2];
        for (int i=0; i<buf.length; i++) {
            buf[i]=(byte) Integer.parseInt(s.substring(i*2, i*2+2), 16);
        }
        return buf;
    }

}
