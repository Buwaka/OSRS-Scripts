import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.Character;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.items.Item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;


@ScriptManifest(name = "CowSlaughterScript", description = "Collect Cow remains and slaughter them if necessary", author = "Semanresu",
        version = 1.0, category = Category.CRAFTING, image = "")
public class CowSlaughterScript extends AbstractScript {

    final Boolean FocusCombatEXP = true;
    final Boolean AlwaysHealUp = true;
    final Boolean PickUpCowHide = true;
    final Boolean PickupBeef = false;
    final Boolean PickupBones = true;
    final Boolean PrayBones = true;

    int[] InventoryExcepts = {};

    final int MinHealthPercent = 10;
    final int PickupDistance = 20;
    final int MinPickupAvailable = 5;

    final Area CowArea = new Tile(3259, 3277, 0).getArea(5);
    final Area BankArea = Area.generateArea(3, new Tile(3208, 3219,2));
    final String CowName = "Cow";
    final String BonesAction = "Bury";
    final String AttackAction = "Attack";
    final String PickupAction = "";
    final int CowHideID = 1739;
    final int BeefID = 2132;
    final int BonesID = 526;

    public enum States{
        TravelToCows,
        Pickup,
        Pray,
        FightCows,
        TravelToBank,
        Banking,
        Healing
    }

    Thread Ranomizer;
    @Override
    public void onStart() {
        super.onStart();
        Ranomizer = OSRSUtilities.StartRandomizerThread();
    }

    public List<GroundItem> GetSurroundingPickups()
    {
        for (GroundItem item : GroundItems.all()) {
            Logger.log(item);
        }
        List<GroundItem> Items = GroundItems.all(t -> {
            boolean result = t.walkingDistance(Players.getLocal().getTile()) < PickupDistance;

            if(PickupBones && t.getID() == BonesID)
            {
                return result;
            }
            else if(PickUpCowHide && t.getID() == CowHideID)
            {
                return result;
            }
            else if(PickupBeef && t.getID() == BeefID)
            {
                return result;
            }

            return false;
        } );

        Items.sort(Comparator.comparingDouble(p -> {
            double dist = ((GroundItem)p).walkingDistance(Players.getLocal().getTile());
            return Math.abs(dist);
        }));
        for (GroundItem item : Items) {
            Logger.log(item);
        }
        return Items;
    }

    States LastState = States.TravelToCows;

    public States GetState()
    {
        States out = LastState;

        if(Inventory.contains(BonesID) && PrayBones)
        {
            out = States.Pray;
        }
        else if(Players.getLocal().getHealthPercent() < MinHealthPercent || Inventory.isFull())
        {
            if(OSRSUtilities.CanReachBank())
            {
                if(!Inventory.onlyContains(InventoryExcepts))
                {
                    out = States.Banking;
                }
                else if (Players.getLocal().getHealthPercent() < MinHealthPercent)
                {
                    out = States.Healing;
                }
            }
            else
            {
                out = States.TravelToBank;
            }
        }
        else if(CowArea.contains(Players.getLocal().getTile()) || NPCs.closest(CowName) != null)
        {
            var Items = GetSurroundingPickups();
            if(Items.size() > MinPickupAvailable && !FocusCombatEXP)
            {
                out = States.Pickup;
            }
            else
            {
                out = States.FightCows;
            }
        }
        else
        {
            out = States.TravelToCows;
        }

        if(out != LastState)
        {
            Logger.log("Transitioning to state: " + out.toString());
            LastState = out;
        }

        return out;
    }

    public List<Integer> GetPickupList()
    {
        List<Integer> ToPickup = new ArrayList<>();
        if(PickupBones)
        {
            ToPickup.add(BonesID);
        }
        if(PickupBeef)
        {
            ToPickup.add(BeefID);
        }
        if(PickUpCowHide)
        {
            ToPickup.add(CowHideID);
        }
        return ToPickup;
    }

    @Override
    public int onLoop() {

        States State = GetState();

        switch (State) {
            case TravelToCows -> {
                OSRSUtilities.SimpleWalkTo(CowArea.getRandomTile());
            }
            case Pickup -> {
                var Items = GetSurroundingPickups();
                OSRSUtilities.PickupItems(Items);
            }
            case Pray -> {
                while(Inventory.contains(BonesID))
                {
                    Inventory.get(BonesID).interact(BonesAction);
                    OSRSUtilities.Wait(500,200);
                }
            }
            case FightCows -> {
                Character Cow = Players.getLocal().getInteractingCharacter();
                if(Cow == null)
                {
                    Cow = NPCs.closest(t -> Objects.equals(t.getName(), CowName) && t.hasAction(AttackAction) && !t.isInteractedWith() && t.canAttack());

                    Cow.interact(AttackAction);
                    Sleep.sleepTicks(3);
                }

                Logger.log("Attacking: " + Cow.toString());

                Tile LastTile = Cow.getTile();
                while (Players.getLocal().isMoving() || Cow.isInteracting(Players.getLocal()))
                {
                    if(Players.getLocal().getHealthPercent() < MinHealthPercent)
                    {
                        //Flee
                        OSRSUtilities.SimpleWalkTo(BankArea.getRandomTile());
                        OSRSUtilities.Wait();
                        break;
                    }
                    LastTile = Cow.getTile();
                    Sleep.sleepTicks(1);
                }

                Character finalCow = Cow;
                Sleep.sleepUntil(() -> !finalCow.exists(), 5000);
                Sleep.sleepTicks(3);

                var PickupList = GetPickupList();
                Logger.log(LastTile);
                OSRSUtilities.PickupOnArea(LastTile.getArea(3), PickupList);

            }
            case TravelToBank -> {
                OSRSUtilities.SimpleWalkTo(BankArea.getRandomTile());
            }
            case Banking -> {
                OSRSUtilities.BankDepositAll(InventoryExcepts);
            }
            case Healing -> {
                OSRSUtilities.BankHeal();
            }
        }
        return 0;
    }
}
