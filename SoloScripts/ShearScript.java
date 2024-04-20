package SoloScripts;

import Utilities.OSRSUtilities;
import Utilities.Scripting.tpircSScript;
import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.input.mouse.MouseSettings;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.walking.path.impl.LocalPath;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;

import java.awt.*;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

@ScriptManifest(name = "SoloScripts.ShearScript", description = "Lumbridge Sheep shearing script", author = "Semanresu", version = 1.0, category = Category.CRAFTING, image = "")
public class ShearScript extends tpircSScript
{

    final int    GateID       = 12987;
    final Tile   GateLocation = new Tile(3213, 3261, 0);
    final Area   SheepArea    = new Area(3193, 3276, 3212, 3258, 0);
    final int    FalseSheepID = 731;
    final String ShearAction  = "Shear";
    final String SheepName    = "Sheep";
    final Area   BankArea     = Area.generateArea(3, new Tile(3209, 3219, 2));
    final int    WoolID       = 1737;
    Random          rand            = new Random();
    Tile            SheepLocationRTile;
    Tile            BankLocationRTile;
    Tile            LastDestination = null;
    LocalPath<Tile> Path            = null;
    States          LastState       = States.TravelToSheep;

    public Optional<GameObject> GetClosedGate()
    {
        var Door = Arrays.stream(GameObjects.getObjectsOnTile(GateLocation)).filter(x -> x.getID() ==
                                                                                         GateID).findFirst();
        if(Door.isPresent() && Door.get().hasAction("Open"))
        {
            return Door;
        }
        return Optional.empty();
    }

    public boolean IsInsideBankArea()
    {
        return BankArea.contains(Players.getLocal().getTile());
    }

    public boolean IsInsideSheepArea()
    {
        return SheepArea.contains(Players.getLocal().getTile());
    }

    public States GetState()
    {

        if(Inventory.isFull())
        {
            if(IsInsideBankArea())
            {
                return States.Banking;
            }
            else
            {
                return States.TravelToBank;
            }
        }
        if(IsInsideSheepArea())
        {
            return States.Shearing;
        }
        else
        {
            return States.TravelToSheep;
        }
    }

    public NPC GetClosestSheep()
    {
        return NPCs.closest(t -> t.getName().equalsIgnoreCase(SheepName) && t.getID() != FalseSheepID &&
                                 SheepArea.contains(t.getTile()) && t.hasAction(ShearAction) && !t.isMoving());
    }

    public void RandomizeTile()
    {
        Logger.log("Randomize");
        SheepLocationRTile = SheepArea.getRandomTile();
        BankLocationRTile  = BankArea.getRandomTile();
        MouseSettings.setSpeed(rand.nextInt(5) + 5);
    }

    @Override
    public int onLoop()
    {

        if(rand.nextInt(100) > rand.nextInt(20))
        {
            return 0;
        }

        if(rand.nextInt(1000) < rand.nextInt(80))
        {
            RandomizeTile();
        }

        States State = GetState();
        if(State != LastState)
        {
            Logger.log(State);
        }


        switch(State)
        {
            case TravelToSheep ->
            {

                var Gate = GetClosedGate();
                if(Gate.isPresent() && Gate.get().canReach() && Gate.get().hasAction("Open"))
                {
                    Gate.get().interact("Open");
                }
                else
                {
                    OSRSUtilities.WalkTo(SheepLocationRTile);

                }
            }
            case Shearing ->
            {
                MouseSettings.setSpeed(rand.nextInt(5) + 10);
                NPC Sheep = GetClosestSheep();
                Logger.log(Sheep);
                while(Sheep == null)
                {
                    Camera.rotateToYaw(Camera.getYaw() + 450 % 2000);
                    Sheep = GetClosestSheep();
                    Logger.log(Sheep);
                    OSRSUtilities.WalkTo(SheepLocationRTile);
                    Sleep.sleep(rand.nextInt(3000) + 500);
                }

                Point Center = Sheep.getCenterPoint();
                Center.translate(rand.nextInt(10) - 5, rand.nextInt(10) - 5);
                Mouse.click(Center);
                while(!Players.getLocal().isAnimating())
                {
                    Mouse.click(Sheep.getClickablePoint());
                    Sleep.sleep(rand.nextInt(1000) + 500);
                    if(!Sheep.hasAction(ShearAction))
                    {
                        Sheep = GetClosestSheep();
                    }
                }
                Sleep.sleepUntil(() -> Players.getLocal().getAnimation() == -1, 10000);
            }
            case TravelToBank ->
            {
                var Gate = GetClosedGate();
                if(SheepArea.contains(Players.getLocal().getTile()))
                {
                    if(Gate.isPresent() && Gate.get().canReach() && Gate.get().hasAction("Open"))
                    {
                        Gate.get().interact("Open");
                    }
                    else
                    {
                        OSRSUtilities.WalkTo(BankLocationRTile);
                    }
                }
                else
                {
                    OSRSUtilities.WalkTo(BankLocationRTile);
                }
            }
            case Banking ->
            {
                Bank.open();
                Sleep.sleep(rand.nextInt(500) + 200);
                Bank.depositAll(WoolID);
                Sleep.sleep(rand.nextInt(500) + 200);
                Bank.close();
                Sleep.sleep(rand.nextInt(500) + 200);
            }
        }

        LastState = State;
        return 0;
    }

    public enum States
    {
        TravelToSheep,
        Shearing,
        TravelToBank,
        Banking
    }
}
