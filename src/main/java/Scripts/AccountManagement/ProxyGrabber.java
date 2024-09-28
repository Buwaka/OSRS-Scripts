package Scripts.AccountManagement;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.net.URIBuilder;
import org.dreambot.api.utilities.Logger;

import java.io.InputStreamReader;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;

public class ProxyGrabber
{
    final static String WebShareKey      = "kac725ydr2rodqgxwzyikg68keugx6nt6ccgcv4l";
    final static String ProxyListEndpont = "https://proxy.webshare.io/api/v2/proxy/list/";

    public static class ProxyData
    {
        public String  id;
        public String  username;
        public String  password;
        public String  proxy_address;
        public String  port;
        public boolean valid;
        public Date    last_verification;
        public String  country_code;
        public String  city_name;
        public Date    created_at;
    }

    public static void main(String[] args)
    {
        final int MaxAttempts = 5;
        int       attempt     = 0;
        while(attempt < MaxAttempts)
        {
            try(CloseableHttpClient client = HttpClients.createDefault())
            {
                HttpGet request = new HttpGet(ProxyListEndpont);
                request.addHeader("Authorization", "Token " + WebShareKey);
                URI uri = new URIBuilder(request.getUri()).addParameter("mode", "direct")
                                                          .addParameter("page", String.valueOf(1))
                                                          .addParameter("page_size",
                                                                        String.valueOf(9999))
                                                          .addParameter("valid",
                                                                        Boolean.toString(true))
                                                          .build();

                request.setUri(uri);
                Gson gson = new Gson().newBuilder().create();
                client.execute(request, httpResponse -> {

                    if(httpResponse.getCode() >= 300)
                    {
                        return null;
                    }
                    var                     content = httpResponse.getEntity().getContent();
                    InputStreamReader       File    = new InputStreamReader(content);
                    JsonReader              Reader  = new JsonReader(File);
                    HashMap<String, String> out     = new HashMap<>();

                    Reader.beginObject();
                    while(Reader.hasNext())
                    {
                        var name = Reader.nextName();
                        if(name.equalsIgnoreCase("results"))
                        {
                            var data = (ProxyData[]) gson.fromJson(Reader, ProxyData[].class);

                            for(var proxy : data)
                            {
                                String nickname = proxy.city_name;
                                int    i        = 2;
                                while(out.containsKey(nickname))
                                {
                                    nickname = proxy.city_name + i;
                                    i++;
                                }
                                String value =
                                        nickname + ":" + proxy.proxy_address + ":" + proxy.port +
                                        ":" + proxy.username + ":" + proxy.password;

                                out.put(nickname, value);
                                System.out.print(value + "\n");
                            }
                        }
                        else
                        {
                            Reader.skipValue();
                        }
                    }
                    Reader.close();
                    return null;
                });
                return;

            } catch(Exception e)
            {
                Logger.log(e);
                attempt++;
            }
        }
    }
}
