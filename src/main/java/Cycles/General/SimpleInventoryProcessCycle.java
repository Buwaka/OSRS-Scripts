package Cycles.General;

import Cycles.Tasks.SimpleTasks.Bank.BankItemsTask;
import Cycles.Tasks.SimpleTasks.ItemProcessing.InteractInventoryTask;
import Cycles.Tasks.SimpleTasks.TravelTask;
import Utilities.OSRSUtilities;
import Utilities.Scripting.IFScript;
import Utilities.Scripting.Logger;
import Utilities.Scripting.SimpleCycle;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.Item;

public class SimpleInventoryProcessCycle extends SimpleCycle
{
    private final     int                   SourceItemID;
    private           String                Action            = null;
    private           Integer               Tool              = null;
    private transient InteractInventoryTask InteractTask      = null;
    private transient BankItemsTask         BankTask          = null;
    private transient boolean               InteractComplete  = false;
    private transient boolean               InteractEveryItem = false;
    private transient boolean               Complete;


    public SimpleInventoryProcessCycle(String name, int ItemID)
    {
        super(name, null);
        SourceItemID = ItemID;
    }

    public SimpleInventoryProcessCycle(String name, int ItemID, int Tool)
    {
        super(name, null);
        SourceItemID = ItemID;
        this.Tool    = Tool;
    }

    public SimpleInventoryProcessCycle(String name, int ItemID, String action)
    {
        super(name, null);
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
    public int onLoop(IFScript Script)
    {
        if(InteractEveryItem)
        {
            final int[][] SlotOrder = new int[4][];
            SlotOrder[0] = new int[]{
                    4,
                    0,
                    1,
                    5,
                    6,
                    2,
                    3,
                    7,
                    11,
                    10,
                    14,
                    13,
                    9,
                    8,
                    12,
                    16,
                    20,
                    21,
                    17,
                    18,
                    22,
                    23,
                    19,
                    15,
                    24,
                    25,
                    26,
                    27};
            SlotOrder[1] = new int[]{
                    0,
                    1,
                    2,
                    3,
                    4,
                    5,
                    6,
                    7,
                    8,
                    9,
                    10,
                    11,
                    12,
                    13,
                    14,
                    15,
                    16,
                    17,
                    18,
                    19,
                    20,
                    21,
                    22,
                    23,
                    24,
                    25,
                    26,
                    27};
            SlotOrder[2] = new int[]{
                    0,
                    1,
                    2,
                    3,
                    7,
                    6,
                    5,
                    4,
                    8,
                    9,
                    10,
                    11,
                    15,
                    14,
                    13,
                    12,
                    16,
                    17,
                    18,
                    19,
                    23,
                    22,
                    21,
                    20,
                    24,
                    25,
                    26,
                    27};
            SlotOrder[3] = new int[]{
                    0,
                    4,
                    5,
                    1,
                    2,
                    6,
                    7,
                    3,
                    8,
                    12,
                    13,
                    9,
                    10,
                    14,
                    15,
                    11,
                    16,
                    20,
                    21,
                    17,
                    18,
                    22,
                    23,
                    19,
                    27,
                    26,
                    25,
                    24};


            int pick = OSRSUtilities.rand.nextInt(SlotOrder.length);
            for(int i = 0; i < OSRSUtilities.InventorySpace; i++)
            {
                int index = SlotOrder[pick][i];
                var item  = Inventory.getItemInSlot(index);
                if(item == null || item.getID() != SourceItemID)
                {
                    continue;
                }
                if(Action == null)
                {
                    Sleep.sleepUntil(() -> item.interact(), 1000);
                }
                else
                {
                    Sleep.sleepUntil(() -> item.interact(Action), 1000);
                }
            }

            int Attempts   = 0;
            int MaxAttempt = 5;

            while(Inventory.contains(SourceItemID) && Attempts < MaxAttempt)
            {
                var item = Inventory.get(SourceItemID);
                if(!item.interact())
                {
                    Attempts++;
                }
            }
        }
        else if(InteractComplete)
        {
            Logger.log("SimpleInventoryProcessCycle: onLoop: Interact Complete");
            //            if(InteractEveryItem && Inventory.contains(SourceItemID))
            //            {
            //                Logger.log(
            //                        "SimpleInventoryProcessCycle: onLoop: Interact every item, creating new task");
            //                Script.addNodes(CreateInteractTask());
            //            }
            if(Dialogues.inDialogue())
            {
                Logger.log(
                        "SimpleInventoryProcessCycle: onLoop: In Dialogue, creating new interact task");
                InteractComplete = false;
                Script.addNodes(CreateInteractTask());
            }
            else if(!Inventory.contains(SourceItemID))
            {
                Logger.log(
                        "SimpleInventoryProcessCycle: onLoop: no more source items in enventory, done");
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
            InteractTask = new InteractInventoryTask("Interacting with items",
                                                     Action,
                                                     SourceItemID);
        }
        else
        {
            if(!InteractEveryItem)
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
    public void onReset(IFScript Script)
    {
        StartCycle(Script);
        super.onReset(Script);
    }

    private void StartCycle(IFScript Script)
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
    public boolean onRestart(IFScript Script)
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
    public boolean isCycleComplete(IFScript Script)
    {
        return !Inventory.contains(SourceItemID);
    }

    @Override
    public boolean isCycleFinished(IFScript Script)
    {
        return !Bank.contains(SourceItemID) && !Inventory.contains(SourceItemID);
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
        return super.onStart(Script);
    }
}
