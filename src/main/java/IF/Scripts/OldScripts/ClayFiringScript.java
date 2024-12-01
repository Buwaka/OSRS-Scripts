package IF.Scripts.OldScripts;

import IF.Utilities.OSRSUtilities;
import IF.Utilities.Scripting.IFScript;
import IF.Utilities.Scripting.Logger;
import org.dreambot.api.Client;
import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;

import java.awt.*;

@ScriptManifest(name = "ClayFiringScript", description = "Pottery Crafting Script, Varrock west bank to barbarian klin", author = "Semanresu", version = 1.0, category = Category.CRAFTING, image = "")
class ClayFiringScript extends AbstractScript
{

    final Tile PottersLocation      = new Tile(3086, 3410);
    final Tile PottersWheelLocation = new Tile(3087, 3410);
    final int  PottersWheelID       = 14887;
    final int  PottingAction        = 2;
    final Area BankLocation         = new Tile(3183, 3437).getArea(2);
    final int  SoftClayID           = 1761;
    final int  UnfiredBowl          = 1789;
    States LastState = States.TravelToWheel;

    public enum States
    {
        TravelToWheel,
        Shaping,
        TravelToBank,
        Banking
    }

    @Override
    public int onLoop()
    {

        States State = GetState();

        switch(State)
        {
            case TravelToWheel ->
            {
                OSRSUtilities.WalkTo(PottersLocation);
            }
            case Shaping ->
            {
                Point Click = GameObjects.closest(PottersWheelID).getCenterPoint();
                if(Client.getViewport().isOnGameScreen(Click))
                {
                    Click = OSRSUtilities.RandomizeClick(Click);
                    Mouse.click(Click);

                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                    OSRSUtilities.Wait();
                    OSRSUtilities.PickSkillingMenuItem(PottingAction);
                    OSRSUtilities.WaitForEndAnimationLoop(1500, 10000);
                }
                else
                {
                    Camera.mouseRotateToTile(PottersWheelLocation);
                }

            }
            case TravelToBank ->
            {
                OSRSUtilities.WalkTo(BankLocation.getRandomTile());
            }
            case Banking ->
            {
                OSRSUtilities.BankDepositAll();

                if(Bank.contains(SoftClayID))
                {
                    OSRSUtilities.BankWithdrawAll(SoftClayID);
                    OSRSUtilities.BankClose();
                }
                else
                {
                    OSRSUtilities.BankClose();
                    OSRSUtilities.Wait();
                    this.stop();
                }

            }
        }

        return 0;
    }

    public States GetState()
    {
        States     out;
        GameObject Wheel = GameObjects.closest(PottersWheelID);
        if(Wheel != null && Wheel.distance(Players.getLocal().getTile()) < 8.0 &&
           Inventory.contains(SoftClayID))
        {
            out = States.Shaping;
        }
        else if(Inventory.contains(SoftClayID))
        {
            out = States.TravelToWheel;
        }
        else if(OSRSUtilities.CanReachBank())
        {
            out = States.Banking;
        }
        else
        {
            out = States.TravelToBank;
        }


        if(out != LastState)
        {
            Logger.log("Transitioning to state: " + out);
            LastState = out;
        }

        return out;
    }
}
