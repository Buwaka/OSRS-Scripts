package Cycles.SimpleTasks.Bank;

import Utilities.Combat.MeleeCombat;
import Utilities.OSRSUtilities;
import Utilities.Scripting.SimpleTask;
import io.vavr.Tuple2;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.utilities.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;

public class BankItemsTask extends SimpleTask
{
    protected final ArrayList<OSRSUtilities.BankEntry> Withdraws = new ArrayList<>();
    protected final ArrayList<OSRSUtilities.BankEntry> Deposits  = new ArrayList<>();
    private         BankLocation                       Location  = null;

    public BankItemsTask(String Name)
    {
        super(Name);
    }

    public boolean AddDeposit(int ID, int Amount, int Tab)
    {
        return Deposits.add(new OSRSUtilities.BankEntry(ID, Amount, Tab));
    }

    public boolean AddDeposit(int ID, int Amount)
    {
        return Deposits.add(new OSRSUtilities.BankEntry(ID, Amount));
    }

    public boolean AddDepositAll(int ID) {return Deposits.add(new OSRSUtilities.BankEntry(ID, -1));}

    public boolean AddDepositAll() {return Deposits.add(new OSRSUtilities.BankEntry(-1, -1));}

    public void AddEquipment(MeleeCombat.Equipment equipment)
    {
        for(var equip : equipment.equip.entrySet())
        {
            if(!Bank.contains(equip.getValue().id))
            {
                continue;
            }

            AddWithdraw(equip.getValue().id, equip.getValue().stackable ? -1 : 1);
        }
    }

    public boolean AddWithdraw(int ID, int Amount)
    {
        return Withdraws.add(new OSRSUtilities.BankEntry(ID, Amount));
    }

    public boolean AddWithdraw(int ID, int Amount, int Tab)
    {
        return Withdraws.add(new OSRSUtilities.BankEntry(ID, Amount, Tab));
    }

    public boolean AddWithdraw(int... IDs)
    {
        boolean result = true;
        for(var ID : IDs)
        {
            result &= AddWithdraw(ID);
        }
        return result;
    }

    public boolean AddWithdraw(int ID)
    {
        return Withdraws.add(new OSRSUtilities.BankEntry(ID, 1));
    }

    public boolean AddWithdrawAll(int ID)
    {
        return Withdraws.add(new OSRSUtilities.BankEntry(ID, -1));
    }

    public boolean AddWithdrawAll(int... IDs)
    {
        boolean result = true;
        for(var ID : IDs)
        {
            result &= AddWithdrawAll(ID);
        }
        return result;
    }

    public boolean AddWithdrawAllNoted(int... IDs)
    {
        boolean result = true;
        for(var ID : IDs)
        {
            result &= AddWithdrawAllNoted(ID);
        }
        return result;
    }

    public boolean AddWithdrawAllNoted(int ID)
    {
        return Withdraws.add(new OSRSUtilities.BankEntry(ID, -1, true));
    }

    public boolean AddWithdrawNoted(int ID, int Amount)
    {
        return Withdraws.add(new OSRSUtilities.BankEntry(ID, Amount, true));
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
        Logger.log("BankItemsTask: FillInventory: tuples: " + Arrays.toString(out));
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
        Logger.log("BankItemsTask: FillInventory: RatioTotal " + RatioTotal);

        for(int i = 0; i < IDRatios.length; i++)
        {
            var item = IDRatios[i];

            if(i + 1 == IDRatios.length)
            {
                Logger.log("BankItemsTask: FillInventory: WithdrawAll  " + item._1);
                AddWithdrawAll(item._1);
            }
            else
            {
                int count = (int) Math.floor(
                        (OSRSUtilities.InventorySpace) / (double) (RatioTotal) * item._2);
                Logger.log("BankItemsTask: FillInventory: AddWithdraw  " + item._1 + " " + count);
                AddWithdraw(item._1, count);
            }

        }

        return true;
    }

    public void SetSpecificBank(BankLocation Location)
    {
        this.Location = Location;
    }

    public static BankItemsTask FullDepositInventory()
    {
        BankItemsTask out = new BankItemsTask("Deposit Inventory");
        out.AddDepositAll();
        return out;
    }

    public static BankItemsTask FullDepositInventory(int... except)
    {
        Tuple2<Integer, Integer>[] out = new Tuple2[except.length];
        for(int i = 0; i < except.length; i++)
        {
            out[i] = new Tuple2<>(except[i], 1);
        }
        return FullDepositInventory(out);
    }

    public static BankItemsTask FullDepositInventory(Tuple2<Integer, Integer>[] except)
    {
        BankItemsTask out = new BankItemsTask(
                "Deposit Inventory, except " + Arrays.toString(except));

        out.AddDepositAll();
        for(var item : except)
        {
            out.AddWithdraw(item._1, item._2);
        }

        return out;
    }

    public static BankItemsTask SimpleWithdraw(int... IDs)
    {
        BankItemsTask out = new BankItemsTask("Deposit Inventory");
        out.AddWithdraw(IDs);
        return out;
    }

    @Override
    public boolean Ready()
    {
        //Logger.log("BankTask: " + Arrays.toString(Withdraws.toArray()) + Arrays.toString(Deposits.toArray()) + OSRSUtilities.CanReachBank(Location));
        return (!Withdraws.isEmpty() || !Deposits.isEmpty()) && super.Ready();
    }

    @Override
    public int Loop()
    {
        Bank.open();
        if(Bank.isOpen())
        {
            Withdraws.sort((x, y) -> Integer.compare(y.GetCount(), x.GetCount()));
            OSRSUtilities.ProcessBankEntries(GetScript(),
                                             Deposits,
                                             Withdraws,
                                             OSRSUtilities.WaitTime(GetScriptIntensity()));
            OSRSUtilities.BankClose();
            return 0;
        }

        return super.Loop();
    }

    @Nonnull
    @Override
    public TaskType GetTaskType()
    {
        return TaskType.BankItems;
    }
}
