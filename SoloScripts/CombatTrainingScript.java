package SoloScripts;

import Cycles.CombatLootBankCycle;
import Cycles.SimpleTasks.Bank.BankItemsTask;
import Cycles.SimpleTasks.Misc.EquipmentTask;
import OSRSDatabase.MonsterDB;
import Utilities.Combat.MeleeCombat;
import Utilities.Scripting.ICycle;
import Utilities.Scripting.tpircSScript;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

@ScriptManifest(name = "SoloScripts.CombatTrainingScript", description = "Kill stuff", author = "Semanresu", version = 1.0, category = Category.COMBAT, image = "")
public class CombatTrainingScript extends tpircSScript
{
    CombatLootBankCycle Cycle = new CombatLootBankCycle("Moss Giant slaughter",
                                                        new Area[]{
                                                                new Area(3264, 3256, 3254, 3296),
                                                                new Area(3253, 3296, 3244, 3281)},
                                                        MonsterDB.GetMonsterIDsByName("Cow", true));

    @Override
    public void onStart()
    {

        onBankCached.Subscribe(this, () -> {
            var           equipment    = MeleeCombat.GetBestEquipment();
            BankItemsTask GetEquipment = new BankItemsTask("Get Equipment");
            GetEquipment.AddEquipment(equipment);
            EquipmentTask EquipEquipment = new EquipmentTask("Equipment Test", equipment);
            addNodes(GetEquipment, EquipEquipment);

            Cycle.SetCycleType(ICycle.CycleType.Endless);
            AddCycle(Cycle);
        });

        if(!Combat.isAutoRetaliateOn())
        {
            Combat.toggleAutoRetaliate(true);
        }


        super.onStart();
    }
}
