package OSRSDatabase;

import Utilities.OSRSUtilities;
import Utilities.Requirement.IRequirement;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.dreambot.api.utilities.Logger;

import javax.annotation.Nullable;
import java.io.InputStreamReader;
import java.util.HashMap;

public class MiningDB extends OSRSDataBase
{
    final private static String                         OreDBPath  = "Skilling/OreDB.json";
    private static       HashMap<Integer, OreData>      OreDBMap   = null;
    final private static String                         ToolDBPath = "Items/Tools/miningtoolsDB.json";
    private static       HashMap<Integer, MineToolData> ToolDBMap  = null;


    public static class OreData
    {
        public           Integer   id;
        public @Nullable Integer[] variations;
        public           String    name;
        public @Nullable String    general_name;
        public           int       level;
        public           float     exp;
        public           boolean   members;

        @Override
        public String toString()
        {
            return _toString(this, MineToolData.class);
        }
    }

    public static class MineToolData extends ToolDB.ToolData
    {
        public float ticks;

        @Override
        public String toString()
        {
            return _toString(this, MineToolData.class);
        }
    }


    private static void ReadOreDB()
    {
        if(OreDBMap != null)
        {
            return;
        }

        OreDBMap = new HashMap<>();

        try
        {
            InputStreamReader File   = new InputStreamReader(GetInputStream(OreDBPath));
            JsonReader        Reader = new JsonReader(File);
            Gson              gson   = OSRSUtilities.OSRSGsonBuilder.create();
            Reader.setLenient(true);

            Reader.beginObject();

            while(Reader.hasNext())
            {
                int     ID  = Integer.parseInt(Reader.nextName());
                OreData Obj = gson.fromJson(Reader, OreData.class);
                OreDBMap.put(ID, Obj);
            }

            Reader.endObject();
            Reader.close();

        } catch(Exception e)
        {
            Logger.log("Error reading OreDB, Exception: " + e);
            throw new RuntimeException(e);
        }
    }

    private static void ReadToolDB()
    {
        if(ToolDBMap != null)
        {
            return;
        }

        ToolDBMap = new HashMap<>();

        try
        {
            InputStreamReader File   = new InputStreamReader(GetInputStream(ToolDBPath));
            JsonReader        Reader = new JsonReader(File);
            Gson              gson   = OSRSUtilities.OSRSGsonBuilder.create();
            Reader.setLenient(true);

            Reader.beginObject();

            while(Reader.hasNext())
            {
                int          ID  = Integer.parseInt(Reader.nextName());
                MineToolData Obj = gson.fromJson(Reader, MineToolData.class);
                ToolDBMap.put(ID, Obj);
            }

            Reader.endObject();
            Reader.close();

        } catch(Exception e)
        {
            Logger.log("Error reading ToolDB, Exception: " + e);
            throw new RuntimeException(e);
        }
    }

    //    public static void main(String[] args) // making the OreDB.Json
    //    {
    //        ReadOreDB();
    //
    //        Gson              gson   = OSRSUtilities.OSRSGsonBuilder.create();
    //
    //
    //        HashMap<Integer, OreData> out        = new HashMap<>();
    //        for(var ore : OreDBMap.entrySet())
    //        {
    ////            ItemDB.ItemData Exactitem = ItemDB.GetClosestMatch(ore.getValue().name + " ore", 0,  true);
    ////            ItemDB.ItemData item = Exactitem != null ? Exactitem :  ItemDB.GetClosestMatch(ore.getValue().name, true);
    //
    //            ore.getValue().general_name = ore.getValue().name;
    //            ore.getValue().name = ItemDB.GetItemData(ore.getKey()).name;
    //            out.put(ore.getKey(), ore.getValue());
    //        }
    //
    //        System.out.print(gson.toJson(out));
    //    }

    public static void main(String[] args)// making the miningtoolDB.Json
    {
        ReadToolDB();
        Gson gson = OSRSUtilities.OSRSGsonBuilder.create();

        HashMap<Integer, MineToolData> out = new HashMap<>();
        for(var tool : ToolDBMap.entrySet())
        {

//            ItemDB.ItemData item = ItemDB.GetClosestMatch(tool.getValue().name, true);
//
//
//            if(item == null)
//            {
//                System.out.print(tool.getValue().name);
//                //                System.out.print(tool.getValue().name + " " + item.name + "\n");
//                //                tool.getValue().name = item.name;
//                //                tool.getValue().id = item.id;
//            }

//            IRequirement[] regs = new IRequirement[]{
//                    new LevelRequirement(ItemDB.Skill.ATTACK, tool.getValue().attack_level),
//                    new LevelRequirement(ItemDB.Skill.MINING, tool.getValue().mining_level)};
//            tool.getValue().requirements = regs;
//            tool.getValue().tags         = new String[]{"cheap"};
//            tool.getValue().attack_level = null;
//            tool.getValue().mining_level = null;

            ItemDB.ItemData item = ItemDB.GetItemData(tool.getKey());

            tool.getValue().equipable = item.equipable;
            tool.getValue().ge_tradable = item.tradeable_on_ge;
            tool.getValue().type = ToolDB.ToolData.Type.Pickaxe;

            out.put(tool.getKey(), tool.getValue());
        }

        System.out.print(gson.toJson(out));
    }
}
