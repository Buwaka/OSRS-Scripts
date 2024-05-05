package Cycles.AdvanceTasks;

import Cycles.SimpleTasks.TravelTask;
import Utilities.OSRSUtilities;
import Utilities.Scripting.SimpleTask;
import Utilities.Scripting.tpircSScript;
import org.dreambot.api.methods.container.impl.bank.BankLocation;

import javax.annotation.Nonnull;

public class OpenBankTask extends SimpleTask
{
    TravelTask travelToBank = null;
    private BankLocation                                    BankingLocation  = null;
    public OpenBankTask()
    {
        super("Opening Bank");
    }

    public void SetBankLocation(BankLocation loc)
    {
        BankingLocation = loc;
    }

    BankLocation GetBankLocation()
    {
        if(BankingLocation != null)
        {
            return BankingLocation;
        }
        else if(BankLocation.getNearest() != null)
        {
            return BankLocation.getNearest();
        }
        return BankLocation.LUMBRIDGE;
    }

    @Override
    public boolean onStartTask(tpircSScript Script)
    {
        if(!OSRSUtilities.CanReachBank())
        {
            travelToBank = new TravelTask("Travel to Bank", GetBankLocation().getCenter());
            travelToBank.CompleteCondition = OSRSUtilities::CanReachBank;
        }
        return super.onStartTask(Script);
    }

    @Override
    public int Loop()
    {
        if(travelToBank != null && travelToBank.IsAlive())
        {
            return travelToBank.execute();
        }
        else
        {
            return OSRSUtilities.OpenBank() ? 0 : super.Loop();
        }
    }

    /**
     * @return
     */
    @Nonnull
    @Override
    public TaskType GetTaskType()
    {
        return TaskType.OpenBank;
    }
}
