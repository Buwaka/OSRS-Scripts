package SoloScripts;

import Cycles.MineGuildCycle;
import Utilities.Scripting.ICycle;
import Utilities.Scripting.tpircSScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

@ScriptManifest(name = "SoloScripts.MineGuildScript", description = "MineGuild shenanigans", author = "Semanresu", version = 1.0, category = Category.MINING, image = "")

public class MineGuildScript extends tpircSScript
{
    MineGuildCycle MGCycle = new MineGuildCycle("MineGuildCycle");

    /**
     *
     */
    @Override
    public void onStart()
    {
        MGCycle.SetCycleType(ICycle.CycleType.Endless);
        AddCycle(MGCycle);

        super.onStart();
    }
}
