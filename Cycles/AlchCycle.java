package Cycles;

import Cycles.SimpleTasks.Bank.BankItemsTask;
import Cycles.SimpleTasks.ItemProcessing.AlchTask;
import OSRSDatabase.ItemDB;
import Utilities.Scripting.SimpleCycle;
import Utilities.Scripting.tpircSScript;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.grandexchange.LivePrices;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.items.Item;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@JsonTypeName("AlchCycle")
public class AlchCycle extends SimpleCycle
{
    private final     int            NatureRuneID   = 561;
    private final     int            FireStaffID    = 1387;
    public            int            ProfitMargin   = 50;
    public            int[]          TabsToConsider = {1};
    private           Instant        StartTime      = null;
    private           Item[]         ItemsToAlch    = null;
    private transient boolean        Started        = false;
    private transient List<AlchTask> Alchs          = new ArrayList<>();

    public AlchCycle(String name)
    {
        super(name);
    }

    /**
     * will be called once there are no active tasks anymore, aka a single cycle has been completed
     *
     * @param Script
     *
     * @return Cycle completed, ready for a restart
     */
    @Override
    public boolean isCycleComplete(tpircSScript Script)
    {
        return isDoneAlching();
    }

    public boolean isDoneAlching()
    {
        for(var task : Alchs)
        {
            if(!task.isFinished())
            {
                return false;
            }
        }
        return true;
    }

    /**
     * @return Whether the cycle needs to be restarted, aka goal hasn't been met yet
     */
    @Override
    public boolean isGoalMet()
    {
        return isDoneAlching() || super.isGoalMet();
    }

    /**
     * @param Script
     *
     * @return if cycle has successfully started
     */
    @Override
    public boolean onStart(tpircSScript Script)
    {
        ItemsToAlch = GetProfitableAlchs();

        BankItemsTask BankTask = new BankItemsTask("AlchCycle: onStart: Grabbing items to combine");
        if(!Inventory.isEmpty())
        {
            BankTask.DepositAll();
        }
        boolean FirestaffToEquip;
        var     CurrentEquip = Equipment.getItemInSlot(EquipmentSlot.WEAPON);
        if(CurrentEquip == null || CurrentEquip.getID() != FireStaffID)
        {
            if(CurrentEquip != null)
            {
                Equipment.unequip(EquipmentSlot.WEAPON);
                BankTask.AddDeposit(CurrentEquip.getID(), 1);
            }
            BankTask.AddWithdraw(FireStaffID, 1);
            FirestaffToEquip = true;
        }
        else {FirestaffToEquip = false;}

        BankTask.WithdrawAll(NatureRuneID);
        for(var item : ItemsToAlch)
        {
            BankTask.WithdrawAllNoted(item.getID());
        }
        BankTask.onComplete.Subscribe(this, () -> {
            if(FirestaffToEquip)
            {
                Inventory.get(FireStaffID).interact();
            }
            Started = true;
        });
        BankTask.TaskPriority.set(0);
        Script.addNodes(BankTask);

        for(var task : ItemsToAlch)
        {
            Logger.log("Alching " + task.getName());
            Script.addNodes(new AlchTask("Alching " + task.getName(), task.getNotedItemID()));
        }

        StartTime = Instant.now();

        return super.onStart(Script);
    }

    public Item[] GetProfitableAlchs()
    {
        List<Item> ItemsToAlch = new ArrayList<>();
        int        RunePrice   = LivePrices.get(NatureRuneID);
        for(var item : Bank.all())
        {
            if(item != null && ItemDB.isAlchable(item.getID()) && item.getAmount() > 3)
            {
                Logger.log("AlchCycle: GetProfitableAlchs: Checking price for " + item.getName());
                ItemDB.ItemData itemData  = ItemDB.GetItemData(item.getID());
                int             GEPrice   = LivePrices.get(item.getID());
                int             AlchPrice = itemData.highalch;
                int             profit    = (AlchPrice - RunePrice) - GEPrice;

                if(profit >= ProfitMargin)
                {
                    Logger.log("AlchCycle: GetProfitableAlchs: Alching " + item.getName() + "(GE: " + GEPrice +
                               ", AlchPrice: " + AlchPrice + ") with profit: " + profit);
                    ItemsToAlch.add(item);
                }
            }
        }

        return ItemsToAlch.toArray(new Item[0]);
    }

    /**
     * @param Script
     *
     * @return
     */
    @Override
    public boolean CanRestart(tpircSScript Script)
    {
        return false;
    }

    /**
     * @param Script
     *
     * @return
     */
    @Override
    public int onLoop(tpircSScript Script)
    {
        return super.onLoop(Script);
    }
}
