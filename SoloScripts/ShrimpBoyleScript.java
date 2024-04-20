package SoloScripts;

import Utilities.OSRSUtilities;
import Utilities.Scripting.tpircSScript;
import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;

import java.awt.*;
import java.util.AbstractMap;


@ScriptManifest(name = "SoloScripts.ShrimpBoyleScript", description = "Draynor Village Shrimp fishing and baking script", author = "Semanresu", version = 1.0, category = Category.FISHING, image = "")
public class ShrimpBoyleScript extends tpircSScript
{

    final int     FishSpotID   = 1525;
    final int     FireID       = 43475;
    final String  FishAction   = "Small Net";
    final Tile    FireLocation = new Tile(3096, 3237);
    final Tile    BankLocation = new Tile(3094, 3243);
    final Tile    FishLocation = new Tile(3086, 3230);
    final int     ShrimpID     = 317;
    final int     AnchoviesID  = 321;
    final int     NetID        = 303;
    final int     CookAction   = 1;
    final boolean CookAncho    = true;
    final boolean CookShrimp   = false;
    States LastState = States.TravelToFishSpot;

    public States GetState()
    {
        States out;
        if(Inventory.isFull() &&
           ((Inventory.contains(ShrimpID) && CookShrimp) || (Inventory.contains(AnchoviesID) && CookAncho)))
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
        else if(Inventory.isFull() || !Inventory.contains(NetID))
        {
            if(OSRSUtilities.CanReachBank())
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
    public int onLoop()
    {
        final States State = GetState();

        switch(State)
        {
            case TravelToFishSpot ->
            {
                OSRSUtilities.SimpleWalkTo(FishLocation);
            }
            case Fish ->
            {
                OSRSUtilities.Fish(FishAction, FishSpotID);
            }
            case TravelToFire ->
            {
                OSRSUtilities.SimpleWalkTo(FireLocation);
            }
            case Bake ->
            {
                // Only bake ancho's for now
                while((Inventory.contains(ShrimpID) && CookShrimp) || (Inventory.contains(AnchoviesID) && CookAncho))
                {
                    if(Inventory.contains(ShrimpID) && CookShrimp)
                    {
                        Inventory.use(ShrimpID);
                    }
                    if(Inventory.contains(AnchoviesID) && CookAncho)
                    {
                        Inventory.use(AnchoviesID);
                    }

                    GameObject Fire = GameObjects.closest(FireID);
                    if(Fire != null && Fire.isOnScreen())
                    {
                        Point pt = Fire.getClickablePoint();
                        OSRSUtilities.RandomizeClick(pt, 2, 2);
                        Mouse.click(pt);
                        OSRSUtilities.Wait();
                        OSRSUtilities.PickSkillingMenuItem(CookAction);
                        OSRSUtilities.WaitForEndAnimationLoop(1500, 10000);
                    }
                    else
                    {
                        OSRSUtilities.SimpleWalkTo(FireLocation);
                        OSRSUtilities.Wait();
                    }
                }

            }
            case TravelToBank ->
            {
                OSRSUtilities.SimpleWalkTo(BankLocation);
            }
            case Bank ->
            {
                OSRSUtilities.BankDepositAll(NetID);
                if(!Inventory.contains(NetID))
                {
                    if(Bank.contains(NetID))
                    {
                        OSRSUtilities.BankWithdraw(new AbstractMap.SimpleEntry<Integer, Integer>(NetID, 1));
                    }
                    else
                    {
                        Logger.log("No small net found, stopping script.");
                        this.stop();
                    }
                }
            }
        }


        return 0;
    }


    public enum States
    {
        TravelToFishSpot,
        Fish,
        TravelToFire,
        Bake,
        TravelToBank,
        Bank
    }
}
