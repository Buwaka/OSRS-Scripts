package Utilities.Encryption;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ResourceEncryptor
{
    private static final String ALGORITHM = "AES";

    public static String decryptResource(String resourcePath, String encryptionKey)
    {
        try(InputStream is = ResourceEncryptor.class.getResourceAsStream(resourcePath))
        {
            if(is == null)
            {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }

            byte[] encryptedData = is.readAllBytes();

            SecretKeySpec keySpec = new SecretKeySpec(encryptionKey.getBytes(), ALGORITHM);
            Cipher        cipher  = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            byte[] decryptedData = cipher.doFinal(encryptedData);
            return new String(decryptedData);

        } catch(Exception e)
        {
            throw new RuntimeException("Error decrypting resource: " + resourcePath, e);
        }
    }

    public static void main(String[] args) throws Exception
    {
        String encryptionKey = SecretFetcher.GetSecret(); // Get key from command line arguments
        String inputDir      = System.getProperty("resources.input.directory");
        String outputDir     = System.getProperty("resources.output.directory");
        //        String encryptionKey = "TEsteroniiAAA123"; // Get key from command line arguments
        //                String inputDir      = "C:\\Users\\SammyLaptop\\Documents\\Git\\OSRS-Scripts\\target\\resources";
        //                String outputDir     = "C:\\Users\\SammyLaptop\\Documents\\Git\\OSRS-Scripts\\target\\encrypted-resources";

        SecretKeySpec keySpec = new SecretKeySpec(encryptionKey.getBytes(), ALGORITHM);
        Cipher        cipher  = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);

        encryptDirectory(cipher, inputDir, outputDir);
    }

    private static void encryptDirectory(Cipher cipher, String sourceDir, String destDir) throws
            Exception
    {
        File dir = new File(sourceDir);
        if(!dir.exists())
        {
            return;
        }

        Files.createDirectories(Path.of(destDir)); // Ensure output directory exists

        for(File file : dir.listFiles())
        {
            if(file.isDirectory())
            {
                encryptDirectory(cipher, file.getAbsolutePath(), destDir + "/" + file.getName());
            }
            else
            {
                encryptFile(cipher, file, new File(destDir, file.getName()));
            }
        }
    }

    private static void encryptFile(Cipher cipher, File inputFile, File outputFile) throws Exception
    {
        System.out.print(inputFile.toString() + "\n");
        System.out.print(outputFile.toString() + "\n");
        try
        {
            FileInputStream  fis       = new FileInputStream(inputFile);
            FileOutputStream fos       = new FileOutputStream(outputFile);
            byte[]           input     = fis.readAllBytes();
            byte[]           encrypted = cipher.doFinal(input);
            fos.write(encrypted);
        } catch(Exception e)
        {
            System.out.print(e + "\n");
        }
    }
}
