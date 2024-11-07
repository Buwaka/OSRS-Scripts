package Scripts.Private;


import Cycles.CycleGenerators.HerbloreCycleGenerator;
import Utilities.Scripting.IFScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

@ScriptManifest(name = "SoloScripts.CleanHerbsScript", description = "Clean Herbs", author = "Semanresu", version = 1.0, category = Category.HERBLORE, image = "")
public class CleanHerbsScript extends IFScript
{


    /**
     *
     */
    @Override
    public void onStart()
    {
        AddCycle(HerbloreCycleGenerator::GetCleanGrimyHerbsCycle);
        AddCycle(HerbloreCycleGenerator::GetUnfPotionCycle);

        super.onStart();
    }
}
