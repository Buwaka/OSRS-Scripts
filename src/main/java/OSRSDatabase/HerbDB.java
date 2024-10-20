package OSRSDatabase;

import Utilities.OSRSUtilities;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.items.Item;

import javax.annotation.Nullable;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class HerbDB extends OSRSDataBase
{
    final private static String                     HerbDBPath = "Skilling/herbDB.json";
    private static       HashMap<Integer, HerbData> HerbDBMap  = null;

    public enum PasteType
    {
        Mox,
        Aga,
        Lye
    }

    public static class HerbData
    {
        public int       id;
        public int       grimy_id;
        public String    name;
        public int       level;
        @Nullable
        public PasteType paste_type;
        @Nullable
        public Integer   paste_count;

        public String toString()
        {
            return _toString(this, this.getClass());
        }
    }

    public static List<Item> FilterHerbs(List<Item> items, boolean IncludeGrimy)
    {
        int[] HerbIDs;
        if(IncludeGrimy)
        {
            HerbIDs = GetHerbIDs();
        }
        else
        {
            HerbIDs = GetCleanHerbIDs();
        }
        var Herbs = items.stream()
                         .filter((t) -> Arrays.stream(HerbIDs)
                                              .anyMatch((x) -> t != null && x == t.getID()));
        return Herbs.toList();
    }

    public static int[] GetCleanHerbIDs()
    {
        if(HerbDBMap == null)
        {
            ReadHerbDB();
        }

        return HerbDBMap.keySet().stream().filter(t -> !isGrimyHerb(t)).mapToInt(t -> t).toArray();
    }

    public static int[] GetHerbIDs()
    {
        if(HerbDBMap == null)
        {
            ReadHerbDB();
        }

        return HerbDBMap.keySet().stream().mapToInt(t -> t).toArray();
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

    public static HerbData GetHerbData(int ID)
    {
        if(HerbDBMap == null)
        {
            ReadHerbDB();
        }

        return HerbDBMap.get(ID);
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

    public static List<HerbData> GetCleanHerbData()
    {
        if(HerbDBMap == null)
        {
            ReadHerbDB();
        }

        return HerbDBMap.values().stream().filter((t) -> isCleanHerb(t.id)).toList();
    }

    public static boolean isCleanHerb(int ID)
    {
        return !isGrimyHerb(ID);
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

    public static List<HerbData> GetGrimyHerbData()
    {
        if(HerbDBMap == null)
        {
            ReadHerbDB();
        }

        return HerbDBMap.values().stream().filter((t) -> isGrimyHerb(t.id)).toList();
    }

    public static int[] GetGrimyHerbIDs()
    {
        if(HerbDBMap == null)
        {
            ReadHerbDB();
        }

        return HerbDBMap.keySet().stream().filter(t -> isGrimyHerb(t)).mapToInt(t -> t).toArray();
    }

    public static boolean isGrimyHerb(int ID)
    {
        if(HerbDBMap == null)
        {
            ReadHerbDB();
        }

        return HerbDBMap.containsKey(ID) && HerbDBMap.get(ID).grimy_id == ID;
    }

    public static HerbData[] GetHerbData()
    {
        if(HerbDBMap == null)
        {
            ReadHerbDB();
        }

        return HerbDBMap.values().toArray(new HerbData[0]);
    }

    public static boolean isDegrimeProfitable(int base)
    {
        HerbData herb       = GetHerbData(base);
        int      GrimyPrice = OSRSPrices.GetAveragePrice(herb.grimy_id);
        int      CleanPrice = OSRSPrices.GetAveragePrice(herb.id);
        return GrimyPrice < CleanPrice;
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


    //    class PasteData
    //    {
    //        public String Paste;
    //        public String type;
    //        public int undefined;
    //    }
    //
    //    public static void main(String[] args) throws IOException
    //    {
    //        InputStreamReader File   = new InputStreamReader(new FileInputStream("C:/Users/SammyLaptop/Downloads/tableConvert.com_02sac8.json"));
    //        JsonReader        Reader = new JsonReader(File);
    //        Gson              gson   = new Gson(); //OSRSUtilities.OSRSGsonBuilder.create();
    //        Reader.setLenient(true);
    //
    //        ReadHerbDB();
    //
    //        Reader.beginArray();
    //
    //        while(Reader.hasNext())
    //        {
    //            PasteData Obj = gson.fromJson(Reader, PasteData.class);
    //
    //           var Herb = HerbDBMap.values().stream().filter((t) -> t.name.equalsIgnoreCase(Obj.Paste)).findAny();
    //            if(Herb.isPresent())
    //            {
    //                HerbDBMap.get(Herb.get().id).paste_type = PasteType.valueOf(Obj.type);
    //                HerbDBMap.get(Herb.get().id).paste_count =Obj.undefined;
    //            }
    //        }
    //
    //        Reader.endArray();
    //
    //        System.out.print(gson.toJson(HerbDBMap));
    //
    //
    //    }
}
