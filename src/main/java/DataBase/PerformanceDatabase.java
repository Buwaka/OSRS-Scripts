package DataBase;

import OSRSDatabase.ItemDB;
import org.bson.BsonTimestamp;
import org.bson.BsonType;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.codecs.pojo.annotations.BsonRepresentation;
import org.dreambot.api.utilities.Logger;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

public class PerformanceDatabase
{
    private static final String DatabaseName          = "AccountPerformance";
    private static final String AccountCollectionName = "DailyPerformance";

    public static boolean UploadPerformanceData(PerformanceData data)
    {
        Logger.log(data);
        var db = DataBaseUtilities.GetDataBase(DatabaseName);

        var collection = DataBaseUtilities.GetCollection(db,
                                                         AccountCollectionName,
                                                         PerformanceData.class);

        var result = collection.insertOne(data);

        return result.wasAcknowledged();
    }


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
        public String AccountName;
        public long          playtime;
        public  long                       total_playtime;
        public  int                        gold;
        public  long                       networth;
        public  int                        total_exp_gain;
        public  Map<ItemDB.Skill, Integer> exp_gain_skill;
        public  int                        total_questpoints;
        public  int                        questpoints_gained;
        public  String                     activity;

        static
        {
            DataBaseUtilities.RegisterPOJO(PerformanceData.class);
        }

        public PerformanceData(String AccountName, long playtime, long total_playtime, int gold, long networth, int total_exp_gain, Map<ItemDB.Skill, Integer> exp_gain_skill, int total_questpoints, int questpoints_gained)
        {
            this.AccountName = AccountName;
            this.playtime           = playtime;
            this.total_playtime     = total_playtime;
            this.gold               = gold;
            this.networth           = networth;
            this.total_exp_gain     = total_exp_gain;
            this.exp_gain_skill     = exp_gain_skill;
            this.total_questpoints  = total_questpoints;
            this.questpoints_gained = questpoints_gained;
            this.activity           = "";
            timestamp               = LocalDateTime.now();
        }

        public PerformanceData(String AccountName, long playtime, long total_playtime, int gold, long networth, int total_exp_gain, Map<ItemDB.Skill, Integer> exp_gain_skill, int total_questpoints, int questpoints_gained, String activity)
        {
            this.AccountName = AccountName;
            this.playtime           = playtime;
            this.total_playtime     = total_playtime;
            this.gold               = gold;
            this.networth           = networth;
            this.total_exp_gain     = total_exp_gain;
            this.exp_gain_skill     = exp_gain_skill;
            this.total_questpoints  = total_questpoints;
            this.questpoints_gained = questpoints_gained;
            this.activity           = activity;
            timestamp               = LocalDateTime.now();
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


}
