package Database;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import org.dreambot.api.utilities.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;


public class OSRSDataBase
{
    final private static String                                  ItemDB                = "items-complete.json";
    final private static ReentrantLock                           ItemDBLock            = new ReentrantLock();
    final private static ConcurrentHashMap<Integer, ItemData>    ItemDBCache           = new ConcurrentHashMap<>();
    final private static String                                  MonsterDB             = "monsters-complete.json";
    final private static ReentrantLock                           MonsterDBLock         = new ReentrantLock();
    final private static ConcurrentHashMap<String, int[]>        MonsterIDsByNameCache = new ConcurrentHashMap<>();
    final private static ConcurrentHashMap<Integer, MonsterData> MonsterDBCache        = new ConcurrentHashMap<>();
    final private static String                                  FoodDB                = "foodID.json";
    final private static ReentrantLock                           FoodDBLock            = new ReentrantLock();
    final private static ConcurrentHashMap<Integer, Food>        FoodDBCache           = new ConcurrentHashMap<>();
    final private static String                                  CommonFoodDB          = "commonfood.json";
    final private static ReentrantLock                           CommonFoodDBLock      = new ReentrantLock();
    final private static ConcurrentHashMap<Integer, Food>        CommonFoodDBCache     = new ConcurrentHashMap<>();
    final private static String                                  BattleFoodDB          = "battlefood.json";
    final private static ReentrantLock                           BattleFoodDBLock      = new ReentrantLock();
    final private static ConcurrentHashMap<Integer, Food>        BattleFoodDBCache     = new ConcurrentHashMap<>();

    private static InputStream GetInputStream(String DB)
    {
        return new BufferedInputStream(Objects.requireNonNull(OSRSDataBase.class.getClassLoader().getResourceAsStream(DB)));
    }

