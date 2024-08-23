package Scripts.Private;

import Cycles.General.CombatLootBankCycle;
import Cycles.General.SimpleProcessCycle;
import Cycles.Tasks.SimpleTasks.ItemProcessing.TanTask;
import OSRSDatabase.ItemDB;
import OSRSDatabase.MonsterDB;
import Utilities.Scripting.tpircSScript;
import io.vavr.Tuple2;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

import java.util.ArrayList;
import java.util.List;

@ScriptManifest(name = "SoloScripts.MoneyMakingScript", description = "Kill stuff... for money", author = "Semanresu", version = 1.0, category = Category.COMBAT, image = "")
public class MoneyMakingScript extends tpircSScript
{
    final int BeefID = 2132;
    CombatLootBankCycle Cycle = new CombatLootBankCycle("Cow slaughter",
                                                        new Area[]{
                                                                new Area(3264, 3256, 3254, 3296),
                                                                new Area(3253, 3296, 3244, 3281)},
                                                        MonsterDB.GetMonsterIDsByName("Cow", true));

    SimpleProcessCycle TanLeather = new SimpleProcessCycle("Tan HardLeather",
                                                           new TanTask("HardLeather",
                                                                       "Hard leather",
                                                                       ItemDB.GetClosestMatch(
                                                                               "Cowhide",
                                                                               true).id,
                                                                       3),
                                                           new Area(3278, 3193, 3283, 3189));

    @Override
    public void onStart()
    {

        //        onBankCached.Subscribe(this, () -> {
        //            var           equipment    = EquipmentManager.GetWeaponOnly();
        //            BankItemsTask GetEquipment = new BankItemsTask("Get Equipment");
        //            GetEquipment.AddEquipment(equipment);
        //            EquipmentTask EquipEquipment = new EquipmentTask("Equipment Test", equipment);
        //            addNodes(GetEquipment, EquipEquipment);
        //            Cycle.setIgnoreLoot(BeefID);
        //            Cycle.HPtoCarry.set(0);
        //            Cycle.SetPray(true);
        //            Cycle.SetCycleType(ICycle.CycleType.Endless);
        //            AddCycle(Cycle);
        //        });
        //
        //        if(!Combat.isAutoRetaliateOn())
        //        {
        //            Combat.toggleAutoRetaliate(true);
        //        }

        List<Tuple2<Integer, Integer>> Req = new ArrayList<>();
        Req.add(new Tuple2<>(ItemDB.GetClosestMatch("Cowhide", true).id, -1));
        Req.add(new Tuple2<>(ItemDB.GetClosestMatch("Coins", true).id, -1));

        TanLeather.SetItemRequirements(Req);
        //TanLeather.SetCycleType(ICycle.CycleType.Endless);

        AddCycle(TanLeather);

        super.onStart();
    }

}
