package OSRSDatabase;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import io.vavr.Tuple2;
import org.dreambot.api.utilities.Logger;

import javax.annotation.Nullable;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class WoodDB extends OSRSDataBase
{
    final private static String                               WoodDBPath = "woodDB.json";
    private static       ConcurrentHashMap<Integer, WoodData> WoodDBMap  = null;

    public static class WoodData
    {
        public           int      id;
        public           String   name;
        public           boolean  member;
        public           int      level;
        public           float    exp;
        public           int      noted_id;
        public           String[] trees;
        public           boolean  pyre;
        public @Nullable Integer  pyre_dose;
        public @Nullable Integer  pyre_id;
        public           boolean  burnable;

        public String toString()
        {
            return _toString(this, this.getClass());
        }
    }

    public static WoodData GetBestWoodCuttingLog(int WoodCuttingLevel)
    {
        if(WoodDBMap == null)
        {
            ReadWoodDB();
        }

        WoodData out = GetWoodData("Logs");

        for(var wood : WoodDBMap.values())
        {
            if(wood.level < WoodCuttingLevel && wood.exp > out.exp)
            {
                out = wood;
            }
        }
        return out;
    }

    private static void ReadWoodDB()
    {
        WoodDBMap = new ConcurrentHashMap<>();

        try
        {
            InputStreamReader File   = new InputStreamReader(GetInputStream(WoodDBPath));
            JsonReader        Reader = new JsonReader(File);
            Gson              gson   = new Gson();
            Reader.setLenient(true);

            Reader.beginObject();

            while(Reader.hasNext())
            {
                int      ID  = Integer.parseInt(Reader.nextName());
                WoodData Obj = gson.fromJson(Reader, WoodData.class);
                WoodDBMap.put(ID, Obj);
            }

            Reader.endObject();
            Reader.close();

        } catch(Exception e)
        {
            Logger.log("Error reading HerbDB, Exception: " + e);
            throw new RuntimeException(e);
        }
    }

    public static WoodData GetWoodData(String Name)
    {
        if(WoodDBMap == null)
        {
            ReadWoodDB();
        }

        return WoodDBMap.search(1, (key, val) -> val.name.equalsIgnoreCase(Name) ? val : null);
    }

    public static WoodData GetBestFireMakingLog(int WoodCuttingLevel)
    {
        if(WoodDBMap == null)
        {
            ReadWoodDB();
        }

        WoodData out = GetWoodData("Logs");

        for(var wood : WoodDBMap.values())
        {
            if(wood.level < WoodCuttingLevel && wood.exp > out.exp && wood.burnable)
            {
                out = wood;
            }
        }
        return out;
    }

    public static WoodData GetMoneyEfficientPyreLog(boolean ignoreLogPrice, boolean IgnoreLowVolume)
    {
        if(WoodDBMap == null)
        {
            ReadWoodDB();
        }


        final int SacredOilID = 3430;
        final int SacredOilPrice = OSRSPrices.GetAveragePrice(SacredOilID);
        final int SacredOilDosePrice = SacredOilPrice/ 4;


        List<Tuple2<Integer, WoodData>> woods = new ArrayList<>();
        for(var wood : WoodDBMap.values())
        {

            if(wood.pyre)
            {
                Logger.log("IsLowVolume: " + OSRSPrices.isLowVolume(wood.pyre_id));
                if(!IgnoreLowVolume || !OSRSPrices.isLowVolume(wood.pyre_id))
                {
                    int LogPrice = ignoreLogPrice ? 0 : OSRSPrices.GetAveragePrice(wood.id);
                    int OilPrice = SacredOilDosePrice * wood.pyre_dose;
                    int PyreValue = OSRSPrices.GetAveragePrice(wood.pyre_id);
                    int profit = PyreValue - (LogPrice + OilPrice);
                    Logger.log(wood.name + ": " + profit + " = " + PyreValue + " - (" + LogPrice + " + " + OilPrice + ")");
                    woods.add(new Tuple2<>(profit, wood));
                }
            }
        }
        woods.sort((t1, t2) -> t2._1.compareTo(t1._1));
        return woods.getFirst()._2;
    }


    public static int GetFireMakingLevel(int LogID)
    {
        return GetWoodCuttingLevel(LogID);
    }

    public static int GetWoodCuttingLevel(int LogID)
    {
        WoodData data = GetWoodData(LogID);

        if(data != null)
        {
            return data.level;
        }
        return 101;
    }

    public static WoodData GetWoodData(int ID)
    {
        if(WoodDBMap == null)
        {
            ReadWoodDB();
        }

        return WoodDBMap.get(ID);
    }

    public static int GetPyreLevel(int LogID)
    {
        WoodData data = GetWoodData(LogID);

        if(data != null)
        {
            return data.level + 5;
        }
        return 101;
    }
}
