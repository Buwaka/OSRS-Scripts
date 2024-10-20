package Scripts.Private;

import Cycles.Specifics.MineGuildCycle;
import Cycles.Tasks.SimpleTasks.TravelTask;
import Utilities.Scripting.ICycle;
import Utilities.Scripting.IFScript;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

@ScriptManifest(name = "SoloScripts.MineGuildScript", description = "MineGuild shenanigans", author = "Semanresu", version = 1.0, category = Category.MINING, image = "")

public class MineGuildScript extends IFScript
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
        MGCycle.AddStartUpTask(() -> new TravelTask[]{
                new TravelTask("Travel to mineguild", new Tile(3754, 5670, 0))});

        super.onStart();
    }
}
