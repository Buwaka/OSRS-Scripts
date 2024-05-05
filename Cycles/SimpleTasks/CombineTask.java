package Cycles.SimpleTasks;

import Utilities.OSRSUtilities;
import Utilities.Scripting.SimpleTask;
import Utilities.Scripting.tpircSScript;
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
    Item       source              = null;
    Item       target              = null;
    int        counter             = 0;
    int     SkillingMenuIndex   = 1;
    int     timeout             = OSRSUtilities.Tick * 6;
    boolean    isCombining         = false;
    AtomicLong lastInventoryChange = new AtomicLong(System.nanoTime());

    public CombineTask(String Name, int itemToUse, int itemToUseItOn)
    {
        super(Name);
        sourceID    = itemToUse;
        sourceRatio = 1;
        targetID    = itemToUseItOn;
        targetRatio = 1;
    }

    public CombineTask(String Name, int itemToUse, int itemToUseRatio, int itemToUseItOn, int itemToUseItOnRatio)
    {
        super(Name);
        sourceID    = itemToUse;
        sourceRatio = itemToUseRatio;
        targetID    = itemToUseItOn;
        targetRatio = itemToUseItOnRatio;
    }

    public void SetSkillingMenuIndex(int index)
    {
        SkillingMenuIndex = index;
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

    private boolean InventoryCheck()
    {
        Logger.log(Inventory.count(sourceID) + " " + Inventory.count(targetID));
        return Inventory.count(sourceID) > sourceRatio && Inventory.count(targetID) > targetRatio;
    }

    private boolean TimeoutCheck()
    {
        boolean result = System.nanoTime() - lastInventoryChange.get() > TimeUnit.MILLISECONDS.toNanos(timeout);
        Logger.log("Timeoutcheck: " + System.nanoTime() + " - " + lastInventoryChange.get() + " > " + TimeUnit.MILLISECONDS.toNanos(timeout) + " = " + result);
        return result;
    }

    private void RefreshItems()
    {
        if(source == null)
        {
            source = Inventory.get(sourceID);
        }

        if(target == null)
        {
            target = Inventory.get(targetID);
        }
    }

    @Override
    public boolean onStartTask(tpircSScript Script)
    {
        lastInventoryChange.set(System.nanoTime());
        Script.onInventory.Subscribe(this::ResetInventoryTimer);

        RefreshItems();

        return true;
    }

    private Boolean ResetInventoryTimer(tpircSScript.ItemAction action, Item item, Item item1) {
        Logger.log("ResetInventoryTimer");
        lastInventoryChange.set(System.nanoTime());
        return true;
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
            Logger.log("CombineTask: Execute: Either done processing or no more items left to process");
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
            RefreshItems();
        }

        if(source.useOn(target))
        {
            GetScript().onGameTick.WaitTicks(2);
            if(Sleep.sleepUntil(() -> OSRSUtilities.PickSkillingMenuItem(SkillingMenuIndex),
                                OSRSUtilities.Tick * 30,
                                OSRSUtilities.Tick))
            {
                isCombining = true;
                GetScript().onGameTick.WaitTicks(6);
                return super.Loop();
            }
            Logger.log("CombineTask: Execute: Failed to pick skilling menu");
            return RetryCheck(super.Loop());
        }
        Logger.log("CombineTask: Execute: Failed to use item");

        return RetryCheck(super.Loop());
    }

    @Nonnull
    @Override
    public TaskType GetTaskType()
    {
        return TaskType.Combine;
    }
}
