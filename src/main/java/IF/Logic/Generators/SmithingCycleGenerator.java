package IF.Logic.Generators;

import IF.Logic.Cycles.Skilling.SmeltCycle;
import IF.Utilities.ECycleTags;
import IF.Utilities.OSRSUtilities;
import IF.Utilities.Requirement.LevelRequirement;
import IF.Utilities.Requirement.QuestRequirement;
import io.vavr.Tuple2;
import org.dreambot.api.methods.quest.book.FreeQuest;
import org.dreambot.api.methods.quest.book.PaidQuest;
import org.dreambot.api.methods.skills.Skill;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class SmithingCycleGenerator extends CycleLibrary
{
    final String SmeltCycleDBPath = "SmeltCycles.json";

    public static String GenerateJSONSmeltCycles()
    {
        final int CopperOre     = 436;
        final int TinOre        = 438;
        final int BlueriteOre   = 668;
        final int IronOre       = 440;
        final int ElementalOre  = 2892;
        final int SilverOre     = 442;
        final int CoalOre       = 453;
        final int GoldOre       = 444;
        final int LovakiteOre   = 13356;
        final int MithrilOre    = 447;
        final int AdamantiteOre = 449;
        final int RuniteOre     = 451;

        List<SmeltCycle> Smelts = new ArrayList<>();


        SmeltCycle SmeltBronze = new SmeltCycle("Smelt Bronze",
                                                "Bronze bar",
                                                new Tuple2<>(CopperOre, 1),
                                                new Tuple2<>(TinOre, 1));
        SmeltBronze.AddTag(EnumSet.of(ECycleTags.Leveling, ECycleTags.Profitable));
        Smelts.add(SmeltBronze);

        SmeltCycle SmeltBluerite = new SmeltCycle("Smelt Blurite",
                                                  "Blurite bar",
                                                  new Tuple2<>(BlueriteOre, 1));
        SmeltBluerite.AddRequirement(new QuestRequirement(FreeQuest.THE_KNIGHTS_SWORD),
                                     new LevelRequirement(Skill.SMITHING, 13));
        SmeltBluerite.AddTag(EnumSet.of(ECycleTags.Member));
        Smelts.add(SmeltBluerite);

        SmeltCycle SmeltIron = new SmeltCycle("Smelt Iron", "Iron bar", new Tuple2<>(IronOre, 1));
        SmeltIron.NeedForgingRing = true;
        SmeltIron.AddRequirement(new LevelRequirement(Skill.SMITHING, 15));
        SmeltIron.AddTag(EnumSet.of(ECycleTags.Leveling, ECycleTags.Profitable));
        Smelts.add(SmeltIron);

        SmeltCycle SmeltElemental = new SmeltCycle("Smelt Elemental metal",
                                                   "Elemental metal",
                                                   new Tuple2<>(ElementalOre, 1));
        SmeltElemental.AddRequirement(new QuestRequirement(PaidQuest.ELEMENTAL_WORKSHOP_I),
                                      new LevelRequirement(Skill.SMITHING, 20));
        SmeltElemental.AddTag(EnumSet.of(ECycleTags.Member));
        Smelts.add(SmeltElemental);


        SmeltCycle SmeltSilver = new SmeltCycle("Smelt Silver",
                                                "Silver bar",
                                                new Tuple2<>(SilverOre, 1));
        SmeltSilver.AddRequirement(new LevelRequirement(Skill.SMITHING, 20));
        SmeltSilver.AddTag(EnumSet.of(ECycleTags.Leveling, ECycleTags.Profitable));
        Smelts.add(SmeltSilver);

        SmeltCycle SmeltSteel = new SmeltCycle("Smelt Steel",
                                               "Steel bar",
                                               new Tuple2<>(IronOre, 1),
                                               new Tuple2<>(CoalOre, 2));
        SmeltSteel.AddRequirement(new LevelRequirement(Skill.SMITHING, 30));
        SmeltSteel.AddTag(EnumSet.of(ECycleTags.Leveling, ECycleTags.Profitable));
        Smelts.add(SmeltSteel);

        SmeltCycle SmeltGold = new SmeltCycle("Smelt Gold", "Gold bar", new Tuple2<>(GoldOre, 1));
        SmeltGold.AddRequirement(new LevelRequirement(Skill.SMITHING, 40));
        SmeltGold.AddTag(EnumSet.of(ECycleTags.Leveling, ECycleTags.Profitable));
        Smelts.add(SmeltGold);

        SmeltCycle SmeltLovakite = new SmeltCycle("Smelt Lovakite",
                                                  "Lovakite bar",
                                                  new Tuple2<>(LovakiteOre, 1),
                                                  new Tuple2<>(CoalOre, 2));
        SmeltLovakite.AddRequirement(new LevelRequirement(Skill.SMITHING, 45));
        SmeltLovakite.AddTag(EnumSet.of(ECycleTags.Member));
        Smelts.add(SmeltLovakite);


        SmeltCycle SmeltMithril = new SmeltCycle("Smelt Mithril",
                                                 "Mithril bar",
                                                 new Tuple2<>(MithrilOre, 1),
                                                 new Tuple2<>(CoalOre, 4));
        SmeltMithril.AddRequirement(new LevelRequirement(Skill.SMITHING, 50));
        SmeltMithril.AddTag(EnumSet.of(ECycleTags.Leveling, ECycleTags.Profitable));
        Smelts.add(SmeltMithril);

        SmeltCycle SmeltAdamanite = new SmeltCycle("Smelt Adamantite",
                                                   "Adamantite bar",
                                                   new Tuple2<>(AdamantiteOre, 1),
                                                   new Tuple2<>(CoalOre, 6));
        SmeltAdamanite.AddRequirement(new LevelRequirement(Skill.SMITHING, 70));
        SmeltAdamanite.AddTag(EnumSet.of(ECycleTags.Leveling, ECycleTags.Profitable));
        Smelts.add(SmeltAdamanite);

        SmeltCycle SmeltRunite = new SmeltCycle("Smelt Runite",
                                                "Runite bar",
                                                new Tuple2<>(RuniteOre, 1),
                                                new Tuple2<>(CoalOre, 8));
        SmeltRunite.AddRequirement(new LevelRequirement(Skill.SMITHING, 85));
        SmeltRunite.AddTag(EnumSet.of(ECycleTags.Leveling, ECycleTags.Profitable));
        Smelts.add(SmeltRunite);

        return OSRSUtilities.OSRSGsonBuilder.create().toJson(Smelts);
    }

    //    public static String GenerateJSONSmithCycles()
    //    {
    //        SmithAdamaniteHelm.SetCycleType(ICycle.CycleType.byGoal);
    //        SmithAdamaniteHelm.AddRequirement(new LevelRequirement(Skill.SMITHING, 73, 88));
    //        SmithAdamaniteHelm.AddGoal(new ItemRequirement(true, new Tuple2<>(AdamaniteBar, 0)),
    //                                   new LevelRequirement(Skill.SMITHING, 88));
    //        AddCycle(SmithAdamaniteHelm);
    //
    //        SmithAdamanitePlateBody.SetCycleType(ICycle.CycleType.byGoal);
    //        SmithAdamanitePlateBody.AddRequirement(new LevelRequirement(Skill.SMITHING, 88));
    //        SmithAdamanitePlateBody.AddGoal(new ItemRequirement(true, new Tuple2<>(AdamaniteBar, -5)));
    //        AddCycle(SmithAdamanitePlateBody);
    //
    //        SmithMithrilSword.SetCycleType(ICycle.CycleType.byGoal);
    //        SmithMithrilSword.AddRequirement(new LevelRequirement(Skill.SMITHING, 54, 74));
    //        SmithMithrilSword.AddGoal(new LevelRequirement(Skill.SMITHING, 74));
    //        AddCycle(SmithMithrilSword);
    ////
    //        SmeltMithril.SetCycleType(ICycle.CycleType.byGoal);
    //        SmeltMithril.AddRequirement(new LevelRequirement(Skill.SMITHING, 58));
    //        SmeltMithril.AddGoal(new ItemRequirement(true, new Tuple2<>(CoalOre, -4), new Tuple2<>(MithrilOre, 0)));
    //        AddCycle(SmeltMithril);
    //    }
}
