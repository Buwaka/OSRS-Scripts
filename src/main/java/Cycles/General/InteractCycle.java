package Cycles.General;

import Cycles.Tasks.SimpleTasks.Bank.BankItemsTask;
import Cycles.Tasks.SimpleTasks.Bank.InventoryCheckTask;
import Cycles.Tasks.SimpleTasks.ItemProcessing.InteractTask;
import Cycles.Tasks.SimpleTasks.TravelTask;
import Utilities.Scripting.IFScript;
import Utilities.Scripting.SimpleCycle;
import io.vavr.Tuple2;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.utilities.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public class InteractCycle extends SimpleCycle
{
    private           Area[]                                   TargetArea            = null;
    private           int[]                                    Targets;
    private           String                                   Action                = null;
    private           EnumSet<InteractTask.InteractableFilter> TargetFilter          = EnumSet.of(
            InteractTask.InteractableFilter.GameObjects);
    private transient InteractTask                             interactTask          = null;
    private           boolean                                  WaitForFullInventory  = true;
    private           boolean                                  DepositInventory      = true;
    private           List<Tuple2<Integer, Integer>>           InventoryRequirements = null;

    public InteractCycle(String name, int... targets)
    {
        super(name);
        Targets = targets;
    }

    public InteractCycle(String name, String Action, int... targets)
    {
        super(name);
        Targets     = targets;
        this.Action = Action;
    }

    public void AddFilter(InteractTask.InteractableFilter... Filter)
    {
        TargetFilter.addAll(List.of(Filter));
    }

    public void AddInventoryRequirement(int... IDs)
    {
        if(InventoryRequirements == null)
        {
            InventoryRequirements = new ArrayList<>();
        }
        for(var ID : IDs)
        {
            InventoryRequirements.add(new Tuple2<>(ID, 1));
        }
    }

    public void SetFilter(InteractTask.InteractableFilter... Filter)
    {
        TargetFilter = EnumSet.noneOf(InteractTask.InteractableFilter.class);
        TargetFilter.addAll(Arrays.asList(Filter));
    }

    public void SetTargetArea(Area... areas)
    {
        TargetArea = areas;
    }

    public boolean hasInventoryRequirements()
    {
        for(var item : InventoryRequirements)
        {
            if(Inventory.count(item._1) < item._2)
            {
                return false;
            }
        }
        return true;
    }

    public void setDepositInventory(boolean depositInventory)
    {
        DepositInventory = depositInventory;
    }

    public void setWaitForFullInventory(boolean waitForFullInventory)
    {
        WaitForFullInventory = waitForFullInventory;
    }

    private TravelTask CreateTravelTask()
    {
        Area       targetArea   = Arrays.stream(TargetArea).findAny().get();
        TravelTask TravelToArea = new TravelTask("Travel to Area", targetArea.getRandomTile());
        TravelToArea.SetTaskPriority(0);
        TravelToArea.CompleteCondition = () -> targetArea.contains(Players.getLocal().getTile());
        TravelToArea.onReachedDestination.Subscribe(this, this::onTravelComplete);
        return TravelToArea;
    }

    private void StartCycle(IFScript Script)
    {
        interactTask = new InteractTask(GetName(), Action, Targets);
        interactTask.SetFilter(TargetFilter);
        if(WaitForFullInventory)
        {
            interactTask.SetWaitForInventory(true);
            interactTask.CompleteCondition = Inventory::isFull;
        }

        if(TargetArea != null)
        {
            GetScript().addNodes(CreateTravelTask());
        }

        Script.addNodes(interactTask);
    }

    private void onTravelComplete()
    {
        if(interactTask != null && interactTask.GetTarget() == null)
        {
            GetScript().addNodes(CreateTravelTask());
        }
    }

    /**
     * @return
     */
    @Override
    public boolean isCycleFinished(IFScript Script)
    {
        return Inventory.isFull();
    }

    /**
     * @param Script
     *
     * @return if cycle has successfully started
     */
    @Override
    public boolean onStart(IFScript Script)
    {
        StartCycle(Script);
        if(InventoryRequirements != null && !hasInventoryRequirements())
        {
            Script.addNodes(new InventoryCheckTask(this.GetName() + " Requirements",
                                                   InventoryRequirements.toArray(new Tuple2[0])));
        }
        return super.onStart(Script);
    }

    @Override
    public boolean onEnd(IFScript Script)
    {
        Logger.log("InteractCycle: OnEnd");
        if(DepositInventory)
        {
            if(InventoryRequirements == null)
            {
                Script.addNodes(BankItemsTask.FullDepositInventory());
            }
            else
            {
                Script.addNodes(BankItemsTask.FullDepositInventory(InventoryRequirements.toArray(new Tuple2[0])));
            }
        }
        return super.onEnd(Script);
    }

    /**
     * @param Script
     *
     * @return
     */
    @Override
    public boolean onRestart(IFScript Script)
    {
        StartCycle(Script);
        return true;
    }
}
