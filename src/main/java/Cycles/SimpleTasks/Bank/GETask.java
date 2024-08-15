package Cycles.SimpleTasks.Bank;

import Utilities.Scripting.SimpleTask;
import Utilities.Scripting.tpircSScript;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.interactive.Players;

import javax.annotation.Nonnull;

public class GETask extends SimpleTask
{

    private BankItemsTask GetItems = null;

    public GETask(String Name)
    {
        super(Name);
    }

    /**
     * @return
     */
    @Override
    public boolean Ready()
    {
        return GetScript().GetGEInstance().HasQueuedActions() && super.Ready();
    }

    /**
     * @return
     */
    @Override
    protected int Loop()
    {
        if(GetItems != null && GetItems.isActive())
        {
            return super.Loop();
        }

        if(!GrandExchange.isOpen())
        {
            GrandExchange.open();
            return super.Loop();
        }
        else
        {
            return GetScript().GetGEInstance().tick();
        }
    }

    /**
     * @return
     */
    @Nonnull
    @Override
    public TaskType GetTaskType()
    {
        return TaskType.GETask;
    }

    /**
     * @param Script
     *
     * @return return true if successful, false if we need more time, keep triggering start until it is ready
     */
    @Override
    public boolean onStartTask(tpircSScript Script)
    {
        var ItemRequirements = GetScript().GetGEInstance().GetAllOrderRequirements();
        if(GetItems == null && ItemRequirements != null && ItemRequirements.length > 0)
        {
            GetItems = new BankItemsTask("Noted items for GE");
            for(var item : ItemRequirements)
            {
                GetItems.AddWithdrawNoted(item._1, item._2);
            }
            GetItems.SetTaskPriority(priority() - 1);
            GetItems.AcceptCondition = () -> BankLocation.getNearest().distance(Players.getLocal().getTile()) < 10;
            GetScript().addNodes(GetItems);
        }

        return super.onStartTask(Script);
    }
}
