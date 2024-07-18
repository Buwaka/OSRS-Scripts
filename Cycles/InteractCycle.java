package Cycles;

import Cycles.SimpleTasks.ItemProcessing.InteractTask;
import Cycles.SimpleTasks.TravelTask;
import Utilities.Scripting.SimpleCycle;
import Utilities.Scripting.tpircSScript;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;

import java.util.Arrays;

public class InteractCycle extends SimpleCycle
{
    private           Area[]       TargetArea   = null;
    private           int[]        Targets;
    private transient InteractTask interactTask = null;

    public InteractCycle(String name, int... targets)
    {
        super(name);
        Targets = targets;
    }

    public void SetTargetArea(Area... areas)
    {
        TargetArea = areas;
    }

    /**
     * @param Script
     *
     * @return if cycle has successfully started
     */
    @Override
    public boolean onStart(tpircSScript Script)
    {
        StartCycle(Script);
        return super.onStart(Script);
    }

    @Override
    public boolean onEnd(tpircSScript Script)
    {
        Logger.log("InteractCycle: OnEnd");
        if(Sleep.sleepUntil(() -> Bank.open(), 60000))
        {
            Bank.depositAllItems();
        }
        return super.onEnd(Script);
    }

    /**
     * @param Script
     *
     * @return
     */
    @Override
    public boolean onRestart(tpircSScript Script)
    {
        StartCycle(Script);
        return true;
    }

    private TravelTask CreateTravelTask()
    {
        TravelTask TravelToArea = new TravelTask("Travel to Area",
                                                 Arrays.stream(TargetArea).findAny().get().getRandomTile());
        TravelToArea.onReachedDestination.Subscribe(this, this::onTravelComplete);
        return TravelToArea;
    }

    private void onTravelComplete()
    {
        if(interactTask != null && interactTask.GetTarget() == null)
        {
            GetScript().addNodes(CreateTravelTask());
        }
    }

    private void StartCycle(tpircSScript Script)
    {
        interactTask = new InteractTask(GetName(), Targets);
        interactTask.TaskPriority.set(0);

        if(TargetArea != null)
        {
            GetScript().addNodes(CreateTravelTask());
        }

        Script.addNodes(interactTask);
    }
}
