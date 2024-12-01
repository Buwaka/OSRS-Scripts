package IF.Scripts.Private;

import IF.Logic.Cycles.General.CombineCycle;
import IF.Utilities.Scripting.ICycle;
import IF.Utilities.Scripting.IFScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

@ScriptManifest(name = "SoloScripts.PieFactory2", description = "Make Pie shells", author = "Semanresu", version = 1.0, category = Category.CRAFTING, image = "")

public class PieFactory2 extends IFScript
{
    final int PieShellID   = 2313;
    final int PastyDoughID = 1953;
    CombineCycle Cycle = new CombineCycle("Pie Shells", PieShellID, PastyDoughID);

    @Override
    public void onStart()
    {
        Cycle.SetCycleType(ICycle.CycleType.Endless);
        AddCycle(Cycle);
        super.onStart();
    }
}
