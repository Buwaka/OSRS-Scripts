package Cycles.Tasks.SimpleTasks.ItemProcessing;

import Utilities.OSRSUtilities;
import Utilities.Scripting.IFScript;
import Utilities.Scripting.SimpleTask;
import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.Item;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class CombineTask extends SimpleTask
{
    final int retries = 10;
    int        sourceID;
    int        sourceRatio;
    int        targetID;
    int        targetRatio;
    int        counter             = 0;
    int        SkillingMenuIndex   = 1;
    int        timeout             = OSRSUtilities.Tick * 6;
    boolean    isCombining         = false;
    boolean    UseSkillingMenu     = true;
    AtomicLong lastInventoryChange = new AtomicLong(System.nanoTime());

    public CombineTask(String Name, int itemToUse, int itemToUseItOn, boolean useSkillingMenu)
    {
        super(Name);
        sourceID        = itemToUse;
        sourceRatio     = 1;
        targetID        = itemToUseItOn;
        targetRatio     = 1;
        UseSkillingMenu = useSkillingMenu;
    }

    public CombineTask(String Name, int itemToUse, int itemToUseRatio, int itemToUseItOn, int itemToUseItOnRatio, boolean useSkillingMenu)
    {
        super(Name);
        sourceID        = itemToUse;
        sourceRatio     = itemToUseRatio;
        targetID        = itemToUseItOn;
        targetRatio     = itemToUseItOnRatio;
        UseSkillingMenu = useSkillingMenu;
    }

    public void SetSkillingMenuIndex(int index)
    {
        SkillingMenuIndex = index;
    }

    @Override
    public boolean Ready()
    {
        return InventoryCheck() && super.Ready();
    }

    @Override
    public int Loop()
    {
        Logger.log("CombineTask: Execute: InventoryCheck " + InventoryCheck());
        if(!InventoryCheck())
        {
            Logger.log(
                    "CombineTask: Execute: Either done processing or no more items left to process");
            return 0;
        }

        if(isCombining)
        {
            Mouse.moveOutsideScreen(true);
            if(!TimeoutCheck())
            {
                return super.Loop();
            }
            Logger.log("CombineTask: Execute: Timeout, trying again");
        }

        var Source = GetSource();
        var Target = GetTarget();

        if(Source != null && Target != null && Source.useOn(GetTarget()))
        {
            if(UseSkillingMenu)
            {
                if(Sleep.sleepUntil(() -> OSRSUtilities.PickSkillingMenuItem(SkillingMenuIndex),
                                    OSRSUtilities.Tick * 30,
                                    OSRSUtilities.Tick))
                {
                    isCombining = true;
                    GetScript().onGameTick.WaitTicks(3);
                    return super.Loop();
                }
                Logger.log("CombineTask: Execute: Failed to pick skilling menu");
                return RetryCheck(super.Loop());
            }
            GetScript().onGameTick.WaitRandomTicks(2);
            return 1;
        }
        Logger.log("CombineTask: Execute: Failed to use item");

        return RetryCheck(super.Loop());
    }

    public Item GetSource()
    {
        return Inventory.get(sourceID);
    }

    public Item GetTarget()
    {
        return Inventory.get(targetID);
    }

    private int RetryCheck(int Super)
    {
        counter++;
        if(counter > retries)
        {
            Logger.log("CombineTask: RetryCheck: Failed all retries, exiting");
            return 0;
        }
        return Super;
    }

    private boolean TimeoutCheck()
    {
        boolean result = System.nanoTime() - lastInventoryChange.get() >
                         TimeUnit.MILLISECONDS.toNanos(timeout);
        Logger.log(
                "Timeoutcheck: " + System.nanoTime() + " - " + lastInventoryChange.get() + " > " +
                TimeUnit.MILLISECONDS.toNanos(timeout) + " = " + result);
        return result;
    }

    private boolean InventoryCheck()
    {
        Logger.log("CombineTask: InventoryCheck: " + Inventory.count(sourceID) + " " +
                   Inventory.count(targetID));
        return Inventory.count(sourceID) >= sourceRatio && Inventory.count(targetID) >= targetRatio;
    }

    @Nonnull
    @Override
    public TaskType GetTaskType()
    {
        return TaskType.Combine;
    }

    @Override
    public boolean onStartTask(IFScript Script)
    {
        lastInventoryChange.set(System.nanoTime());
        Script.onInventory.Subscribe(this, this::ResetInventoryTimer);
        return true;
    }

    private Boolean ResetInventoryTimer(IFScript.ItemAction action, Item item, Item item1)
    {
        Logger.log("ResetInventoryTimer");
        lastInventoryChange.set(System.nanoTime());
        return true;
    }

    void UseSkillingMenu(boolean Use)
    {
        UseSkillingMenu = Use;
    }
}
