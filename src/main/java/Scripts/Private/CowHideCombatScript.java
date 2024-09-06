package Scripts.Private;

import Cycles.General.CombatLootBankCycle;
import Cycles.General.SimpleProcessCycle;
import Cycles.Tasks.SimpleTasks.ItemProcessing.TanTask;
import DataBase.PerformanceDatabase;
import OSRSDatabase.ItemDB;
import OSRSDatabase.MonsterDB;
import Utilities.GrandExchange.GEInstance;
import Utilities.Requirement.ItemRequirement;
import Utilities.Requirement.ORRequirement;
import Utilities.Scripting.ICycle;
import Utilities.Scripting.SimpleCycle;
import Utilities.Scripting.tpircSScript;
import io.vavr.Tuple2;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Logger;

import java.util.ArrayList;
import java.util.List;

@ScriptManifest(name = "SoloScripts.CowHideCombatScript", description = "Kill stuff, then tan stuff", author = "Semanresu", version = 1.0, category = Category.COMBAT, image = "")
public class CowHideCombatScript extends tpircSScript
{


    @Override
    public void onStart()
    {
        int CowHideCount = 600;

        AddCycle(() -> {
            List<SimpleCycle> out         = new ArrayList<>();
            ItemDB.ItemData   CowHide     = ItemDB.GetClosestMatch("Cowhide", true);
            ItemDB.ItemData   HardLeather = ItemDB.GetClosestMatch("Hard leather", true);

            CombatLootBankCycle CombatCycle = new CombatLootBankCycle("Cow slaughter",
                                                                      new Area[]{
                                                                              new Area(3264,
                                                                                       3256,
                                                                                       3254,
                                                                                       3296),
                                                                              new Area(3253,
                                                                                       3296,
                                                                                       3244,
                                                                                       3281)},
                                                                      MonsterDB.GetMonsterIDsByName(
                                                                              "Cow",
                                                                              true));
            CombatCycle.SetPray(true);
            CombatCycle.SetEscapeLowHP(false);
            CombatCycle.HPtoCarry.set(0);
            CombatCycle.setIgnoreLoot(2132);
            CombatCycle.AddGoal(new ItemRequirement(true, new Tuple2<>(CowHide.id, CowHideCount)));
            CombatCycle.SetCycleType(ICycle.CycleType.byGoal);

            int attackLV   = Skills.getRealLevel(Skill.ATTACK);
            int strengthLV = Skills.getRealLevel(Skill.STRENGTH);
            int defenseLV  = Skills.getRealLevel(Skill.DEFENCE);

            if(attackLV < strengthLV && attackLV < defenseLV)
            {
                CombatCycle.SetEXPType(ItemDB.StanceData.ExperienceType.attack);
            }
            else if(strengthLV < defenseLV)
            {
                CombatCycle.SetEXPType(ItemDB.StanceData.ExperienceType.strength);
            }
            else
            {
                CombatCycle.SetEXPType(ItemDB.StanceData.ExperienceType.defence);
            }
            out.add(CombatCycle);


            // Tan stuff
            if(GEInstance.GetLiquidMoney() > 3 && Bank.count(CowHide.id) > CowHideCount)
            {
                SimpleProcessCycle TanLeather = new SimpleProcessCycle("Tan HardLeather",
                                                                       new TanTask("HardLeather",
                                                                                   "Hard leather",
                                                                                   CowHide.id,
                                                                                   3),
                                                                       new Area(3278,
                                                                                3193,
                                                                                3283,
                                                                                3189));
                TanLeather.SetCycleType(ICycle.CycleType.byGoal);
                TanLeather.AddGoal(new ORRequirement(new ItemRequirement(true,
                                                                         new Tuple2<>(CowHide.id,
                                                                                      -30)),
                                                     new ItemRequirement(true,
                                                                         new Tuple2<>(GEInstance.CoinID,
                                                                                      -3))));
                TanLeather.AddItemRequirements(new Tuple2<>(GEInstance.CoinID, 100),
                                               new Tuple2<>(CowHide.id, Integer.MAX_VALUE));
                //                TanLeather.AddEndTask(() -> {
                //                    GetGEInstance().AddUniqueOrder(MarketSellOrder.SellAll(HardLeather.id));
                //                    return new GETask[]{
                //                            new GETask("Go Sell HardLeather and pick up money")};
                //                });
                out.add(TanLeather);
                Logger.log("CowHideCombatScript: StartCycle: Tan leather");

                var report = PerformanceDatabase.GeneratePerformanceReport("CowHideTanning");
                PerformanceDatabase.UploadPerformanceData(report);
            }


            // Sell the leather in another script specifically to sell stuff and clean up the acc
            //            if(Bank.contains(HardLeather.id) || GetGEInstance().ReadyToCollect())
            //            {
            //                if(Bank.contains(HardLeather.id))
            //                {
            //                    Logger.log("CowHideCombatScript: onStart: Bank has Hardleather, go sell");
            //                    GetGEInstance().AddUniqueOrder(new MarketSellOrder(HardLeather.id));
            //                }
            //                var task = new GETask("Go Sell HardLeather and pick up money");
            //                task.SetTaskPriority(-10);
            //                addNodes(task);
            //            }

            //return new SimpleCycle[0];
            return out.toArray(new SimpleCycle[0]);
        });

        if(!Combat.isAutoRetaliateOn())
        {
            Combat.toggleAutoRetaliate(true);
        }


        super.onStart();
    }
}
