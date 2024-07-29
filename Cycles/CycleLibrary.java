package Cycles;

import Cycles.SimpleTasks.Bank.BankItemsTask;
import Cycles.SimpleTasks.ItemProcessing.InteractTask;
import OSRSDatabase.NPCDB;
import OSRSDatabase.ObjectDB;
import OSRSDatabase.WoodDB;
import Utilities.ECycleTags;
import Utilities.OSRSUtilities;
import Utilities.Requirement.LevelRequirement;
import Utilities.Requirement.MemberRequirement;
import Utilities.Requirement.QuestRequirement;
import Utilities.Scripting.SimpleCycle;
import com.google.gson.Gson;
import io.vavr.Tuple2;
import org.dreambot.api.Client;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.quest.book.FreeQuest;
import org.dreambot.api.methods.quest.book.PaidQuest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.utilities.Logger;

import java.util.*;

public class CycleLibrary
{
    // Json Paths
    final String SmeltCycleDBPath       = "SmeltCycles.json";
    final String MineCycleDBPath        = "MineCycles.json";
    final String CombineCycleDBPath     = "CombineCycles.json";
    final String CombatLootCycleDBPath  = "CombatLootCycles.json";
    final String WoodCuttingCycleDBPath = "WoodCuttingCycles.json";
    final String FireMakingCycleDBPath  = "FireMakingCycles.json";
    final String PyreCycleDBPath        = "PyreCycles.json";

