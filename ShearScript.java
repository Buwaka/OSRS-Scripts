import org.dreambot.api.input.Mouse;
import org.dreambot.api.input.mouse.destination.impl.PointDestination;
import org.dreambot.api.methods.ViewportTools;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.input.mouse.MouseSettings;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Map;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.walking.path.impl.LocalPath;
import org.dreambot.api.methods.walking.pathfinding.impl.local.LocalPathFinder;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.graphics.Viewport;
import org.dreambot.api.wrappers.interactive.*;

import java.awt.*;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

@ScriptManifest(name = "ShearScript", description = "My script description!", author = "Developer Name",
        version = 1.0, category = Category.CRAFTING, image = "")
public class ShearScript extends AbstractScript{

    public enum States
    {
        TravelToSheep,
        Shearing,
        TravelToBank,
        Banking
    }

    final int GateID = 12987;
    final Tile GateLocation = new Tile(3213, 3261, 0);
    final Area SheepArea = new Area(3193, 3276, 3212, 3258, 0);
    final int FalseSheepID = 731;
    final String ShearAction = "Shear";
    final String SheepName = "Sheep";
    final Area BankArea = Area.generateArea(3, new Tile(3209, 3219,2));
    final int WoolID = 1737;

    public Optional<GameObject> GetClosedGate()
    {
        var Door = Arrays.stream(GameObjects.getObjectsOnTile(GateLocation)).filter(x -> x.getID() == GateID).findFirst();
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
        return NPCs.closest(t -> t.getName().equalsIgnoreCase(SheepName) &&
                t.getID() != FalseSheepID &&
                SheepArea.contains(t.getTile()) &&
                t.hasAction(ShearAction) &&
                !t.isMoving());
    }

    Random rand = new Random();
    Tile SheepLocationRTile;
    Tile BankLocationRTile;

    @Override
    public void onStart(String... params) {
        super.onStart(params);
        Logger.log(MouseSettings.getMouseTiming());
        Logger.log(MouseSettings.getSpeed());
        MouseSettings.setSpeed(1);
        Walking.toggleNoClickWalk(true);
        RandomizeTile();
    }

    Tile LastDestination = null;
    LocalPath<Tile> Path = null;

    public void WalkTo(Tile Destination)
    {
        if(LastDestination == null || LastDestination != Destination || Path == null)
        {
            Path = LocalPathFinder.getLocalPathFinder().calculate(Players.getLocal().getTile(), Destination);
            LastDestination = Destination;
            Logger.log(Path);

            if(Path == null || Path.isEmpty())
            {
                return ;
            }
        }


        if(!Players.getLocal().isMoving())
        {
            Tile Next = Path.next();
            Tile NextNext =  Path.next(1);
            Logger.log("Next tile: " + Next);
            Logger.log("PlayerTile: "+ Players.getLocal().getTile());
            if(Players.getLocal().getTile().equals(Next))
            {
                Logger.log("Walking");
                Walking.walk(Destination);
                Sleep.sleep(rand.nextInt(2000) + 1000);
            }
            else if (Next != null && Next.canReach())
            {
                Logger.log("Clicking");
                Logger.log(Next.distance(NextNext));
                if(Map.isTileOnScreen(Next) && Math.abs(Next.distance(NextNext)) > 1.0)
                {

                    Point Center = Map.tileToScreen(Next);
                    Center.translate(rand.nextInt(5) - 3, rand.nextInt(5) - 3);
                    if(!Mouse.click(Center))
                    {
                        Center = Map.tileToScreen(Next);
                        Mouse.click(Center);
                    }
                }
                else
                {
                    Point Center = Map.tileToMiniMap(Next);
                    if (Center.x == 0 && Center.y == 0)
                    {
                        // shouldn't happen
                        return;
                    }
                    else
                    {
                        Center.translate(rand.nextInt(5) - 3, rand.nextInt(5) - 3);
                        Walking.clickTileOnMinimap(Next);
                    }
                }

                Sleep.sleep(rand.nextInt(2000) + 500);
            }
            else
            {
                Logger.log("Last resort walking");
                Walking.walk(Destination);
                Sleep.sleep(rand.nextInt(2000) + 1000);
            }
        }
    }

    public void RandomizeTile()
    {
        Logger.log("Randomize");
        SheepLocationRTile = SheepArea.getRandomTile();
        BankLocationRTile = BankArea.getRandomTile();
        MouseSettings.setSpeed(rand.nextInt(5) + 5);
    }

    States LastState = States.TravelToSheep;
    @Override
    public int onLoop() {

        if(rand.nextInt(100) > rand.nextInt(20))
            return 0;

        if(rand.nextInt(1000) < rand.nextInt(80))
            RandomizeTile();

        States State = GetState();
        if(State != LastState)
        {
            Logger.log(State);
        }


        switch (State)
        {
            case TravelToSheep -> {

                var Gate = GetClosedGate();
                if(Gate.isPresent() && Gate.get().canReach() && Gate.get().hasAction("Open"))
                {
                    Gate.get().interact("Open");
                }
                else
                {
                    WalkTo(SheepLocationRTile);

                }
            }
            case Shearing -> {
                MouseSettings.setSpeed(rand.nextInt(5) + 10);
                NPC Sheep = GetClosestSheep();
                Logger.log(Sheep);
                while (Sheep == null)
                {
                    Camera.rotateToYaw(Camera.getYaw() + 450 % 2000);
                    Sheep = GetClosestSheep();
                    Logger.log(Sheep);
                    WalkTo(SheepLocationRTile);
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
            case TravelToBank -> {
                var Gate = GetClosedGate();
                if(SheepArea.contains(Players.getLocal().getTile()))
                {
                    if(Gate.isPresent() && Gate.get().canReach() && Gate.get().hasAction("Open"))
                    {
                        Gate.get().interact("Open");
                    }
                    else
                    {
                        WalkTo(BankLocationRTile);
                    }
                }
                else
                {
                    WalkTo(BankLocationRTile);
                }
            }
            case Banking -> {
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
}
