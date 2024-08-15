package OSRSDatabase;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Objects;


public class OSRSDataBase
{
    protected static String _toString(Object ths, Class klas)
    {
        Field[]       fields = klas.getFields();
        StringBuilder result = new StringBuilder();
        for(Field field : fields)
        {
            try
            {
                if(field.getType().isArray())
                {
                    result.append(field.getName()).append(",\n ");
                    for(var item : (Object[]) field.get(ths))
                    {
                        result.append(field.getName())
                              .append(": ")
                              .append(item.toString())
                              .append(",\n");
                    }
                }
                else
                {
                    result.append(field.getName())
                          .append(": ")
                          .append(field.get(ths).toString())
                          .append(",\n");
                }

            } catch(Exception ignored)
            {
            }
        }
        return result.toString();

    }

    private static HashSet<JSONObject> GetDataDebug()
    {
        HashSet<JSONObject> out = new HashSet<>();


        try
        {
            InputStreamReader File   = new InputStreamReader(GetInputStream("Food.json"));
            JsonReader        Reader = new JsonReader(File);
            Gson              gson   = new Gson();
            Reader.setLenient(true);

            //Reader.beginObject();

            Reader.beginArray();
            //            while(Reader.hasNext())
            //            {
            //                //Reader.beginObject();
            //                Foodjson Obj = gson.fromJson(Reader, Foodjson.class);
            //                out.add(Obj);
            //                //Reader.endObject();
            //            }
            //            Reader.endArray();
            //int ID = Integer.parseInt(Reader.nextName());

            //                MonsterData Obj = gson.fromJson(Reader, MonsterData.class);
            //                MonsterDBCache.put(ID, Obj);
            //                Reader.beginObject();
            //                while(Reader.hasNext())
            //                {
            //                    String NextName = Reader.nextName();
            //                    if(Objects.equals(NextName, "slayer_masters") && Reader.peek() != NULL)
            //                    {
            //                        Reader.beginArray();
            //                        while(Reader.hasNext())
            //                        {
            //                            if(Reader.peek() != NULL)
            //                            {
            //                                out.add(Reader.nextString());
            //                            }
            //                            else
            //                            {
            //                                Reader.skipValue();
            //                            }
            //                        }
            //                        Reader.endArray();
            //                    }
            //                    else
            //                    {
            //                        Reader.skipValue();
            //                    }
            //                }
            //                Reader.endObject();
            //            }
            //
            //            Reader.endObject();

        } catch(Exception e)
        {
            throw new RuntimeException(e);
        }

        return out;
    }

    protected static InputStream GetInputStream(String DB)
    {
        return new BufferedInputStream(Objects.requireNonNull(OSRSDataBase.class.getClassLoader()
                                                                                .getResourceAsStream(
                                                                                        DB)));
    }

    private static void main(String[] args) throws JSONException
    {
        //var food = new int[] {13441, 24592, 11936, 7060, 391, 24589, 397, 385, 6969, 20390, 23874, 20856, 20858, 20860, 20862, 20864, 20866, 20868, 20871, 20873, 20875, 20877, 20879, 20881, 20883, 1883, 1885, 4608, 7058, 2011, 7056, 6705, 7946, 20547, 7568, 2343, 7054, 6703, 373, 365, 7068, 379, 2003, 1993, 2149, 5003, 7066, 10136, 7530, 2878, 7228, 5988, 7088, 1971, 22929, 361, 7086, 329, 7064, 6883, 3381, 351, 339, 9988, 333, 5972, 6293, 6297, 6295, 6299, 6303, 3373, 355, 337, 9980, 5004, 24785, 3228, 7062, 7082, 7084, 7078, 347, 3369, 3371, 2309, 6701, 1875, 1895, 1901, 325, 1861, 2142, 4293, 2140, 4291, 7072, 1985, 9996, 7070, 1963, 3162, 1982, 1869, 9994, 319, 315, 1942, 5984, 2118, 2116, 1957, 1871, 2108, 2112, 5504, 1965, 9475, 4561, 10476, 27351, 4049, 25202, 25730, 464, 6469, 6311, 247, 2126, 2398, 2130, 2128, 403, 28443, 28422, 1891, 24549, 1897, 1893, 1899, 2325, 2327, 2323, 2289, 2293, 2297, 2301, 2333, 2331, 2291, 2295, 2299, 2303, 7178, 7180, 7188, 7190, 19662, 19659, 21690, 21687, 7198, 7200, 22795, 22792, 7208, 7210, 7218, 7220, 7521, 7523, 7524, 7525, 7526};


        //        var item = GetItemData(35);
        //
        //        System.out.println("Item: " + item.toString() + "\n " + item);


        //        var monster = GetMonsterLootTable(2090);
        //        System.out.println("Monster: " + Arrays.toString(monster));
        //        monster = GetMonsterLootTable(2091);
        //        System.out.println("Monster: " + Arrays.toString(monster));
        //        monster = GetMonsterLootTable(2087);
        //        System.out.println("Monster: " + Arrays.toString(monster));
        //        monster = GetMonsterLootTable(2);
        //        System.out.println("Monster: " + Arrays.toString(monster));

        //OSRSUtilities.IsTimeElapsed(UUID.randomUUID().getMostSignificantBits(), 1000);

        //F2P
        //        List<Altar> path = new ArrayList<>();
        //
        //
        //
        //        path.add(new Altar(new Tile(3052, 3497, 1)));
        //        path.add(new Altar(new Tile(3027, 3512, 1)));


        //        var gson = OSRSUtilities.OSRSGsonBuilder.create();
        //
        //        System.out.println(gson.toJson(Req));


    }

}

