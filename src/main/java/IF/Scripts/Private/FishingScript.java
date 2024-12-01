package IF.Scripts.Private;


import IF.Logic.Generators.FishingCycleGenerator;
import IF.Utilities.Scripting.IFScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

@ScriptManifest(name = "SoloScripts.FishingScript", description = "fish stuff", author = "Semanresu", version = 1.0, category = Category.FISHING, image = "")
public class FishingScript extends IFScript
{

    /**
     *
     */
    @Override
    public void onStart()
    {

        AddCycle(FishingCycleGenerator.GetFishingCycle());
        super.onStart();
    }
}
