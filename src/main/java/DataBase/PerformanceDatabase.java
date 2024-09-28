package DataBase;

import OSRSDatabase.OSRSPrices;
import OSRSDatabase.SkillsDB;
import Utilities.GrandExchange.GEInstance;
import Utilities.Patterns.Playtime;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.BsonType;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.codecs.pojo.annotations.BsonRepresentation;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.quest.Quests;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.utilities.Logger;

import javax.annotation.Nullable;
import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;

public class PerformanceDatabase
{
    private static final String DatabaseName          = "AccountPerformance";
    private static final String AccountCollectionName = "DailyPerformance";

    public static class PerformanceData implements Serializable
    {
        @BsonIgnore
        @Serial
        private static final long serialVersionUID = 8051917413739833367L;

        @BsonId()
        @BsonRepresentation(BsonType.OBJECT_ID)
        public String        ID;
        @BsonProperty("timestamp")
        //@BsonRepresentation(BsonType.TIMESTAMP)
        public LocalDateTime timestamp;
        public String        AccountName;
        public long          playtime;
        public long          total_playtime;
        public String        playtime_string;
        public int           value_gained;
        public int           liquid_gold;
        public long          networth;
        public long          total_exp;
        public int           total_exp_gain;
        @BsonProperty(useDiscriminator = true)
        public SkillsDB      skill_levels;
        public int           total_questpoints;
        public int           questpoints_gained;
        @Nullable
        public String        activity;

        public PerformanceData()
        {

        }

        public PerformanceData(String accountName, long playtime, long total_playtime, String playtime_string, int value_gained, int liquid_gold, long networth, long total_exp, int total_exp_gain, SkillsDB skill_levels, int total_questpoints, int questpoints_gained, @Nullable String activity)
        {
            timestamp               = LocalDateTime.now();
            AccountName             = accountName;
            this.playtime           = playtime;
            this.total_playtime     = total_playtime;
            this.playtime_string    = playtime_string;
            this.value_gained       = value_gained;
            this.liquid_gold        = liquid_gold;
            this.networth           = networth;
            this.total_exp          = total_exp;
            this.total_exp_gain     = total_exp_gain;
            this.skill_levels       = skill_levels;
            this.total_questpoints  = total_questpoints;
            this.questpoints_gained = questpoints_gained;
            this.activity           = activity;
        }

        public Duration GetPlayTime()
        {
            return Duration.ofNanos(playtime);
        }

        public Duration GetTotalPlayTime()
        {
            return Duration.ofNanos(total_playtime);
        }
    }

    public static PerformanceData GeneratePerformanceReport(String Activity)
    {
        String name = Players.getLocal().getName();

        long   TotalPlayTime = Playtime.GetPlaytimeLong();
        String strPlaytime   = Playtime.GetPlaytime();
        int    LiquidGold    = GEInstance.GetLiquidMoney();
        long   NetWorth      = OSRSPrices.GetNetWorth();
        int    QPoints       = Quests.getQuestPoints();
        long   TotalEXP      = Arrays.stream(Skills.getExperience()).sum();
        var    SkillMap      = new SkillsDB();

        var recent = GetMostRecentData(name);
        Logger.log(recent);

        long AddedTimePlayed = 0;
        int  ValueGained     = 0;
        int  QPointsGained   = 0;
        int  EXPGained       = 0;
        if(recent != null)
        {
            Logger.log("GeneratePerformanceReport: Recent found " + recent);
            AddedTimePlayed = TotalPlayTime - recent.playtime;
            ValueGained     = (int) (NetWorth - recent.networth);
            QPointsGained   = QPoints - recent.total_questpoints;
            EXPGained       = (int) (TotalEXP - recent.total_exp);
            Logger.log("GeneratePerformanceReport: " + ValueGained);
        }


        return new PerformanceDatabase.PerformanceData(name,
                                                       AddedTimePlayed,
                                                       TotalPlayTime,
                                                       strPlaytime,
                                                       ValueGained,
                                                       LiquidGold,
                                                       NetWorth,
                                                       TotalEXP,
                                                       EXPGained,
                                                       SkillMap,
                                                       QPoints,
                                                       QPointsGained,
                                                       Activity);
    }

    public static PerformanceData GetMostRecentData(String Nickname)
    {
        RegisterTypes();
        Logger.log(Nickname);
        var db = DataBaseUtilities.GetDataBase(DatabaseName);
        if(db == null)
        {
            return null;
        }

        var collection = DataBaseUtilities.GetCollection(db,
                                                         AccountCollectionName,
                                                         PerformanceData.class);

        var candidates = collection.find(Filters.eq("AccountName", Nickname))
                                   .sort(Sorts.descending("timestamp"));

        return candidates.first();
        // get list of nickname, sort by date, get and return latest
    }

    private static void RegisterTypes() // neccesary because static block won't be called until its too late
    {
        DataBaseUtilities.RegisterPOJO(PerformanceData.class);
        DataBaseUtilities.RegisterPOJO(SkillsDB.class);
    }

    public static boolean UploadPerformanceData(PerformanceData data)
    {
        Logger.log(data);
        var db = DataBaseUtilities.GetDataBase(DatabaseName);
        if(db == null)
        {
            return false;
        }

        var collection = DataBaseUtilities.GetCollection(db,
                                                         AccountCollectionName,
                                                         PerformanceData.class);

        var result = collection.insertOne(data);
        if(result.wasAcknowledged())
        {
            DataBaseUtilities.CloseClient();
            return true;
        }
        return false;
    }


}
