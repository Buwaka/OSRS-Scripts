package Cycles.AdvanceTasks;

import Cycles.SimpleTasks.TravelTask;
import Utilities.OSRSUtilities;
import Utilities.Scripting.SimpleTask;
import Utilities.Scripting.tpircSScript;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.utilities.Logger;

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

    private BankLocation NearestThatIsntGE()
    {
        var all = BankLocation.getSortedValidLocations(Players.getLocal().getTile());
        return all.stream().filter(t -> t != BankLocation.GRAND_EXCHANGE).toList().getFirst();
    }

    BankLocation GetBankLocation()
    {
        if(BankingLocation != null)
        {
            return BankingLocation;
        }
        else if(NearestThatIsntGE() != null)
        {
            return NearestThatIsntGE();
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
        if(Bank.isOpen())
        {
            return 0;
        }

        if(travelToBank != null && travelToBank.isActive())
        {
            Logger.log("OpenBankTask: Travel");
            int result = travelToBank.execute();
            if(result == 0)
            {
                travelToBank = null;
                return OSRSUtilities.WaitTime(ScriptIntensity.get());
            }
            return result;
        }
        else
        {
            Logger.log("OpenBankTask: Open Bank");
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
