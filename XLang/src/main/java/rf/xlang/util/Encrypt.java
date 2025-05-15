/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2025 Roar Foshaug

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

package rf.xlang.util;

import java.security.MessageDigest;

/**
 * This class implements a "stream cipher", in the form of a state machine, which essentially creates
 * a pseudo-random stream of bytes.
 * 
 * When encrypting, these are added to the plain-text bytes, and when decrypting, they are subtracted
 * from the encrypted value (modulo positive 256). The period of the stream is over 10^40 bytes.
 * 
 * To use it as a random-number generator, just call the process() method in encrypt or decrypt
 * mode, and some fixed value, for example 0. 
 * 
 * What this class DOES NOT do, is include mechanisms for obtaining lost synchronization in the
 * case of lost bytes, or even verify that decryption is valid. Entering the wrong password or
 * salt, when decrypting data, will result in a totally invalid result, but this code has no way of
 * knowing if that is the case, as it adds no initial sanity check bytes. 
 * 
 * The idea of password + salt, is that the password is secret, while the salt is publicly associated
 * with each encrypted block of data (ex. file). The salt varies the initialization of the
 * state machine, both the content of the matrix, which is a buffer of 11000 pseudo random bytes, and the 
 * start position for N pointers, which point into the matrix.
 * 
 * The algorithm
 * -------------
 * To generate one pseudo-random number, we add the values referred to by all pointers into the matrix, then
 * advance each pointer by one. Each pointer has a different max value, all being primes just above 10.000,
 * for maximum period. 
 * 
 * In addition, there is a "jump" mechanism, which for certain values of two of the pointers, jumps two random
 * pointers, to break the regularity. Not knowing the matrix and the positions, there is no way to know if
 * the jump criteria are met, nor to know which pointers are modified, and how. 
 */

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
     * To break this encryption, given full access to the code, and assuming the
     * code breaker knows (some of) the original content of a file, the task becomes
     * guessing the matrix content and N pointers, which for N=10 is 10^40 possibilities.
     * 
     * Also, as the matrix isn't known, an intruder can not know if the conditions of some 
     * imaginary current position leads to a "pointer jump" or not. These should be
     * happening roughly every 10 bytes, modifying two random pointers each time.
     * 
     */  
    
    private int[] matrix;
    private int[] pointerPositions=new int[N];

    
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
        
        // Populate matrix with data
        for (int i=0; i<matrix.length; i++) {
            int sum=key1[pos1] + key2[pos2] + key3[pos3] + key4[pos4];

            pos1=(pos1+1)%len1;
            pos2=(pos2+1)%len2;
            pos3=(pos3+1)%len3;
            pos4=(pos4+1)%len4;
            
            if (sum<0) sum=-sum;
            matrix[i]=sum%50; 
                // Collapsing many values on to each other
                // creates a less "crisp" result
        }
        
        // Initialize pointer positions into the matrix
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
            pointerPositions[i]=(secretHash[i*2])+(secretHash[i*2+1]<<8) + (secretHash2[i*2]<<16) + secretHash2[i*2+1];
            
            if (pointerPositions[i]<0) pointerPositions[i] = -pointerPositions[i];
            
            pointerPositions[i]=pointerPositions[i] % MAX_POINTER_POS[i];
        }

    }



    final int[] MAX_POINTER_POS= {
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
                a=a+matrix[pointerPositions[j]];
            } else {
                a=a+256-matrix[pointerPositions[j]];
            }
            pointerPositions[j]=(pointerPositions[j]+1)%MAX_POINTER_POS[j];
        }
        // occasionally jump two pointers 
        if (matrix[pointerPositions[0]]>=45 && matrix[pointerPositions[1]]<=5) {
            int x=pointerPositions[2]%N;
            int y=pointerPositions[3]%N;
            pointerPositions[x]=(pointerPositions[x]+pointerPositions[y]) % MAX_POINTER_POS[x];
            pointerPositions[y]=(pointerPositions[y]+pointerPositions[4] + 1) % MAX_POINTER_POS[y];
        }
        return (byte) (a%256);
    }


}
