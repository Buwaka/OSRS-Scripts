package Cycles.Tasks.AdvanceTasks;

import Cycles.Tasks.SimpleTasks.TravelTask;
import Utilities.Scripting.SimpleTask;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.settings.PlayerSettings;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.interactive.NPC;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class GraveStoneTask extends SimpleTask
{
    public static final  int    GraveStoneNPCID    = 10046;
    public static final  int    GraveTimerVarBitID = 10465;
    private static final String LootAction         = "Loot";
    private static final String GraveName          = "Grave";
    private              Tile   GraveStoneLocation;


    private TravelTask Travel = null;

    public GraveStoneTask(String Name, Tile location)
    {
        super(Name);
        GraveStoneLocation = location;
        Travel             = new TravelTask("Travel To GraveStone", location);
        SetTaskPriority(-5);
    }

    /**
     * @return
     */
    @Override
    public boolean Ready()
    {
        return GraveStoneExists() && super.Ready();
    }

    /**
     * @return
     */
    @Override
    protected int Loop()
    {
        var GraveStone = GetGraveStone();
        var tile       = Players.getLocal().getTile();

        var varbit = PlayerSettings.getBitValue(GraveTimerVarBitID);
        Logger.log("GraveStoneTask: varbit = " + varbit);

        if(!GraveStoneExists())
        {
            Logger.log("GraveStoneTask: Gravestone has disappeared, exiting");
            return 0;
        }

        Logger.log("GraveStoneTask: Objs on tile" + Arrays.toString(NPCs.all("Grave")
                                                                        .stream()
                                                                        .mapToInt((t) -> t != null
                                                                                ? t.getID()
                                                                                : 0)
                                                                        .toArray()));
        Logger.log("GraveStoneTask: Loop: " + (GraveStone != null));
        if(GraveStone != null)
        {
            Logger.log("GraveStoneTask: Loop: " + GraveStone.canReach() +
                       GraveStone.hasAction(LootAction));
            if(GraveStone.canReach() && GraveStone.hasAction(LootAction))
            {
                if(GraveStone.interact(LootAction))
                {
                    Logger.log("GraveStoneTask: Loop: Interaction Complete");
                }
            }
        }
        else
        {
            Travel.Loop();
        }
        return super.Loop();
    }

    NPC GetGraveStone()
    {
        return NPCs.closest((t) -> t.hasAction(LootAction) &&
                                   t.getName().toLowerCase().contains(GraveName.toLowerCase()));
        //return GameObjects.closest(GraveStoneID);
    }

    boolean GraveStoneExists()
    {
        return PlayerSettings.getBitValue(GraveTimerVarBitID) > 0;
    }


    @Nonnull
    @Override
    public TaskType GetTaskType()
    {
        return TaskType.GraveStone;
    }
}
