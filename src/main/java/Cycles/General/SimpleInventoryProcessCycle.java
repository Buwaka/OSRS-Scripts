package Cycles.General;

import Cycles.Tasks.SimpleTasks.Bank.BankItemsTask;
import Cycles.Tasks.SimpleTasks.ItemProcessing.InteractInventoryTask;
import Cycles.Tasks.SimpleTasks.TravelTask;
import Utilities.OSRSUtilities;
import Utilities.Scripting.SimpleCycle;
import Utilities.Scripting.tpircSScript;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.items.Item;

public class SimpleInventoryProcessCycle extends SimpleCycle
{
    private           String                Action           = null;
    private           int                   SourceItemID;
    private           Integer               Tool             = null;
    private transient InteractInventoryTask InteractTask     = null;
    private transient BankItemsTask         BankTask         = null;
    private transient boolean               InteractComplete = false;
    private transient boolean InteractEveryItem = false;
    private transient boolean Complete;



    public SimpleInventoryProcessCycle(String name, int ItemID)
    {
        super(name);
        SourceItemID = ItemID;
    }

    public SimpleInventoryProcessCycle(String name, int ItemID, int Tool)
    {
        super(name);
        SourceItemID = ItemID;
        this.Tool    = Tool;
    }

    public SimpleInventoryProcessCycle(String name, int ItemID, String action)
    {
        super(name);
        Action       = action;
        SourceItemID = ItemID;
    }

    public void SetInteractEveryItem(boolean interactEveryItem)
    {
        InteractEveryItem = interactEveryItem;
    }

    /**
     * @param Script
     *
     * @return
     */
    @Override
    public int onLoop(tpircSScript Script)
    {
        if(InteractComplete)
        {
            Logger.log("SimpleInventoryProcessCycle: onLoop: Interact Complete");
            if(InteractEveryItem && Inventory.contains(SourceItemID))
            {
                Logger.log("SimpleInventoryProcessCycle: onLoop: Interact every item, creating new task");
                Script.addNodes(CreateInteractTask());
            }
            if(Dialogues.inDialogue())
            {
                Logger.log("SimpleInventoryProcessCycle: onLoop: In Dialogue, creating new interact task");
                InteractComplete = false;
                Script.addNodes(CreateInteractTask());
            }
            else if(!Inventory.contains(SourceItemID))
            {
                Logger.log("SimpleInventoryProcessCycle: onLoop: no more source items in enventory, done");
                return 0;
            }
        }
        return super.onLoop(Script);
    }

    private InteractInventoryTask CreateInteractTask()
    {
        Item item = Inventory.get(SourceItemID);
        if(item == null)
        {
            InteractTask = new InteractInventoryTask("Interacting with items", Action, SourceItemID);
        }
        else
        {
            if(InteractEveryItem)
            {
                for(var inv : Inventory.all())
                {
                    if(inv != null && inv.getID() == SourceItemID)
                    {
                        InteractTask = new InteractInventoryTask("Interacting with items", Action, inv);
                    }
                }
            }
            else
            {
                InteractTask = new InteractInventoryTask("Interacting with items", Action, item);
            }
        }

        if(Tool != null)
        {
            InteractTask.setTool(Tool);
        }
        InteractTask.AcceptCondition = () -> !BankTask.isActive();
        InteractTask.onComplete.Subscribe(this, () -> InteractComplete = true);
        return InteractTask;
    }

    /**
     * When all cycles have been completed and we want to do the cycle again, this is called
     *
     * @param Script
     */
    @Override
    public void onReset(tpircSScript Script)
    {
        StartCycle(Script);
        super.onReset(Script);
    }

    private void StartCycle(tpircSScript Script)
    {
        Complete         = false;
        InteractTask     = null;
        BankTask         = null;
        InteractComplete = false;

        if(!OSRSUtilities.CanReachBank())
        {
            TravelTask Travel = new TravelTask("", BankLocation.getNearest().getTile());
            Travel.SetTaskName("SIPC: Travel To Bank For ItemRequirements");
            Travel.SetTaskPriority(0);
            Travel.CompleteCondition = OSRSUtilities::CanReachBank;
            Script.addNodes(Travel);
        }

        BankTask = new BankItemsTask("Grabbing items to combine");
        if(!Inventory.isEmpty())
        {
            BankTask.AddDepositAll();
        }
        if(Tool != null)
        {
            BankTask.AddWithdraw(Tool, 1);
        }
        BankTask.AddWithdrawAll(SourceItemID);

        Script.addNodes(BankTask, CreateInteractTask());
    }

    /**
     * When a cycle has been completed, this will be called
     *
     * @param Script
     */
    @Override
    public boolean onRestart(tpircSScript Script)
    {
        StartCycle(Script);
        return super.onRestart(Script);
    }

    /**
     * will be called once there are no active tasks anymore, aka a single cycle has been completed
     *
     * @param Script
     *
     * @return true when Cycle is completed, ready for a restart
     */
    @Override
    public boolean isCycleComplete(tpircSScript Script)
    {
        return !Inventory.contains(SourceItemID);
    }

    @Override
    public boolean isCycleFinished(tpircSScript Script)
    {
        return !Bank.contains(SourceItemID) && !Inventory.contains(SourceItemID);
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
}
