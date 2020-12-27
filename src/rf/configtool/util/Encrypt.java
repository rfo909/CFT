package rf.configtool.util;

import java.security.MessageDigest;

public class Encrypt {
	
	private int[] matrix;
	private int[] readPos=new int[N];
	private int[] maxPos=new int[N];  // different length for maximum period


	public Encrypt (byte[] password, byte[] salt) throws Exception {
		// 7 x 11 x 17 x 29 = 37961 (max unique sequence)
		byte[] key1 = "w/-P32w".getBytes("ISO-8859-1");
		byte[] key2 = "0lu&av#kPoD".getBytes("ISO-8859-1");
		byte[] key3 = "_u0 Kd0;5-(,e09Um".getBytes("ISO-8859-1");
		byte[] key4 = "*)8&(/s0t4ZP#xiO;VPfr=L6:b(E.".getBytes("ISO-8859-1");

		matrix=new int[11000];
		
		int pos1=0;
		int pos2=0;
		int pos3=0;
		int pos4=0;
		
		for (int i=0; i<matrix.length; i++) {
			int sum=key1[pos1] + key2[pos2] + key3[pos3] + key4[pos4];

			pos1=(pos1+1)%key1.length;
			pos2=(pos2+1)%key2.length;
			pos3=(pos3+1)%key3.length;
			pos4=(pos4+1)%key4.length;
			if (sum<0) sum=-sum;
			matrix[i]=sum%25; 
				// Collapsing many values on to each other
				// creates a less "crispy" result, which means longer
				// sequences of known characters will match output before failing - harder to crack
		}
		
		// Initialize counters
		MessageDigest md1 = MessageDigest.getInstance("SHA1"); // 160 bits = 20 bytes
		md1.update(password);
		md1.update(salt);
		final byte[] secretHash=md1.digest(); // used to decide start positions for N counters

		for (int i=0;i<N;i++) {
			maxPos[i]=COUNTER_MAX[i];

			readPos[i]=(secretHash[i*2])+(secretHash[i*2+1]<<8);
			while (readPos[i]<0) readPos[i] += maxPos[i];
			if (readPos[i]>=maxPos[i]) {
				readPos[i]=(readPos[(i+1)%N] & 0x0AA0) % maxPos[i];
			}
			//System.out.println(readPos[i]);
		}

	}

	// Number of counters moving independently inside the matrix buffer
	
	public static final int N = 10;
		// with N counters, the number of start positions is calculated
		// multiplying the N first values from COUNTER_MAX array below. Since all are
		// (just) above 10k, each counter contributes with 10^4 new possible start values
		//
		// N=5: 10^20
		// N=6: 10^24
		// N=7: 10^28
		// N=8: 10^32
		// N=9: 10^36
		// N=10: 10^40
		//

	final int[] COUNTER_MAX= {
			10357,  10369,  10391,  10399,  10427,  10429,  10433,  10453,  10457,  10459 
	};

	private byte[] intToBytes(int i) {
		byte[] arr=new byte[4];
		arr[0]=(byte) ((i>>24) & 0xFF);
		arr[1]=(byte) ((i>>16) & 0xFF);
		arr[2]=(byte) ((i>>8) & 0xFF);
		arr[3]=(byte) (i & 0xFF);
		return arr;
	}
	
	public byte process (boolean encrypt, byte value) throws Exception {
		int a=value;
		for (int j=0; j<N; j++) {
			if (encrypt) {
				a=a+matrix[readPos[j]];
			} else {
				a=a+256-matrix[readPos[j]];
			}
			readPos[j]=(readPos[j]+1)%maxPos[j];
		}
		return (byte) (a%256);
	}


}
