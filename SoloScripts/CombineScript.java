package SoloScripts;

import Cycles.AdvanceTasks.OpenBankTask;
import Utilities.Scripting.CycleUtilities;
import Utilities.Scripting.tpircSScript;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Logger;

@ScriptManifest(name = "SoloScripts.CombineScript", description = "Go through the list of combine tasks", author = "Semanresu", version = 1.0, category = Category.CRAFTING, image = "")
public class CombineScript extends tpircSScript
{

    void CheckCombineTasks()
    {
        Logger.log("Travel to bank Complete, checking possible combinetasks");
        var all = CycleUtilities.GetAllValidCombineCycles();
        for(var cycle : all)
        {
            Logger.log(cycle);
            AddCycle(cycle);
        }
    }
    @Override
    public void onStart()
    {
        if(!Bank.isCached())
        {
            OpenBankTask openBank = new OpenBankTask();
            openBank.onComplete.Subscribe(this::CheckCombineTasks);
            addNodes(openBank);
        }
        else
        {
            CheckCombineTasks();
        }
        super.onStart();
    }
}
