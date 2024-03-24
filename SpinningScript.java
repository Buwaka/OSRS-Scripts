import org.dreambot.api.input.Keyboard;
import org.dreambot.api.input.Mouse;
import org.dreambot.api.input.event.impl.keyboard.awt.Key;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManager;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;

import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

@ScriptManifest(name = "SpinningScript", description = "Lumbridge Spinning script", author = "Semanresu",
        version = 1.0, category = Category.CRAFTING, image = "")
public class SpinningScript extends AbstractScript {

    public enum State
    {
        WalkingToSpin,
        Spinning,
        WalkingToBank,
        Banking
    }

    // consts
    final Area BankLocation = Area.generateArea(3, new Tile(3208, 3219,2));
    final int BankID = 18492;
    final Tile StairLocation1 = new Tile(3205, 3208,1);
    final Tile StairLocation2 = new Tile(3205, 3208,2);
    final int Stair2ID = 16673;
    final int Stair1ID = 16672;
    final Tile SpinLocation = new Tile(3209, 3213, 1);
    final Area SpinArea = new Area(3213,3217,3208,3212,1);
    final Tile SpinDoor = new Tile(3207, 3214,1);
    final int SpinDoorID = 1543;
    final int SpinID = 14889;
    final int WoolID = 1737;
    final int BallWoolID = 1759;


    final String SpinAction = "Spin";
    final Key SpinStart = Key.SPACE;
    final String StairUpAction = "Climb-up";
    final String StairDownAction = "Climb-down";

    // vars
    State CurrentState = State.WalkingToSpin;
    Random rand = new Random();

    Thread Ranomizer;
    @Override
    public void onStart() {
        super.onStart();
        Ranomizer = OSRSUtilities.StartRandomizerThread();
    }

    public boolean IsInsideSpinningArea()
    {
        return SpinArea.contains(Players.getLocal().getTile()) && Players.getLocal().getTile().getZ() == 1;
    }

    public boolean IsSpinDoorOpen()
    {
        var Door = Arrays.stream(GameObjects.getObjectsOnTile(SpinDoor)).filter(x -> x.getID() == SpinDoorID).findFirst();
        Logger.log(Door);
        return !Door.isPresent() || !Door.get().hasAction("Open");
    }

    public boolean OpenSpinDoor()
    {
        if (!IsSpinDoorOpen())
        {
            Logger.log("Opening Spin Door");
            GameObject Door = GameObjects.closest(t -> t.getID() == SpinDoorID, SpinDoor);
            return Door.interact("Open");
        }
        return false;
    }


