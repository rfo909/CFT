package rf.configtool.main.runtime.lib.net.low;

import java.security.MessageDigest;

public class Hash {
	final String alphabet="abcdefgh<>ijklmnopqrstuvwxyz;:@$/&=%.,()?!ABCDEFGHIJKLMNOPQRSTUVWXYZ+-0123456789";
	
	private String encode (int i, int offset) {
		offset=(offset*17)+131;
		
		int a=i & 0xF;
		int b=(i & 0xF0) >> 4;
		
		a=(a+offset)%alphabet.length();
		b=(b+offset)%alphabet.length();
		return ""+alphabet.charAt(a)+alphabet.charAt(b);
	}
	
	public String getHash (String s) throws Exception {
		MessageDigest md=MessageDigest.getInstance("MD5");
		md.update(s.getBytes("UTF-8"));
		byte[] hash=md.digest();
		StringBuffer sb=new StringBuffer();
		for (int i=0; i<hash.length; i++) {
			sb.append(encode(hash[i],i));
		}
		return sb.toString();
	}
	

	public String getHash(String a, String b) throws Exception {
		return getHash(a + "/" + b);
	}
	
	public String getCryptoBlockString (String secret) throws Exception {
		StringBuffer sb=new StringBuffer();
		final String[] prefix= {
				"the idea here is to hash",
				"many strings together with the secret",
				"and so produce a long string og hash output",
				"which is concatenated, then converted",
				"to a sequence of bytes using a rolling base16 encoder",
				"for maximum diversity in the output string",
				"------------------------------------",
				"sdf9g00dsfllllllllllldsdf-sdfdf---sdf",
				"----------------",
				"seems we need more data, to get",
				"big enough block 3994950309999----..",
				"499503 039490 30049 300400 3++4+4",
				"--------------------",
				"so what else is there to say?",
				"security matters! Woha, there we reached 512 bytes",
				"but it should be a prime, aiming for 547",
				"the point of a prime is to get full scatter when encoding"
		};
		
		for (String s:prefix) {
			sb.append(getHash(s, secret));
		}
		
		return sb.toString()+"547";
	}
	
	public byte[] getCryptoBlock (String secret) throws Exception {
		return getCryptoBlockString(secret).getBytes("UTF-8");
	}
	
	public static void main (String... args) {
		try {
			Hash h=new Hash();
			System.out.println(h.getHash("this is a test"));
			
			System.out.println(h.getCryptoBlockString("this is a test"));
			System.out.println(h.getCryptoBlock("this is a test").length);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
}
