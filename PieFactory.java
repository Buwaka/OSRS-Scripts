import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;

import java.util.AbstractMap;

@ScriptManifest(name = "PieFactory", description = "Make Pie shells", author = "Semanresu",
        version = 1.0, category = Category.CRAFTING, image = "")
public class PieFactory extends AbstractScript {


    final int PieShellID = 2313;
    final int PastyDoughID = 1953;

    public enum States{
        Banking, Combining
    }

    Thread Ranomizer;
    @Override
    public void onStart() {
        super.onStart();

        if(!OSRSUtilities.CanReachBank())
        {
            Logger.log("Not within reach of a bank");
            this.stop();
            return;
        }

        Ranomizer = OSRSUtilities.StartRandomizerThread();
    }

    States CurrentState = States.Banking;
    @Override
    public int onLoop() {

        switch (CurrentState)
        {
            case Banking -> {
                OSRSUtilities.BankDepositAll();
                OSRSUtilities.BankWithdraw(new AbstractMap.SimpleEntry<Integer, Integer>(PieShellID, 14),
                        new AbstractMap.SimpleEntry<Integer, Integer>(PastyDoughID, 14));
                OSRSUtilities.BankClose();

                OSRSUtilities.Wait(300, 1500);

                if(Inventory.contains(PieShellID) && Inventory.contains(PastyDoughID))
                {
                    CurrentState = States.Combining;
                }
                else
                {
                    Logger.log("Not enough materials, exiting script");
                    this.stop();
                    return 1;
                }
            }
            case Combining -> {
                while (Inventory.contains(PieShellID) && Inventory.contains(PastyDoughID))
                {
                    OSRSUtilities.ProcessItems(1, PieShellID, PastyDoughID, 60000);
                }
                CurrentState = States.Banking;
            }
        }

        return 0;
    }
}