    public static SimpleCycle[] FireMakingWoodCuttingTraining()
    {
        var regLog    = WoodDB.GetWoodData(WoodDB.WoodType.Logs);
        var OakLog    = WoodDB.GetWoodData(WoodDB.WoodType.Oak);
        var WillowLog = WoodDB.GetWoodData(WoodDB.WoodType.Willow);
        var YewLog    = WoodDB.GetWoodData(WoodDB.WoodType.Yew);
        var MagicLog  = WoodDB.GetWoodData(WoodDB.WoodType.Magic);

        // Areas
        var DraynorWillowArea = new Area(3087, 3237, 3092, 3227);
       // var DraynorOakArea    = new Area(3098, 3250, 3105, 3239); aggressive prison guards
        var WCGuildArea       = new Area(1581, 3495, 1585, 3490);
        var LumbridgeArea     = new Area(3198, 3239, 3148, 3215);
        var LumbridgeOakArea = new Area(3247, 3277, 3215, 3261);
        var VarrockArea       = new Area(3202, 3506, 3223, 3501);


        int TinderBoxID = 590;

        int FireMakingLevel  = Skills.getRealLevel(Skill.FIREMAKING);
        int WoodCuttingLevel = Skills.getRealLevel(Skill.WOODCUTTING);
        var result = WoodDB.GetBestFireMakingLog(Math.min(WoodCuttingLevel, FireMakingLevel),
                                                 Client.isMembers());
        int[] BurnAbleLogs =  WoodDB.GetBurnableLogs(FireMakingLevel, Client.isMembers()).stream().mapToInt(t -> t.id).toArray();


        SimpleCycle WoodCuttingCycle = null;
        SimpleCycle FireMakingCycle  = null;

        WoodDB.WoodType type = result._1;
        Logger.log("CycleLibrary: FireMakingWoodCuttingTraining: IsMember: " + Client.isMembers());
        Logger.log("CycleLibrary: FireMakingWoodCuttingTraining: result: " + result);

        if(!Client.isMembers())
        {
            if(Objects.requireNonNull(result._1) == WoodDB.WoodType.Maple)
            {
                type = WoodDB.WoodType.Willow;
            }
        }

        switch(type)
        {
            case Logs ->
            {
                InteractCycle CutReg = new InteractCycle("Go cut " + regLog.name,
                                                         ObjectDB.GetObjectIDsByName(regLog.trees));
                CutReg.SetTargetArea(LumbridgeArea);
                CutReg.setDepositInventory(false);
                CutReg.AddInventoryRequirement(TinderBoxID);

                WoodCuttingCycle = CutReg;
                var RegLogCycle = new InteractOnPositionCycle("Burn logs",
                                                              TinderBoxID,
                                                              (t) -> Arrays.stream(WoodDB.GetFireMakingPositions(FireMakingLevel,
                                                                      t)).toList(), BurnAbleLogs);
                RegLogCycle.TileChecker = WoodDB::isTileBurnableAndWithinReach;
                FireMakingCycle         = RegLogCycle;
            } case Oak ->
        {
            InteractCycle CutOak = new InteractCycle("Go cut " + OakLog.name,
                                                     ObjectDB.GetObjectIDsByName(OakLog.trees));
            CutOak.SetTargetArea(LumbridgeOakArea);
            CutOak.AddRequirement(new LevelRequirement(Skill.WOODCUTTING, OakLog.level));
            CutOak.setDepositInventory(false);
            CutOak.AddInventoryRequirement(TinderBoxID);
            WoodCuttingCycle = CutOak;

            var OakLogCycle = new InteractOnPositionCycle("Burn logs",
                                                          TinderBoxID,
                                                          (t) -> Arrays.stream(WoodDB.GetFireMakingPositions(FireMakingLevel,
                                                                                                             t)).toList(), BurnAbleLogs);
            OakLogCycle.TileChecker = WoodDB::isTileBurnableAndWithinReach;
            OakLogCycle.AddRequirement(new LevelRequirement(Skill.FIREMAKING, OakLog.level));
            FireMakingCycle = OakLogCycle;
        }
            case Willow ->
            {
                InteractCycle CutWillow = new InteractCycle("Go cut " + WillowLog.name,
                                                            ObjectDB.GetObjectIDsByName(WillowLog.trees));
                CutWillow.SetTargetArea(DraynorWillowArea); // Draynor village
                CutWillow.AddRequirement(new LevelRequirement(Skill.WOODCUTTING, WillowLog.level));
                CutWillow.setDepositInventory(false);
                CutWillow.AddInventoryRequirement(TinderBoxID);
                WoodCuttingCycle = CutWillow;

                var WillowLogCycle = new InteractOnPositionCycle("Burn logs",
                                                                 TinderBoxID,
                                                                 (t) -> Arrays.stream(WoodDB.GetFireMakingPositions(FireMakingLevel,
                                                                                                                    t)).toList(), BurnAbleLogs);
                WillowLogCycle.TileChecker = WoodDB::isTileBurnableAndWithinReach;
                WillowLogCycle.AddRequirement(new LevelRequirement(Skill.FIREMAKING,
                                                                   WillowLog.level));
                FireMakingCycle = WillowLogCycle;
            }
            case Maple ->
            {

            }
            case Yew ->
            {
                InteractCycle CutYew = new InteractCycle("Go cut " + YewLog.name,
                                                         ObjectDB.GetObjectIDsByName(YewLog.trees));
                CutYew.SetTargetArea(VarrockArea);
                CutYew.AddRequirement(new LevelRequirement(Skill.WOODCUTTING, YewLog.level));
                CutYew.setDepositInventory(false);
                CutYew.AddInventoryRequirement(TinderBoxID);
                WoodCuttingCycle = CutYew;

                var YewLogCycle = new InteractOnPositionCycle("Burn logs",
                                                              TinderBoxID,
                                                              (t) -> Arrays.stream(WoodDB.GetFireMakingPositions(FireMakingLevel,
                                                                                                                 t)).toList(), BurnAbleLogs);
                YewLogCycle.TileChecker = WoodDB::isTileBurnableAndWithinReach;
                YewLogCycle.AddRequirement(new LevelRequirement(Skill.FIREMAKING, YewLog.level));
                FireMakingCycle = YewLogCycle;
            }
            case Magic ->
            {
                InteractCycle CutMagic = new InteractCycle("Go cut " + MagicLog.name,
                                                           ObjectDB.GetObjectIDsByName(MagicLog.trees));
                CutMagic.AddRequirement(new LevelRequirement(Skill.WOODCUTTING, MagicLog.level),
                                        new MemberRequirement());
                CutMagic.SetTargetArea(WCGuildArea); //WC Guild
                CutMagic.setDepositInventory(false);
                CutMagic.AddInventoryRequirement(TinderBoxID);
                WoodCuttingCycle = CutMagic;


                //FireMakingCycle = RegLogCycle;
            }
            case Redwood -> {}
            case Teak -> {}
            case Mahogany -> {}
        }


        //        var log         = WoodDB.GetMoneyEfficientPyreLog(true, true);
        //        Logger.log(log);
        //        CombineCycle CreatePyreLog = new CombineCycle("Regular Pyre Logs",
        //                                                      log.id,
        //                                                      1,
        //                                                      SacredOilID,
        //                                                      1);
        //        CreatePyreLog.onCycleEnd.Subscribe(CycleLifeTimeCheck, () -> {
        //            var halfpotions = Inventory.all(t -> t.getID() == SacredOil2ID);
        //            for(int i = 1; i < halfpotions.size(); i += 2)
        //            {
        //                halfpotions.get(i - 1).useOn(halfpotions.get(i));
        //                Sleep.sleepTick();
        //                Sleep.sleepTick();
        //                Sleep.sleepTick();
        //            }
        //            //decant sacred oil
        //        });
        //        CreatePyreLog.SetUseSkillingMenu(false);
        //AddCycle(CreatePyreLog);


        //Cut wood cycle

        Logger.log("CycleLibrary: FireMakingWoodCuttingTraining: Type: " + type.value + " Cycles " +
                   WoodCuttingCycle.GetName() + " " + FireMakingCycle.GetName());

        return new SimpleCycle[]{WoodCuttingCycle, FireMakingCycle};
    }

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

