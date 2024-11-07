package Scripts.OldScripts;

import Utilities.OSRSUtilities;
import Utilities.Scripting.IFScript;
import Utilities.Scripting.Logger;
import org.dreambot.api.input.Keyboard;
import org.dreambot.api.input.Mouse;
import org.dreambot.api.input.event.impl.keyboard.awt.Key;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankQuantitySelection;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Sleep;

import java.util.Random;

@ScriptManifest(name = "SoloScripts.ClayWatering", description = "Clay to soft clay using water vials, needs to be close to a bank", author = "Semanresu", version = 1.0, category = Category.CRAFTING, image = "")
public class ClayWatering extends IFScript
{

    final int WaterVialID = 227;
    final int ClayID      = 434;
    States CurrentState = States.Banking;
    Random rand         = new Random();

    enum States
    {
        Watering,
        Banking
    }

    public int onLoop()
    {

        switch(CurrentState)
        {
            case Watering ->
            {
                while(Inventory.contains(ClayID) && Inventory.contains(WaterVialID))
                {
                    while(!Dialogues.inDialogue())
                    {
                        OSRSUtilities.ClickCombine(WaterVialID, ClayID);
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
            case Banking ->
            {
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
                    //Utilities.ScriptStarter scriptStarter = new Utilities.ScriptStarter(SoloScripts.ClayMiningScript.class);
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

    @Override
    public void onStart()
    {
        super.onStart();
        if(!OSRSUtilities.CanReachBank())
        {
            Logger.log("Not within reach of a bank");
            this.stop();
        }
    }

}
