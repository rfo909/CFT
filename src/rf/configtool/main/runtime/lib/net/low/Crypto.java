package rf.configtool.main.runtime.lib.net.low;

public class Crypto {


	private final int[] pos= {
			103,31,490,290,1
	};
	private final int[] adv= {
			1,2,1,1,1
	};
	private final byte[] cryptoBlock;

	
	public Crypto (String accessCode) throws Exception {
		Hash h=new Hash();
		cryptoBlock=h.getCryptoBlock(accessCode);		
	}
	
	public byte[] encrypt (byte[] data, int offset) {
		int[] currPos=new int[pos.length];
		for (int i=0; i<currPos.length; i++) {
			currPos[i]=pos[i] + cryptoBlock[pos[i]] + offset;
			if (currPos[i] < 0) currPos[i]=-currPos[i];
		}
		byte[] result=new byte[data.length];
		for (int i=0; i<data.length; i++) {
			int sum=data[i];
			for (int j=0; j<currPos.length; j++) {
				currPos[j] += adv[j];
				currPos[j] %= cryptoBlock.length;
				sum += cryptoBlock[currPos[j]];
			}
			while (sum < 0) sum += 256;
			result[i]=(byte) (sum%256);
		}
		return result;
	}
	
	public byte[] decrypt (byte[] data, int offset) {
		int[] currPos=new int[pos.length];
		for (int i=0; i<currPos.length; i++) {
			currPos[i]=pos[i] + cryptoBlock[pos[i]] + offset;
			if (currPos[i] < 0) currPos[i]=-currPos[i];
		}
		byte[] result=new byte[data.length];
		for (int i=0; i<data.length; i++) {
			int sum=data[i];
			for (int j=0; j<currPos.length; j++) {
				currPos[j] += adv[j];
				currPos[j] %= cryptoBlock.length;
				sum -= cryptoBlock[currPos[j]];
			}
			while (sum < 0) sum += 256;
			result[i]=(byte) (sum%256);
		}
		return result;
	}
	
	public static void main (String... args) {
		final String testString=
				"txxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxhis is a longer test æøåÆØÅ"
				+"txxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxhis is a longer test æøåÆØÅ"
				+"txxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxhis is a longer test æøåÆØÅ"
				+"txxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxhis is a longer test æøåÆØÅ"
				+"txxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxhis is a longer test æøåÆØÅ"
				+"txxxxxxxxxxxxxx 600 characters xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxhis is a longer test æøåÆØÅ"
			;
			// 600 characters = 16ms on PC
		
			// not very fast, but hopefully quite good
		
		try {
			long startTime=System.currentTimeMillis();
			Crypto e=new Crypto("longSecretPassword!23");
			byte[] x=e.encrypt(testString.getBytes("UTF-8"),10);
			
			byte[] y=e.decrypt(x,10);
			String result=new String(y,"UTF-8");
			long endTime=System.currentTimeMillis();

			System.out.println(result.equals(testString) ? "OK" : "ERROR");
			System.out.println((endTime-startTime) + " ms");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
