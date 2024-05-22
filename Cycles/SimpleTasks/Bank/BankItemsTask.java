package Cycles.SimpleTasks.Bank;

import Utilities.OSRSUtilities;
import Utilities.Scripting.SimpleTask;
import io.vavr.Tuple2;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.utilities.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class BankItemsTask extends SimpleTask
{
    private final List<OSRSUtilities.BankEntry> Withdraws = new ArrayList<>();
    private final List<OSRSUtilities.BankEntry> Deposits  = new ArrayList<>();
    private       BankLocation                  Location  = null;
    private       Supplier<Boolean>             CompleteCondition;

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

    public boolean FillInventory(int... IDRatios)
    {
        if(IDRatios == null || IDRatios.length == 0)
        {
            Logger.log("BankItemsTask: FillInventory: IDRatio is empty or null");
            return false;
        }

        if(IDRatios.length % 2 != 0)
        {
            Logger.log("BankItemsTask: FillInventory: IDRatio is not even");
            return false;
        }

        ArrayList<Tuple2<Integer, Integer>> tuples = new ArrayList<>();
        for(int i = 0; i < IDRatios.length; i += 2)
        {
            var tuple = new Tuple2<>(IDRatios[i], IDRatios[i + 1]);
            tuples.add(tuple);
            //Logger.log(tuple);
        }

        Tuple2<Integer, Integer>[] out = tuples.toArray(new Tuple2[tuples.size()]);
        //Logger.log(Arrays.toString(out));
        return FillInventory(out);
    }

    public boolean FillInventory(Tuple2<Integer, Integer>... IDRatios)
    {
        if(IDRatios == null || IDRatios.length == 0)
        {
            Logger.log("BankItemsTask: FillInventory: IDRatio is empty or null");
            return false;
        }

        int RatioTotal = Arrays.stream(IDRatios).mapToInt(t -> t._2).sum();

        for(var item : IDRatios)
        {
            int count = (int) Math.floor((OSRSUtilities.InventorySpace) / (double) (RatioTotal) * item._2);
            AddWithdraw(item._1, count);
        }

        return true;
    }

    @Override
    public boolean Ready()
    {
        return OSRSUtilities.CanReachBank(Location) && (!Withdraws.isEmpty() || !Deposits.isEmpty()) && super.Ready();
    }

    @Override
    public int Loop()
    {
        Withdraws.sort((x, y) -> Integer.compare(y.GetCount(), x.GetCount()));
        OSRSUtilities.ProcessBankEntries(GetScript(), Deposits, Withdraws, OSRSUtilities.WaitTime(ScriptIntensity.get()));
        OSRSUtilities.BankClose();
        return 0;
    }

    @Nonnull
    @Override
    public TaskType GetTaskType()
    {
        return TaskType.BankItems;
    }

}
