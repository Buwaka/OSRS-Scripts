package Cycles;

import Cycles.AdvanceTasks.OpenBankTask;
import Cycles.SimpleTasks.Bank.BankItemsTask;
import Cycles.SimpleTasks.ItemProcessing.SmithTask;
import Cycles.SimpleTasks.ItemProcessing.UseObjectTask;
import Cycles.SimpleTasks.TravelTask;
import Utilities.Scripting.SimpleCycle;
import Utilities.Scripting.tpircSScript;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.interactive.GameObject;

import java.util.Objects;

public class SmithCycle extends SimpleCycle
{
    final String SmithAction = "Smith";
    int        HammerID = 2347;
    int        AnvilID  = 2097;
    GameObject Anvil    = null;
    String TargetName;
    int    BarID;
    private OpenBankTask OpenBank     = null;
    private TravelTask   BackupTravel = null;
    private Tile         BackupTile   = new Tile(3187, 3424, 0);

    public SmithCycle(String name, String TargetName, int BarID)
    {
        super(name);
        this.TargetName = TargetName;
        this.BarID      = BarID;
    }

    private void StartCycle(tpircSScript Script)
    {
        OpenBankTask OpenBank = new OpenBankTask();

        BankItemsTask GetItems = new BankItemsTask("Get Bars");
        GetItems.AcceptCondition = () -> !OpenBank.isActive();
        if(!Inventory.isEmpty())
        {
            if(Inventory.contains(t -> t.getID() != HammerID && !Objects.equals(t.getName(), TargetName)))
            {
                GetItems.DepositAll();
                GetItems.AddWithdraw(HammerID, 1);
            }
            else
            {
                GetItems.DepositAll(Inventory.get(TargetName).getID());
            }
        }

        if(!Inventory.contains(HammerID))
        {
            Logger.log("Hammer");
            GetItems.AddWithdraw(HammerID, 1);
        }

        GetItems.FillInventory(BarID, 1);


        SmithTask Smith = new SmithTask("Smith stuff", Anvil, TargetName, SmithAction);

        Script.addNodes(OpenBank, GetItems, Smith);
    }

    /**
     * @param Script
     *
     * @return if cycle has successfully started
     */
    @Override
    public boolean onStart(tpircSScript Script)
    {
        if(!Bank.isCached())
        {
            OpenBank = new OpenBankTask();
            OpenBank.onComplete.Subscribe(() -> OpenBank = null);
            Script.addNodes(OpenBank);
        }
        if(OpenBank != null)
        {
            return false;
        }

        Anvil = GameObjects.closest(AnvilID);

        if(Anvil != null)
        {
            StartCycle(Script);
            BackupTile = Anvil.getTile();
            return super.onStart(Script);
        }

        if(BackupTravel != null)
        {
            BackupTravel                   = new TravelTask("Travel to Anvil", BackupTile);
            BackupTravel.CompleteCondition = () -> GameObjects.closest(AnvilID) != null;
            Script.addNodes(BackupTravel);
            return false;
        }

        return super.onStart(Script);
    }

    /**
     * @param Script
     *
     * @return
     */
    @Override
    public boolean onRestart(tpircSScript Script)
    {
        StartCycle(Script);
        return super.onRestart(Script);
    }
}
