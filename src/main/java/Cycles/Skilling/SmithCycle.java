package Cycles.Skilling;

import Cycles.Tasks.AdvanceTasks.OpenBankTask;
import Cycles.Tasks.SimpleTasks.Bank.BankItemsTask;
import Cycles.Tasks.SimpleTasks.Skill.SmithTask;
import Cycles.Tasks.SimpleTasks.TravelTask;
import Utilities.Scripting.IFScript;
import Utilities.Scripting.SimpleCycle;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;

import java.util.Objects;

public class SmithCycle extends SimpleCycle
{
    private static final String       SmithAction     = "Smith";
    private static final int          HammerID        = 2347;
    private static final int          AnvilID         = 2097;
    private              String       TargetName;
    private              int          BarID;
    private              int          MinimumBarCount = 1;
    private transient    OpenBankTask OpenBank        = null;
    private transient    TravelTask   BackupTravel    = null;
    private transient    SmithTask    Smithtask       = null;
    private              Tile         BackupTile      = new Tile(3187, 3424, 0);
    private              Area         TargetArea      = new Area(3179, 3438, 3190, 3424);

    public SmithCycle(String name, String TargetName, int BarID)
    {
        super(name);
        this.TargetName = TargetName;
        this.BarID      = BarID;
    }

    public void setMinimumBarCount(int minimumBarCount)
    {
        MinimumBarCount = minimumBarCount;
    }

    @Override
    public boolean onEnd(IFScript Script)
    {
        Smithtask    = null;
        BackupTravel = null;

        if(Sleep.sleepUntil(() -> Bank.open(), 60000))
        {
            Bank.depositAllItems();
        }
        return super.onEnd(Script);
    }

    /**
     * @param Script
     *
     * @return
     */
    @Override
    public boolean onRestart(IFScript Script)
    {
        StartCycle(Script);
        return super.onRestart(Script);
    }

    private void StartCycle(IFScript Script)
    {
        if(Bank.count(BarID) < MinimumBarCount && Inventory.count(BarID) < MinimumBarCount)
        {
            Script.StopCurrentCycle();
            return;
        }

        OpenBankTask OpenBank = new OpenBankTask();

        BankItemsTask GetItems = new BankItemsTask("Get Bars");
        GetItems.AcceptCondition = () -> Bank.isOpen();
        GetItems.SetTaskPriority(0);
        if(!Inventory.isEmpty())
        {
            if(Inventory.contains(t -> t.getID() != HammerID &&
                                       !Objects.equals(t.getName(), TargetName)))
            {
                GetItems.AddDepositAll();
                GetItems.AddWithdraw(HammerID, 1);
            }
            else if(Inventory.contains(TargetName))
            {
                GetItems.AddDepositAll(Inventory.get(TargetName).getID());
            }
        }

        if(!Inventory.contains(HammerID))
        {
            Logger.log("Hammer");
            GetItems.AddWithdraw(HammerID, 1);
        }

        GetItems.FillInventory(BarID, 1);


        Smithtask = new SmithTask("Smith stuff", TargetName, SmithAction, AnvilID);

        Script.addNodes(OpenBank, GetItems, Smithtask);
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
        return Inventory.count(BarID) < MinimumBarCount && Bank.count(BarID) < MinimumBarCount;
    }

    @Override
    public boolean isCycleFinished(IFScript Script)
    {
        return Inventory.count(BarID) < MinimumBarCount;
    }

    /**
     * @param Script
     *
     * @return if cycle has successfully started
     */
    @Override
    public boolean onStart(IFScript Script)
    {
        if(!Bank.isCached())
        {
            OpenBank = new OpenBankTask();
            OpenBank.onComplete.Subscribe(this, () -> OpenBank = null);
            Script.addNodes(OpenBank);
        }
        if(OpenBank != null)
        {
            return false;
        }


        if(TargetArea.contains(Players.getLocal().getTile()))
        {
            StartCycle(Script);
            return super.onStart(Script);
        }
        else
        {
            BackupTravel                   = new TravelTask("Travel to Anvil", BackupTile);
            BackupTravel.AcceptCondition   = Bank::isCached;
            BackupTravel.CompleteCondition = () -> TargetArea.contains(Players.getLocal()
                                                                              .getTile());
            BackupTravel.onComplete.Subscribe(this, () -> StartCycle(Script));
            Script.addNodes(BackupTravel);
            return super.onStart(Script);
        }
    }
}
