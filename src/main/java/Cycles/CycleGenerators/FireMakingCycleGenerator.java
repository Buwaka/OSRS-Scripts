package Cycles.CycleGenerators;

import Cycles.General.InteractCycle;
import Cycles.General.InteractOnPositionCycle;
import Cycles.Tasks.SimpleTasks.Bank.BankItemsTask;
import Cycles.Tasks.SimpleTasks.Bank.GETask;
import Cycles.Tasks.SimpleTasks.Misc.EquipmentTask;
import OSRSDatabase.DBTags;
import OSRSDatabase.OSRSPrices;
import OSRSDatabase.ObjectDB;
import OSRSDatabase.WoodDB;
import Utilities.GrandExchange.GEInstance;
import Utilities.GrandExchange.Orders.MarketBuyOrder;
import Utilities.Requirement.LevelRequirement;
import Utilities.Requirement.MemberRequirement;
import Utilities.Scripting.SimpleCycle;
import org.dreambot.api.Client;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.utilities.Logger;

import java.util.Arrays;
import java.util.Objects;

public class FireMakingCycleGenerator extends CycleLibrary
{
    final String FireMakingCycleDBPath = "FireMakingCycles.json";
    final String PyreCycleDBPath       = "PyreCycles.json";


    public static SimpleCycle[] FireMakingWoodCuttingTraining()
    {
        var regLog    = WoodDB.GetWoodData(WoodDB.WoodType.Logs);
        var OakLog    = WoodDB.GetWoodData(WoodDB.WoodType.Oak);
        var WillowLog = WoodDB.GetWoodData(WoodDB.WoodType.Willow);
        var YewLog    = WoodDB.GetWoodData(WoodDB.WoodType.Yew);
        var MagicLog  = WoodDB.GetWoodData(WoodDB.WoodType.Magic);

        // Areas
        var DraynorWillowArea = new Area(3087, 3237, 3092, 3227, 0);
        // var DraynorOakArea    = new Area(3098, 3250, 3105, 3239); aggressive prison guards
        var WCGuildArea      = new Area(1581, 3495, 1585, 3490, 0);
        var LumbridgeArea    = new Area(3198, 3239, 3148, 3215, 0);
        var LumbridgeOakArea = new Area(3247, 3277, 3215, 3261, 0);
        var VarrockArea      = new Area(3202, 3506, 3223, 3501, 0);


        WoodDB.WoodCuttingTool BestWoodcuttingTool = WoodDB.GetBestWoodCuttingTool(Client.isMembers(),
                                                                                   DBTags.fire_making_exp,
                                                                                   DBTags.cheap);
        WoodDB.WoodCuttingTool AvailableWoodcuttingTool = WoodDB.GetBestWoodCuttingTool(Client.isMembers(),
                                                                                        GEInstance.GetAllItemsID(),
                                                                                        DBTags.fire_making_exp);


        Logger.log("CycleLibrary: FireMakingWoodCuttingTraining: Available tool: " +
                   AvailableWoodcuttingTool.name);
        Logger.log("CycleLibrary: FireMakingWoodCuttingTraining: Best tool: " +
                   BestWoodcuttingTool.name);

        WoodDB.WoodCuttingTool ToolToUse = AvailableWoodcuttingTool;
        if(BestWoodcuttingTool != AvailableWoodcuttingTool &&
           BestWoodcuttingTool.WoodCuttingStrength > AvailableWoodcuttingTool.WoodCuttingStrength &&
           BestWoodcuttingTool.ge_tradable)
        {
            Logger.log("CycleLibrary: FireMakingWoodCuttingTraining: Want to buy new axe: " +
                       BestWoodcuttingTool);
            if(OSRSPrices.GetAveragePrice(BestWoodcuttingTool.id) < GEInstance.GetLiquidMoney())
            {
                OwnerScript.GetGEInstance()
                           .AddUniqueOrder(new MarketBuyOrder(BestWoodcuttingTool.id, 1));
                var neworder = new GETask("Buy New Woodcutting tool");
                neworder.SetTaskPriority(-1);
                OwnerScript.addNodes(neworder);
            }
            else
            {
                Logger.log("CycleLibrary: FireMakingWoodCuttingTraining: Cannot afford : " +
                           BestWoodcuttingTool);
            }

        }
        Logger.log("CycleLibrary: FireMakingWoodCuttingTraining: ToolToUse: " + ToolToUse);


        int TinderBoxID = 590;

        int FireMakingLevel  = Skills.getRealLevel(Skill.FIREMAKING);
        int WoodCuttingLevel = Skills.getRealLevel(Skill.WOODCUTTING);
        var result = WoodDB.GetBestFireMakingLog(Math.min(WoodCuttingLevel, FireMakingLevel),
                                                 Client.isMembers());
        int[] BurnAbleLogs = WoodDB.GetBurnableLogs(FireMakingLevel, Client.isMembers())
                                   .stream()
                                   .mapToInt(t -> t.id)
                                   .toArray();


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
                                                              (t) -> Arrays.stream(WoodDB.GetFireMakingPositions(
                                                                      FireMakingLevel,
                                                                      t)).toList(),
                                                              BurnAbleLogs);
                RegLogCycle.TileChecker = WoodDB::isTileBurnableAndWithinReach;
                FireMakingCycle         = RegLogCycle;
            }
            case Oak ->
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
                                                              (t) -> Arrays.stream(WoodDB.GetFireMakingPositions(
                                                                      FireMakingLevel,
                                                                      t)).toList(),
                                                              BurnAbleLogs);
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
                                                                 (t) -> Arrays.stream(WoodDB.GetFireMakingPositions(
                                                                         FireMakingLevel,
                                                                         t)).toList(),
                                                                 BurnAbleLogs);
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
                                                              (t) -> Arrays.stream(WoodDB.GetFireMakingPositions(
                                                                      FireMakingLevel,
                                                                      t)).toList(),
                                                              BurnAbleLogs);
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


        if(!Inventory.contains(ToolToUse.id))
        {
            WoodCuttingCycle.AddStartUpTask(() -> new BankItemsTask[]{
                    BankItemsTask.SimpleWithdraw(ToolToUse.id)});
            WoodCuttingCycle.AddStartUpTask(() -> new EquipmentTask[]{
                    EquipmentTask.SimpleEquip("Equip Axe", ToolToUse.id, EquipmentSlot.WEAPON)});
        }

        Logger.log("CycleLibrary: FireMakingWoodCuttingTraining: Type: " + type.value + " Cycles " +
                   WoodCuttingCycle.GetName() + " " + FireMakingCycle.GetName());

        return new SimpleCycle[]{WoodCuttingCycle, FireMakingCycle};
    }
}