    @Override
    public int onLoop() {

        if(rand.nextInt(100) > rand.nextInt(35))
            return 0;


        Tile CurrentPosition = Players.getLocal().getTile();
        int Floor = CurrentPosition.getZ();


        Optional<GameObject> Stair1 = Arrays.stream(GameObjects.getObjectsOnTile(StairLocation1)).filter(x -> x.getID() == Stair1ID).findFirst();
        Optional<GameObject> Stair2 = Arrays.stream(GameObjects.getObjectsOnTile(StairLocation2)).filter(x -> x.getID() == Stair2ID).findFirst();

        Logger.log(CurrentState.toString());

        switch (CurrentState) {
            case WalkingToSpin:
                switch (Floor) {
                    // bank floor
                    case 2:

                        Logger.log("Travel to stairs, to spin");

                        if (Stair2.isPresent() && Players.getLocal().canReach(Stair2.get().getTile()))
                        {
                            Logger.log("Down the stairs");
                            if(Stair2.get().interact(StairDownAction))
                            {
                                Floor = 1;
                            }

                        }
                        else
                        {
                            Walking.walk(StairLocation2.getRandomized(1));
                        }
                        break;
                    // spin floor
                    case 1:
                        Logger.log("Travel to spin");


                        GameObject Spin = GameObjects.closest(t -> t.getID() == SpinID, SpinLocation);

                        if (!IsInsideSpinningArea()) {
                            Logger.log("Not inside Spin area, entering");

                            if(!OpenSpinDoor())
                            {
                                Walking.walk(SpinLocation);
                            }
                        }
                        else
                        {
                            Logger.log("inside Spin area, reaching for spin");
                            if (Spin.canReach()) {
                                if (Spin.interact(SpinAction)) {
                                    Logger.log("Transitioning state to Spinning");
                                    CurrentState = State.Spinning;
                                }
                            } else {
                                Walking.walk(SpinLocation);
                            }
                        }
                        break;

                    // somewhere we're not supposed to be
                    default:
                        break;
                }
                break;
            case Spinning:
                if (!Inventory.contains("Wool"))
                {
                    Logger.log("Transitioning state to Walking to the bank");
                    CurrentState = State.WalkingToBank;
                    break;
                }

                if(Players.getLocal().getAnimation() != -1)
                {
                    Logger.log("Spinning in progress");
                    break;
                }

                Logger.log("Transitioning state to Spinning");
                Keyboard.typeKey(SpinStart);
                Sleep.sleep(rand.nextInt(3000) + 1000);
                if(Players.getLocal().getAnimation() != -1)
                {
                    Mouse.moveOutsideScreen();
                    Logger.log("Is Spinning, wait until");
                    Sleep.sleepUntil(() -> {Logger.log("Current Animation: " + Players.getLocal().getAnimation());return Players.getLocal().getAnimation() == -1;}, 100000, 5000);
                    Logger.log("Spinning is done or timeout is reached");
                    Sleep.sleep(rand.nextInt(3000) + 500);
                }


                if (!Inventory.contains("Wool"))
                {
                    Logger.log("Transitioning state to Spinning");
                    CurrentState = State.WalkingToBank;
                }
                else
                {
                    while (Dialogues.inDialogue())
                    {
                        Dialogues.clickContinue();
                        Sleep.sleep(rand.nextInt(3000) + 500);
                    }
                    GameObject Spin = GameObjects.closest(t -> t.getID() == SpinID, SpinLocation);
                    Spin.interact(SpinAction);
                }

                break;
            case WalkingToBank:
                switch (Floor) {
                    case 1:
                        Logger.log("Travel to stairs, to bank");
                        if (IsInsideSpinningArea())
                        {
                            Logger.log("Inside Spin area, checking door");
                            if(!OpenSpinDoor())
                            {
                                Walking.walk(StairLocation1);
                            }
                        }
                        else
                        {
                            Logger.log("Outside spin area, going to stairs");
                            Logger.log(Stair1);


                            if (Stair1.isPresent() && Stair1.get().hasAction(StairUpAction))
                            {
                                Logger.log("Down the stairs");
                                if(Stair1.get().interact(StairUpAction))
                                {
                                    Floor = 2;
                                }
                            }
                            else
                            {
                                Walking.walk(StairLocation1.getRandomized(1));
                            }
                        }
                        break;
                    case 2:
                        Tile PlayerTile = Players.getLocal().getTile();
                        GameObject BankObj = GameObjects.closest(BankID);
                        if(BankLocation.contains(PlayerTile))
                        {
                            if(Bank.open())
                            {
                                CurrentState = State.Banking;
                            }
                            else
                            {
                                Walking.walk(BankObj);
                            }
                        }
                        else
                        {
                            Walking.walk(BankLocation.getRandomTile());
                        }
                        break;
                }
                break;
            case Banking:
                Sleep.sleep(rand.nextInt(3000) + 100);
                Bank.depositAllItems();
                Sleep.sleep(rand.nextInt(3000) + 100);
                boolean WoolLeft = Bank.withdrawAll(WoolID);
                Sleep.sleep(rand.nextInt(3000) + 100);

                if(WoolLeft)
                {
                    CurrentState = State.WalkingToSpin;
                }
                else
                {
                    Sleep.sleep(rand.nextInt(50000) + 10000);
                    ScriptManager.getScriptManager().start("ShearScript","");
                    this.stop();
                    return 1;
                }
                break;
        }

        Sleep.sleep(rand.nextInt(3000) + 1000);
        return 0;
    }

}