package Scripts.Private;

import Cycles.General.CombatLootBankCycle;
import Cycles.General.SimpleProcessCycle;
import Cycles.Tasks.SimpleTasks.Bank.GETask;
import Cycles.Tasks.SimpleTasks.ItemProcessing.TanTask;
import OSRSDatabase.ItemDB;
import OSRSDatabase.MonsterDB;
import Utilities.GrandExchange.GEInstance;
import Utilities.GrandExchange.Orders.MarketSellOrder;
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

@ScriptManifest(name = "SoloScripts.CowHideCombatScript", description = "Kill stuff, then tan stuff", author = "Semanresu", version = 1.0, category = Category.COMBAT, image = "")
public class CowHideCombatScript extends tpircSScript
{


    @Override
    public void onStart()
    {


        AddCycle(() -> {
            ItemDB.ItemData CowHide = ItemDB.GetClosestMatch("Cowhide", true);
            ItemDB.ItemData HardLeather = ItemDB.GetClosestMatch("Hard leather", true);
            CombatLootBankCycle CombatCycle = new CombatLootBankCycle("Moss Giant slaughter",
                                                                new Area[]{
                                                                        new Area(3264,
                                                                                 3256,
                                                                                 3254,
                                                                                 3296),
                                                                        new Area(3253,
                                                                                 3296,
                                                                                 3244,
                                                                                 3281)},
                                                                MonsterDB.GetMonsterIDsByName("Cow",
                                                                                              true));
            CombatCycle.SetPray(true);
            CombatCycle.SetEscapeLowHP(false);
            CombatCycle.setIgnoreLoot(2132);
            CombatCycle.AddGoal(new ItemRequirement(true, new Tuple2<>(CowHide.id, 1000)));
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


            // Tan stuff
            SimpleProcessCycle TanLeather = new SimpleProcessCycle("Tan HardLeather",
                                                                   new TanTask("HardLeather",
                                                                               "Hard leather",
                                                                               CowHide.id, 3),
                                                                   new Area(3278,
                                                                            3193,
                                                                            3283,
                                                                            3189));
            TanLeather.SetCycleType(ICycle.CycleType.byGoal);
            TanLeather.AddGoal(new ORRequirement(new ItemRequirement(true, new Tuple2<>(CowHide.id, -30), new Tuple2<>(GEInstance.CoinID, -3))));
            TanLeather.AddItemRequirements(new Tuple2<>(GEInstance.CoinID, 100), new Tuple2<>(CowHide.id, Integer.MAX_VALUE));
            //TanLeather.AddStartUpTask(BankItemsTask.GetMoney(10000));
//            TanLeather.AddEndTask(() -> new BankItemsTask[]{
//                    BankItemsTask.FullDepositInventory(new Tuple2<>(GEInstance.CoinID,
//                                                                    Integer.MAX_VALUE))});
            TanLeather.onCycleEnd.Subscribe(this, () ->{
                GetGEInstance().AddOrder(new MarketSellOrder(HardLeather.id, Bank.count(HardLeather.id)));
                addNodes(new GETask("Go Sell HardLeather"));
            });

            return new SimpleCycle[]{TanLeather, CombatCycle};


        });

        if(!Combat.isAutoRetaliateOn())
        {
            Combat.toggleAutoRetaliate(true);
        }


        super.onStart();
    }
}
