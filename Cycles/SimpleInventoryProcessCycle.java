package Cycles;

import Cycles.SimpleTasks.Bank.BankItemsTask;
import Cycles.SimpleTasks.ItemProcessing.InteractInventoryTask;
import Cycles.SimpleTasks.TravelTask;
import Utilities.OSRSUtilities;
import Utilities.Scripting.SimpleCycle;
import Utilities.Scripting.tpircSScript;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.dialogues.Dialogues;

@JsonTypeName("SimpleInventoryProcessCycle")
public class SimpleInventoryProcessCycle extends SimpleCycle
{
    private           String                Action           = null;
    private           int                   SourceItemID;
    private           Integer               Tool             = null;
    private transient InteractInventoryTask InteractTask     = null;
    private transient BankItemsTask         BankTask         = null;
    private transient boolean               InteractComplete = false;
    private transient boolean               Complete;

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

    private InteractInventoryTask CreateInteractTask()
    {
        InteractTask = new InteractInventoryTask("Interacting with items", Action, SourceItemID);
        if(Tool != null)
        {
            InteractTask.setTool(Tool);
        }
        InteractTask.AcceptCondition = () -> !BankTask.isActive();
        InteractTask.onComplete.Subscribe(this, () -> InteractComplete = true);
        return InteractTask;
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
     * @param Script
     *
     * @return
     */
    @Override
    public int onLoop(tpircSScript Script)
    {
        if(InteractComplete)
        {
            if(Dialogues.inDialogue())
            {
                InteractComplete = false;
                Script.addNodes(CreateInteractTask());
            }
            else if(!Inventory.contains(SourceItemID))
            {
                return 0;
            }
        }
        return super.onLoop(Script);
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
