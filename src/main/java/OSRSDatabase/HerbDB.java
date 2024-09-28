package OSRSDatabase;

import Utilities.OSRSUtilities;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.dreambot.api.utilities.Logger;

import java.io.InputStreamReader;
import java.util.HashMap;

public class HerbDB extends OSRSDataBase
{
    final private static String                     HerbDBPath = "Skilling/herbDB.json";
    private static       HashMap<Integer, HerbData> HerbDBMap  = null;

    public static class HerbData
    {
        public int    id;
        public int    grimy_id;
        public String name;
        public int    level;

        public String toString()
        {
            return _toString(this, this.getClass());
        }
    }

    /**
     * @param ID of clean herb
     *
     * @return
     */
    public static HerbData GetCleanHerb(int ID)
    {
        HerbData herb = GetHerbData(ID);

        if(herb == null)
        {
            return null;
        }

        return GetHerbData(herb.id);
    }

    public static int[] GetCleanHerbList()
    {
        if(HerbDBMap == null)
        {
            ReadHerbDB();
        }

        return HerbDBMap.keySet().stream().filter(t -> !isGrimyHerb(t)).mapToInt(t -> t).toArray();
    }

    public static boolean isGrimyHerb(int ID)
    {
        if(HerbDBMap == null)
        {
            ReadHerbDB();
        }

        return HerbDBMap.containsKey(ID) && HerbDBMap.get(ID).grimy_id == ID;
    }

    private static void ReadHerbDB()
    {
        if(HerbDBMap != null)
        {
            return;
        }

        HerbDBMap = new HashMap<>();

        try
        {
            InputStreamReader File   = new InputStreamReader(GetInputStream(HerbDBPath));
            JsonReader        Reader = new JsonReader(File);
            Gson              gson   = OSRSUtilities.OSRSGsonBuilder.create();
            Reader.setLenient(true);

            Reader.beginObject();

            while(Reader.hasNext())
            {
                int      ID  = Integer.parseInt(Reader.nextName());
                HerbData Obj = gson.fromJson(Reader, HerbData.class);
                HerbDBMap.put(ID, Obj);
            }

            Reader.endObject();
            Reader.close();

        } catch(Exception e)
        {
            Logger.log("Error reading HerbDB, Exception: " + e);
            throw new RuntimeException(e);
        }
    }

    /**
     * @param ID of clean herb
     *
     * @return
     */
    public static HerbData GetGrimyHerb(int ID)
    {
        HerbData herb = GetHerbData(ID);

        if(herb == null)
        {
            return null;
        }

        return GetHerbData(herb.grimy_id);
    }

    public static int[] GetGrimyHerbList()
    {
        if(HerbDBMap == null)
        {
            ReadHerbDB();
        }

        return HerbDBMap.keySet().stream().filter(t -> isGrimyHerb(t)).mapToInt(t -> t).toArray();
    }

    public static int[] GetHerbList()
    {
        if(HerbDBMap == null)
        {
            ReadHerbDB();
        }

        return HerbDBMap.keySet().stream().mapToInt(t -> t).toArray();
    }

    public static boolean isCleanHerb(int ID)
    {
        return !isGrimyHerb(ID);
    }

    public static boolean isDegrimeProfitable(int base)
    {
        HerbData herb       = GetHerbData(base);
        int      GrimyPrice = OSRSPrices.GetAveragePrice(herb.grimy_id);
        int      CleanPrice = OSRSPrices.GetAveragePrice(herb.id);
        return GrimyPrice < CleanPrice;
    }

    public static HerbData GetHerbData(int ID)
    {
        if(HerbDBMap == null)
        {
            ReadHerbDB();
        }

        return HerbDBMap.get(ID);
    }

    public static boolean isHerb(int ID)
    {
        if(HerbDBMap == null)
        {
            ReadHerbDB();
        }

        return HerbDBMap.containsKey(ID);
    }

    public static boolean isHerb(String name)
    {
        if(HerbDBMap == null)
        {
            ReadHerbDB();
        }

        return HerbDBMap.entrySet()
                        .stream()
                        .anyMatch((entry) -> entry.getValue().name.equalsIgnoreCase(name));
    }
}
