package OSRSDatabase;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;

import javax.annotation.Nullable;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class FoodDB extends OSRSDataBase
{

    final private static String                           FoodDB            = "Items/foodDB.json";
    final private static ReentrantLock                    FoodDBLock        = new ReentrantLock();
    final private static ConcurrentHashMap<Integer, Food> FoodDBCache       = new ConcurrentHashMap<>();
    final private static String                           CommonFoodDB      = "Items/commonfoodDB.json";
    final private static ReentrantLock                    CommonFoodDBLock  = new ReentrantLock();
    final private static ConcurrentHashMap<Integer, Food> CommonFoodDBCache = new ConcurrentHashMap<>();
    final private static String                           BattleFoodDB      = "Items/battlefoodDB.json";
    final private static ReentrantLock                    BattleFoodDBLock  = new ReentrantLock();
    final private static ConcurrentHashMap<Integer, Food> BattleFoodDBCache = new ConcurrentHashMap<>();

    public static class Food implements Serializable
    {
        @SerializedName("id")
        public           int     id;
        @SerializedName("name")
        public           String  name;
        @SerializedName("hitpoints")
        public           Integer hitpoints;
        @SerializedName("hitpointAlt")
        public @Nullable String  hitpointAlt;
        @SerializedName("uses")
        public           Integer uses;
        @SerializedName("members")
        public           boolean members;

        public String toString()
        {
            return _toString(this, this.getClass());
        }
    }

    public static Food[] GetBattleFoods(boolean f2p)
    {
        if(!BattleFoodDBCache.isEmpty())
        {
            return BattleFoodDBCache.values().toArray(new Food[0]);
        }

        List<Food> out = new ArrayList<>();
        try
        {
            BattleFoodDBLock.lock();
            InputStreamReader File   = new InputStreamReader(GetInputStream(BattleFoodDB));
            JsonReader        Reader = new JsonReader(File);
            Gson              gson   = new Gson();
            Reader.setLenient(true);

            Reader.beginObject();

            while(Reader.hasNext())
            {
                int  ID  = Integer.parseInt(Reader.nextName());
                Food Obj = gson.fromJson(Reader, Food.class);
                if(!f2p || Obj.members)
                {
                    out.add(Obj);
                }
                BattleFoodDBCache.put(ID, Obj);
            }

            Reader.endObject();
            Reader.close();

        } catch(Exception e)
        {
            if(BattleFoodDBLock.isLocked() && BattleFoodDBLock.isHeldByCurrentThread())
            {
                BattleFoodDBLock.unlock();
            }
            throw new RuntimeException(e);
        }

        BattleFoodDBLock.unlock();
        return out.toArray(out.toArray(new Food[0]));
    }

    public static Food[] GetCommonFoods(boolean Member)
    {
        if(!CommonFoodDBCache.isEmpty())
        {
            return CommonFoodDBCache.values().toArray(new Food[0]);
        }

        List<Food> out = new ArrayList<>();
        try
        {
            CommonFoodDBLock.lock();
            InputStreamReader File   = new InputStreamReader(GetInputStream(CommonFoodDB));
            JsonReader        Reader = new JsonReader(File);
            Gson              gson   = new Gson();
            Reader.setLenient(true);

            Reader.beginObject();

            while(Reader.hasNext())
            {
                int  ID  = Integer.parseInt(Reader.nextName());
                Food Obj = gson.fromJson(Reader, Food.class);
                if(!Obj.members || Member)
                {
                    out.add(Obj);
                }
                CommonFoodDBCache.put(ID, Obj);
            }

            Reader.endObject();
            Reader.close();

        } catch(Exception e)
        {
            if(CommonFoodDBLock.isLocked() && CommonFoodDBLock.isHeldByCurrentThread())
            {
                CommonFoodDBLock.unlock();
            }
            throw new RuntimeException(e);
        }

        CommonFoodDBLock.unlock();
        return out.toArray(out.toArray(new Food[0]));
    }

    public static Food[] GetFoods(boolean f2p)
    {
        if(!FoodDBCache.isEmpty())
        {
            return FoodDBCache.values().toArray(new Food[0]);
        }

        List<Food> out = new ArrayList<>();
        try
        {
            FoodDBLock.lock();
            InputStreamReader File   = new InputStreamReader(GetInputStream(FoodDB));
            JsonReader        Reader = new JsonReader(File);
            Gson              gson   = new Gson();
            Reader.setLenient(true);

            Reader.beginObject();

            while(Reader.hasNext())
            {
                int  ID  = Integer.parseInt(Reader.nextName());
                Food Obj = gson.fromJson(Reader, Food.class);
                if(!f2p || Obj.members)
                {
                    out.add(Obj);
                }
                FoodDBCache.put(ID, Obj);
            }

            Reader.endObject();
            Reader.close();

        } catch(Exception e)
        {
            if(FoodDBLock.isLocked() && FoodDBLock.isHeldByCurrentThread())
            {
                FoodDBLock.unlock();
            }
            throw new RuntimeException(e);
        }

        FoodDBLock.unlock();
        return out.toArray(out.toArray(new Food[0]));
    }

    public static boolean isFood(int ID)
    {
        var food = GetFood(ID);
        return food != null;
    }

    public static Food GetFood(int FoodID)
    {
        if(!FoodDBCache.containsKey(FoodID))
        {
            return FoodDBCache.get(FoodID);
        }

        try
        {
            FoodDBLock.lock();
            InputStreamReader File   = new InputStreamReader(GetInputStream(FoodDB));
            JsonReader        Reader = new JsonReader(File);
            Gson              gson   = new Gson();
            Reader.setLenient(true);

            Reader.beginObject();

            while(Reader.hasNext())
            {
                int ID = Integer.parseInt(Reader.nextName());

                if(FoodID == ID)
                {
                    Food Obj = gson.fromJson(Reader, Food.class);
                    FoodDBCache.put(ID, Obj);
                    FoodDBLock.unlock();
                    return Obj;
                }
                else
                {
                    Reader.skipValue();
                }
            }

            Reader.endObject();
            Reader.close();

        } catch(Exception e)
        {
            if(FoodDBLock.isLocked() && FoodDBLock.isHeldByCurrentThread())
            {
                FoodDBLock.unlock();
            }
            throw new RuntimeException(e);
        }

        FoodDBLock.unlock();
        return null;
    }
}
