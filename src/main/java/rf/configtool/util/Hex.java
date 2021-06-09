/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020 Roar Foshaug

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

public class Hex {
    
    static final String HEX_DIGITS = "0123456789ABCDEF";

    public static String toHex (byte[] buf) {
        return toHex(buf, buf.length);
    }

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
