package Database;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.FileReader;
import java.lang.reflect.Field;


public class OSRSDataBase
{
    final private static String ItemDBPath = "DataBase/items/items-cache-data.json";
    final private  static String MonsterLootDBPath = "DataBase/monsters/monsters-drops.json";

    public class ItemData
    {
        public int id;
        public String name;
        public boolean tradeable_on_ge;
        public boolean members;
        public boolean noted;
        public boolean noteable;
        public boolean placeholder;
        public boolean stackable;
        public boolean equipable;
        public int highalch;
        public int stacked;

        public String toString()
        {
            Field[] fields = this.getClass().getFields();
            String result = "";
            for (int i = 0; i < fields.length; i++)
            {
                Field field = fields[i];
                try
                {
                    result += field.getName() + ": " + field.get(this).toString() + ", ";
                }
                catch (Exception e)
                {}
            }
            return result;
        }
    }

    public class Monster
    {


        public String toString()
        {
            Field[] fields = this.getClass().getFields();
            String result = "";
            for (int i = 0; i < fields.length; i++)
            {
                Field field = fields[i];
                try
                {
                    result += field.getName() + ": " + field.get(this).toString() + ", ";
                }
                catch (Exception e)
                {}
            }
            return result;
        }
    }

    public class MonsterDrop
    {
        int id; //The ID number of the item drop
        String name;//	The name of the item drop
        boolean members; //If the drop is a members-only item
        String quantity; //The quantity of the item drop (integer, comma-separated or range).
        boolean noted; //If the item drop is noted, or not.
        float rarity; //The rarity of the item drop (as a float out of 1.0).
        int rolls; //Number of rolls from the drop.


        public String toString()
        {
            Field[] fields = this.getClass().getDeclaredFields();
            String result = "";
            for (int i = 0; i < fields.length; i++)
            {
                Field field = fields[i];
                try
                {
                    result += field.getName() + ": " + field.get(this).toString() + ", ";
                }
                catch (Exception e)
                {
                    System.out.println("Fail");
                }
            }
            return result;
        }
    }

    public static MonsterDrop[] GetMonsterLootTable(int MonsterID)
    {
        try
        {
            FileReader File = new FileReader(MonsterLootDBPath);
            JsonReader Reader = new JsonReader(File);
            Gson gson = new Gson();
            Reader.setLenient(true);

            Reader.beginObject();

            while(Reader.hasNext())
            {
                int ID = Integer.parseInt(Reader.nextName());
                if(MonsterID == ID)
                {
                    System.out.println("Name: " + ID);
                    MonsterDrop[] LootTable = gson.fromJson(Reader, MonsterDrop[].class);
                    Reader.close();
                    return LootTable;
                }
                else
                {
                    Reader.skipValue();
                }
            }

            Reader.endObject();

        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        return null;
    }

    public static ItemData GetItemData(int ItemID)
    {
        try
        {
            FileReader File = new FileReader(ItemDBPath);
            JsonReader Reader = new JsonReader(File);
            Gson gson = new Gson();
            Reader.setLenient(true);

            Reader.beginObject();

            while(Reader.hasNext())
            {
                int ID = Integer.parseInt(Reader.nextName());
                if(ItemID == ID)
                {
                    System.out.println("Name: " + ID);
                    ItemData Obj = gson.fromJson(Reader, ItemData.class);
                    Reader.close();
                    return Obj;
                }
                else
                {
                    Reader.skipValue();
                }
            }

            Reader.endObject();

        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        return null;
    }

    public static void main(String[] args)
    {
        var Item = GetMonsterLootTable(2093);
        for(var item : Item)
        {
            System.out.println("Item: " + item.toString());
        }

    }
}

