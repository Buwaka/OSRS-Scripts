//package IF.Scripts.AccountManagement;
//
//import IF.Utilities.DefaultAuthenticator;
//import io.ipinfo.api.IPinfo;
//import io.ipinfo.api.errors.RateLimitedException;
//import io.ipinfo.api.model.IPResponse;
//import org.apache.commons.codec.binary.Base64;
//import org.apache.http.HttpHost;
//import org.apache.http.client.fluent.Executor;
//import org.apache.http.client.fluent.Request;
//import org.dreambot.api.methods.container.impl.bank.Bank;
//import org.dreambot.api.wrappers.items.Item;
//
//import java.io.File;
//import java.io.IOException;
//import java.net.Authenticator;
//import java.net.PasswordAuthentication;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class IPWhoisChecker
//{
//    static File                 ProxyList = new File("C:/Users/SammyLaptop/Downloads/data.txt");
//    static DefaultAuthenticator auth      = DefaultAuthenticator.getInstance();
//
//    //    public static void main(String[] args) throws Exception
//    //    {
//    //        List<String> lines = Files.readAllLines(ProxyList.toPath());
//    //
//    //        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//    //
//    //        Map<String, IPResponse> responses = new HashMap<>();
//    //
//    //        for(var line : lines)
//    //        {
//    //            var parts    = line.splitWithDelimiters(":", 4);
//    //            var response = GetResponse(parts[0], Integer.parseInt(parts[2]));
//    //
//    //            if(responses.containsKey(line))
//    //            {
//    //                responses.put("[SAMEIPHIT] " + line, response);
//    //            }
//    //            else
//    //            {
//    //                responses.put(line, response);
//    //            }
//    //
//    //        }
//    //
//    //        String json = gson.toJson(responses);
//    //        System.out.println(json);
//    //        Files.writeString(Path.of("C:/Users/SammyLaptop/Downloads/datatest.json"), json);
//    //    }
//    static ProxyAuth auth2 = null;
//
//    static
//    {
//        Authenticator.setDefault(auth);
//
//    }
//
//    public static class ProxyAuth extends Authenticator
//    {
//        private final PasswordAuthentication auth;
//
//        private ProxyAuth(String user, String password)
//        {
//            auth = new PasswordAuthentication(user,
//                                              password == null
//                                                      ? new char[]{}
//                                                      : password.toCharArray());
//        }
//
//        protected PasswordAuthentication getPasswordAuthentication()
//        {
//            return auth;
//        }
//    }
//
//    public static void main(String[] args) throws IOException, RateLimitedException
//    {
//        List<String> lines = Files.readAllLines(Path.of(
//                "C:/Users/SammyLaptop/Downloads/commands.txt"));
//
//        //Gson gson = new GsonBuilder().setPrettyPrinting().create();
//
//        Map<String, IPResponse> responses = new HashMap<>();
//
//        for(var line : lines)
//        {
//            var parts    = line.splitWithDelimiters(":", 6);
//            var response = GetResponse(parts[0], Integer.parseInt(parts[2]), parts[4], parts[6]);
//
//            System.out.print(
//                    parts[4] + "\n" + response.getIp() + " " + response.getAsn().getName() + " " +
//                    response.getAsn().getRoute() + " " + response.getAsn().getType() + "\n");
//        }
//        var item = new Item(69, 69);
//        Bank.contains(item);
//        //                String json = gson.toJson(responses);
//        //                System.out.println(json);
//        //                Files.writeString(Path.of("C:/Users/SammyLaptop/Downloads/datatest.json"), json);
//    }
//
//    static IPResponse GetResponse(String Address, int port, String username, String password) throws
//            IOException,
//            RateLimitedException
//    {
//        System.setProperty("socksProxyHost", Address);
//        System.setProperty("socksProxyPort", String.valueOf(port));
//        System.setProperty("java.net.socks.username", username);
//        System.setProperty("java.net.socks.password", password);
//        System.setProperty("http.proxyHost", Address);
//        System.setProperty("http.proxyPort", String.valueOf(port));
//        System.setProperty("https.proxyHost", Address);
//        System.setProperty("https.proxyPort", String.valueOf(port));
//        auth2 = new ProxyAuth(username, password);
//        Authenticator.setDefault(auth2);
//        String encoded = new String(Base64.encodeBase64((username + ":" + password).getBytes()));
//
//        HttpHost entry = new HttpHost(Address, port);
//        String query = Executor.newInstance()
//                               .auth(entry, username, password)
//                               .execute(Request.Get("https://api.ipify.org")
//                                               .viaProxy(entry)
//                                               .addHeader("Proxy-Authorization",
//                                                          "Basic " + encoded))
//                               .returnContent()
//                               .asString();
//        System.out.println(query);
//
//        String IP = query;
//
//
//        System.setProperty("socksProxyHost", "");
//        System.setProperty("socksProxyPort", String.valueOf(1080));
//        System.setProperty("java.net.socks.username", "");
//        System.setProperty("java.net.socks.password", "");
//        System.setProperty("http.proxyHost", "");
//        System.setProperty("http.proxyPort", String.valueOf(80));
//        System.setProperty("https.proxyHost", "");
//        System.setProperty("https.proxyPort", String.valueOf(443));
//
//
//        IPinfo ipInfo = new IPinfo.Builder().setToken("2c178d026a6d83").build();
//
//        var response = ipInfo.lookupIP(IP);
//
//        //System.out.println(response);
//
//        return response;
//    }
//
//
//    //        try
//    //        {
//    //            System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
//    //            //eu.visitxiangtan.com:10001:user-spnuzjftdk-sessionduration-60:l7Fj~c1y7zWyMipg9I
//    //            InetSocketAddress proxyAddress = new InetSocketAddress(Address,
//    //                                                                   port); // Set proxy IP/port.
//    //            Proxy proxy = new Proxy(Proxy.Type.SOCKS, proxyAddress);
//    //            auth.SetUserPass(username, password);
//    //
//    //            URI uri = new URI("https://api.ipify.org"); //enter target URL
//    //            URL url = uri.toURL();
//    //            String credentials = username + ":" + password;
//    //            String basicAuth = "Basic " + new String(Base64.getEncoder().encode(credentials.getBytes()));
//    //
//    ////            System.setProperty("http.proxyHost", Address);
//    ////            System.setProperty("http.proxyPort", String.valueOf(port));
//    ////            System.setProperty("http.proxyUser", username);
//    ////            System.setProperty("http.proxyPassword", password);
//    //
//    //            HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
//    //            connection.setRequestMethod("GET");
//    //            connection.setRequestProperty ("Authorization", basicAuth);
//    //            //connection.setRequestProperty("Proxy-Authorization", "Basic " + basicAuth);
//    //            //URLConnection urlConnection = url.openConnection(proxy);
//    //
//    //
//    //            Scanner scanner = new Scanner(connection.getInputStream());
//    //            String  IP      = scanner.nextLine();
//    //            //System.out.println(IP);
//    //            scanner.close();
//    //
//    //            IPinfo ipInfo = new IPinfo.Builder().setToken("2c178d026a6d83").build();
//    //
//    //            var response = ipInfo.lookupIP(IP);
//    //
//    //            //System.out.println(response);
//    //
//    //            return response;
//    //        } catch(Exception e)
//    //        {
//    //            System.out.print(e);
//    //        }
//    //        return null;
//    //    }
//}
