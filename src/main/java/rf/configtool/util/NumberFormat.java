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

public class NumberFormat {
    
    public static String formatInt (long val, String thousandSep) {
        String s;
        boolean neg=false;
        if (val < 0) {
            s=""+(-val);
            neg=true;
        } else {
            s=""+val;
        }
        while (s.length() % 3 != 0) {
            s=" "+s;
        }
        StringBuffer sb=new StringBuffer();
        for (int i=0; i<s.length(); i++) {
            if (i>0 && i%3==0) sb.append(thousandSep);
            sb.append(s.charAt(i));
        }
        return (neg?"-":"") + (sb.toString()).trim();
    }

    public static String formatFloat (double val, String thousandSep, String decimalComma, int numDecimals) {
        String s = new java.math.BigDecimal(val).toPlainString();
        int pos=s.indexOf('.');
        String intPart;
        String decimalPart;
        if (pos < 0) {
            intPart=s;
            decimalPart="000000";
        } else {
            intPart=s.substring(0,pos);
            decimalPart=s.substring(pos+1);
        }
        
        String result = formatInt(Long.parseLong(intPart), thousandSep);
        if (numDecimals > 0) {
            while (decimalPart.length() < numDecimals) decimalPart=decimalPart+"000000";
            result=result+decimalComma+decimalPart.substring(0,numDecimals);
        }
        return result;
    }

}
