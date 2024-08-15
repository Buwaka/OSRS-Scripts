package DataBase;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.ArrayList;
import java.util.List;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;

public class DataBaseUtilities
{
    String MDBUser     = "ScriptUser";
    String MDBPassword = "tb482uLsd2gX7Hse";

    private static final List<String> POJOPackages = new ArrayList<>();

    public static <T> MongoCollection<T> GetCollection(MongoDatabase database, String Name, Class<T> clazz)
    {
        return database.getCollection(Name, clazz).withCodecRegistry(GetProviders(database));
    }

    private static CodecRegistry GetProviders(MongoDatabase database)
    {
        return fromProviders(database.getCodecRegistry(),
                             PojoCodecProvider.builder()
                                              .register(POJOPackages.toArray(new String[0]))
                                              .conventions(Conventions.DEFAULT_CONVENTIONS)
                                              .build());
    }

    public static void RegisterPOJO(Class clazz)
    {
        POJOPackages.add(clazz.getPackageName());
    }

    public static void RegisterPOJO(String PackageName)
    {
        POJOPackages.add(PackageName);
    }

    public static void main(String[] args)
    {
        var client = GetClient("mongodb+srv",
                               "ScriptUser",
                               "tb482uLsd2gX7Hse",
                               "osrs-database.ehnphrp.mongodb.net",
                               "retryWrites=true&w=majority&appName=OSRS-Database");

        var database = GetDataBase(client, "AccountDB");

        CodecRegistry providers = fromProviders(database.getCodecRegistry(),
                                                PojoCodecProvider.builder()
                                                                 .register(AccountDatabase.AccountData.class.getPackageName())
                                                                 .conventions(Conventions.DEFAULT_CONVENTIONS)
                                                                 .build());

        MongoCollection<AccountDatabase.AccountData> collection = database.getCollection(
                "AccountProxyDB",
                AccountDatabase.AccountData.class).withCodecRegistry(providers);


        //        var codecRegistry = fromRegistries(List.of(providers));


        //        AccountDatabase.AccountData testdata = new AccountDatabase.AccountData();
        //        testdata.Email = "bruh2024197+AM37@gmail.com";
        //        testdata.LastUsed = Date.from(Instant.now());
        //        testdata.DateCreation = DateUtils.addDays(Date.from(Instant.now()), -3);
        //        testdata.Password = "5Mstryredad";
        //        testdata.NickName = "IDontRememberTheName";
        //        testdata.ProxyArea = AccountDatabase.Area.LA;
        //        testdata.ProxyIP = "127.0.0.0";
        //        testdata.ProxyPassword = "fancypassword";
        //        testdata.ID = new ObjectId();
        //
        //        collection.insertOne(testdata);

        var data = collection.find().first();
        System.out.print(data.ID);

    }


    public static MongoClient GetClient()
    {
        return GetClient("mongodb+srv",
                         "ScriptUser",
                         "tb482uLsd2gX7Hse",
                         "osrs-database.ehnphrp.mongodb.net",
                         "retryWrites=true&w=majority&appName=OSRS-Database");
    }
    public static MongoClient GetClient(String Protocol, String Username, String Password, String Host, String Options)
    {
        //String connectionString = "mongodb+srv://ScriptUser:<password>@osrs-database.ehnphrp.mongodb.net/?retryWrites=true&w=majority&appName=OSRS-Database";
        String URI = Protocol + "://" + Username + ":" + Password + "@" + Host +
                     (Options.isEmpty() ? "" : "/?" + Options);

        ServerApi serverApi = ServerApi.builder().version(ServerApiVersion.V1).build();

        MongoClientSettings settings = MongoClientSettings.builder()
                                                          .applyConnectionString(new ConnectionString(
                                                                  URI))
                                                          .serverApi(serverApi)
                                                          .build();
        // Create a new client and connect to the server
        try
        {
            return MongoClients.create(settings);
        } catch(Exception e)
        {
            System.out.print("DataBaseUtilities: ConnectToDataBase: Exception " + e);
            System.out.print("DataBaseUtilities: ConnectToDataBase: URI" + URI);
            //            Logger.log("DataBaseUtilities: ConnectToDataBase: Exception " + e);
            //            Logger.log("DataBaseUtilities: ConnectToDataBase: URI" + URI);
        }

        return null;
    }

    public static MongoDatabase GetDataBase(String DataBaseID)
    {
        MongoDatabase database = GetClient().getDatabase(DataBaseID);
        database.runCommand(new Document("ping", 1));
        System.out.print(
                "DataBaseUtilities: GetDataBase: Pinged your deployment. You successfully connected to MongoDB!");
        //        Logger.log(
        //                "DataBaseUtilities: GetDataBase: Pinged your deployment. You successfully connected to MongoDB!");
        return database;
    }

    public static MongoDatabase GetDataBase(MongoClient Client, String DataBaseID)
    {
        MongoDatabase database = Client.getDatabase(DataBaseID);
        database.runCommand(new Document("ping", 1));
        System.out.print(
                "DataBaseUtilities: GetDataBase: Pinged your deployment. You successfully connected to MongoDB!");
        //        Logger.log(
        //                "DataBaseUtilities: GetDataBase: Pinged your deployment. You successfully connected to MongoDB!");
        return database;
    }

}
