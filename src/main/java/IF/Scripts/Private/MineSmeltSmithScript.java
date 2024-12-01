package IF.Scripts.Private;

import IF.Logic.Cycles.Skilling.MineCycle;
import IF.Logic.Cycles.Skilling.SmeltCycle;
import IF.Logic.Cycles.Skilling.SmithCycle;
import IF.Utilities.Requirement.ItemRequirement;
import IF.Utilities.Requirement.LevelRequirement;
import IF.Utilities.Scripting.ICycle;
import IF.Utilities.Scripting.IFScript;
import io.vavr.Tuple2;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Sleep;

@ScriptManifest(name = "SoloScripts.MineSmeltSmithScript", description = "Mine, Smelt and Smith stuff", author = "Semanresu", version = 1.0, category = Category.MINING, image = "")
public class MineSmeltSmithScript extends IFScript
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
    final  int       MithrilOre      = 447;
    final  int       CoalOre         = 453;
    SmeltCycle SmeltBronze    = new SmeltCycle("Smelt Bronze",
                                               "Bronze bar",
                                               new Tuple2<>(436, 1),
                                               new Tuple2<>(438, 1));
    SmeltCycle SmeltIron      = new SmeltCycle("Smelt Iron", "Iron bar", new Tuple2<>(440, 1));
    SmeltCycle SmeltSteel     = new SmeltCycle("Smelt Steel",
                                               "Steel bar",
                                               new Tuple2<>(440, 1),
                                               new Tuple2<>(453, 2));
    SmeltCycle SmeltMithril   = new SmeltCycle("Smelt Mithril",
                                               "Mithril bar",
                                               new Tuple2<>(447, 1),
                                               new Tuple2<>(453, 4));
    SmeltCycle SmeltAdamanite = new SmeltCycle("Smelt Adamantite",
                                               "Adamantite bar",
                                               new Tuple2<>(449, 1),
                                               new Tuple2<>(453, 6));

    SmithCycle SmithMithrilSword       = new SmithCycle("Smith Mithril Stuff",
                                                        "Mithril sword",
                                                        MithrilBar);
    SmithCycle SmithAdamaniteHelm      = new SmithCycle("Smith Adamantite Stuff",
                                                        "Adamant med helm",
                                                        AdamaniteBar);
    SmithCycle SmithAdamanitePlateBody = new SmithCycle("Smith Adamantite Stuff",
                                                        "Adamant platebody",
                                                        AdamaniteBar);

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
        SmithAdamaniteHelm.AddRequirement(new LevelRequirement(Skill.SMITHING, 73, 88));
        SmithAdamaniteHelm.AddGoal(new ItemRequirement(true, new Tuple2<>(AdamaniteBar, 0)),
                                   new LevelRequirement(Skill.SMITHING, 88));
        AddCycle(SmithAdamaniteHelm);

        SmithAdamanitePlateBody.SetCycleType(ICycle.CycleType.byGoal);
        SmithAdamanitePlateBody.AddRequirement(new LevelRequirement(Skill.SMITHING, 88));
        SmithAdamanitePlateBody.AddGoal(new ItemRequirement(true, new Tuple2<>(AdamaniteBar, -5)));
        AddCycle(SmithAdamanitePlateBody);

        SmithMithrilSword.SetCycleType(ICycle.CycleType.byGoal);
        SmithMithrilSword.AddRequirement(new LevelRequirement(Skill.SMITHING, 54, 74));
        SmithMithrilSword.AddGoal(new LevelRequirement(Skill.SMITHING, 74));
        AddCycle(SmithMithrilSword);
        //
        SmeltMithril.SetCycleType(ICycle.CycleType.byGoal);
        SmeltMithril.AddRequirement(new LevelRequirement(Skill.SMITHING, 58));
        SmeltMithril.AddGoal(new ItemRequirement(true,
                                                 new Tuple2<>(CoalOre, -4),
                                                 new Tuple2<>(MithrilOre, 0)));
        AddCycle(SmeltMithril);

        SmeltAdamanite.SetCycleType(ICycle.CycleType.byGoal);
        SmeltAdamanite.AddRequirement(new LevelRequirement(Skill.SMITHING, 70));
        SmeltAdamanite.onCycleEnd.Subscribe(SmeltAdamanite,
                                            () -> Sleep.sleepUntil(Bank::depositAllItems, 30000));
        AddCycle(SmeltAdamanite);

        CoalCycle.PreferredBank = BankLocation.FALADOR_EAST;
        CoalCycle.SetCycleType(ICycle.CycleType.byGoal);
        AddCycle(CoalCycle);

        MithrilCycle.PreferredBank = BankLocation.FALADOR_EAST;
        MithrilCycle.SetCycleType(ICycle.CycleType.byGoal);
        AddCycle(MithrilCycle);

        AdamantiteCycle.PreferredBank = BankLocation.FALADOR_EAST;
        AdamantiteCycle.SetCycleType(ICycle.CycleType.byGoal);
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
