package IF.OSRSDatabase;

import IF.Utilities.Requirement.IRequirement;
import IF.Utilities.Serializers.RequirementSerializer;
import IF.Utilities.Serializers.SerializableSupplier;
import IF.Utilities.Serializers.SerializableSupplierSerializer;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class ToolDB
{
    public static class ToolData implements Serializable
    {
        public           int            id;
        public           Type           type;
        public           String         name;
        public           boolean        equipable;
        public           boolean        members;
        public           boolean        ge_tradable;
        public @Nullable IRequirement[] requirements;
        public @Nullable DBTags[]       tags;

        public enum Type implements Serializable
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

    public static void main(String[] args)
    {
        List<ToolData> tools = new ArrayList<>();
        var            vals  = ToolData.Type.values();

        for(var val : vals)
        {
            for(var match : ItemDB.GetAllItemKeywordMatch(val.name().replace("_", " "), true))
            {
                ToolData New = new ToolData();
                New.name        = match.name;
                New.id          = match.id;
                New.equipable   = match.equipable;
                New.type        = val;
                New.members     = match.members;
                New.ge_tradable = match.tradeable_on_ge;
                if(match.equipment != null && match.equipment.requirements != null)
                {
                    New.requirements = match.equipment.requirements.GetLevelRequirements();
                }

                tools.add(New);
            }
        }

        var OSRSGsonBuilder = new GsonBuilder().setPrettyPrinting()
                                               .setLenient()
                                               .disableHtmlEscaping()
                                               .excludeFieldsWithModifiers(Modifier.STATIC,
                                                                           Modifier.TRANSIENT)
                                               .registerTypeAdapter(IRequirement.class,
                                                                    new RequirementSerializer())
                                               .registerTypeAdapter(ItemDB.Requirement.class,
                                                                    new ItemDB.Requirement.RequirementDeserializer())
                                               .registerTypeAdapter(SerializableSupplier.class,
                                                                    new SerializableSupplierSerializer<>())
                                               .create();

        String json = OSRSGsonBuilder.toJson(tools.toArray(), ToolData[].class);

        System.out.print(json);

    }
}
