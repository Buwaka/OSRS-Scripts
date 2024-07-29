package SoloScripts;


import Cycles.CycleLibrary;
import Utilities.Scripting.tpircSScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

@ScriptManifest(name = "SoloScripts.FishingScript", description = "fish stuff", author = "Semanresu", version = 1.0, category = Category.FISHING, image = "")
public class FishingScript extends tpircSScript
{

    /**
     *
     */
    @Override
    public void onStart()
    {

        AddCycle(CycleLibrary.GetFishingCycle());
        super.onStart();
    }
}
