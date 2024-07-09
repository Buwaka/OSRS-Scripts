package SoloScripts;

import Cycles.MineCycle;
import Cycles.SmeltCycle;
import Cycles.SmithCycle;
import Utilities.OSRSUtilities;
import Utilities.Scripting.ICycle;
import Utilities.Scripting.tpircSScript;
import com.google.gson.Gson;
import io.vavr.Tuple2;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;

@ScriptManifest(name = "SoloScripts.MineSmeltSmithScript", description = "Mine, Smelt and Smith stuff", author = "Semanresu", version = 1.0, category = Category.MINING, image = "")
public class MineSmeltSmithScript extends tpircSScript
{
    static Area[]    MineArea1       = {new Area(3281, 3370, 3289, 3361)};
    static Area[]    MineArea2       = {
            new Area(3298, 3315, 3301, 3298), new Area(3302, 3279, 3295, 3282)};
    static Area[]    MineArea3       = {new Area(3080, 3425, 3087, 3422)};
    static Area[]    MineArea4       = {new Area(3045, 9733, 3048, 9738)};
    static String    TinName         = "Tin Rocks";
    static String    CopperName      = "Copper Rocks";
    static String    IronName        = "Iron Rocks";
    static String    CoalName        = "Coal Rocks";
    static String    MithrilName     = "Mithril Rocks";
    static String    AdamantiteName  = "Adamantite rocks";
    static int       MithrilBar      = 2359;
    static int       AdamaniteBar    = 2361;
    static MineCycle TinCycle        = new MineCycle("Tin ore", MineArea1, TinName);
    static MineCycle CopperCycle     = new MineCycle("Copper ore", MineArea1, CopperName);
    static MineCycle IronCycle       = new MineCycle("Iron ore", MineArea2, IronName);
    static MineCycle CoalCycle       = new MineCycle("Coal", MineArea4, CoalName);
    static MineCycle MithrilCycle    = new MineCycle("Mithril", MineArea4, MithrilName);
    static MineCycle AdamantiteCycle = new MineCycle("Adamantite", MineArea4, AdamantiteName);

    SmeltCycle SmeltBronze  = new SmeltCycle("Smelt Bronze", "Bronze bar", new Tuple2<>(436, 1), new Tuple2<>(438, 1));
    SmeltCycle SmeltIron    = new SmeltCycle("Smelt Iron", "Iron bar", new Tuple2<>(440, 1));
    SmeltCycle SmeltSteel   = new SmeltCycle("Smelt Steel", "Steel bar", new Tuple2<>(440, 1), new Tuple2<>(453, 2));
    SmeltCycle SmeltMithril = new SmeltCycle("Smelt Mithril",
                                             "Mithril bar",
                                             new Tuple2<>(447, 1),
                                             new Tuple2<>(453, 4));
    SmeltCycle SmeltAdamanite = new SmeltCycle("Smelt Adamantite",
                                             "Adamantite bar",
                                             new Tuple2<>(449, 1),
                                             new Tuple2<>(453, 6));

    SmithCycle SmithMithrilSword       = new SmithCycle("Smith Mithril Stuff", "Mithril sword", MithrilBar);
    SmithCycle SmithAdamaniteHelm      = new SmithCycle("Smith Adamantite Stuff", "Adamant med helm", AdamaniteBar);
    SmithCycle SmithAdamanitePlateBody = new SmithCycle("Smith Adamantite Stuff", "Adamant platebody", AdamaniteBar);


    public static void main(String[] args)
    {
        Gson   gson = OSRSUtilities.OSRSGsonBuilder.create();
        String json = gson.toJson(CoalCycle) + ",";
        json += gson.toJson(IronCycle) + ",";
        json += gson.toJson(CopperCycle) + ",";
        json += gson.toJson(TinCycle);

        System.out.println(json);
    }

