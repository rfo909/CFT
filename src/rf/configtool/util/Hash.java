package rf.configtool.util;

import java.security.MessageDigest;

public class Hash {
    
    private MessageDigest digest;
    
    public Hash () throws Exception {
        this("SHA-256");
    }
    public Hash (String alg) throws Exception {
        digest=MessageDigest.getInstance("SHA-256");  // 32 bytes hash
    }
    public void add (byte[] data) {
        digest.update(data);
    }
    public byte[] getHashBytes() {
        return digest.digest();
    }
    public String getHashString() {
        byte[] hash=digest.digest();

        String digits="0123456789abcdef";
        StringBuffer sb=new StringBuffer();
        for (int i=0; i<hash.length; i++) {
            byte b=hash[i];
            sb.append(digits.charAt( (b>>4) & 0x0F ));
            sb.append(digits.charAt( b & 0x0F ));
        }
        return sb.toString();
    }
}
 
