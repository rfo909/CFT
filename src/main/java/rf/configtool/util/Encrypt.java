/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2022 Roar Foshaug

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

public class Encrypt {

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

    /*
    To break this algorithm, given access to the code, and without guessing the
    passoword + salt, AND assuming that the code breaker knows or guesses
    some plain-text for encrypted data, one then has to guess a combination 
    of N start points.
    
    For each, call process() on the first plain byte. This will most likely fail, and so
    one must try a new start position, until one gets a match. When this happens, on average
    every  1/256 attempts, one advances to the next byte, and calls process(). This will
    match every 1/256, and so on. When a sufficient number of bytes match, one has
    found the encryption key. If the same password + salt is used on other data, these
    can be decrypted as well.
    
    But the number of starting positions is described above. With N=8 we have 10^32
    different start positions.

    My laptop CPU (Ryzen 5 5300) manages more than 100 MBytes per second (single thread),
    which means 10^8 bytes per second. A top CPU may do ten times that, but let's assume
    hundred times, and also assume 1000 CPU's. 10^8 x 100 x 1000 = 10^13 combinations
    checked per second.

    10^32 / 10^13 = 10^19 seconds = 3x10^11 years. 

    On average one would find the key in half that time, though .... :-)

    Safe enough for a while. Increasing to 10 pointers, of course increases the
    start space by factor 10^8.

    Also note, that if the known sequence of plain-text is not at the start of the data,
    then the same method applies, for finding the key, but in order to find the start
    condition, one have to calculate backwards. 

    This would have been simple, if it weren't for the occasional "jump" of two pointers,
    based on a (to the code breaker) unknown original location of those two pointers.

    -

    In addition to proper use of salt, if the application starts by 
    encrypting a number of random or unknown bytes, such as the hash of the salt, 
    before adding data, breaking becomes factor (X+1) times harder, where X is the number 
    of (unknown or secret) bytes prefixing the secret data.

    */  
    
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

    /**
    * Constructor: sets up matrix, readPos and maxPos for N counters
    */
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
            matrix[i]=sum%50; 
                // Collapsing many values on to each other
                // creates a less "crispy" result, which means longer
                // sequences of known characters will match output before failing - harder to crack
        }
        
        // Initialize counters
        MessageDigest md1 = MessageDigest.getInstance("SHA1"); // 160 bits = 20 bytes
        md1.update(password);
        md1.update(salt);
        md1.update(key1);
        md1.update(key2);
        md1.update(key3);
        md1.update(key4);
        final byte[] secretHash=md1.digest(); // used to decide start positions for N counters

        md1.update(key1);
        md1.update(password);
        md1.update(salt);
        md1.update(key2);
        final byte[] secretHash2=md1.digest(); // used to decide start positions for N counters

        for (int i=0;i<N;i++) {
            maxPos[i]=COUNTER_MAX[i];

            readPos[i]=(secretHash[i*2])+(secretHash[i*2+1]<<8) + (secretHash2[i*2]<<16) + secretHash2[i*2+1];
            if (readPos[i]<0) readPos[i] = -readPos[i];
            readPos[i]=readPos[i] % maxPos[i];
            //System.out.println(readPos[i]);
        }

    }



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
        // occasionally jump two pointers 
        if (matrix[readPos[0]]>=45 && matrix[readPos[1]]<=5) {
            int x=readPos[2]%N;
            int y=readPos[3]%N;
            readPos[x]=(readPos[x]+readPos[y]) % maxPos[x];
            readPos[y]=(readPos[y]+readPos[4] + 1) % maxPos[y];
        }
        return (byte) (a%256);
    }


}
