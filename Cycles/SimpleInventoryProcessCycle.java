package Cycles;

import Cycles.SimpleTasks.Bank.BankItemsTask;
import Cycles.SimpleTasks.ItemProcessing.InteractInventoryTask;
import Cycles.SimpleTasks.TravelTask;
import Utilities.OSRSUtilities;
import Utilities.Scripting.SimpleCycle;
import Utilities.Scripting.tpircSScript;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.dialogues.Dialogues;

@JsonTypeName("SimpleInventoryProcessCycle")
public class SimpleInventoryProcessCycle extends SimpleCycle
{
    private           String                Action           = null;
    private           int                   SourceItemID;
    private transient InteractInventoryTask Interacttask     = null;
    private transient BankItemsTask         BankTask         = null;
    private transient boolean               InteractComplete = false;

    public SimpleInventoryProcessCycle(String name, int ItemID)
    {
        super(name);
        SourceItemID = ItemID;
    }

    public SimpleInventoryProcessCycle(String name, int ItemID, String action)
    {
        super(name);
        Action       = action;
        SourceItemID = ItemID;
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

    private void StartCycle(tpircSScript Script)
    {
        Interacttask     = null;
        BankTask         = null;
        InteractComplete = false;

        if(!OSRSUtilities.CanReachBank())
        {
            TravelTask Travel = new TravelTask("", BankLocation.getNearest().getTile());
            Travel.SetTaskName("Travel To Bank For ItemRequirements");
            Travel.TaskPriority.set(0);
            Travel.CompleteCondition = OSRSUtilities::CanReachBank;
            Script.addNodes(Travel);
        }

        BankTask = new BankItemsTask("Grabbing items to combine");
        if(!Inventory.isEmpty())
        {
            BankTask.DepositAll();
        }
        BankTask.WithdrawAll(SourceItemID);

        Script.addNodes(BankTask, CreateInteractTask());
    }

    private InteractInventoryTask CreateInteractTask()
    {
        Interacttask                 = new InteractInventoryTask("Interacting with items", Action, SourceItemID);
        Interacttask.AcceptCondition = () -> !BankTask.isActive();
        Interacttask.onComplete.Subscribe(this, () -> InteractComplete = true);
        return Interacttask;
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
