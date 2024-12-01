package IF.Scripts.Private;

import IF.Logic.Cycles.Skilling.AlchCycle;
import IF.Utilities.Scripting.IFScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

@ScriptManifest(name = "SoloScripts.AlchCleanupScript", description = "Alching profitable items", author = "Semanresu", version = 1.0, category = Category.MAGIC, image = "")

public class AlchCleanupScript extends IFScript
{
    @Override
    public void onStart()
    {
        AddCycle(new AlchCycle("Alching stuff"));
        super.onStart();
    }
}