    public static SimpleCycle GetFishingCycle()
    {
        List<InteractCycle> Cycles = new ArrayList<>();
        //        final int SmallBaitSpotID   = 1530;
        //        final int RodFishingSpotID  = 1527;
        //        final int CageHarpoonSpotID = 1522;

        final String FishingSpot  = "Fishing spot";
        final int[]  FishingSpots = NPCDB.GetObjectIDsByName(FishingSpot);

        final String SmallNetAction = "Small Net";
        final String RodBaitAction  = "Bait";

        final int SmallNetID   = 303;
        final int RodID        = 307;
        final int FlyRodID     = 309;
        final int LobsterPotID = 301;
        final int HarpoonID    = 311;


        // F2P
        Area LumbridgeSwampNet = new Area(3236, 3150, 3241, 3159);
        Area DraynorNet        = new Area(3089, 3228, 3087, 3234);
        Area[] EdgeVillBait = {
                new Area(3102, 3435, 3106, 3432), new Area(3099, 3426, 3101, 3422)};
        Area KaramjaCageHarpoon = new Area(2924, 3180, 2925, 3172);


        InteractCycle ShrimpCycle = new InteractCycle("Shrimps and Anchovy",
                                                      SmallNetAction,
                                                      FishingSpots);
        ShrimpCycle.SetFilter(InteractTask.InteractableFilter.NPCs);
        ShrimpCycle.SetTargetArea(DraynorNet);
        ShrimpCycle.AddInventoryRequirement(SmallNetID);
        ShrimpCycle.AddRequirement(new LevelRequirement(Skill.FISHING, 0));
        ShrimpCycle.AddEndTask(BankItemsTask.FullDepositInventory(SmallNetID));
        Cycles.add(ShrimpCycle);


        Gson gson = OSRSUtilities.OSRSGsonBuilder.create();


        return ShrimpCycle;

    }

