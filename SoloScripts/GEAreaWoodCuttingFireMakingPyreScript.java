package SoloScripts;

import Cycles.CombineCycle;
import Cycles.InteractCycle;
import Cycles.SimpleTasks.Bank.BankItemsTask;
import OSRSDatabase.ObjectDB;
import OSRSDatabase.WoodDB;
import Utilities.Scripting.tpircSScript;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;

@ScriptManifest(name = "SoloScripts.GEAreaWoodCuttingFireMakingPyreScript", description = "Cut wood, Create Pyre Logs, sell on GE, repeat", author = "Semanresu", version = 1.0, category = Category.FIREMAKING, image = "")
public class GEAreaWoodCuttingFireMakingPyreScript extends tpircSScript
{

    final int SacredOilID  = 3430;
    final int SacredOil2ID = 3434;
    boolean SellPyreLogs = true;
    private Boolean CycleLifeTimeCheck = true;

    /**
     *
     */
    @Override
    public void onStart()
    {
        CreateCycles();
        super.onStart();
    }

    private void CreateCycles()
    {
//        var log = WoodDB.GetMoneyEfficientPyreLog(true, true);
//        Logger.log(log);
//        CombineCycle CreatePyreLog = new CombineCycle("Regular Pyre Logs", log.id, 1, SacredOilID, 1);
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
//        AddCycle(CreatePyreLog);

        if(SellPyreLogs)
        {
            // Put Pyre logs on GE
        }

        //Cut wood cycle
        InteractCycle CutWood = new InteractCycle("Go cut wood", ObjectDB.GetObjectIDsByName("Magic tree"));
        CutWood.SetTargetArea(new Area(1581, 3495, 1585, 3490));

        CutWood.onCompleteCycle.Subscribe(this, () -> {
            BankItemsTask Bank = new BankItemsTask("Go drop at bank");
            Bank.TaskPriority.set(0);
            this.addNodes(Bank);
        });
        AddCycle(CutWood);




    }
}
