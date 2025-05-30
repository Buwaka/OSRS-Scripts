package Cycles.Skilling;

import Cycles.Tasks.SimpleTasks.Bank.BankItemsTask;
import Cycles.Tasks.SimpleTasks.ItemProcessing.AlchTask;
import OSRSDatabase.ItemDB;
import Utilities.OSRSUtilities;
import Utilities.Scripting.IFScript;
import Utilities.Scripting.SimpleCycle;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.grandexchange.LivePrices;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.items.Item;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class AlchCycle extends SimpleCycle
{
    private final     int              NatureRuneID   = 561;
    private final     int              FireStaffID    = 1387;
    public            int              ProfitMargin   = 50;
    public            int[]            TabsToConsider = {1};
    private           Instant          StartTime      = null;
    private           LinkedList<Item> ItemsToAlch    = null;
    private transient boolean          Started        = false;
    private transient List<AlchTask>   Alchs          = new ArrayList<>();

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
    public boolean isCycleComplete(IFScript Script)
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
     * @param Script
     *
     * @return true when Cycle is completely done and should/will be terminated, typically the same as isCycleComplete
     */
    @Override
    public boolean isCycleFinished(IFScript Script)
    {
        return ItemsToAlch.isEmpty();
    }

    /**
     * @param Script
     *
     * @return if cycle has successfully started
     */
    @Override
    public boolean onStart(IFScript Script)
    {
        ItemsToAlch = GetProfitableAlchs();

        BankItemsTask BankTask = new BankItemsTask("AlchCycle: onStart: Grabbing items to combine");
        if(!Inventory.isEmpty())
        {
            BankTask.AddDepositAll();
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

        BankTask.AddWithdrawAll(NatureRuneID);
        int MaxStacks = Math.min(OSRSUtilities.InventorySpace - 2, ItemsToAlch.size());
        for(int i = 0; i < MaxStacks; i++)
        {
            var item = ItemsToAlch.poll();
            Logger.log("Alching " + item.getName());
            Script.addNodes(new AlchTask("Alching " + item.getName(), item.getNotedItemID()));
            BankTask.AddWithdrawAllNoted(item.getID());
        }
        BankTask.onComplete.Subscribe(this, () -> {
            if(FirestaffToEquip)
            {
                Inventory.get(FireStaffID).interact();
            }
            Started = true;
        });
        BankTask.SetTaskPriority(0);
        Script.addNodes(BankTask);

        StartTime = Instant.now();

        return super.onStart(Script);
    }

    public LinkedList<Item> GetProfitableAlchs()
    {
        LinkedList<Item> ItemsToAlch = new LinkedList<>();
        int              RunePrice   = LivePrices.get(NatureRuneID);
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
                    Logger.log(
                            "AlchCycle: GetProfitableAlchs: Alching " + item.getName() + "(GE: " +
                            GEPrice + ", AlchPrice: " + AlchPrice + ") with profit: " + profit);
                    ItemsToAlch.add(item);
                }
            }
        }

        return ItemsToAlch;
    }

    /**
     * @param Script
     *
     * @return
     */
    @Override
    public int onLoop(IFScript Script)
    {
        return super.onLoop(Script);
    }
}
