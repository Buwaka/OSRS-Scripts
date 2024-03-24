import org.dreambot.api.input.Mouse;
import org.dreambot.api.input.mouse.destination.impl.PointDestination;
import org.dreambot.api.methods.ViewportTools;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.dialogues.Dialogues;
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
import org.dreambot.api.script.ScriptManager;
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

@ScriptManifest(name = "ClayMiningScript", description = "Varrock Clay mining script, Varrock west bank to Varrock mine", author = "Varrock",
        version = 1.0, category = Category.MINING, image = "")
public class ClayMiningScript extends AbstractScript{

    final Area BankLocation = new Tile(3183, 3437).getArea(2);
    final Tile MinePosition = new Tile(3180, 3371);
    final Tile Rock1 = new Tile(3180, 3372);
    final Tile Rock2 = new Tile(3179, 3371);
    final int ClayID = 434;

    public enum States
    {
        TravelToRocks,
        Mining,
        TravelToBank,
        Banking
    }

    States LastState = States.TravelToRocks;

    public boolean IsAtMinePosition()
    {
        var dist = Players.getLocal().getTile().distance(MinePosition);
        return dist < 1;
    }

    public States GetState()
    {
        States State;
        if(Bank.isOpen())
        {
            State = States.Banking;
        }
        else if(Inventory.isFull())
        {
            State = States.TravelToBank;
        }
        else if(IsAtMinePosition())
        {
            State = States.Mining;
        }
        else
        {
            State = States.TravelToRocks;
        }

        if(State != LastState)
        {
            Logger.log(State.toString());
            LastState = State;
        }

        return State;
    }

    Tile LastDestination = null;
    LocalPath<Tile> Path = null;
    Random rand = new Random();

    Thread Ranomizer;
    @Override
    public void onStart() {
        super.onStart();
        Ranomizer = OSRSUtilities.StartRandomizerThread();
    }


    public int onLoop()
    {

        States State = GetState();

        switch (State)
        {
            case TravelToRocks -> {
                OSRSUtilities.WalkTo(MinePosition);
            }
            case Mining -> {
                OSRSUtilities.Mine(Rock1);
                OSRSUtilities.Mine(Rock2);
            }
            case TravelToBank -> {
                OSRSUtilities.WalkTo(BankLocation.getRandomTile());
                Bank.open();
            }
            case Banking -> {
                Bank.depositAllItems();

//                if(Bank.count(ClayID) > (rand.nextInt(200) + 500))
//                {
//                    ScriptManager.getScriptManager().start("ClayWatering","");
//                    return 0;
//                }

                Bank.close();
            }
        }

        return 0;
    }
}
