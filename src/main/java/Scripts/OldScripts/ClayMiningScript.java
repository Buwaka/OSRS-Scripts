package Scripts.OldScripts;

import Utilities.OSRSUtilities;
import Utilities.Scripting.IFScript;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Logger;


@ScriptManifest(name = "SoloScripts.ClayMiningScript", description = "Varrock Clay mining script, Varrock west bank to Varrock mine", author = "Varrock", version = 1.0, category = Category.MINING, image = "")
public class ClayMiningScript extends IFScript
{

    final Area BankLocation = new Tile(3183, 3437).getArea(2);
    final Tile MinePosition = new Tile(3180, 3371);
    final Tile Rock1        = new Tile(3180, 3372);
    final Tile Rock2        = new Tile(3179, 3371);
    final int  ClayID       = 434;
    States LastState = States.TravelToRocks;

    public enum States
    {
        TravelToRocks,
        Mining,
        TravelToBank,
        Banking
    }

    public int onLoop()
    {

        States State = GetState();

        switch(State)
        {
            case TravelToRocks ->
            {
                OSRSUtilities.WalkTo(MinePosition);
            }
            case Mining ->
            {
                OSRSUtilities.Mine(Rock1);
                OSRSUtilities.Mine(Rock2);
            }
            case TravelToBank ->
            {
                OSRSUtilities.WalkTo(BankLocation.getRandomTile());
                Bank.open();
            }
            case Banking ->
            {
                Bank.depositAllItems();

                //                if(Bank.count(ClayID) > (rand.nextInt(200) + 500))
                //                {
                //                    ScriptManager.getScriptManager().start("SoloScripts.ClayWatering","");
                //                    return 0;
                //                }

                Bank.close();
            }
        }

        return 0;
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

    public boolean IsAtMinePosition()
    {
        var dist = Players.getLocal().getTile().distance(MinePosition);
        return dist < 1;
    }
}
