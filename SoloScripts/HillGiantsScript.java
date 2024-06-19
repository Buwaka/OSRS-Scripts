package SoloScripts;

import Utilities.Combat.CombatManager;
import Utilities.OSRSUtilities;
import Utilities.Scripting.tpircSScript;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.interactive.Character;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.GroundItem;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

@ScriptManifest(name = "SoloScripts.HillGiantsScript", description = "Slaughter and loot Hill Giants", author = "Semanresu", version = 1.0, category = Category.COMBAT, image = "")
public class HillGiantsScript extends tpircSScript
{

    final int     MinimumHP   = 15;
    final Boolean PrayBones   = false;
    final Boolean FocusPickup = true;

    final Area   GiantArea          = new Area(new Tile(3120, 9850), new Tile(3101, 9829));
    final Area   BankLocation       = new Tile(3183, 3437).getArea(2);
    final String HillGiantName      = "Hill Giant";
    final int[]  PickupExcludes     = {884, 1917, 527, 526, 1918};
    final int[]  PickupFocus        = {
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
    final int    BrassKeyID         = 983;
    final int    BigBonesID         = 532;
    final int    BakedSalmonID      = 329;
    final int    MinimumSalmonCount = 1;
    List<GroundItem> _pickups      = new ArrayList<GroundItem>();
    NPC              _closestGiant = null;
    States           LastState     = States.TravelToHillGiants;

    enum States
    {
        TravelToHillGiants,
        Fighting,
        FocusPickup,
        Praying,
        Healing,
        TravelToBank,
        Banking
    }

    @Override
    public int onLoop()
    {
        States State = GetState();

        switch(State)
        {
            case TravelToHillGiants ->
            {
                OSRSUtilities.SimpleWalkTo(GiantArea.getRandomTile());
            }
            case Fighting ->
            {
                if(!OSRSUtilities.SlaughterAndLoot(1000, 15000, PickupExcludes, HillGiantName) &&
                   !Players.getLocal().isMoving() && !Players.getLocal().isInteractedWith())
                {
                    OSRSUtilities.SimpleWalkTo(GiantArea.getRandomTile());
                    OSRSUtilities.ResetCameraRandom(1000);
                }

                if(!Players.getLocal().isInteractedWith() && OSRSUtilities.IsAreaBusy(10, true))
                {
                    OSRSUtilities.JumpToOtherWorld();
                }
            }
            case FocusPickup ->
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
                    CombatManager.GetInstance(Players.getLocal()).Fight(Foe);
                }
                //resume fight if possible
            }
            case Praying ->
            {
                OSRSUtilities.PrayAll(5000, BigBonesID);
            }
            case Healing ->
            {
                OSRSUtilities.Heal();
            }
            case TravelToBank ->
            {
                OSRSUtilities.SimpleWalkTo(BankLocation.getRandomTile());
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
                if(Inventory.count(BakedSalmonID) < MinimumSalmonCount)
                {
                    OSRSUtilities.BankWithdraw(new AbstractMap.SimpleEntry<Integer, Integer>(BakedSalmonID,
                                                                                             MinimumSalmonCount));
                }
                OSRSUtilities.BankClose();
            }
        }

        return 0;
    }

    States GetState()
    {
        States out;

        if(Players.getLocal().getHealthPercent() < MinimumHP)
        {
            if(!Inventory.contains(BakedSalmonID))
            {
                if(OSRSUtilities.CanReachBank())
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
        else if(!GetPickups().isEmpty())
        {
            out = States.FocusPickup;
        }
        else if(Inventory.contains(BigBonesID) && PrayBones)
        {
            out = States.Praying;
        }
        else if(Inventory.isFull())
        {
            if(OSRSUtilities.CanReachBank())
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
            NPC Giant = GetNearestHillGiant();
            if(Players.getLocal().isInCombat() || Giant != null)
            {
                out = States.Fighting;
            }
            else
            {
                out = States.TravelToHillGiants;
            }
        }

        if(out != LastState)
        {
            Logger.log("Transitioning to state: " + out);
            LastState = out;
        }

        return out;
    }

    List<GroundItem> GetPickups()
    {
//         caching
//        if(OSRSUtilities.IsTimeElapsed(Players.getLocal().getUID(), 1, 1000))
//        {
//            _pickups = GroundItems.all(t -> Arrays.stream(PickupFocus).anyMatch(x -> x == t.getID()));
//        }
        return _pickups;
    }

    NPC GetNearestHillGiant()
    {
        if(OSRSUtilities.IsTimeElapsed(Players.getLocal().getUID(), 300))
        {
            _closestGiant = OSRSUtilities.GetClosestAttackableEnemy(HillGiantName);
        }
        return _closestGiant;
    }
}

