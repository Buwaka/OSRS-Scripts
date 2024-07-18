package OSRSDatabase;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.dreambot.api.utilities.Logger;

import java.io.InputStreamReader;
import java.util.concurrent.ConcurrentHashMap;

public class HerbDB extends OSRSDataBase
{
    final private static String                               HerbDBPath = "herbDB.json";
    private static       ConcurrentHashMap<Integer, HerbData> HerbDBMap  = null;

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
        HerbDBMap = new ConcurrentHashMap<>();

        try
        {
            InputStreamReader File   = new InputStreamReader(GetInputStream(HerbDBPath));
            JsonReader        Reader = new JsonReader(File);
            Gson              gson   = new Gson();
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

        return HerbDBMap.search(1, (key, val) -> val.name.equalsIgnoreCase(name)) != null;
    }

    public static boolean isCleanHerb(int ID)
    {
        return !isGrimyHerb(ID);
    }

    public static boolean isGrimyHerb(int ID)
    {
        if(HerbDBMap == null)
        {
            ReadHerbDB();
        }

        return HerbDBMap.containsKey(ID) && HerbDBMap.get(ID).grimy_id == ID;
    }

    public static int[] GetGrimyHerbList()
    {
        if(HerbDBMap == null)
        {
            ReadHerbDB();
        }

        return HerbDBMap.keySet().stream().filter(t -> isGrimyHerb(t)).mapToInt(t -> t).toArray();
    }

    public static int[] GetCleanHerbList()
    {
        if(HerbDBMap == null)
        {
            ReadHerbDB();
        }

        return HerbDBMap.keySet().stream().filter(t -> !isGrimyHerb(t)).mapToInt(t -> t).toArray();
    }

    public static int[] GetHerbList()
    {
        if(HerbDBMap == null)
        {
            ReadHerbDB();
        }

        return HerbDBMap.keySet().stream().mapToInt(t -> t).toArray();
    }
}
