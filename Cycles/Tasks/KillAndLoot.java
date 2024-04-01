package Cycles.Tasks;

import Utilities.OSRSUtilities;
import Utilities.tpircSScript;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.interactive.Character;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.GroundItem;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KillAndLoot extends tpircSScript
{
    public int MinimumHPPercent = 25;
    public Boolean PrayBones = true;
    public Boolean FocusPickup = true;

    private Area KillingArea = null;
    private Area BankArea = null;
    private String TargetName = "Hill Giant";
    private int[] TargetIDs = null;
    private int[] LootExcludeIDs = {884, 1917};
    private int[] LootFocusIDs = {
            563,
            560,
            561,
            564,
            225,
            1446,
            20754,
            13474,
            13475,
            10976,
            10977,
            1249,
            1263,
            3176,
            5716,
            5730,
            2366,
            1247,
            1261,
            830,
            985,
            987,
            1617,
            1462,
            1452,
            1619,
            1621,
            1623,
            22879,
            207,
            205,
            209,
            211,
            213,
            215,
            2485,
            217};

    private int[] InventoryRequirementIDs = null;
    final int BrassKeyID = 983;
    final int BigBonesID = 532;
    final int BakedSalmonID = 329;
    final int MinimumSalmonCount = 4;


    public enum States
    {
        TravelToTargetArea,
        Fighting,
        Collecting,
        Praying,
        Healing,
        TravelToBank,
        Banking
    }

    public void Init(Area TargetArea, BankLocation Bank, String TargetName, int[] InventoryRequirements, int[] LootExcludes, int[] LootFocus)
    {
        KillingArea = TargetArea;
        BankArea = Bank.getArea(3);
        this.TargetName = TargetName;
        InventoryRequirementIDs = InventoryRequirements;
        LootExcludeIDs = LootExcludes;
        LootFocusIDs = LootFocus;
    }

    public void Init(Area TargetArea, BankLocation Bank, int[] TargetIDs, int[] InventoryRequirements, int[] LootExcludes, int[] LootFocus)
    {
        KillingArea = TargetArea;
        BankArea = Bank.getArea(3);
        this.TargetIDs = TargetIDs;
        InventoryRequirementIDs = InventoryRequirements;
        LootExcludeIDs = LootExcludes;
        LootFocusIDs = LootFocus;
    }

    List<GroundItem> _pickups = new ArrayList<GroundItem>();

    List<GroundItem> GetPickups()
    {
        // caching
        if (OSRSUtilities.IsTimeElapsed(Players.getLocal().getUID(), 1, 1000))
        {
            _pickups = GroundItems.all(t -> Arrays.stream(LootFocusIDs).anyMatch(x -> x == t.getID()));
        }
        return _pickups;
    }

    NPC _closestTarget = null;

    NPC GetNearestTarget()
    {
        if (OSRSUtilities.IsTimeElapsed(Players.getLocal().getUID(), 2, 300))
        {
            if(TargetIDs != null && TargetIDs.length > 0)
            {
                _closestTarget = OSRSUtilities.GetClosestUnoccupiedEnemy(TargetIDs);
            }
            else
            {
                _closestTarget = OSRSUtilities.GetClosestUnoccupiedEnemy(TargetName);
            }

        }
        return _closestTarget;
    }


    private States LastState = States.TravelToTargetArea;

    States GetState()
    {
        States out;

        if (Players.getLocal().getHealthPercent() < MinimumHPPercent)
        {
            if(!Inventory.contains(BakedSalmonID))
            {
                if (OSRSUtilities.CanReachBank())
                {
                    out = States.Banking;
                }
                else
                {
                    out = States.TravelToBank;
                }
            }
            else
            {
                out = States.Healing;
            }
        }
        else if (!GetPickups().isEmpty())
        {
            out = States.Collecting;
        }
        else if (Inventory.contains(BigBonesID))
        {
            out = States.Praying;
        }
        else if (Inventory.isFull())
        {
            if (OSRSUtilities.CanReachBank())
            {
                out = States.Banking;
            }
            else
            {
                out = States.TravelToBank;
            }
        }
        else
        {
            NPC Giant = GetNearestTarget();
            if (Players.getLocal().isInCombat() || Giant != null)
            {
                out = States.Fighting;
            }
            else
            {
                out = States.TravelToTargetArea;
            }
        }

        if (out != LastState)
        {
            Logger.log("Transitioning to state: " + out);
            LastState = out;
        }

        return out;
    }

    @Override
    public int onLoop()
    {
        States State = GetState();

        switch (State)
        {
            case TravelToTargetArea ->
            {
                OSRSUtilities.SimpleWalkTo(KillingArea.getRandomTile());
            }
            case Fighting ->
            {
                if (!OSRSUtilities.SlaughterAndLoot(1000, 15000, LootExcludeIDs, TargetName)
                        && !Players.getLocal().isMoving()
                        && !Players.getLocal().isInteractedWith())
                {
                    OSRSUtilities.SimpleWalkTo(KillingArea.getRandomTile());
                    OSRSUtilities.ResetCameraRandom(1000);
                }

                if (!Players.getLocal().isInteractedWith() && OSRSUtilities.IsAreaBusy(10))
                {
                    OSRSUtilities.JumpToOtherWorld();
                }
            }
            case Collecting ->
            {
                Character Foe = null;
                if(Players.getLocal().isInteractedWith())
                {
                    Logger.log("Is fighting Foe, continue after pickup");
                    Foe = Players.getLocal().getCharacterInteractingWithMe();
                }

                OSRSUtilities.PickupItems(GetPickups());
                if(Foe != null && Foe.canAttack())
                {
                    Logger.log("Continue fighting " + Foe.toString());
                    OSRSUtilities.Fight(Foe);
                }
            }
            case Praying ->
            {
                OSRSUtilities.Pray(5000, BigBonesID);
            }
            case Healing ->
            {
                OSRSUtilities.Heal();
            }
            case TravelToBank ->
            {
                OSRSUtilities.SimpleWalkTo(BankArea.getRandomTile());
            }
            case Banking ->
            {
                if(Players.getLocal().getHealthPercent() < 80 && Inventory.contains(BakedSalmonID))
                {
                    OSRSUtilities.Healup();
                }

                //Utilities.OSRSUtilities.BankDepositAll(BrassKeyID, BakedSalmonID);
                OSRSUtilities.BankDepositAll();
                OSRSUtilities.BankWithdraw(new AbstractMap.SimpleEntry<>(BrassKeyID, 1));
                if (Inventory.count(BakedSalmonID) < MinimumSalmonCount)
                {
                    OSRSUtilities.BankWithdraw(new AbstractMap.SimpleEntry<Integer, Integer>(BakedSalmonID,
                                                                                             MinimumSalmonCount));
                }
                OSRSUtilities.BankClose();
            }
        }

        return 0;
    }
}
