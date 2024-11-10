package Utilities.Encryption;

import Utilities.Patterns.SYMaths;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.net.URIBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectStreamClass;
import java.net.URISyntaxException;
import java.util.zip.GZIPOutputStream;

public class SecretFetcher
{

    private static final String AUTH_URL = "https://auth.idp.hashicorp.com/oauth2/token";
    private static final String API_URL  = "https://api.cloud.hashicorp.com";

    private final String clientId;
    private final String clientSecret;
    private       String apiToken;

    public SecretFetcher(String pass)
    {
        String[] parts = pass.split("\\|\\|\\|");
        if(parts == null || parts.length != 2)
        {
            throw new IllegalArgumentException("Invalid pass");
        }
        this.clientId     = parts[0];
        this.clientSecret = parts[1];
    }

    public static String GetSecret() throws IOException, URISyntaxException
    {
        String clientId       = "IlebGGGCVVP7GWbcyhedZdL0Twowacz8";
        String clientSecret   = "baqbJmcA0IhMZW2Z2pqkvdILVgGmLU5myjqCFSYJjE4p8HqESrwI4Mwy62uoainK";
        String organizationId = "264ed4c7-e8b0-4e05-8a7d-8c46810360d0";
        String projectId      = "e3edcc21-e33e-4b69-885b-c796dd1e15ba";
        String appId          = "OSRS-Scripts";

        String pass = clientId + "|||" + clientSecret;

        SecretFetcher api    = new SecretFetcher(pass);
        String        secret = api.getSecret(organizationId, projectId, appId);
        return secret;
    }

    public String getSecret(String organizationId, String projectId, String appId) throws
            IOException,
            URISyntaxException
    {
        String url = String.format("%s/secrets/2023-06-13/organizations/%s/projects/%s/apps/%s/open",
                                   API_URL,
                                   organizationId,
                                   projectId,
                                   appId);

        URIBuilder builder = new URIBuilder(url);

        try(CloseableHttpClient httpClient = HttpClients.createDefault())
        {
            HttpGet request = new HttpGet(builder.build());
            request.setHeader("Authorization", "Bearer " + Key());

            try(ClassicHttpResponse response = httpClient.execute(request))
            {
                HttpEntity entity = response.getEntity();

                if(entity != null)
                {
                    String     jsonResponse = EntityUtils.toString(entity);
                    JsonObject jsonObject   = JsonParser.parseString(jsonResponse)
                                                        .getAsJsonObject();
                    //                    System.out.print(jsonObject);

                    var secretObj = jsonObject.get("secrets");
                    if(secretObj != null)
                    {
                        var secrets = secretObj.getAsJsonArray();
                        for(var secret : secrets)
                        {
                            var obj = secret.getAsJsonObject();
                            if(obj != null)
                            {
                                var name = obj.get("name");
                                String serialVersionUID = 'a' +
                                                          String.valueOf(Math.abs(ObjectStreamClass.lookup(
                                                                                                           SYMaths.class)
                                                                                                   .getSerialVersionUID()));
                                if(name != null && name.isJsonPrimitive() &&
                                   name.getAsString().equals(serialVersionUID))
                                {
                                    var version = obj.get("version");
                                    if(version != null && version.getAsJsonObject() != null)
                                    {
                                        var value = version.getAsJsonObject().get("value");
                                        if(value != null && value.isJsonPrimitive())
                                        {
                                            return value.getAsString();
                                        }
                                    }
                                }
                            }
                        }
                    }


                    return null;
                }
                else
                {
                    throw new IOException("Failed to retrieve secret. Empty response entity.");
                }
            } catch(ParseException e)
            {
                throw new RuntimeException(e);
            }
        } catch(IOException e)
        {
            throw new IOException("Failed to retrieve secret.", e);
        }
    }

    public String Key() throws IOException
    {
        if(apiToken == null)
        {
            apiToken = retrieveApiToken();
        }
        return apiToken;
    }

    private String retrieveApiToken() throws IOException
    {
        try(CloseableHttpClient httpClient = HttpClients.createDefault())
        {
            HttpPost request = new HttpPost(AUTH_URL);
            request.setHeader("Content-Type", "application/x-www-form-urlencoded");

            String body = String.format(
                    "client_id=%s&client_secret=%s&grant_type=client_credentials&audience=https://api.hashicorp.cloud",
                    clientId,
                    clientSecret);
            request.setEntity(new StringEntity(body));

            try(ClassicHttpResponse response = httpClient.execute(request))
            {
                System.out.print(response);
                HttpEntity entity = response.getEntity();
                if(response.getCode() < 200 || response.getCode() >= 300)
                {
                    return null;
                }
                if(entity != null)
                {
                    String     jsonResponse = EntityUtils.toString(entity);
                    JsonObject jsonObject   = JsonParser.parseString(jsonResponse)
                                                        .getAsJsonObject();
                    return jsonObject.get("access_token").getAsString();
                }
                else
                {
                    throw new IOException("Failed to retrieve API token. Empty response entity.");
                }
            }
        } catch(IOException | ParseException e)
        {
            throw new IOException("Failed to retrieve API token.", e);
        }
    }

    public static void main(String[] args) throws IOException, URISyntaxException
    {
        String clientId       = "IlebGGGCVVP7GWbcyhedZdL0Twowacz8";
        String clientSecret   = "baqbJmcA0IhMZW2Z2pqkvdILVgGmLU5myjqCFSYJjE4p8HqESrwI4Mwy62uoainK";
        String organizationId = "264ed4c7-e8b0-4e05-8a7d-8c46810360d0";
        String projectId      = "e3edcc21-e33e-4b69-885b-c796dd1e15ba";
        String appId          = "OSRS-Scripts";

        String pass = clientId + "|||" + clientSecret;

        ByteArrayOutputStream out  = new ByteArrayOutputStream();
        GZIPOutputStream      gzip = new GZIPOutputStream(out);
        gzip.write(pass.getBytes());
        gzip.close();
        //        pass = Base64.getEncoder().encodeToString(out.toByteArray());

        SecretFetcher api    = new SecretFetcher(pass);
        String        secret = api.getSecret(organizationId, projectId, appId);
        System.out.println("Secret: " + secret);
    }
}
