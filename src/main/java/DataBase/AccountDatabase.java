package DataBase;

import OSRSDatabase.OSRSPrices;
import OSRSDatabase.SkillsDB;
import org.bson.BsonType;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.codecs.pojo.annotations.BsonRepresentation;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.utilities.AccountManager;
import org.dreambot.api.wrappers.items.Item;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Date;

public class AccountDatabase
{
    private static final String DatabaseName          = "AccountDB";
    private static final String AccountCollectionName = "AccountProxyDB";

    public static boolean RegisterAccount(String nickname, String email, String password, String proxyArea, String proxyIP, String proxyUsername, String proxyPassword)
    {
        AccountData NewAccount = new AccountData();
        NewAccount.NickName     = nickname;
        NewAccount.Email        = email;
        NewAccount.Password     = password;
        NewAccount.Proxy        = new ProxyData(proxyArea, proxyIP, proxyUsername, proxyPassword);
        NewAccount.Stats        = new PlayerStats(nickname);
        NewAccount.DateCreation = Date.from(Instant.now());
        NewAccount.LastUsed     = Date.from(Instant.now());

        var db = DataBaseUtilities.GetDataBase(DatabaseName);

        var collection = DataBaseUtilities.GetCollection(db,
                                                         AccountCollectionName,
                                                         AccountData.class);

        var result = collection.insertOne(NewAccount);

        return result.wasAcknowledged();
    }


    public static class AccountData implements Serializable
    {
        @BsonIgnore
        @Serial
        private static final long serialVersionUID = 3421471181987297658L;

        @BsonId()
        @BsonRepresentation(BsonType.OBJECT_ID)
        public String      ID;
        @BsonProperty("nickname")
        public String      NickName;
        @BsonProperty("email")
        public String      Email;
        @BsonProperty("password")
        public String      Password;
        @BsonProperty("proxy")
        public ProxyData   Proxy;
        public Date        DateCreation;
        public Date        LastUsed;
        @BsonProperty("stats")
        public PlayerStats Stats;

        static
        {
            DataBaseUtilities.RegisterPOJO(AccountData.class);
        }
    }

    public static class ProxyData implements Serializable
    {
        @BsonIgnore
        @Serial
        private static final long serialVersionUID = -1503263709917871061L;

        @BsonProperty("id")
        public String ID;
        @BsonProperty("ip")
        public String IP;
        @BsonProperty("username")
        public String Username;
        @BsonProperty("password")
        public String Password;

        ProxyData(String area, String ip, String username, String password)
        {
            ID       = area;
            IP       = ip;
            Username = username;
            Password = password;
        }

        static
        {
            DataBaseUtilities.RegisterPOJO(ProxyData.class);
        }
    }

    public static class PlayerStats implements Serializable
    {
        @BsonIgnore
        @Serial
        private static final long serialVersionUID = 1918901397117107531L;

        public enum AccStatus
        {
            New,
            Active,
            Resting,
            Banned
        }

        @BsonProperty("status")
        public AccStatus Status       = AccStatus.Active;
        @BsonProperty("money")
        public long      Money        = 0;
        @BsonProperty("account_value")
        public long      AccountValue = 0;
        @BsonProperty("skills")
        public SkillsDB  Skills;

        public PlayerStats(String Nickname)
        {
            if(AccountManager.isBanned(Nickname))
            {
                Status = AccStatus.Banned;
            }

            if(Bank.isCached())
            {
                var AllItems = Bank.all();
                AllItems.add((Item) Inventory.all());
                AccountValue = OSRSPrices.GetTotalValue(AllItems);

                Money = Inventory.count(OSRSPrices.CoinID) + Bank.count(OSRSPrices.CoinID);
            }

            Skills = new SkillsDB();
        }

        static
        {
            DataBaseUtilities.RegisterPOJO(PlayerStats.class);
        }
    }


}
