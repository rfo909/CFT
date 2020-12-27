package rf.configtool.util;

import java.security.MessageDigest;

public class Encrypt {
	
	private int[] matrix;
	private int[] readPos=new int[N];
	private int[] maxPos=new int[N];  // different length for maximum period

	
	private byte[] makeKey (byte[] password, byte[] salt, byte[] post) throws Exception {
		MessageDigest md1 = MessageDigest.getInstance("SHA1"); // 160 bits = 20 bytes
		md1.update(password);
		md1.update(salt);
		md1.update(post);
		return md1.digest();
	}

	public Encrypt (byte[] password, byte[] salt) throws Exception {
		final byte[] pre1 = "w/-P0 ;4ZP#xi*)8(E.OKd03Pfr=L2w".getBytes("ISO-8859-1");
		final byte[] pre2 = "0_ue09Umlu&(/s0t;V6:b&av#5-(,kPoD".getBytes("ISO-8859-1");
		
		final byte[] key1=makeKey(password,salt,pre1);
		final byte[] key2=makeKey(password,salt,pre2);
		final byte[] key3=makeKey(password,salt,key1);
		final byte[] key4=makeKey(password,salt,key2);
		
		final int len1=7;
		final int len2=13;
		final int len3=11;
		final int len4=19;
	
		
		matrix=new int[11000];
		
		int pos1=0;
		int pos2=0;
		int pos3=0;
		int pos4=0;
		
		for (int i=0; i<matrix.length; i++) {
			int sum=key1[pos1] + key2[pos2] + key3[pos3] + key4[pos4];

			pos1=(pos1+1)%len1;
			pos2=(pos2+1)%len2;
			pos3=(pos3+1)%len3;
			pos4=(pos4+1)%len4;
			
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