    @Override
    public void onStart()
    {
//        CopperCycle.SetCycleType(ICycle.CycleType.byCount);
//        CopperCycle.GetCycleType().Count = 2;
//        AddCycle(CopperCycle);
//        TinCycle.SetCycleType(ICycle.CycleType.byCount);
//        TinCycle.GetCycleType().Count = 20;
//        AddCycle(TinCycle);
//        IronCycle.SetCycleType(ICycle.CycleType.Endless);
//        AddCycle(IronCycle);

        SmithAdamaniteHelm.SetCycleType(ICycle.CycleType.byGoal);
        SmithAdamaniteHelm.Requirement = () -> Skills.getRealLevel(Skill.SMITHING) >= 73 && Skills.getRealLevel(Skill.SMITHING) < 88;
        SmithAdamaniteHelm.Goal        = () -> Bank.count(AdamaniteBar) == 0 || Skills.getRealLevel(Skill.SMITHING) >= 88;
        AddCycle(SmithAdamaniteHelm);

        SmithAdamanitePlateBody.SetCycleType(ICycle.CycleType.byGoal);
        SmithAdamanitePlateBody.Requirement = () -> Skills.getRealLevel(Skill.SMITHING) >= 88;
        SmithAdamanitePlateBody.Goal        = () -> Bank.count(AdamaniteBar) < 5;
        AddCycle(SmithAdamanitePlateBody);

        SmithMithrilSword.SetCycleType(ICycle.CycleType.byGoal);
        SmithMithrilSword.Requirement = () -> Skills.getRealLevel(Skill.SMITHING) >= 54 && Skills.getRealLevel(Skill.SMITHING) < 74;
        SmithMithrilSword.Goal        = () -> Bank.count(MithrilBar) == 0 || Skills.getRealLevel(Skill.SMITHING) >= 73;
        AddCycle(SmithMithrilSword);
//
        SmeltMithril.SetCycleType(ICycle.CycleType.byGoal);
        SmeltMithril.Requirement = () -> Skills.getRealLevel(Skill.SMITHING) >= 50;
        SmeltMithril.Goal = () -> {
            Logger.log("SmeltMithril count: " + Bank.count(453) + " " + Bank.count(447));
            return Bank.isCached() && (Bank.count(447) == 0 || Bank.count(453) < 4);
        };
        SmeltMithril.onCycleEnd.Subscribe(SmeltMithril, () -> Sleep.sleepUntil(Bank::depositAllItems, 30000));
        AddCycle(SmeltMithril);

        SmeltAdamanite.SetCycleType(ICycle.CycleType.byGoal);
        SmeltAdamanite.Requirement = () -> Skills.getRealLevel(Skill.SMITHING) >= 70;
        SmeltAdamanite.Goal = () -> {
            Logger.log("SmeltAdamanite count: " + Bank.count(453) + " " + Bank.count(449));
            return Bank.isCached() && (Bank.count(449) == 0 || Bank.count(453) < 6);
        };
        SmeltAdamanite.onCycleEnd.Subscribe(SmeltAdamanite, () -> Sleep.sleepUntil(Bank::depositAllItems, 30000));
        AddCycle(SmeltAdamanite);

        CoalCycle.PreferredBank = BankLocation.FALADOR_EAST;
        CoalCycle.SetCycleType(ICycle.CycleType.byGoal);
        CoalCycle.Goal = () -> {
            Logger.log("CoalCycle count: " + Bank.count(453));
            return Bank.isCached() && Bank.count(453) > 600;
        };
        AddCycle(CoalCycle);

        MithrilCycle.PreferredBank = BankLocation.FALADOR_EAST;
        MithrilCycle.SetCycleType(ICycle.CycleType.byGoal);
        MithrilCycle.Goal = () -> {
            Logger.log("MithrilCycle count: " + Bank.count(447));
            return Bank.isCached() && Bank.count(447) > 100;
        };
        AddCycle(MithrilCycle);

        AdamantiteCycle.PreferredBank = BankLocation.FALADOR_EAST;
        AdamantiteCycle.SetCycleType(ICycle.CycleType.byGoal);
        AdamantiteCycle.Goal = () -> {
            Logger.log("AdamantiteCycle count: " + Bank.count(449));
            return Bank.isCached() && Bank.count(449) > 100;
        };
        AddCycle(AdamantiteCycle);


//        Gson   gson = OSRSUtilities.OSRSGsonBuilder.create();
//        String json = gson.toJson(CoalCycle);
//        json += gson.toJson(IronCycle);
//        json += gson.toJson(CopperCycle);
//        json += gson.toJson(TinCycle);
//
//        Logger.log(json);

        super.onStart();
    }
}
