package OSRSDatabase;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import org.dreambot.api.utilities.Logger;

import javax.annotation.Nullable;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class MonsterDB extends OSRSDataBase
{

    final private static String                                  MonsterDB             = "monsters/monsters-complete.json";
    final private static ReentrantLock                           MonsterDBLock         = new ReentrantLock();
    final private static ConcurrentHashMap<String, int[]>        MonsterIDsByNameCache = new ConcurrentHashMap<>();
    final private static ConcurrentHashMap<Integer, MonsterData> MonsterDBCache        = new ConcurrentHashMap<>();

    public static class MonsterData
    {

        public           int                            id;
        public           String                         name;
        public @Nullable String                         last_updated;
        public           boolean                        incomplete;
        public           boolean                        members;
        public @Nullable String                         release_date;
        public           int                            combat_level;
        public           int                            size;
        public @Nullable int                            hitpoints;
        public @Nullable int                            max_hit;
        public           ItemDB.StanceData.Attacktype[] attack_type;
        public @Nullable int                            attack_speed;
        public           boolean                        aggressive;
        public           boolean                        poisonous;
        public           boolean                        venomous;
        public           boolean                        immune_poison;
        public           boolean                        immune_venom;
        public           MonsterAttribute[]             attributes;
        public           MonsterCategory[]              category; // make enum
        public           boolean                        slayer_monster;
        public @Nullable Integer                        slayer_level;
        public @Nullable Float                          slayer_xp;
        public           SlayerMaster[]                 slayer_masters; // make enum
        public           boolean                        duplicate;
        public           String                         examine;
        public           String                         wiki_name;
        public           String                         wiki_url;
        public           int                            attack_level;
        public           int                            strength_level;
        public           int                            defence_level;
        public           int                            magic_level;
        public           int                            ranged_level;
        public           int                            attack_bonus;
        public           int                            strength_bonus;
        public           int                            attack_magic;
        public           int                            magic_bonus;
        public           int                            attack_ranged;
        public           int                            ranged_bonus;
        public           int                            defence_stab;
        public           int                            defence_slash;
        public           int                            defence_crush;
        public           int                            defence_magic;
        public           int                            defence_ranged;
        public           MonsterDrop[]                  drops;

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

        public String toString()
        {
            return _toString(this, this.getClass());
        }
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
}
