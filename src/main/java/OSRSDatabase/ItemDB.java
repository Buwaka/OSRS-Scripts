package OSRSDatabase;

import Utilities.OSRSUtilities;
import Utilities.Requirement.CombatRequirement;
import Utilities.Requirement.IRequirement;
import Utilities.Requirement.LevelRequirement;
import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import io.vavr.Tuple2;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.dreambot.api.methods.combat.CombatStyle;
import org.dreambot.api.methods.grandexchange.LivePrices;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.utilities.Logger;

import javax.annotation.Nullable;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class ItemDB extends OSRSDataBase
{

    final private static String                               ItemDB      = "Items/items-complete.json";
    final private static ReentrantLock                        ItemDBLock  = new ReentrantLock();
    final private static ConcurrentHashMap<Integer, ItemData> ItemDBCache = new ConcurrentHashMap<>();


    public enum Skill
    {
        @SerializedName("attack") ATTACK,
        @SerializedName("defence") DEFENCE,
        @SerializedName("strength") STRENGTH,
        @SerializedName("combat") COMBAT,
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
        @SerializedName("construction") CONSTRUCTION,
        @SerializedName("processing") PROCESSING,
        @SerializedName("misc") MISC,
        ;

        public org.dreambot.api.methods.skills.Skill GetDreamBotSkill()
        {
            if(this == COMBAT || this == PROCESSING || this == MISC)
            {
                return null;
            }

            return org.dreambot.api.methods.skills.Skill.valueOf(this.name().toUpperCase());
        }
    }

    public static class Requirement
    {
        public List<Tuple2<Skill, Integer>> SkillLevelPair = new ArrayList<>();

        public static class RequirementDeserializer implements JsonDeserializer<Requirement>
        {
            public Requirement deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws
                    JsonParseException
            {
                Requirement out = new Requirement();
                for(var skill : json.getAsJsonObject().entrySet())
                {
                    Skill skell;
                    if(skill.getKey().toUpperCase().equals("RUNECRAFT"))
                    {
                        skell = Skill.RUNECRAFTING;
                    }
                    else
                    {
                        skell = Skill.valueOf(skill.getKey().toUpperCase());
                    }
                    out.SkillLevelPair.add(new Tuple2<Skill, Integer>(skell,
                                                                      skill.getValue().getAsInt()));
                }
                return out;
            }
        }

        public IRequirement[] GetLevelRequirements()
        {
            List<IRequirement> out = new ArrayList<>();
            for(var pair : SkillLevelPair)
            {
                if(pair._1 == null)
                {
                    continue;
                }
                if(pair._1 == Skill.COMBAT)
                {
                    out.add(new CombatRequirement(pair._2));
                }
                else
                {
                    out.add(new LevelRequirement(pair._1, pair._2));
                }

            }
            return out.toArray(new IRequirement[0]);
        }

        public boolean isMet()
        {
            for(var skill : SkillLevelPair)
            {
                if(Skills.getRealLevel(skill._1.GetDreamBotSkill()) < skill._2)
                {
                    return false;
                }
            }
            return true;
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


        public double GetDPS(StanceData.Attacktype type)
        {
            if(weapon == null || equipment == null)
            {
                return 0;
            }

            int TickInterval = weapon.attack_speed;
            var possibleStances = Arrays.stream(weapon.stances)
                                        .filter((t) -> t.attack_type == type)
                                        .toList();
            double max = 0;
            for(var stance : possibleStances)
            {
                double speed = TickInterval;
                if(stance.boosts != null &&
                   stance.boosts.equalsIgnoreCase("attack speed by 1 tick"))
                {
                    speed++;
                }

                double attack = 0;
                switch(type)
                {
                    case stab ->
                    {
                        attack = equipment.attack_stab;
                    }
                    case slash ->
                    {
                        attack = equipment.attack_slash;
                    }
                    case crush ->
                    {
                        attack = equipment.attack_crush;
                    }
                    case magic, defensive_casting, spellcasting ->
                    {
                        attack = equipment.magic_damage;
                    }
                    case ranged ->
                    {
                        attack = equipment.ranged_strength;
                    }
                }

                max = Math.max(max, attack / speed);
            }

            return max;
        }

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
        public @Nullable Requirement   requirements;

        public enum EquipmentSlot
        {
            head,
            cape,
            neck,
            weapon,
            body,
            shield,
            legs,
            hands,
            feet,
            ring,
            ammo,
            @SerializedName("2h") two_handed;

            public org.dreambot.api.methods.container.impl.equipment.EquipmentSlot GetDreamBotEquipmentSlot()
            {
                switch(this)
                {
                    case two_handed ->
                    {
                        return org.dreambot.api.methods.container.impl.equipment.EquipmentSlot.WEAPON;
                    }
                    case ammo ->
                    {
                        return org.dreambot.api.methods.container.impl.equipment.EquipmentSlot.ARROWS;
                    }
                    case neck ->
                    {
                        return org.dreambot.api.methods.container.impl.equipment.EquipmentSlot.AMULET;
                    }
                    case body ->
                    {
                        return org.dreambot.api.methods.container.impl.equipment.EquipmentSlot.CHEST;
                    }
                    case head ->
                    {
                        return org.dreambot.api.methods.container.impl.equipment.EquipmentSlot.HAT;
                    }
                }
                return org.dreambot.api.methods.container.impl.equipment.EquipmentSlot.valueOf(this.name()
                                                                                                   .toUpperCase());
            }

            public static EquipmentSlot FromDreamBotEquipSlot(org.dreambot.api.methods.container.impl.equipment.EquipmentSlot slot)
            {
                switch(slot)
                {
                    case org.dreambot.api.methods.container.impl.equipment.EquipmentSlot.HAT ->
                    {
                        return head;
                    }
                    case org.dreambot.api.methods.container.impl.equipment.EquipmentSlot.ARROWS ->
                    {
                        return ammo;
                    }
                    case org.dreambot.api.methods.container.impl.equipment.EquipmentSlot.AMULET ->
                    {
                        return neck;
                    }
                    case org.dreambot.api.methods.container.impl.equipment.EquipmentSlot.CHEST ->
                    {
                        return body;
                    }
                }
                return EquipmentSlot.valueOf(slot.name().toLowerCase());
            }
        }

        public int GetMaxDef()
        {
            return Math.max(Math.max(Math.max(Math.max(defence_crush, defence_stab), defence_slash),
                                     defence_magic), defence_ranged);
        }

        public int GetMaxMelee()
        {
            return Math.max(Math.max(attack_crush, attack_stab), attack_slash);
        }

        public int GetTotal()
        {
            return attack_stab + attack_slash + attack_crush + attack_magic + attack_ranged +
                   defence_stab + defence_slash + defence_crush + defence_magic + defence_ranged +
                   melee_strength + ranged_strength + magic_damage + prayer;
        }

        public String toString()
        {
            return _toString(this, this.getClass());
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

            public CombatStyle GetDBCStyle()
            {
                switch(this)
                {
                    case magic -> {return CombatStyle.MAGIC;}
                    case shared -> {return CombatStyle.SHARED;}
                    case magic_and_defence -> {return CombatStyle.MAGIC_DEFENCE;}
                    case strength -> {return CombatStyle.STRENGTH;}
                    case defence -> {return CombatStyle.DEFENCE;}
                    case ranged -> {return CombatStyle.RANGED_RAPID;}
                    case attack -> {return CombatStyle.ATTACK;}
                    case ranged_and_defence -> {return CombatStyle.RANGED_DEFENCE;}
                }
                return CombatStyle.ATTACK;
            }
        }

        public String toString()
        {
            return _toString(this, this.getClass());
        }
    }

    public static ItemData[] GetAllItemKeywordMatch(String keyword, boolean IgnoreNoteAndPlaceHolder)
    {
        List<ItemData> out = new ArrayList<>();
        try
        {
            ItemDBLock.lock();
            InputStreamReader File   = new InputStreamReader(GetInputStream(ItemDB));
            JsonReader        Reader = new JsonReader(File);
            Gson              gson   = OSRSUtilities.OSRSGsonBuilder.create();
            Reader.setLenient(true);

            Reader.beginObject();

            while(Reader.hasNext())
            {
                int      ID  = Integer.parseInt(Reader.nextName());
                ItemData Obj = gson.fromJson(Reader, ItemData.class);

                if(IgnoreNoteAndPlaceHolder && Obj.linked_id_item != null)
                {
                    continue;
                }

                if((Obj.name).toLowerCase().contains(keyword.toLowerCase()))
                {
                    out.add(Obj);
                }
            }

            Reader.endObject();
            Reader.close();

        } catch(Exception e)
        {
            Logger.log("Tried to find keyword: " + keyword);
            if(ItemDBLock.isLocked() && ItemDBLock.isHeldByCurrentThread())
            {
                ItemDBLock.unlock();
            }
            return new ItemData[0];
        }

        ItemDBLock.unlock();
        return out.toArray(new ItemData[0]);
    }

    public static ItemData GetClosestMatch(String keyword, boolean IgnoreNoteAndPlaceHolder)
    {
        return GetClosestMatch(keyword, 10, IgnoreNoteAndPlaceHolder);
    }

    public static ItemData GetClosestMatch(String keyword, int threshold, boolean IgnoreNoteAndPlaceHolder)
    {
        SortedMap<Integer, ItemData> out = new TreeMap<>();
        try
        {
            ItemDBLock.lock();
            InputStreamReader File   = new InputStreamReader(GetInputStream(ItemDB));
            JsonReader        Reader = new JsonReader(File);
            Gson              gson   = OSRSUtilities.OSRSGsonBuilder.create();
            Reader.setLenient(true);

            Reader.beginObject();

            LevenshteinDistance calc = new LevenshteinDistance(threshold);

            while(Reader.hasNext())
            {
                int      ID       = Integer.parseInt(Reader.nextName());
                ItemData Obj      = gson.fromJson(Reader, ItemData.class);
                int      distance = calc.apply(Obj.name.toLowerCase(), keyword.toLowerCase());

                if(IgnoreNoteAndPlaceHolder && Obj.linked_id_item != null)
                {
                    continue;
                }

                if(distance != -1)
                {
                    out.put(distance, Obj);
                }
            }

            Reader.endObject();
            Reader.close();

        } catch(Exception e)
        {
            Logger.log("Tried to find keyword: " + keyword);
            if(ItemDBLock.isLocked() && ItemDBLock.isHeldByCurrentThread())
            {
                ItemDBLock.unlock();
            }
            throw new RuntimeException(e);
        }

        ItemDBLock.unlock();
        return out.isEmpty() ? null : out.firstEntry().getValue();
    }

    public static int GetProfitAlch(int ID)
    {
        var item = GetItemData(ID);
        if(item != null)
        {
            return _GetProfitAlch(item);
        }
        Logger.log("ItemDB: GetProfitAlch: Item with ID " + ID + " not found");
        return Integer.MIN_VALUE;
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
            Gson              gson   = OSRSUtilities.OSRSGsonBuilder.create();
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

    private static int _GetProfitAlch(ItemData data)
    {
        if(data.highalch == null)
        {
            Logger.log("ItemDB: _GetProfitAlch: item with name " + data.name + " is not alchable");
            return Integer.MIN_VALUE;
        }

        final int NatureRuneID = 561;
        int       RunePrice    = LivePrices.get(NatureRuneID);
        int       AlchPrice    = data.highalch;
        return (AlchPrice - RunePrice) - data.cost;
    }

    public static int GetProfitAlch(String Name)
    {
        var item = GetItemDataByName(Name);
        if(item.length > 0)
        {
            return _GetProfitAlch(item[0]);
        }
        Logger.log("ItemDB: GetProfitAlch: Item with ID " + Name + " not found");
        return Integer.MIN_VALUE;
    }

    public static ItemData[] GetItemDataByName(String Name)
    {
        List<ItemData> out = new ArrayList<>();
        try
        {
            ItemDBLock.lock();
            InputStreamReader File   = new InputStreamReader(GetInputStream(ItemDB));
            JsonReader        Reader = new JsonReader(File);
            Gson              gson   = OSRSUtilities.OSRSGsonBuilder.create();
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

    public static int GetProfitGE(int ID)
    {
        var item = GetItemData(ID);
        if(item != null)
        {
            return _GetProfitGE(item);
        }
        Logger.log("ItemDB: GetProfitGE: Item with ID " + ID + " not found");
        return Integer.MIN_VALUE;
    }

    private static int _GetProfitGE(ItemData data)
    {
        if(!data.tradeable_on_ge)
        {
            Logger.log("ItemDB: _GetProfitGE: item with name " + data.name +
                       " is not tradable on the GE");
            return Integer.MIN_VALUE;
        }

        int GEPrice = LivePrices.get(data.id);
        return GEPrice - data.cost;
    }

    public static int GetProfitGE(String Name)
    {
        var item = GetItemDataByName(Name);
        if(item.length > 0)
        {
            return _GetProfitGE(item[0]);
        }
        Logger.log("ItemDB: GetProfitGE: Item with ID " + Name + " not found");
        return Integer.MIN_VALUE;
    }

    public static boolean isAlchable(String Name)
    {
        var item = GetItemDataByName(Name);
        if(item.length > 0)
        {
            return item[0].highalch != null;
        }
        Logger.log("ItemDB: IsAlchable: Item with name: " + Name + " not found, returning false");
        return false;
    }

    public static boolean isAlchable(int ID)
    {
        var item = GetItemData(ID);
        if(item != null)
        {
            return item.highalch != null;
        }
        Logger.log("IsAlchable: Item with ID: " + ID + " not found, returning false");
        return false;
    }

    public static boolean isProfitableItem(String Name)
    {
        var item = GetItemDataByName(Name);
        if(item.length > 0)
        {
            return _isProfitableItem(item[0]);
        }
        Logger.log("ItemDB: isProfitable: Item with Name " + Name + " not found");
        return false;
    }

    private static boolean _isProfitableItem(ItemData data)
    {
        if(data != null)
        {
            boolean AlchCheck  = data.highalch != null;
            boolean QuestCheck = !data.quest_item;
            boolean TradeAble  = data.tradeable_on_ge;
            return AlchCheck && QuestCheck && TradeAble;
        }
        Logger.log("ItemDB: _isProfitable: Item dats is null");
        return false;
    }

    public static boolean isProfitableItem(int ID)
    {
        var item = GetItemData(ID);
        if(item != null)
        {
            return _isProfitableItem(item);
        }
        Logger.log("ItemDB: isProfitable: Item with ID " + ID + " not found");
        return false;
    }


}
