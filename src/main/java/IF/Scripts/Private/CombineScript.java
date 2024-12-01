package IF.Scripts.Private;

import IF.Logic.Tasks.AdvanceTasks.OpenBankTask;
import IF.Utilities.Scripting.IFScript;
import IF.Utilities.Scripting.Logger;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

@ScriptManifest(name = "SoloScripts.CombineScript", description = "Go through the list of combine tasks", author = "Semanresu", version = 1.0, category = Category.CRAFTING, image = "")
public class CombineScript extends IFScript
{

    @Override
    public void onStart()
    {
        if(!Bank.isCached())
        {
            OpenBankTask openBank = new OpenBankTask();
            openBank.onComplete.Subscribe(openBank, this::CheckCombineTasks);
            addNodes(openBank);
        }
        else
        {
            CheckCombineTasks();
        }
        super.onStart();
    }

    void CheckCombineTasks()
    {
        Logger.log("Travel to bank Complete, checking possible combinetasks");
//        var all = CycleUtilities.GetAllValidCombineCycles();
//        for(var cycle : all)
//        {
//            Logger.log(cycle);
//            AddCycle(cycle);
//        }
    }
}
