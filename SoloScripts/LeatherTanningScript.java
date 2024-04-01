package SoloScripts;

import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.widgets.Menu;

import Utilities.OSRSUtilities;
import Utilities.tpircSScript;

import java.awt.*;

@ScriptManifest(name = "SoloScripts.LeatherTanningScript", description = "Clay to soft clay using water vials, needs to be close to a bank", author = "Varrock",
        version = 1.0, category = Category.CRAFTING, image = "")
public class LeatherTanningScript extends tpircSScript
{

    final int CoinID = 995;
    final int CowHideID = 1739;
    final int TannerID = 3231;
    final Tile TannerTile = new Tile(3276, 3191);
    final Tile BankTile = new Tile(3270, 3166);
    final String TannerAction = "Trade";
    final String TanAllAction = "Tan All";
    final int TannerWidgetID = 324;
    final int LeatherWidgetID = 100;

    NPC GetTanner()
    {
        return NPCs.closest(TannerID);
    }

    enum States
    {
        TraveltoTan,
        Tan,
        TravelToBank,
        Banking
    }

    States LastState = States.TraveltoTan;

    States GetState()
    {
        States result = LastState;
        if (Inventory.contains(CowHideID) && Inventory.contains(CoinID))
        {
            NPC Tanner = GetTanner();
            if (Tanner != null)
            {
                result = States.Tan;
            }
            else
            {
                result = States.TraveltoTan;
            }
        }
        else
        {
            if (OSRSUtilities.CanReachBank())
            {
                result = States.Banking;
            }
            else
            {
                result = States.TravelToBank;
            }
        }

        if (result != LastState)
        {
            Logger.log("Transitioning to state: " + result);
            LastState = result;
        }

        return result;
    }

    @Override
    public int onLoop()
    {

        States State = GetState();

        switch (State)
        {
            case TraveltoTan ->
            {
                OSRSUtilities.SimpleWalkTo(TannerTile);
            }
            case Tan ->
            {
                NPC Tanner = GetTanner();
                if (Tanner.canReach())
                {
                    while (!Widgets.isVisible(TannerWidgetID))
                    {
                        Tanner.interact(TannerAction);

                        Sleep.sleepUntil(() -> Widgets.isVisible(TannerWidgetID), 5000);
                    }

                    var LeatherWidget = Widgets.get(TannerWidgetID, LeatherWidgetID);
                    if (LeatherWidget != null)
                    {
                        Point Click = new Point((int) LeatherWidget.getRectangle().getCenterX(),
                                                (int) LeatherWidget.getRectangle().getCenterY());
                        Mouse.click(Click, true);
                        Sleep.sleepUntil(() -> Menu.contains(TanAllAction), 5000);
                        OSRSUtilities.Wait(300, 300);
                        Menu.clickAction(TanAllAction);
                        OSRSUtilities.Wait();
                    }
                }

            }
            case TravelToBank ->
            {
                OSRSUtilities.SimpleWalkTo(BankTile);
            }
            case Banking ->
            {
                OSRSUtilities.BankDepositAll(CoinID);
                if (!Bank.contains(CowHideID) && !Inventory.contains(CowHideID))
                {
                    Logger.log("No more Cowhide, stopping script");
                    this.stop();
                    return 0;
                }

                if (!Inventory.contains(CoinID))
                {
                    Bank.withdraw(CoinID, 1000);
                }
                OSRSUtilities.BankWithdrawAll(CowHideID);
                OSRSUtilities.BankClose();
            }
        }
        return 0;
    }
}
