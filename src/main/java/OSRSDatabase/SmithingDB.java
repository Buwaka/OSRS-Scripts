package OSRSDatabase;

import Utilities.OSRSUtilities;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import io.vavr.Tuple2;
import org.dreambot.api.utilities.Logger;

import javax.annotation.Nullable;
import java.io.InputStreamReader;
import java.util.concurrent.ConcurrentHashMap;

public class SmithingDB extends OSRSDataBase
{
    final private static String                                  SmithDBPath   = "Skilling/smithingDB.json";
    final private static String                                  SmeltDBPath   = "smeltingDB.json";
    private static       ConcurrentHashMap<Integer, SmithData[]> SmithingDBMap = null;
    private static       ConcurrentHashMap<Integer, SmeltData>   SmeltDBMap    = null;

    public static class SmithData
    {
        public int     id;
        public int     Level;
        public String  Name;
        public float   XP;
        public int     Bars;
        public boolean Members;

        public String toString()
        {
            return _toString(this, this.getClass());
        }
    }

    public static class SmeltData
    {

        public           int                        id;
        public           int                        Level;
        public           String                     Name;
        //public int[]  Ore;
        public           Tuple2<Integer, Integer>[] Ores;
        public           float                      Experience;
        public           boolean                    Members;
        public @Nullable Boolean                    NeedForgingRing;

        public String toString()
        {
            return _toString(this, this.getClass());
        }
    }

    public static void main(String[] args)
    {
        ReadSmithDB();
        ReadSmeltDB();


        ConcurrentHashMap<Integer, SmithData[]> copy = new ConcurrentHashMap<Integer, SmithData[]>(
                SmithingDBMap);
        ConcurrentHashMap<Integer, SmeltData> copy2 = new ConcurrentHashMap<Integer, SmeltData>(
                SmeltDBMap);

        //        for(var bar : SmeltDBMap.entrySet())
        //        {
        //            List<Tuple2<Integer, Integer>> ores = new ArrayList<>();
        //            for(int i = 0; i < bar.getValue().Ore.length; i += 2)
        //            {
        //                ores.add(new Tuple2<>(bar.getValue().Ore[i], bar.getValue().Ore[i + 1]));
        //            }
        //            copy2.get(bar.getKey()).Ores = ores.toArray(new Tuple2[0]);
        //        }

        //        for(var bar : SmithingDBMap.entrySet())
        //        {
        //            String BarName = SmeltDBMap.get(bar.getKey()).Name.toLowerCase().replace("bar", "").replace(" ", "");
        //
        //            if(BarName.equalsIgnoreCase("runite"))
        //            {
        //                BarName = "rune";
        //            }
        //            if(BarName.equalsIgnoreCase("adamantite"))
        //            {
        //                BarName = "adamant";
        //            }
        //
        //
        //            var items =  ItemDB.GetAllItemKeywordMatch(BarName);
        //            for(var smithItem : bar.getValue())
        //            {
        //                var newname = smithItem.Name.replace("Two-handed", "2h").toLowerCase();
        //                String finalBarName = BarName;
        //                Arrays.sort(items, (t1, t2) ->
        //                {
        //                    Integer left = LevenshteinDistance.getDefaultInstance().apply(finalBarName + " " + newname, t1.name.toLowerCase());
        //                    Integer right = LevenshteinDistance.getDefaultInstance().apply(finalBarName + " " + newname, t2.name.toLowerCase());
        //                    if(left == -1)
        //                    {
        //                        left = Integer.MAX_VALUE;
        //                    }
        //                    if(right == -1)
        //                    {
        //                        right = Integer.MAX_VALUE;
        //                    }
        //                    return left.compareTo(right);
        //                });
        //
        //                for(int i = 0; i < copy.get(bar.getKey()).length; i++ )
        //                {
        //                    if(Objects.equals(copy.get(bar.getKey())[i].Name, smithItem.Name))
        //                    {
        //                        System.out.println(BarName + " " + smithItem.Name + " <=> " + copy.get(bar.getKey())[i].Name + " " + items[0].name);
        //                        copy.get(bar.getKey())[i].Name = items[0].name;
        //                        copy.get(bar.getKey())[i].id = items[0].id;
        //                    }
        //                }
        //            }
        //
        //        }

        Gson gson = OSRSUtilities.OSRSGsonBuilder.create();
        System.out.println(gson.toJson(copy2));
    }

    private static void ReadSmeltDB()
    {
        SmeltDBMap = new ConcurrentHashMap<>();

        try
        {
            InputStreamReader File   = new InputStreamReader(GetInputStream(SmeltDBPath));
            JsonReader        Reader = new JsonReader(File);
            Gson              gson   = new Gson();
            Reader.setLenient(true);

            Reader.beginObject();

            while(Reader.hasNext())
            {
                int       ID  = Integer.parseInt(Reader.nextName());
                SmeltData Obj = gson.fromJson(Reader, SmeltData.class);
                SmeltDBMap.put(ID, Obj);
            }

            Reader.endObject();
            Reader.close();

        } catch(Exception e)
        {
            Logger.log("Error reading SmeltDB, Exception: " + e);
            throw new RuntimeException(e);
        }
    }

    private static void ReadSmithDB()
    {
        SmithingDBMap = new ConcurrentHashMap<>();

        try
        {
            InputStreamReader File   = new InputStreamReader(GetInputStream(SmithDBPath));
            JsonReader        Reader = new JsonReader(File);
            Gson              gson   = new Gson();
            Reader.setLenient(true);

            Reader.beginObject();

            while(Reader.hasNext())
            {
                int         ID  = Integer.parseInt(Reader.nextName());
                SmithData[] Obj = gson.fromJson(Reader, SmithData[].class);
                SmithingDBMap.put(ID, Obj);
            }

            Reader.endObject();
            Reader.close();

        } catch(Exception e)
        {
            Logger.log("Error reading SmithDB, Exception: " + e);
            throw new RuntimeException(e);
        }
    }

}
