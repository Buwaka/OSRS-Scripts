package IF.Utilities.Encryption;

import IF.Utilities.Patterns.Runnables.Runnable1;
import IF.Utilities.Patterns.TimedVariable;
import IF.Utilities.Scripting.Logger;
import io.vavr.Tuple2;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Authenticator
{
    private final String DOMAIN         = "dev-b7mcmaf4lhvamwbg.eu.auth0.com";
    private final String CLIENT_ID      = "ifCwHKTYuVyDJexP8AXAiokJbIaqNd0H";
    private final String CLIENT_SECRET  = "3tSSyoNBJcwV-OXIULunnzvltWNVWWRBestAH6FwVyHB1UvYdykLCgVmZ_he05IL";
    private final String AUDIENCE       = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"; // API identity
    private final String AccessTokenKey = "access_token";
    private final long   time           = System.nanoTime();
    private       byte[] secrets        = null; // byte[] > string > base64 > string > JSONObject > 1 = token, 2 = key, 3 = map

    private final AtomicBoolean IsAuthenticating = new AtomicBoolean(false);

    private static TimedVariable<Authenticator> instance = null;
    private static String                       UserKey  = null;
    private final  long                         seed     = System.nanoTime();

    private final int HttpTimeout = 1000;

    public static Authenticator GetInstance()
    {
        if(instance == null || !instance.isRunning() ||
           System.nanoTime() - instance.getValue().time > TimeUnit.SECONDS.toNanos(10))
        {
            instance = new TimedVariable<>(new Authenticator(), 10, TimeUnit.SECONDS);
            instance.onStop.Subscribe(instance, (inst) -> inst.secrets = null);
        }
        return instance.getValue();
    }

    private Authenticator()
    {

    }


    public boolean isAuthenticated(String key)
    {
        UserKey = key;
        var check = GetScriptKeyMap(GetUser(key), GetPass(key), false);
        return check != null;
    }

    public void isAuthenticated(String key, Runnable1<Boolean> response)
    {
        UserKey = key;
        Thread AuthThread = new Thread(() -> {
            var check = GetScriptKeyMap(GetUser(key), GetPass(key), false);
            response.Run(check != null);
        });
        AuthThread.start();
    }

    public Tuple2<byte[], Object> GetScriptKeyMap(String username, String password, boolean Binary)
    {
        if(IsAuthenticating.get())
        {
            Logger.log("Busy authenticating");
            return null;
        }

        if(username == null || password == null)
        {
            Logger.error("No valid credentials");
            return null;
        }

        IsAuthenticating.set(true);
        Logger.log("Start Auth");
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String              token      = GetSecret(1);
        Logger.log("Token: " + token);
        int attempts    = 0;
        int MaxAttempts = 5;

        while(attempts < MaxAttempts)
        {
            if(token == null || !Binary)
            {
                Logger.log("Token is null");
                token = GetAccessToken(httpClient, username, password, Binary);
                if(token == null)
                {
                    attempts++;
                }
                if(!Binary)
                {
                    WriteSecret(token.getBytes(), null, null);
                    IsAuthenticating.set(false);
                    return new Tuple2<>(new byte[0], new Object());
                }
            }
            else
            {
                Logger.log("Token is not null");
            }


            var data = GetKeyAndData(httpClient, token);
            if(data != null)
            {
                WriteSecret(token.getBytes(),
                            data._1,
                            JSONObject.valueToString(data._2).getBytes());
                IsAuthenticating.set(false);
                return data;
            }
            attempts++;
            token = null;
        }
        IsAuthenticating.set(false);
        return null;
    }

    Tuple2<byte[], Object> GetScriptKeyMap()
    {
        return GetScriptKeyMap(GetUser(UserKey), GetPass(UserKey), true);
    }

    byte[] GetScriptKey()
    {
        var response = GetScriptKeyMap(GetUser(UserKey), GetPass(UserKey), true);
        if(response != null)
        {
            Logger.log(new String(response._1));
            return response._1;
        }
        return null;
    }

    private void WriteSecret(byte[] token, byte[] secret, byte[] data)
    {
        // deterministic random numbers to prevent string literals
        JSONObject obj = new JSONObject();
        Random     random;
        if(secrets != null)
        {
            obj = GetSecret();
        }

        random = new Random(seed);
        int i = random.nextInt();
        if(token != null)
        {
            obj.put(String.valueOf(i), Base64.getEncoder().encodeToString(token));
        }

        i = random.nextInt();
        if(secret != null)
        {
            obj.put(String.valueOf(i), Base64.getEncoder().encodeToString(secret));
        }

        i = random.nextInt();
        if(data != null)
        {
            obj.put(String.valueOf(i), Base64.getEncoder().encodeToString(data));
        }
        secrets = obj.toString().getBytes();
    }

    private String GetSecret(int i)
    {
        var secret = GetSecret();
        if(secret == null)
        {
            return null;
        }
        Random random = new Random(seed);
        for(int j = 0; j < i; j++)
        {
            int a = random.nextInt();
            if((j + 1) == i)
            {
                return secret.getString(String.valueOf(a));
            }
        }
        return null;
    }

    private JSONObject GetSecret()
    {
        if(secrets == null)
        {
            return null;
        }
        JSONObject obj = new JSONObject();
        JSONObject old;
        try
        {
            old = new JSONObject(new String(secrets));
        } catch(Throwable t)
        {
            Logger.error("Failed to retrieve token, getting a new token");
            secrets = null;
            return null;
        }

        Random random = new Random(seed);
        int    e      = 0;
        while(e < 3)
        {
            int i = random.nextInt();
            if(old.has(String.valueOf(i)))
            {
                obj.put(String.valueOf(i), old.getString(String.valueOf(i)));
            }
            e++;
        }
        return obj;
    }


    private String GetAccessToken(HttpClient client, String username, String password, boolean GetBinaryData)
    {
        int      attempts     = 0;
        int      MaxAttempts  = 5;
        HttpPost tokenRequest = new HttpPost(String.format("https://%s/oauth/token", DOMAIN));
        tokenRequest.setHeader("Content-Type", "application/json");

        JSONObject requestBody = new JSONObject();
        requestBody.put("grant_type", "password");
        requestBody.put("username", username);
        requestBody.put("password", password);
        requestBody.put("audience", AUDIENCE);
        requestBody.put("client_id", CLIENT_ID);
        requestBody.put("client_secret", CLIENT_SECRET);
        if(GetBinaryData)
        {
            requestBody.put("Juicy.Sex", true);
        }

        requestBody.put("scope", "openid");
        tokenRequest.setEntity(new StringEntity(requestBody.toString()));


        JSONObject tokenResponse = null;
        while(attempts < MaxAttempts)
        {
            try
            {
                tokenResponse = client.execute(tokenRequest, (response) -> {
                    if(response.getCode() >= 200 && response.getCode() < 300)
                    {
                        String content = new String(response.getEntity()
                                                            .getContent()
                                                            .readAllBytes());
                        return new JSONObject(content);
                    }
                    else
                    {
                        Logger.error("Http error code: " + response.getCode() + ": " +
                                     new String(response.getEntity().getContent().readAllBytes()) +
                                     "\n");
                    }
                    return null;
                });
            } catch(Exception e)
            {
                attempts++;
                Logger.error("Authentication: Failed to get access token " + attempts + ", " + e);
            }

            if(tokenResponse != null && tokenResponse.has(AccessTokenKey))
            {
                return tokenResponse.get(AccessTokenKey).toString();
            }
            else
            {
                Logger.error("Authentication: Failed to get access token " + attempts);
                attempts++;
            }
            try
            {
                Thread.sleep(HttpTimeout);
            } catch(InterruptedException ignored)
            {
            }
        }
        Logger.error("Authentication: Response does not contain token");
        return null;
    }

    private Tuple2<byte[], Object> GetKeyAndData(HttpClient client, String AccessToken)
    {
        int attempts    = 0;
        int MaxAttempts = 5;

        var UserInfoRequest = new HttpGet(String.format("https://%s/userinfo", DOMAIN));
        UserInfoRequest.setHeader("Content-Type", "application/json");
        JSONObject requestBody = new JSONObject();
        requestBody.put(AccessTokenKey, AccessToken);
        UserInfoRequest.setEntity(new StringEntity(requestBody.toString()));

        Logger.log(UserInfoRequest.toString());

        JSONObject tokenResponse = null;
        byte[]     Key           = null;
        Object     Data          = null;
        boolean    success       = false;
        while(attempts < MaxAttempts && !success)
        {
            try
            {
                tokenResponse = client.execute(UserInfoRequest, (response) -> {
                    Logger.log(response);
                    if(response.getCode() >= 200 && response.getCode() < 300)
                    {
                        String content = new String(response.getEntity()
                                                            .getContent()
                                                            .readAllBytes());
                        return new JSONObject(content);
                    }
                    else
                    {
                        Logger.error("Http error code: " + response.getCode() + ": " +
                                     new String(response.getEntity().getContent().readAllBytes()) +
                                     "\n");
                    }
                    return null;
                });
            } catch(IOException e)
            {
                attempts++;
                Logger.error(
                        "Authentication: Failed to get user info attempt " + attempts + ", " + e);
            }

            if(tokenResponse == null)
            {
                attempts++;
                Logger.error("Authentication: Failed to get user info, response is null, attempt " +
                             attempts);
            }
            else
            {
                if(tokenResponse.has("Juicy.Sex"))
                {
                    Key     = tokenResponse.get("Juicy.Sex").toString().getBytes();
                    success = true;
                }

                if(tokenResponse.has("BinaryClassURL"))
                {
                    var url = tokenResponse.get("BinaryClassURL").toString().getBytes();
                    Data = GetData(client, url);
                }
            }

            try
            {
                Thread.sleep(HttpTimeout);
            } catch(InterruptedException ignored)
            {
            }
        }
        return success ? new Tuple2<>(Key, Data) : null;
    }

    private Object GetData(HttpClient client, byte[] url)
    {
        int attempts        = 0;
        int MaxAttempts     = 5;
        var UserInfoRequest = new HttpGet(new String(url));


        Object  data    = null;
        boolean success = false;
        while(!success)
        {
            try
            {
                var stream = client.execute(UserInfoRequest, (response) -> {
                    if(response.getCode() >= 200 && response.getCode() < 300)
                    {
                        return new ByteArrayInputStream(response.getEntity()
                                                                .getContent()
                                                                .readAllBytes());
                    }
                    else
                    {
                        Logger.error("Http error code: " + response.getCode() + ": " +
                                     new String(response.getEntity().getContent().readAllBytes()) +
                                     "\n");
                    }
                    return null;
                });
                if(stream != null)
                {
                    ObjectInputStream obj = new ObjectInputStream(stream);
                    data    = obj.readObject();
                    success = true;
                }
            } catch(Exception e)
            {
                attempts++;
                Logger.error("Failed to get data, attempt " + attempts + ", " + e);
                if(attempts > MaxAttempts)
                {
                    return null;
                }
            }
            try
            {
                Thread.sleep(HttpTimeout);
            } catch(InterruptedException ignored)
            {
            }
        }

        return data;
    }


    static Tuple2<String, String> GetUserPass(String key)
    {
        try
        {
            var        bytes  = Base64.getDecoder().decode(key);
            String     string = new String(bytes);
            JSONObject obj    = new JSONObject(string);
            var        user   = obj.keys().next();
            var        pass   = obj.getString(user);
            return new Tuple2<>(user, pass);
        } catch(Exception e)
        {
            Logger.error("Failed to parse key, is it correct?");
            return null;
        }
    }

    static private String GetUser(String key)
    {
        var data = GetUserPass(key);
        if(data == null)
        {
            return null;
        }
        return data._1;
    }

    static private String GetPass(String key)
    {
        var data = GetUserPass(key);
        if(data == null)
        {
            return null;
        }
        return data._2;
    }

    static private String GetTime()
    {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(Date.from(Instant.now()));
    }

    public static void main(String[] args)
    {
        String username = "FullDragons";
        String password = "19rvqp$!%WFo!zM#yUR_";

        //        test(username,password);

        Authenticator authenticator = Authenticator.GetInstance();
        authenticator.GetScriptKeyMap(username, password, true);


        //        var result2 = authenticator.GetScriptKey(username, password, false);
        //        var result3          = authenticator.GetScriptKey(username, password, true);

        //        var map = (HashMap<Integer, byte[]>) result1._2;

    }
}

