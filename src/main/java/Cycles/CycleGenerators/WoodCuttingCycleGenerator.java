package Cycles.CycleGenerators;

import Cycles.General.InteractCycle;
import OSRSDatabase.ObjectDB;
import OSRSDatabase.WoodDB;
import Utilities.Requirement.LevelRequirement;
import Utilities.Requirement.MemberRequirement;
import Utilities.Scripting.SimpleCycle;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;

public class WoodCuttingCycleGenerator extends CycleLibrary
{
    final          String       WoodCuttingCycleDBPath = "WoodCuttingCycles.json";

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
}
