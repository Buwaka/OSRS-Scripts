package IF.Utilities.Encryption;


import org.apache.commons.codec.digest.DigestUtils;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.json.JSONObject;
import org.passay.CharacterData;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Base64;

import static org.passay.IllegalCharacterRule.ERROR_CODE;

public class Keygen
{
    private final String DOMAIN        = "dev-b7mcmaf4lhvamwbg.eu.auth0.com";
    private final String CLIENT_ID     = "ifCwHKTYuVyDJexP8AXAiokJbIaqNd0H";
    private final String CLIENT_SECRET = "3tSSyoNBJcwV-OXIULunnzvltWNVWWRBestAH6FwVyHB1UvYdykLCgVmZ_he05IL";
    private final String AUDIENCE      = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"; // API identity

    private boolean SignUp(String username, String password)
    {
        HttpPost postRequest = new HttpPost(String.format("https://%s/dbconnections/signup",
                                                          DOMAIN));
        postRequest.setHeader("Content-Type", "application/json");

        JSONObject requestBody = new JSONObject();
        requestBody.put("username", username);
        requestBody.put("password", password);
        requestBody.put("connection", "UserPass");
        requestBody.put("client_id", CLIENT_ID);
        requestBody.put("client_secret", CLIENT_SECRET);
        requestBody.put("user_metadata",
                        new JSONObject().put("Binary",
                                             GetUserFile(username)));

        postRequest.setEntity(new StringEntity(requestBody.toString()));

        boolean success = false;
        try
        {
            success = HttpClients.createDefault().execute(postRequest, (response) -> {
                if(response.getCode() >= 200 && response.getCode() < 300)
                {
                    return true;
                }
                return false;
            });
        } catch(Exception e)
        {
            System.out.print(e);
        }
        return success;
    }

    static String GetUserFile(String username)
    {
        return Base64.getEncoder().encodeToString(DigestUtils.sha256((username)));
    }

    static String RandomString(int length)
    {
        PasswordGenerator gen            = new PasswordGenerator();
        CharacterData     lowerCaseChars = EnglishCharacterData.LowerCase;
        CharacterRule     lowerCaseRule  = new CharacterRule(lowerCaseChars);
        lowerCaseRule.setNumberOfCharacters(2);

        CharacterData upperCaseChars = EnglishCharacterData.UpperCase;
        CharacterRule upperCaseRule  = new CharacterRule(upperCaseChars);
        upperCaseRule.setNumberOfCharacters(2);

        CharacterData digitChars = EnglishCharacterData.Digit;
        CharacterRule digitRule  = new CharacterRule(digitChars);
        digitRule.setNumberOfCharacters(2);

        CharacterData specialChars = new CharacterData()
        {
            public String getErrorCode()
            {
                return ERROR_CODE;
            }

            public String getCharacters()
            {
                return "!@#$%^&*()_+";
            }
        };
        CharacterRule splCharRule = new CharacterRule(specialChars);
        splCharRule.setNumberOfCharacters(2);

        return gen.generatePassword(length, splCharRule, lowerCaseRule, upperCaseRule, digitRule);
    }

    static void MakeBinaryFile(String name)
    {
        var paths = System.getProperty("java.class.path");
        var path = paths.substring(0,paths.indexOf(';')).replace("classes","");
        try(FileInputStream input = new FileInputStream(path + "Juicy") )
        {
            FileOutputStream output = new FileOutputStream(path + name);
            output.write(input.readAllBytes());
            System.out.print("Now go upload it to cloudflare cuz I ain't installing amazon sdks here " + path + name + "\n");
        }catch(Exception e)
        {
            System.out.print("Failed to write file, " + e+ "\n");
        }
    }

    static String MakeKey(String username, String password)
    {
        JSONObject obj = new JSONObject();
        obj.put(username, password);
        var key = Base64.getEncoder().encodeToString(obj.toString().getBytes());
        return key;
    }


    public static void main(String[] args) throws Exception
    {
        Keygen gen      = new Keygen();
        String username = "FullDragons";
        String pass     = RandomString(20);

        if(gen.SignUp(username, pass))
        {
            System.out.print("Password: " + pass + "\n");
            MakeBinaryFile(GetUserFile(username));

            var key = MakeKey(username, pass);
            System.out.print("Key: " + key + "\n");
            var userpass = Authenticator.GetUserPass(key);
            System.out.print("userpass: " + userpass + "\n");

            if(userpass._1.contentEquals(username) && userpass._2.contentEquals(pass))
            {
                System.out.print("Key is OK!\n");
            }
            else
            {
                System.out.print("Key is not OK!\n");
            }
        }




    }


}
