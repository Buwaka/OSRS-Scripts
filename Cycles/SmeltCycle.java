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
import org.dreambot.api.wrappers.interactive.GameObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public class SmeltCycle extends SimpleCycle implements Serializable
{
    private static final int                        ForgingRingID   = 2568;
    private static       Tile                       BackupTile      = new Tile(3108, 3499, 0);
    private final        String                     SmeltAction     = "Smelt";
    public               AtomicInteger              FurnaceID       = new AtomicInteger(16469); // default furnace is the furnace in Edgeville
    public @Nullable     Boolean                    NeedForgingRing = null;
    private transient    GameObject                 Furnace         = null;
    private transient    Integer                    Amount          = null;
    private              Tuple2<Integer, Integer>[] ItemIDRatio     = null;
    private transient    TravelTask                 BackupTravel    = null;
    private              String                     TargetName      = null;
    private              OpenBankTask               OpenBank        = null;


    @SafeVarargs
    public SmeltCycle(String name, String SmeltTargetName, @Nonnull Tuple2<Integer, Integer>... Ores)
    {
        super(name);
        TargetName  = SmeltTargetName;
        ItemIDRatio = Ores;
    }

    public boolean HasRingOfForging()
    {
        return Inventory.contains(ForgingRingID) || Bank.contains(ForgingRingID);
    }

    private void StartCycle(tpircSScript Script)
    {
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

        UseObjectTask Smelt = new UseObjectTask("Smelt Ores", Furnace, TargetName, SmeltAction);

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

        Script.addNodes(OpenBank, GetItems, Smelt);
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

        if(NeedForgingRing != null && NeedForgingRing.booleanValue() && !HasRingOfForging())
        {
            Logger.log("SmeltCycle: onStart: No ring of forging, doing nothing");
            return super.onStart(Script);
        }

        Furnace = GameObjects.closest(FurnaceID.get());

        if(Furnace != null)
        {
            StartCycle(Script);
            BackupTile = Furnace.getTile();
            return super.onStart(Script);
        }

        if(BackupTravel != null)
        {
            BackupTravel                   = new TravelTask("Travel to backup Furnace", BackupTile);
            BackupTravel.CompleteCondition = () -> GameObjects.closest(FurnaceID.get()) != null;
            Script.addNodes(BackupTravel);
            return false;
        }

        return false;
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
}
