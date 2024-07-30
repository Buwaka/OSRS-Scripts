package Cycles.SimpleTasks.ItemProcessing;

import OSRSDatabase.ItemDB;
import OSRSDatabase.NPCDB;
import Utilities.OSRSUtilities;
import Utilities.Scripting.SimpleTask;
import Utilities.Scripting.tpircSScript;
import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.Shop;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.widgets.Menu;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Arrays;

public class TanTask extends SimpleTask
{

    final int    CoinID         = ItemDB.GetClosestMatch("Coins", true).id; //995
    final int    CowHideID      = ItemDB.GetClosestMatch("Cowhide", true).id; //1739
    final int    TannerID       = NPCDB.GetClosestMatch("Ellis").id; //3231
    final String TanAllAction   = "Tan All";
    final int    TannerWidgetID = 324;


    private String LeatherName; // Hard leather
    private int    SourceItemID;

    public TanTask(String Name, String leatherName, int sourceItemID)
    {
        super(Name);
        LeatherName  = leatherName;
        SourceItemID = sourceItemID;
    }

    private WidgetChild GetTargetWidget()
    {
        var target = Arrays.stream(Widgets.get(TannerWidgetID).getChildren())
                           .filter((t) -> t.getText().equalsIgnoreCase(LeatherName))
                           .findFirst();
        return target.orElse(null);
    }

    private NPC GetTanner()
    {
        Logger.log("TanTask: GetTanner: TannerID" + TannerID);
        return NPCs.closest(TannerID);
    }

    /**
     * @return
     */
    @Override
    protected int Loop()
    {
        if(!Inventory.contains(CoinID))
        {
            Logger.log("TanTask: Loop: No money to tan, exiting");
            return 0;
        }

        if(!Inventory.contains(SourceItemID))
        {
            Logger.log("TanTask: Loop: No more Source item, exiting");
            return 0;
        }

        if(!Widgets.isVisible(TannerWidgetID))
        {
            Logger.log("TanTask: Loop: Opening shop");
            Shop.open(TannerID);
            return super.Loop();
        }

        if(Menu.contains(TanAllAction))
        {
            Logger.log("TanTask: Loop: Tanning All");
            Menu.clickAction(TanAllAction);
            Sleep.sleepTick();
            return super.Loop();
        }

        var LeatherWidget = GetTargetWidget();
        if(LeatherWidget != null)
        {
            Logger.log("TanTask: Loop: Opening menu");
            Point Click = new Point((int) LeatherWidget.getRectangle().getCenterX() + (OSRSUtilities.rand.nextInt(100) - 50),
                                    (int) LeatherWidget.getRectangle().getCenterY() + (OSRSUtilities.rand.nextInt(100) - 50));
            Mouse.click(Click, true);
        }

        return super.Loop();
    }


    /**
     * @return
     */
    @Override
    protected boolean Ready()
    {
        var Tanner = GetTanner();
        var invCheck = Inventory.contains(SourceItemID);
        Logger.log("TanTask: Ready " + invCheck + " " + Tanner + " ");
        return Inventory.contains(SourceItemID) && Tanner != null && Tanner.canReach() && super.Ready();
    }



    /**
     * @return
     */
    @Nonnull
    @Override
    public TaskType GetTaskType()
    {
        return TaskType.Tan;
    }

    /**
     * @param Script
     *
     * @return return true if successful, false if we need more time, keep triggering start until it is ready
     */
    @Override
    public boolean onStartTask(tpircSScript Script)
    {
        return super.onStartTask(Script);
    }
}
