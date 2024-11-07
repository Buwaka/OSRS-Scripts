package Utilities.Encryption;

import com.auth0.client.auth.AuthAPI;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class Keygen
{
    private static final String Domain        = "semanresu-osrs.eu.auth0.com";
    private static final String RedirectURL   = "https://semanresu-osrs.eu.auth0.com/callback";
    private static final String ID            = "8YYikSYE0ktnj6li2kzjUKUZteTYnyUs";
    private static final String Secret   = "SkejuSed0PxRa02mETYelbIkyi-O6gHMscEOCy2ZfNQnVZlmqFosMkNXV16CMaxX";
    private static final String Audiance = "IFScript-Users";

    public static void main(String[] args) throws Exception
    {
        String user = "test";
        String Key  = ".D=!yC=7W)igjq,";

        //        HttpGet GetCodeRequest = new HttpGet(buildAuthorizationUrl(GetCodeChallenge()));
        //        try (CloseableHttpClient client = HttpClients.createDefault()) {
        //            try (CloseableHttpResponse response = client.execute(GetCodeRequest)) {
        //                System.out.println(EntityUtils.toString(response.getEntity()));
        //            }
        //        }


        AuthAPI auth = new AuthAPI(Domain, ID, Secret);
        var response = auth.requestToken(
                                             "https://" + Domain + "/oauth/token")
                                     .addParameter("grant_type", "password")
                                     .addParameter("username", user)
                                     .addParameter("password", Key)
                                     .addParameter("audience", Audiance).execute();
                                     //.addParameter("scope", SCOPE)





        // Return the access token if authentication is successful
        System.out.print(response);

    }

    static String GetCodeChallenge() throws NoSuchAlgorithmException
    {
        SecureRandom sr   = new SecureRandom();
        byte[]       code = new byte[32];
        sr.nextBytes(code);
        String verifier = Base64.getUrlEncoder().withoutPadding().encodeToString(code);

        byte[]        bytes = verifier.getBytes(StandardCharsets.US_ASCII);
        MessageDigest md    = MessageDigest.getInstance("SHA-256");
        md.update(bytes, 0, bytes.length);
        byte[] digest = md.digest();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String buildAuthorizationUrl(String codeChallenge)
    {
        //var urlBuilder = new AuthorizeUrlBuilder(okhttp3.HttpUrl.get(Domain), ID, RedirectDomain);

        String State    = "TestState";
        String Audiance = "Semanresu-OSRS-API";
        return "https://" + Domain + "/authorize?response_type=code&code_challenge=" +
               codeChallenge + "&code_challenge_method=S256&client_id=" + ID +
               "&scope=SCOPE&audience=" + Audiance + "&state=" + State;
    }
}
