package systems.v.wallet.basic.wallet;

import org.whispersystems.curve25519.SecureRandomProvider;

public class MySecureRandomProvider implements SecureRandomProvider {
    public byte[] btSeed;

    public void SetBytes(byte[] output){
        btSeed = new byte[output.length];
        System.arraycopy(output, 0, btSeed, 0, output.length);
    }

    public void nextBytes(byte[] output){
        System.arraycopy(btSeed, 0, output, 0, btSeed.length);
    }
    public int nextInt(int maxValue){
        return  0;
    }
}
