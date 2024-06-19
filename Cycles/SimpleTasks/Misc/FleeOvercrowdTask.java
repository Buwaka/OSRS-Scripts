package Cycles.SimpleTasks.Misc;

import Utilities.OSRSUtilities;
import Utilities.Scripting.SimpleTask;
import org.dreambot.api.methods.interactive.Players;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class FleeOvercrowdTask extends SimpleTask
{
    /**
     * how many players do we tolerate before jumping world
     */
    public AtomicInteger MaxPlayers         = new AtomicInteger(10);
    /**
     * only count players that are interacting and/or in combat
     */
    public AtomicBoolean OnlyActivePlayers  = new AtomicBoolean(true);
    /**
     * Wait until we're done with our current interaction
     */
    public AtomicBoolean WaitForEndInteract = new AtomicBoolean(true);

    public FleeOvercrowdTask()
    {
        super("Flee Overcrowded Area");
        //SetPersistant(true);
        TaskPriority.set(-1);
    }

    @Override
    public boolean Ready()
    {
        boolean IsNotInteracting = !Players.getLocal().isInteractedWith() && WaitForEndInteract.get();
        return IsNotInteracting && OSRSUtilities.IsAreaBusy(MaxPlayers.get(), OnlyActivePlayers.get()) && super.Ready();
    }

    @Override
    public int Loop()
    {
        if(OSRSUtilities.JumpToOtherWorld())
        {
            return 0;
        }
        return OSRSUtilities.WaitTime(ScriptIntensity.get());
    }

    @Nonnull
    @Override
    public TaskType GetTaskType()
    {
        return TaskType.FleeOvercrowd;
    }
}
