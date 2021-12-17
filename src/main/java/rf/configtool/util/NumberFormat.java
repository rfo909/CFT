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
