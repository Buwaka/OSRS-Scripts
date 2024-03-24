import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.interactive.Entity;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;

import java.util.AbstractMap;


@ScriptManifest(name = "ShrimpBoyleScript", description = "Draynor Village Shrimp fishing and baking script", author = "Semanresu",
        version = 1.0, category = Category.FISHING, image = "")
public class ShrimpBoyleScript extends AbstractScript {

    public enum States{
        TravelToFishSpot,
        Fish,
        TravelToFire,
        Bake,
        TravelToBank,
        Bank
    }

    final int FishSpotID = 1525;
    final int FireID = 43475;

    final String FishAction = "Small Net";
    final Tile FireLocation = new Tile(3096, 3237);
    final Tile BankLocation = new Tile(3094, 3243);
    final Tile FishLocation = new Tile(3086, 3230);

    final int ShrimpID = 317;
    final int NetID = 303;
    final int CookAction = 1;


    States LastState = States.TravelToFishSpot;
    public States GetState()
    {
        States out;
        if(Inventory.isFull() && Inventory.contains(ShrimpID))
        {
            GameObject Fire = GameObjects.closest(FireID);
            if(Fire.isOnScreen())
            {
                out = States.Bake;
            }
            else
            {
                out = States.TravelToFire;
            }
        }
        else if (Inventory.isFull() || !Inventory.contains(NetID))
        {
            if (OSRSUtilities.CanReachBank())
            {
                out = States.Bank;
            }
            else
            {
                out = States.TravelToBank;
            }
        }
        else
        {
            NPC FishSpot = NPCs.closest(FishSpotID);
            Logger.log(FishSpot);
            Logger.log(FishSpot.isOnScreen());
            if(FishSpot != null && FishSpot.isOnScreen())
            {
                out = States.Fish;
            }
            else
            {
                out = States.TravelToFishSpot;
            }
        }

        if(LastState != out)
        {
            LastState = out;
            Logger.log("Transitioning to state: " + out);
        }

        return out;
    }



    @Override
    public int onLoop() {
        final States State = GetState();

        switch (State)
        {
            case TravelToFishSpot -> {
                OSRSUtilities.WalkTo(FishLocation);
            }
            case Fish -> {
                OSRSUtilities.Fish(FishAction, FishSpotID);
            }
            case TravelToFire -> {
                OSRSUtilities.WalkTo(FireLocation);
            }
            case Bake -> {
                while(Inventory.contains(ShrimpID))
                {
                    OSRSUtilities.PickSkillingMenuItem(CookAction);
                    OSRSUtilities.WaitForEndAnimationLoop(1500, 10000);
                }

            }
            case TravelToBank -> {
                OSRSUtilities.WalkTo(BankLocation);
            }
            case Bank -> {
                OSRSUtilities.BankDepositAll(NetID);
                if(!Inventory.contains(NetID))
                {
                    OSRSUtilities.BankWithdraw(new AbstractMap.SimpleEntry<Integer, Integer>(NetID, 1));
                }
            }
        }


        return 0;
    }
}
