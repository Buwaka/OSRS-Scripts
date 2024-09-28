package OSRSDatabase;

import Utilities.OSRSUtilities;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.dreambot.api.utilities.Logger;

import javax.annotation.Nullable;
import java.io.InputStreamReader;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class CookingDB extends OSRSDataBase
{
    final private static String                                   CookingDBPath  = "FuckedcookingDB.json";
    final private static String                                   MeatFishDBPath = "cookingMeatFishDB.json";
    private static       ConcurrentHashMap<Integer, CookingData>  MeatFishDBMap  = null;
    private static       ConcurrentHashMap<Integer, CookingData2> CookingDBMap   = null;


    //TODO database for food that just needs to be cooked, and one for food that needs to be combined

    public static class CookingData
    {
        public @Nullable Integer   id;
        public           int       level;
        public           String    name;
        public           int       xp;
        public           boolean   members;
        public @Nullable Integer[] ingredients;
        public           int       burnt_id;
    }

    public static class CookingData2
    {
        public String  Name;
        public Boolean Members;
        public Integer CookingLevel;
        public float   CookingXP;

        public List<String>          Ingredients = null;
        public List<String>          Tools       = null;
        public List<String>          Facilities  = null;
        //public List<String> Skills         = new ArrayList<>();
        public EnumSet<ItemDB.Skill> Skill       = null;
        //        public List<String> Materialsused2 = new ArrayList<>();
        //        public List<String> Toolsused2      = new ArrayList<>();
        //        public List<String> Facilitiesused2 = new ArrayList<>();
        //        public List<String> Skillsinvolved2 = new ArrayList<>();
    }

    public static void main(String[] args)
    {
        ReadCookingDB();
    }

    private static void ReadCookingDB()
    {
        CookingDBMap = new ConcurrentHashMap<>();

        try
        {
            InputStreamReader File   = new InputStreamReader(GetInputStream(CookingDBPath));
            JsonReader        Reader = new JsonReader(File);
            Gson              gson   = new Gson();
            Reader.setLenient(true);

            //Reader.beginObject();
            Reader.beginArray();

            int i    = 0;
            int last = i;
            while(Reader.hasNext())
            {
                //int             ID  = Integer.parseInt(Reader.nextName());
                CookingData2 Obj = gson.fromJson(Reader, CookingData2.class);

                if(Obj.Name == null)
                {
                    //                    if(Obj.Materialsused != null)
                    //                    {
                    //                        CookingDBMap.get(last).Materialsused2.add(Obj.Materialsused);
                    //                    }
                    //                    if(Obj.Toolsused != null)
                    //                    {
                    //                        CookingDBMap.get(last).Toolsused2.add(Obj.Toolsused);
                    //                    }
                    //                    if(Obj.Facilitiesused != null)
                    //                    {
                    //                        CookingDBMap.get(last).Facilitiesused2.add(Obj.Facilitiesused);
                    //
                    //                    }
                    //                    if(Obj.Skillsinvolved != null)
                    //                    {
                    //                        CookingDBMap.get(last).Skillsinvolved2.add(Obj.Skillsinvolved);
                    //
                    //                    }


                }
                else
                {
                    //                    Obj.Ingredients    = Obj.Materialsused2;
                    //                    Obj.Tools          = Obj.Toolsused2;
                    //                    Obj.Facilities = Obj.Facilitiesused2;
                    //                    Obj.Skills     = Obj.Skillsinvolved2;

                    //                    if(!Obj.Skills.isEmpty())
                    //                    {
                    //                        EnumSet<ItemDB.Skill> skol = EnumSet.noneOf(ItemDB.Skill.class);
                    //                        for(var skill : Obj.Skills)
                    //                        {
                    //                            if(skill == null)
                    //                            {
                    //                                continue;
                    //                            }
                    //                            String sskil = Arrays.stream(skill.toUpperCase().split(" ")).findFirst().get();
                    //                            ItemDB.Skill skel = ItemDB.Skill.valueOf(sskil);
                    //                            skol.add(skel);
                    //                        }
                    //                        Obj.Skill = skol;
                    //                    }


                    CookingDBMap.put(i, Obj);
                    last = i;
                    i++;
                }


            }

            Reader.endArray();
            //Reader.endObject();
            Reader.close();

        } catch(Exception e)
        {
            Logger.log("Error reading HerbDB, Exception: " + e);
            throw new RuntimeException(e);
        }


        Gson gson = OSRSUtilities.OSRSGsonBuilder.disableHtmlEscaping().create();
        System.out.println(gson.toJson(CookingDBMap.values()));
    }

    private static void ReadMeatFishDB()
    {
        MeatFishDBMap = new ConcurrentHashMap<>();

        try
        {
            InputStreamReader File   = new InputStreamReader(GetInputStream(MeatFishDBPath));
            JsonReader        Reader = new JsonReader(File);
            Gson              gson   = new Gson();
            Reader.setLenient(true);

            //Reader.beginObject();
            Reader.beginArray();

            int i = 0;
            while(Reader.hasNext())
            {
                //int             ID  = Integer.parseInt(Reader.nextName());
                CookingData Obj = gson.fromJson(Reader, CookingData.class);
                MeatFishDBMap.put(i, Obj);

                i++;
            }

            Reader.endArray();
            //Reader.endObject();
            Reader.close();

        } catch(Exception e)
        {
            Logger.log("Error reading HerbDB, Exception: " + e);
            throw new RuntimeException(e);
        }

        for(var item : MeatFishDBMap.entrySet())
        {
            ItemDB.ItemData CookedData = ItemDB.GetClosestMatch(item.getValue().name, true);
            String          RawName    = item.getValue().name;
            if(RawName.contains("Cooked"))
            {
                RawName = RawName.replace("Cooked", "Raw");
            }
            else
            {
                RawName = "Raw " + RawName;
            }
            ItemDB.ItemData RawData = ItemDB.GetClosestMatch(RawName, true);

            System.out.println(
                    item.getValue().name + " = " + CookedData.name + " = " + RawData.name);
            MeatFishDBMap.get(item.getKey()).id          = CookedData.id;
            MeatFishDBMap.get(item.getKey()).ingredients = new Integer[]{RawData.id};
        }

        Gson gson = OSRSUtilities.OSRSGsonBuilder.create();
        System.out.println(gson.toJson(MeatFishDBMap.values()));
    }


}
