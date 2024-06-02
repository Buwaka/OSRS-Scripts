package Cycles;

import Cycles.AdvanceTasks.OpenBankTask;
import Cycles.SimpleTasks.Bank.BankItemsTask;
import Cycles.SimpleTasks.ItemProcessing.UseObjectTask;
import Cycles.SimpleTasks.Misc.EquipmentTask;
import Cycles.SimpleTasks.TravelTask;
import Utilities.Scripting.SimpleCycle;
import Utilities.Scripting.tpircSScript;
import io.vavr.Tuple2;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class SmeltCycle extends SimpleCycle implements Serializable
{
    private static final int                        ForgingRingID   = 2568;
    private final        String                     SmeltAction     = "Smelt";
    private           int                        FurnaceID         = 16469; // default furnace is the furnace in Edgeville
    public @Nullable  Boolean                    NeedForgingRing   = null;
    private           Tuple2<Integer, Integer>[] ItemIDRatio       = null;
    private final     Tile                       FurnaceBackupTile = new Tile(3108, 3498, 0);
    private transient TravelTask                 BackupTravel      = null;
    private           String                     TargetName        = null;
    private  UseObjectTask SmeltTask = null;


    @SafeVarargs
    public SmeltCycle(String name, String SmeltTargetName, @Nonnull Tuple2<Integer, Integer>... Ores)
    {
        super(name);
        TargetName  = SmeltTargetName;
        ItemIDRatio = Ores;
    }

    public int getFurnaceID()
    {
        return FurnaceID;
    }

    public void setFurnaceID(int furnaceID)
    {
        FurnaceID = furnaceID;
    }

    public boolean HasRingOfForging()
    {
        return Inventory.contains(ForgingRingID) || Bank.contains(ForgingRingID);
    }

    private void StartCycle(tpircSScript Script)
    {
        if(Arrays.stream(ItemIDRatio).anyMatch(t -> Bank.count(t._1) < t._2 && Inventory.count(t._1) < t._2))
        {
            Script.StopCurrentCycle();
            return;
        }

        OpenBankTask OpenBank = new OpenBankTask();

        BankItemsTask GetItems = new BankItemsTask("Get Ores");
        GetItems.AcceptCondition = () -> !OpenBank.isActive();
        if(!Inventory.isEmpty())
        {
            GetItems.DepositAll();
        }
        if(ItemIDRatio.length == 1)
        {
            GetItems.WithdrawAll(ItemIDRatio[0]._1);
        }
        else
        {
            GetItems.FillInventory(ItemIDRatio);
        }

        SmeltTask = new UseObjectTask("Smelt Ores", TargetName, SmeltAction, FurnaceID);

        if(NeedForgingRing != null && NeedForgingRing.booleanValue() && !Equipment.contains(ForgingRingID))
        {
            if(!Inventory.contains(ForgingRingID))
            {
                GetItems.AddWithdraw(ForgingRingID, 1);
            }

            EquipmentTask ForgingRingCheck = new EquipmentTask("Equip Forging Ring");
            ForgingRingCheck.TaskPriority.set(-1);
            ForgingRingCheck.AcceptCondition = () -> !Equipment.slotContains(EquipmentSlot.RING, ForgingRingID);
            ForgingRingCheck.Equip(EquipmentSlot.RING, ForgingRingID);

            Script.addNodes(ForgingRingCheck);
        }

        Script.addNodes(OpenBank, GetItems, SmeltTask);
    }

    /**
     * @param Script
     *
     * @return
     */
    @Override
    public boolean CanRestart(tpircSScript Script)
    {
        // TODO check ring of forging
        boolean itemRequirement = true;
        for(var item : ItemIDRatio)
        {
            itemRequirement &= Bank.count(item._1) >= item._2 || Inventory.count(item._1) >= item._2;
        }
        if(Boolean.TRUE.equals(NeedForgingRing))
        {
            itemRequirement &= HasRingOfForging();
        }

        return super.CanRestart(Script) && itemRequirement;
    }

    /**
     * @param Script
     *
     * @return if cycle has successfully started
     */
    @Override
    public boolean onStart(tpircSScript Script)
    {
        if(NeedForgingRing != null && NeedForgingRing.booleanValue() && !HasRingOfForging())
        {
            Logger.log("SmeltCycle: onStart: No ring of forging, doing nothing");
            return super.onStart(Script);
        }

        if(GameObjects.closest(FurnaceID) != null && GameObjects.closest(FurnaceID).canReach())
        {
            Logger.log("SmeltCycle: Can reach Furnace");
            StartCycle(Script);
        }
        else
        {
            Logger.log("SmeltCycle: Can't reach Furnace");
            BackupTravel = new TravelTask("Travel to backup Furnace", FurnaceBackupTile);
            BackupTravel.CompleteCondition = () -> UseObjectTask.GetObjectStatic(FurnaceID) != null && UseObjectTask.GetObjectStatic(FurnaceID).canReach();
            BackupTravel.onComplete.Subscribe(this, () -> {
                if(UseObjectTask.GetObjectStatic(FurnaceID) != null)
                {
                    StartCycle(Script);
                }
                else
                {
                    this.EndNow(Script);
                }
            });
            Script.addNodes(BackupTravel);
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
        // TODO check ring of forging
        StartCycle(Script);
        return true;
    }

    @Override
    public void onReset(tpircSScript Script)
    {
        BackupTravel = null;
        SmeltTask      = null;
    }

    /**
     * End cycle after current cycle has finished
     *
     * @param Script
     *
     * @return if cycle has successfully ended
     */
    @Override
    public boolean onEnd(tpircSScript Script)
    {
        if(Sleep.sleepUntil(()->Bank.open(), 60000))
        {
            Bank.depositAllItems();
        }
        return super.onEnd(Script);
    }
}
