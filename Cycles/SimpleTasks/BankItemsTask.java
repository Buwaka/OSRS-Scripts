package Cycles.SimpleTasks;

import Utilities.OSRSUtilities;
import Utilities.Scripting.SimpleTask;
import org.dreambot.api.methods.container.impl.bank.BankLocation;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class BankItemsTask extends SimpleTask
{
    private final List<OSRSUtilities.BankEntry> Withdraws = new ArrayList<>();
    private final List<OSRSUtilities.BankEntry> Deposits  = new ArrayList<>();
    private       BankLocation                  Location  = null;
    private Supplier<Boolean> CompleteCondition;

    public BankItemsTask(String Name)
    {
        super(Name);
    }

    public void SetSpecificBank(BankLocation Location)
    {
        this.Location = Location;
    }

    public boolean AddDeposit(int ID, int Amount, int Tab)
    {
        return Deposits.add(new OSRSUtilities.BankEntry(ID, Amount, Tab));
    }

    public boolean AddDeposit(int ID, int Amount)
    {
        return Deposits.add(new OSRSUtilities.BankEntry(ID, Amount));
    }

    public boolean DepositAll(int ID) {return Deposits.add(new OSRSUtilities.BankEntry(ID, -1));}

    public boolean DepositAll()       {return Deposits.add(new OSRSUtilities.BankEntry(-1, -1));}

    public boolean AddWithdraw(int ID, int Amount, int Tab)
    {
        return Withdraws.add(new OSRSUtilities.BankEntry(ID, Amount, Tab));
    }

    public boolean AddWithdraw(int ID, int Amount)
    {
        return Withdraws.add(new OSRSUtilities.BankEntry(ID, Amount));
    }

    public boolean WithdrawAll(int ID)
    {
        return Withdraws.add(new OSRSUtilities.BankEntry(ID, -1));
    }

    @Override
    public boolean accept()
    {
        return OSRSUtilities.CanReachBank(Location) && (!Withdraws.isEmpty() || !Deposits.isEmpty()) && super.accept();
    }

    @Override
    public int execute()
    {
        OSRSUtilities.ProcessBankEntries(Deposits, Withdraws, OSRSUtilities.WaitTime(ScriptIntensity.get()));
        return 0;
    }

    @Nonnull
    @Override
    public TaskType GetTaskType()
    {
        return TaskType.BankItems;
    }

}
