package IF.Utilities.Encryption;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Encryption
{


    private static final String ALGORITHM = "AES";


    public static byte[] decrypt(byte[] data, byte[] key) throws Exception
    {
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        Cipher        cipher  = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        return cipher.doFinal(data);
    }

    public static byte[] encrypt(byte[] data, byte[] key) throws Exception
    {
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        Cipher        cipher  = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);

        return cipher.doFinal(data);
    }
}
