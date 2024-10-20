package Scripts.OldScripts;

import Utilities.OSRSUtilities;
import Utilities.Scripting.IFScript;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.interactive.NPC;

import java.util.AbstractMap;

@ScriptManifest(name = "SoloScripts.SalmonScript", description = "Varrock Salmon Fishing and cooking script", author = "Semanresu", version = 1.0, category = Category.FISHING, image = "")
public class SalmonScript extends IFScript
{

    final int    FireID       = 43475;
    final int    RodFishingID = 1526;
    final int    RodID        = 309;
    final int    FeatherID    = 314;
    final int    RawSalmonID  = 331;
    final int    RawTroutID   = 335;
    final int    BurntFishID  = 343;
    final Tile   FishingTile  = new Tile(3103, 3430);
    final Area   BankLocation = new Tile(3183, 3437).getArea(2);
    final String FishAction   = "Lure";
    States LastState = States.Banking;

    enum States
    {
        Fishing,
        Cooking,
        TravelToBank,
        Banking,
        TravelToFish
    }

    @Override
    public int onLoop()
    {

        States State = GetState();

        switch(State)
        {
            case Fishing ->
            {
                NPC FishingSpot = GetFishingSpot();
                OSRSUtilities.Fish(FishAction, RodFishingID);
            }
            case Cooking ->
            {
                OSRSUtilities.Bake(RawSalmonID, RawTroutID);
                if(Inventory.contains(BurntFishID))
                {
                    Inventory.dropAll(BurntFishID);
                }
            }
            case TravelToBank ->
            {
                OSRSUtilities.SimpleWalkTo(BankLocation.getRandomTile());
            }
            case Banking ->
            {
                OSRSUtilities.BankDepositAll(RodID, FeatherID);
                if(!Inventory.contains(FeatherID))
                {
                    if(!OSRSUtilities.BankWithdrawAll(FeatherID))
                    {
                        Logger.log("No Feathers, exiting script");
                        this.stop();
                        return 0;
                    }
                }
                if(!Inventory.contains(RodID))
                {
                    OSRSUtilities.BankWithdraw(new AbstractMap.SimpleEntry<Integer, Integer>(RodID,
                                                                                             1));
                }

                OSRSUtilities.BankClose();
            }
            case TravelToFish ->
            {
                OSRSUtilities.SimpleWalkTo(FishingTile);
            }
        }
        return 0;
    }

    States GetState()
    {
        States out;
        NPC    FishingSpot = GetFishingSpot();
        if(!Inventory.contains(FeatherID) || !Inventory.contains(RodID))
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
        else if(Inventory.isFull() && !Inventory.contains(RawSalmonID) &&
                !Inventory.contains(RawTroutID))
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
        else if(FishingSpot != null && FishingSpot.canReach())
        {
            if(Inventory.isFull())
            {
                out = States.Cooking;
            }
            else
            {
                out = States.Fishing;
            }
        }
        else
        {
            out = States.TravelToFish;
        }

        if(LastState != out)
        {
            LastState = out;
            Logger.log("Transitioning to state: " + out);
        }

        return out;
    }

    NPC GetFishingSpot()
    {
        return NPCs.closest(RodFishingID);
    }
}
