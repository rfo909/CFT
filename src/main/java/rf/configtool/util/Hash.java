/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2023 Roar Foshaug

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

package rf.configtool.util;

import java.security.MessageDigest;

public class Hash {
    
    private MessageDigest digest;
    
    public Hash () throws Exception {
        this("SHA-256");
    }
    public Hash (String alg) throws Exception {
        digest=MessageDigest.getInstance(alg);
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
 
