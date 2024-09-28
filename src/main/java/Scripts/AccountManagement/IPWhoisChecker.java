package Scripts.AccountManagement;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.ipinfo.api.IPinfo;
import io.ipinfo.api.model.IPResponse;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class IPWhoisChecker
{
    static File ProxyList = new File("C:/Users/SammyLaptop/Downloads/data.txt");


    static IPResponse GetResponse(String Address, int port)
    {
        try
        {
            //eu.visitxiangtan.com:10001:user-spnuzjftdk-sessionduration-60:l7Fj~c1y7zWyMipg9I
            InetSocketAddress proxyAddress = new InetSocketAddress(Address,
                                                                   port); // Set proxy IP/port.
            Proxy proxy = new Proxy(Proxy.Type.HTTP, proxyAddress);
            URL   url   = new URI("https://api.ipify.org").toURL(); //enter target URL

            URLConnection urlConnection = url.openConnection(proxy);


            Scanner scanner = new Scanner(urlConnection.getInputStream());
            String  IP      = scanner.nextLine();
            //System.out.println(IP);
            scanner.close();

            IPinfo ipInfo = new IPinfo.Builder().setToken("16249ee136ecf5").build();

            var response = ipInfo.lookupIP(IP);

            //System.out.println(response);

            return response;
        } catch(Exception e)
        {

        }
        return null;
    }

//    public static void main(String[] args) throws Exception
//    {
//        List<String> lines = Files.readAllLines(ProxyList.toPath());
//
//        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//
//        Map<String, IPResponse> responses = new HashMap<>();
//
//        for(var line : lines)
//        {
//            var parts    = line.splitWithDelimiters(":", 4);
//            var response = GetResponse(parts[0], Integer.parseInt(parts[2]));
//
//            if(responses.containsKey(line))
//            {
//                responses.put("[SAMEIPHIT] " + line, response);
//            }
//            else
//            {
//                responses.put(line, response);
//            }
//
//        }
//
//        String json = gson.toJson(responses);
//        System.out.println(json);
//        Files.writeString(Path.of("C:/Users/SammyLaptop/Downloads/datatest.json"), json);
//    }

    public static void main(String[] args) throws IOException
    {
                List<String> lines = Files.readAllLines(Path.of(
                        "C:/Users/SammyLaptop/Downloads/Webshare 50 proxies(3).txt"));

                Gson gson = new GsonBuilder().setPrettyPrinting().create();

                Map<String, IPResponse> responses = new HashMap<>();

                for(var line : lines)
                {
                    var parts    = line.splitWithDelimiters(":", 4);
                    var response = GetResponse(parts[0], Integer.parseInt(parts[2]));

                    System.out.print(response.getIp() + " " + response.getAsn().getName() + " " + response.getAsn().getRoute() + " " + response.getAsn().getType() + "\n");
                }

//                String json = gson.toJson(responses);
//                System.out.println(json);
//                Files.writeString(Path.of("C:/Users/SammyLaptop/Downloads/datatest.json"), json);
    }
}
