package Scripts.Private;

import Cycles.CycleGenerators.FireMakingCycleGenerator;
import Utilities.Scripting.IFScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

@ScriptManifest(name = "SoloScripts.WoodCuttingFireMakingTraining", description = "Cut wood, burn logs, repeat", author = "Semanresu", version = 1.0, category = Category.FIREMAKING, image = "")
public class WoodCuttingFireMakingTraining extends IFScript
{

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
    }


}
