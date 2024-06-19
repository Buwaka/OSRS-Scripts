package OSRSDatabase;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import org.dreambot.api.utilities.Logger;

import javax.annotation.Nullable;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class ItemDB extends OSRSDataBase
{

    final private static String                               ItemDB      = "items-complete.json";
    final private static ReentrantLock                        ItemDBLock  = new ReentrantLock();
    final private static ConcurrentHashMap<Integer, ItemData> ItemDBCache = new ConcurrentHashMap<>();

    public enum Skill
    {
        @SerializedName("attack") ATTACK,
        @SerializedName("defence") DEFENCE,
        @SerializedName("strength") STRENGTH,
        @SerializedName("hitpoints") HITPOINTS,
        @SerializedName("ranged") RANGED,
        @SerializedName("prayer") PRAYER,
        @SerializedName("magic") MAGIC,
        @SerializedName("cooking") COOKING,
        @SerializedName("woodcutting") WOODCUTTING,
        @SerializedName("fletching") FLETCHING,
        @SerializedName("fishing") FISHING,
        @SerializedName("firemaking") FIREMAKING,
        @SerializedName("crafting") CRAFTING,
        @SerializedName("smithing") SMITHING,
        @SerializedName("mining") MINING,
        @SerializedName("herblore") HERBLORE,
        @SerializedName("agility") AGILITY,
        @SerializedName("thieving") THIEVING,
        @SerializedName("slayer") SLAYER,
        @SerializedName("farming") FARMING,
        @SerializedName("runecrafting") RUNECRAFTING,
        @SerializedName("hunter") HUNTER,
        @SerializedName("construction") CONSTRUCTION;
    }

    public static class Requirement
    {
        public Skill skill;
        public int   level;

        private static class RequirementDeserializer implements JsonDeserializer<Requirement>
        {
            public Requirement deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws
                                                                                                               JsonParseException
            {
                Requirement out = new Requirement();
                for(var skill : json.getAsJsonObject().entrySet())
                {
                    out.skill = Skill.valueOf(skill.getKey());
                    out.level = skill.getValue().getAsInt();
                }
                return out;
            }
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
        public @Nullable Integer       stacked;
        public           boolean       noted;
        public           boolean       noteable;
        public @Nullable Integer       linked_id_item;
        public @Nullable Integer       linked_id_noted;
        public @Nullable Integer       linked_id_placeholder;
        public           boolean       placeholder;
        public           boolean       equipable;
        public           boolean       equipable_by_player;
        public           boolean       equipable_weapon;
        public           int           cost;
        public @Nullable Integer       highalch;
        public @Nullable Integer       lowalch;
        public @Nullable Float         weight;
        public @Nullable Integer       buy_limit;
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
        public @Nullable Requirement[] requirements;

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

        public String toString()
        {
            return _toString(this, this.getClass());
        }
    }

    public static class ToolData
    {
        public           Type          type;
        public           String        name;
        public           boolean       equipable;
        public @Nullable Integer       strength;
        public @Nullable Requirement[] requirements;

        public enum Type
        {
            Axe,
            Chisel,
            Hammer,
            Knife,
            Machete,
            @SerializedName("Pestle and mortar") Pestle_and_mortar,
            Pickaxe,
            Saw,
            Shears,
            Tinderbox,
            @SerializedName("Tool space") Tool_space,
            @SerializedName("Tool store") Tool_store,
            @SerializedName("Ammo mould") Ammo_mould,
            @SerializedName("Amulet mould") Amulet_mould,
            @SerializedName("Bolt mould") Bolt_mould,
            @SerializedName("Bracelet mould") Bracelet_mould,
            @SerializedName("Glassblowing pipe") Glassblowing_pipe,
            @SerializedName("Holy mould") Holy_mould,
            @SerializedName("Necklace mould") Necklace_mould,
            Needle,
            @SerializedName("Ring mould") Ring_mould,
            @SerializedName("Sickle mould") Sickle_mould,
            @SerializedName("Tiara mould") Tiara_mould,
            @SerializedName("Unholy mould") Unholy_mould,
            @SerializedName("Gardening trowel") Gardening_trowel,
            Rake,
            Secateurs,
            @SerializedName("Seed dibber") Seed_dibber,
            Spade,
            Trowel,
            @SerializedName("Watering can") Watering_can,
            @SerializedName("Barbarian rod") Barbarian_rod,
            @SerializedName("Big fishing net") Big_fishing_net,
            @SerializedName("Fishing rod") Fishing_rod,
            @SerializedName("Fly fishing rod") Fly_fishing_rod,
            Harpoon,
            @SerializedName("Karambwan vessel") Karambwan_vessel,
            @SerializedName("Lobster pot") Lobster_pot,
            @SerializedName("Oily fishing rod") Oily_fishing_rod,
            @SerializedName("Small fishing net") Small_fishing_net,
            @SerializedName("Bird snare") Bird_snare,
            @SerializedName("Box trap") Box_trap,
            @SerializedName("Butterfly net") Butterfly_net,
            @SerializedName("Magic box") Magic_box,
            @SerializedName("Noose wand") Noose_wand,
            @SerializedName("Rabbit snare") Rabbit_snare,
            @SerializedName("Teasing stick") Teasing_stick,
            Torch
        }
    }

    public static class WeaponData
    {
        public int          attack_speed;
        public WeaponType   weapon_type;
        //public JSONObject stances;
        public StanceData[] stances;

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

        public String toString()
        {
            return _toString(this, this.getClass());
        }
    }

    public static class StanceData
    {

        public           CombatStyles   combat_style;
        public @Nullable Attacktype     attack_type;
        public @Nullable AttackStyles   attack_style;
        public           ExperienceType experience;
        public @Nullable String         boosts; //attack range by 2 squares, accuracy and damage, attack speed by 1 tick

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

        public String toString()
        {
            return _toString(this, this.getClass());
        }
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

    public static boolean IsAlchable(String Name)
    {
        var item = GetItemDataByName(Name);
        if(item.length > 0)
        {
            return item[0].highalch != null;
        }
        Logger.log("IsAlchable: Item with name: " + Name + " not found, returning false");
        return false;
    }

    public static boolean IsAlchable(int ID)
    {
        var item = GetItemData(ID);
        if(item != null)
        {
            return item.highalch != null;
        }
        Logger.log("IsAlchable: Item with ID: " + ID + " not found, returning false");
        return false;
    }
}