    public static SimpleCycle GetWoodCuttingCycle(WoodDB.WoodType type)
    {
        var regLog    = WoodDB.GetWoodData("Logs");
        var OakLog    = WoodDB.GetWoodData("Oak logs");
        var WillowLog = WoodDB.GetWoodData("Willow logs");
        var YewLog    = WoodDB.GetWoodData("Yew logs");
        var MagicLog  = WoodDB.GetWoodData("Magic logs");

        // Areas
        var DraynorWillowArea = new Area(3087, 3237, 3092, 3227);
        var DraynorOakArea    = new Area(3098, 3250, 3105, 3239);
        var WCGuildArea       = new Area(1581, 3495, 1585, 3490);
        var LumbridgeArea     = new Area(3198, 3239, 3148, 3215);
        var VarrockArea       = new Area(3202, 3506, 3223, 3501);


        //int TinderBoxID = 590;

        SimpleCycle WoodCuttingCycle = null;
        SimpleCycle FireMakingCycle  = null;

        switch(type)
        {
            case Logs ->
            {
                InteractCycle CutReg = new InteractCycle("Go cut " + regLog.name,
                                                         ObjectDB.GetObjectIDsByName(regLog.trees));
                CutReg.SetTargetArea(LumbridgeArea); // Draynor village
                //                CutReg.setDepositInventory(false);
                //                CutReg.AddInventoryRequirement(TinderBoxID);

                return CutReg;
            }
            case Oak ->
            {
                InteractCycle CutOak = new InteractCycle("Go cut " + OakLog.name,
                                                         ObjectDB.GetObjectIDsByName(OakLog.trees));
                CutOak.SetTargetArea(DraynorOakArea); // Draynor village //TODO
                CutOak.AddRequirement(new LevelRequirement(Skill.WOODCUTTING, OakLog.level));
                //                CutOak.setDepositInventory(false);
                //                CutOak.AddInventoryRequirement(TinderBoxID);
                return CutOak;
            }
            case Willow ->
            {
                InteractCycle CutWillow = new InteractCycle("Go cut " + WillowLog.name,
                                                            ObjectDB.GetObjectIDsByName(WillowLog.trees));
                CutWillow.SetTargetArea(DraynorWillowArea); // Draynor village
                CutWillow.AddRequirement(new LevelRequirement(Skill.WOODCUTTING, WillowLog.level));
                //                CutWillow.setDepositInventory(false);
                //                CutWillow.AddInventoryRequirement(TinderBoxID);
                return CutWillow;
            }
            case Maple ->
            {

            }
            case Yew ->
            {
                InteractCycle CutYew = new InteractCycle("Go cut " + YewLog.name,
                                                         ObjectDB.GetObjectIDsByName(YewLog.trees));
                CutYew.SetTargetArea(VarrockArea); // Draynor village // TODO
                CutYew.AddRequirement(new LevelRequirement(Skill.WOODCUTTING, YewLog.level));
                //                CutYew.setDepositInventory(false);
                //                CutYew.AddInventoryRequirement(TinderBoxID);
                return CutYew;
            }
            case Magic ->
            {
                InteractCycle CutMagic = new InteractCycle("Go cut " + MagicLog.name,
                                                           ObjectDB.GetObjectIDsByName(MagicLog.trees));
                CutMagic.AddRequirement(new LevelRequirement(Skill.WOODCUTTING, MagicLog.level),
                                        new MemberRequirement());
                CutMagic.SetTargetArea(WCGuildArea); //WC Guild
                //                CutMagic.setDepositInventory(false);
                //                CutMagic.AddInventoryRequirement(TinderBoxID);
                return CutMagic;
            }
            case Redwood -> {}
            case Teak -> {}
            case Mahogany -> {}
        }
        return null;
    }

    public static void main(String[] args)
    {
        System.out.print(GetFishingCycle());
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
    //
    //    public static String GenerateJSONMineCycles()
    //    {
    //
    //    }
    //
    //    public static String GenerateJSONWoodCuttingCycles()
    //    {
    //
    //    }
    //
    //    public static String GenerateJSONFireMakingCycles()
    //    {
    //
    //    }
    //
    //    public static String GenerateJSONAllCycles()
    //    {
    //
    //    }
}
