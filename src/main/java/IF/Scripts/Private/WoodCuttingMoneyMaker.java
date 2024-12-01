package IF.Scripts.Private;

import IF.Logic.Generators.FireMakingCycleGenerator;
import IF.Utilities.Scripting.IFScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

@ScriptManifest(name = "SoloScripts.WoodCuttingMoneyMaker", description = "Cut wood, Create Pyre Logs, sell on GE, repeat", author = "Semanresu", version = 1.0, category = Category.FIREMAKING, image = "")
public class WoodCuttingMoneyMaker extends IFScript
{
    final int SacredOilID  = 3430;
    final int SacredOil2ID = 3434;
    private final Boolean CycleLifeTimeCheck = true;
    boolean SellPyreLogs = true;


    //    /**
    //     *
    //     */
    //    @Override
    //    public void onResume()
    //    {
    //        this.StopCurrentCycle();
    //
    //        super.onResume();
    //    }

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
        AddCycle(FireMakingCycleGenerator::FireMakingWoodCuttingTraining);


        // AddCycle(CycleLibrary.GetWoodCuttingCycle(WoodDB.WoodType.Yew));
    }


}