    public static ItemData GetItemData(int ItemID)
    {
        if(ItemDBCache.containsKey(ItemID))
        {
            return ItemDBCache.get(ItemID);
        }
        try
        {
            ItemDBLock.lock();
            InputStreamReader File   = new InputStreamReader(GetInputStream(ItemDB));
            JsonReader        Reader = new JsonReader(File);
            Gson              gson   = new Gson();
            Reader.setLenient(true);

            Reader.beginObject();

            while(Reader.hasNext())
            {
                int ID = Integer.parseInt(Reader.nextName());
                if(ItemID == ID)
                {
                    ItemData Obj = gson.fromJson(Reader, ItemData.class);
                    Reader.close();
                    ItemDBCache.put(ItemID, Obj);
                    ItemDBLock.unlock();
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
            Logger.log("Tried to find ID: " + ItemID);
            if(ItemDBLock.isLocked() && ItemDBLock.isHeldByCurrentThread())
            {
                ItemDBLock.unlock();
            }
            throw new RuntimeException(e);
        }

        ItemDBLock.unlock();
        return null;
    }

    public static ItemData[] GetItemDataByName(String Name)
    {
        List<ItemData> out = new ArrayList<>();
        try
        {
            ItemDBLock.lock();
            InputStreamReader File   = new InputStreamReader(GetInputStream(ItemDB));
            JsonReader        Reader = new JsonReader(File);
            Gson              gson   = new Gson();
            Reader.setLenient(true);

            Reader.beginObject();

            while(Reader.hasNext())
            {
                int      ID  = Integer.parseInt(Reader.nextName());
                ItemData Obj = gson.fromJson(Reader, ItemData.class);
                if(Obj.name.equals(Name) && !Obj.placeholder && !Obj.noted)
                {
                    out.add(Obj);
                }
            }

            Reader.endObject();
            Reader.close();

        } catch(Exception e)
        {
            Logger.log("Tried to find ID: " + Name);
            if(ItemDBLock.isLocked() && ItemDBLock.isHeldByCurrentThread())
            {
                ItemDBLock.unlock();
            }
            throw new RuntimeException(e);
        }

        ItemDBLock.unlock();
        return out.toArray(out.toArray(new ItemData[0]));
    }

    public static int[] GetMonsterIDsByName(String Name, boolean Exact)
    {
        if(MonsterIDsByNameCache.containsKey(Name))
        {
            return MonsterIDsByNameCache.get(Name);
        }
        List<Integer> Result = new ArrayList<Integer>();
        try
        {
            MonsterDBLock.lock();
            InputStreamReader File   = new InputStreamReader(GetInputStream(MonsterDB));
            JsonReader        Reader = new JsonReader(File);
            Reader.setLenient(true);

            Reader.beginObject();

            while(Reader.hasNext())
            {
                int ID = Integer.parseInt(Reader.nextName());
                Reader.beginObject();
                while(Reader.hasNext())
                {
                    if(Reader.nextName().equals("name"))
                    {
                        String name = Reader.nextString();
                        if(Exact)
                        {
                            if(name.equalsIgnoreCase(Name))
                            {
                                Result.add(ID);
                            }
                        }
                        else if(name.toLowerCase().contains(Name.toLowerCase()))
                        {
                            Result.add(ID);
                        }
                    }
                    else
                    {
                        Reader.skipValue();
                    }
                }
                Reader.endObject();
            }

            Reader.endObject();
            Reader.close();

        } catch(Exception e)
        {
            Logger.log("Tried to find Name: " + Name);
            if(MonsterDBLock.isLocked() && MonsterDBLock.isHeldByCurrentThread())
            {
                MonsterDBLock.unlock();
            }
            throw new RuntimeException(e);
        }
        var Arr = Result.stream().mapToInt(Integer::intValue).toArray();
        MonsterIDsByNameCache.put(Name, Arr);
        MonsterDBLock.unlock();
        return Arr;
    }

    public static MonsterData GetMonsterData(int MonsterID)
    {
        if(MonsterDBCache.containsKey(MonsterID))
        {
            return MonsterDBCache.get(MonsterID);
        }

        try
        {
            MonsterDBLock.lock();
            InputStreamReader File   = new InputStreamReader(GetInputStream(MonsterDB));
            JsonReader        Reader = new JsonReader(File);
            Gson              gson   = new Gson();
            Reader.setLenient(true);

            Reader.beginObject();

            while(Reader.hasNext())
            {
                int ID = Integer.parseInt(Reader.nextName());

                if(MonsterID == ID)
                {
                    MonsterData Data = gson.fromJson(Reader, MonsterData.class);
                    MonsterDBCache.put(MonsterID, Data);
                    MonsterDBLock.unlock();
                    return Data;
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
            if(MonsterDBLock.isLocked() && MonsterDBLock.isHeldByCurrentThread())
            {
                MonsterDBLock.unlock();
            }
            throw new RuntimeException(e);
        }

        MonsterDBLock.unlock();
        return null;
    }

    public static MonsterData.MonsterDrop[] GetMonsterLootTable(int MonsterID)
    {
        MonsterData monster = GetMonsterData(MonsterID);
        if(monster != null)
        {
            return monster.drops;
        }
        return null;
    }


    /**
     * @param MonsterID
     *
     * @return Only returns null if monster is not found, since monsters are required to have a size
     */
    public static @Nullable Integer GetMonsterSize(int MonsterID)
    {
        MonsterData monster = GetMonsterData(MonsterID);
        if(monster != null)
        {
            return monster.size;
        }
        return null;
    }

    public static boolean isFood(int ID)
    {
        var food = GetFood(ID);
        if(food != null)
        {
            return true;
        }
        return false;
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

    private static String _toString(Object ths, Class klas)
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
                        result.append(field.getName()).append(": ").append(item.toString()).append(",\n");
                    }
                }
                else
                {
                    result.append(field.getName()).append(": ").append(field.get(ths).toString()).append(",\n");
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

    public static void main(String[] args) throws JSONException
    {
        //var food = new int[] {13441, 24592, 11936, 7060, 391, 24589, 397, 385, 6969, 20390, 23874, 20856, 20858, 20860, 20862, 20864, 20866, 20868, 20871, 20873, 20875, 20877, 20879, 20881, 20883, 1883, 1885, 4608, 7058, 2011, 7056, 6705, 7946, 20547, 7568, 2343, 7054, 6703, 373, 365, 7068, 379, 2003, 1993, 2149, 5003, 7066, 10136, 7530, 2878, 7228, 5988, 7088, 1971, 22929, 361, 7086, 329, 7064, 6883, 3381, 351, 339, 9988, 333, 5972, 6293, 6297, 6295, 6299, 6303, 3373, 355, 337, 9980, 5004, 24785, 3228, 7062, 7082, 7084, 7078, 347, 3369, 3371, 2309, 6701, 1875, 1895, 1901, 325, 1861, 2142, 4293, 2140, 4291, 7072, 1985, 9996, 7070, 1963, 3162, 1982, 1869, 9994, 319, 315, 1942, 5984, 2118, 2116, 1957, 1871, 2108, 2112, 5504, 1965, 9475, 4561, 10476, 27351, 4049, 25202, 25730, 464, 6469, 6311, 247, 2126, 2398, 2130, 2128, 403, 28443, 28422, 1891, 24549, 1897, 1893, 1899, 2325, 2327, 2323, 2289, 2293, 2297, 2301, 2333, 2331, 2291, 2295, 2299, 2303, 7178, 7180, 7188, 7190, 19662, 19659, 21690, 21687, 7198, 7200, 22795, 22792, 7208, 7210, 7218, 7220, 7521, 7523, 7524, 7525, 7526};


        var item = GetItemData(35);

        System.out.println("Item: " + item.toString() + "\n " + item);


//        var monster = GetMonsterLootTable(2090);
//        System.out.println("Monster: " + Arrays.toString(monster));
//        monster = GetMonsterLootTable(2091);
//        System.out.println("Monster: " + Arrays.toString(monster));
//        monster = GetMonsterLootTable(2087);
//        System.out.println("Monster: " + Arrays.toString(monster));
//        monster = GetMonsterLootTable(2);
//        System.out.println("Monster: " + Arrays.toString(monster));

        //OSRSUtilities.IsTimeElapsed(UUID.randomUUID().getMostSignificantBits(), 1000);

    }

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

    public static class ItemData
    {

        public           int           id;
        public           String        name;
        public           String        last_updated;
        public           boolean       incomplete;
        public           boolean       members;
        public           boolean       tradeable;
        public           boolean       tradeable_on_ge;
        public           boolean       stackable;
        public @Nullable int           stacked;
        public           boolean       noted;
        public           boolean       noteable;
        public @Nullable int           linked_id_item;
        public @Nullable int           linked_id_noted;
        public @Nullable int           linked_id_placeholder;
        public           boolean       placeholder;
        public           boolean       equipable;
        public           boolean       equipable_by_player;
        public           boolean       equipable_weapon;
        public           int           cost;
        public @Nullable int           highalch;
        public @Nullable int           lowalch;
        public @Nullable float         weight;
        public @Nullable int           buy_limit;
        public           boolean       quest_item;
        public @Nullable String        release_date;
        public           boolean       duplicate;
        public @Nullable String        examine;
        public           String        icon;
        public @Nullable String        wiki_name;
        public @Nullable String        wiki_url;
        public @Nullable EquipmentData equipment;
        public @Nullable WeaponData    weapon;

        public String toString()
        {
            return _toString(this, this.getClass());
        }
    }

    public static class EquipmentData
    {
        public           int           attack_stab;
        public           int           attack_slash;
        public           int           attack_crush;
        public           int           attack_magic;
        public           int           attack_ranged;
        public           int           defence_stab;
        public           int           defence_slash;
        public           int           defence_crush;
        public           int           defence_magic;
        public           int           defence_ranged;
        public           int           melee_strength;
        public           int           ranged_strength;
        public           int           magic_damage;
        public           int           prayer;
        public           EquipmentSlot slot;
        public @Nullable JSONObject    requirements;

        public String toString()
        {
            return _toString(this, this.getClass());
        }


        public enum EquipmentSlot
        {
            hat,
            cape,
            amulet,
            weapon,
            chest,
            shield,
            legs,
            hands,
            feet,
            ring,
            arrows;
        }
    }

    public static class WeaponData
    {
        public int          attack_speed;
        public WeaponType   weapon_type;
        //public JSONObject stances;
        public StanceData[] stances;

        public String toString()
        {
            return _toString(this, this.getClass());
        }

        public enum WeaponType
        {
            scythe,
            gun,
            crossbow,
            slash_sword,
            pickaxe,
            spiked,
            bow,
            salamander,
            powered_staff,
            axe,
            whip,
            bulwark,
            spear,
            bladed_staff,
            partisan,
            stab_sword,
            @SerializedName("2h_sword") Twohand_sword,
            chinchompas,
            polearm,
            blunt,
            thrown,
            banner,
            staff,
            polestaff,
            claw,
            unarmed,
            bludgeon;
        }
    }

    public static class StanceData
    {

        public           CombatStyles   combat_style;
        public @Nullable Attacktype     attack_type;
        public @Nullable AttackStyles   attack_style;
        public           ExperienceType experience;
        public @Nullable String         boosts; //attack range by 2 squares, accuracy and damage, attack speed by 1 tick

        public String toString()
        {
            return _toString(this, this.getClass());
        }

        public enum CombatStyles
        {
            flare,
            accurate,
            rapid,
            focus,
            jab,
            lunge,
            pummel,
            spike,
            punch,
            hack,
            pound,
            blaze,
            reap,
            block,
            fend,
            smash,
            flick,
            @SerializedName("aim and fire") aim_and_fire,
            @SerializedName("short fuse") short_fuse,
            spell,
            @SerializedName("long fuse") long_fuse,
            longrange,
            chop,
            impale,
            @SerializedName("medium fuse") medium_fuse,
            stab,
            kick,
            swipe,
            @SerializedName("spell (defensive)") spell_defensive,
            deflect,
            lash,
            slash,
            bash,
            scorch;
        }

        public enum Attacktype
        {
            stab,
            slash,
            crush,
            magic,
            ranged,
            @SerializedName("defensive casting") defensive_casting,
            spellcasting;

        }


        public enum AttackStyles
        {
            controlled,
            magic,
            accurate,
            defensive,
            aggressive;
        }

        public enum ExperienceType
        {
            magic,
            shared,
            @SerializedName("magic and defence") magic_and_defence,
            strength,
            defence,
            ranged,
            attack,
            @SerializedName("ranged and defence") ranged_and_defence;
        }
    }

    public class MonsterData
    {

        public           int                     id;
        public           String                  name;
        public @Nullable String                  last_updated;
        public           boolean                 incomplete;
        public           boolean                 members;
        public @Nullable String                  release_date;
        public           int                     combat_level;
        public           int                     size;
        public @Nullable int                     hitpoints;
        public @Nullable int                     max_hit;
        public           StanceData.Attacktype[] attack_type;
        public @Nullable int                     attack_speed;
        public           boolean                 aggressive;
        public           boolean                 poisonous;
        public           boolean                 venomous;
        public           boolean                 immune_poison;
        public           boolean                 immune_venom;
        public           MonsterAttribute[]      attributes;
        public           MonsterCategory[]       category; // make enum
        public           boolean                 slayer_monster;
        public @Nullable int                     slayer_level;
        public @Nullable float                   slayer_xp;
        public           SlayerMaster[]          slayer_masters; // make enum
        public           boolean                 duplicate;
        public           String                  examine;
        public           String                  wiki_name;
        public           String                  wiki_url;
        public           int                     attack_level;
        public           int                     strength_level;
        public           int                     defence_level;
        public           int                     magic_level;
        public           int                     ranged_level;
        public           int                     attack_bonus;
        public           int                     strength_bonus;
        public           int                     attack_magic;
        public           int                     magic_bonus;
        public           int                     attack_ranged;
        public           int                     ranged_bonus;
        public           int                     defence_stab;
        public           int                     defence_slash;
        public           int                     defence_crush;
        public           int                     defence_magic;
        public           int                     defence_ranged;
        public           MonsterDrop[]           drops;

        public String toString()
        {
            return _toString(this, this.getClass());
        }

        public enum MonsterAttribute
        {
            spectral,
            golem,
            vampyre,
            xerician,
            fiery,
            penance,
            shade,
            undead,
            leafy,
            demon,
            dragon,
            kalphite
        }

        public enum MonsterCategory
        {
            @SerializedName("cave bugs") cave_bugs,
            crocodiles,
            scorpions,
            skeletons,
            banshees,
            @SerializedName("lava dragons") lava_dragons,
            shadow,
            warriors,
            @SerializedName("spiritual creatures") spiritual_creatures,
            none,
            rogues,
            kurask,
            @SerializedName("warped creatures") warped_creatures,
            @SerializedName("fossil island wyverns") fossil_island_wyverns,
            @SerializedName("lesser nagua") lesser_nagua,
            hellhounds,
            jellies,
            @SerializedName("green dragons") green_dragons,
            @SerializedName("flesh crawlers") flesh_crawlers,
            @SerializedName("moss giants") moss_giants,
            mogres,
            @SerializedName("jungle horrors") jungle_horrors,
            @SerializedName("black demons") black_demons,
            @SerializedName("otherworldly beings") otherworldly_beings,
            @SerializedName("harpie bug swarms") harpie_bug_swarms,
            bloodveld,
            birds,
            @SerializedName("mithril_dragons") mithril_dragons,
            brine_rats,
            @SerializedName("cave slimes") cave_slimes,
            @SerializedName("cave kraken") cave_kraken,
            bosses,
            @SerializedName("abyssal demons") abyssal_demons,
            zombies,
            @SerializedName("fever spiders") fever_spiders,
            @SerializedName("dark beasts") dark_beasts,
            elves,
            pirates,
            cows,
            @SerializedName("hill giants") hill_giants,
            lizardmen,
            @SerializedName("ice giants") ice_giants,
            @SerializedName("iron dragons") iron_dragons,
            @SerializedName("crawling hands") crawling_hands,
            @SerializedName("infernal mages") infernal_mages,
            ghosts,
            @SerializedName("dust devils") dust_devils,
            @SerializedName("fire giants") fire_giants,
            @SerializedName("black dragons") black_dragons,
            @SerializedName("chaos druids") chaos_druids,
            nechryael,
            vampyres,
            @SerializedName("red dragons") red_dragons,
            hydras,
            ogres,
            icefiends,
            basilisks,
            @SerializedName("mutated zygomites") mutated_zygomites,
            @SerializedName("aberrant spectres") aberrant_spectres,
            kalphite,
            @SerializedName("rune dragons") rune_dragons,
            killerwatts,
            @SerializedName("bronze dragons") bronze_dragons,
            waterfiends,
            dogs,
            scabarites,
            suqahs,
            rockslugs,
            @SerializedName("magic axes") magic_axes,
            @SerializedName("dark warriors") dark_warriors,
            @SerializedName("earth warriors") earth_warriors,
            tzhaar,
            bears,
            dwarves,
            spiders,
            pyrefiends,
            lizards,
            monkeys,
            wyrms,
            @SerializedName("sea snakes") sea_snakes,
            @SerializedName("black knights") black_knights,
            mammoths,
            sourhogs,
            bandits,
            gargoyles,
            hobgoblins,
            @SerializedName("skeletal wyverns") skeletal_wyverns,
            @SerializedName("cave horrors") cave_horrors,
            @SerializedName("greater demons") greater_demons,
            shades,
            @SerializedName("lesser demons") lesser_demons,
            @SerializedName("wall beasts") wall_beasts,
            minotaurs,
            @SerializedName("blue dragons") blue_dragons,
            trolls,
            ankou,
            wolves,
            werewolves,
            bats,
            @SerializedName("ice warriors") ice_warriors,
            dagannoth,
            rats,
            lizard,
            @SerializedName("cave crawlers") cave_crawlers,
            goblins,
            cockatrice,
            molanisks,
            @SerializedName("smoke devils") smoke_devils,
            catablepon,
            turoth,
            @SerializedName("terror dogs") terror_dogs,
            aviansies,
            @SerializedName("adamant dragons") adamant_dragons,
            @SerializedName("steel dragons") steel_dragons,
            revenants,
            ghouls,
            drakes
        }

        public enum SlayerMaster
        {
            duradel,
            @SerializedName("konar quo maten") konar_quo_maten,
            spria,
            chaeldar,
            nieve,
            vannaka,
            konar,
            krystilia,
            mazchna,
            turael
        }

        public static class MonsterDrop
        {
            public           int     id; //The ID number of the item drop
            public           String  name;//	The name of the item drop
            public           boolean members; //If the drop is a members-only item
            public @Nullable String  quantity; //The quantity of the item drop (integer, comma-separated or range).
            public           boolean noted; //If the item drop is noted, or not.
            public           float   rarity; //The rarity of the item drop (as a float out of 1.0).
            public           int     rolls; //Number of rolls from the drop.

            public String toString()
            {
                return _toString(this, this.getClass());
            }
        }
    }
}

