import org.dreambot.api.Client;
import org.dreambot.api.input.Keyboard;
import org.dreambot.api.input.Mouse;
import org.dreambot.api.input.event.impl.keyboard.awt.Key;
import org.dreambot.api.input.mouse.destination.impl.PointDestination;
import org.dreambot.api.methods.ViewportTools;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.container.impl.bank.BankQuantitySelection;
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
import org.dreambot.api.script.loader.LocalLoader;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.graphics.Viewport;
import org.dreambot.api.wrappers.interactive.*;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ScriptManifest(name = "ClayWatering", description = "Clay to soft clay using water vials, needs to be close to a bank", author = "Varrock",
        version = 1.0, category = Category.CRAFTING, image = "")
public class ClayWatering extends AbstractScript {

    public enum States {
        Watering, Banking
    }

    States CurrentState = States.Banking;
    Random rand = new Random();
    final int WaterVialID = 227;
    final int ClayID = 434;

    Thread Ranomizer;
    @Override
    public void onStart() {
        super.onStart();
        Ranomizer = OSRSUtilities.StartRandomizerThread();
    }
    public int onLoop() {
        Logger.log(Dialogues.inDialogue());
        switch (CurrentState)
        {
            case Watering -> {
                while (Inventory.contains(ClayID) && Inventory.contains(WaterVialID))
                {
                    while(!Dialogues.inDialogue())
                    {
                        OSRSUtilities.Combine(WaterVialID, ClayID);
                        Sleep.sleep(rand.nextInt(1000) + 500);
                    }

                    Keyboard.typeKey(Key.SPACE);

                    Sleep.sleep(rand.nextInt(2000) + 1000);

                    Mouse.moveOutsideScreen();

                    Sleep.sleepUntil(() -> {
                        return !Inventory.contains(WaterVialID) || !Inventory.contains(ClayID);
                    }, 60000);

                    Sleep.sleep(rand.nextInt(2000) + 1000);
                }

                CurrentState = States.Banking;
            }
            case Banking -> {
                Bank.open();
                if(Bank.getDefaultQuantity() != BankQuantitySelection.X)
                {
                    Bank.setDefaultQuantity(BankQuantitySelection.X);
                }


                if(!Inventory.isEmpty())
                {
                    Bank.depositAllItems();
                }


                if(!Bank.contains(WaterVialID) || !Bank.contains(ClayID))
                {
                    Logger.log("No Clay or water vials left");
                    //ScriptStarter scriptStarter = new ScriptStarter(ClayMiningScript.class);
                    //log("trying to start cCCannonballs");
                    //Thread t1 = new Thread(scriptStarter);
                    //t1.start();
                    this.stop();
                    // jump to clay script
                    return 0;
                }

                Bank.withdraw(WaterVialID, 14);
                Sleep.sleep(rand.nextInt(1000) + 100);
                Bank.withdraw(ClayID, 14);
                Sleep.sleep(rand.nextInt(1000) + 100);
                Bank.close();
                CurrentState = States.Watering;
            }
        }

        return 0;
    }

}
