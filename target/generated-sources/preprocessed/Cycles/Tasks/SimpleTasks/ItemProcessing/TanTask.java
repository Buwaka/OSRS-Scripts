package Cycles.Tasks.SimpleTasks.ItemProcessing;

import OSRSDatabase.ItemDB;
import OSRSDatabase.NPCDB;
import Utilities.GrandExchange.GEInstance;
import Utilities.OSRSUtilities;
import Utilities.Scripting.IFScript;
import Utilities.Scripting.Logger;
import Utilities.Scripting.SimpleTask;
import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.Shop;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.widgets.Menu;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Arrays;

public class TanTask extends SimpleTask
{

    final int    CowHideID      = ItemDB.GetClosestMatch("Cowhide", true).id; //1739
    final int    TannerID       = NPCDB.GetClosestMatch("Ellis").id; //3231
    final String TanAllAction   = "Tan All";
    final int    TannerWidgetID = 324;


    private final String LeatherName; // Hard leather
    private final int    SourceItemID;
    private final int    UnitCost;

    public TanTask(String Name, String leatherName, int sourceItemID, int unitCost)
    {
        super(Name);
        LeatherName  = leatherName;
        SourceItemID = sourceItemID;
        UnitCost     = unitCost;
    }

    /**
     * @return
     */
    @Override
    public boolean Ready()
    {
        var Tanner   = GetTanner();
        var invCheck = Inventory.contains(SourceItemID);
        Logger.log("TanTask: Ready " + invCheck + " " + Tanner + " ");
        return Inventory.contains(SourceItemID) && Tanner != null && Tanner.canReach() &&
               super.Ready();
    }

    /**
     * @return
     */
    @Override
    protected int Loop()
    {
        if(Inventory.count(GEInstance.CoinID) < UnitCost)
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
            if(!Shop.open(TannerID))
            {
                return super.Loop();
            }
        }


        var LeatherWidget = GetTargetWidget();
        if(LeatherWidget != null)
        {
            Logger.log("TanTask: Loop: Opening menu");
            Point Click = new Point((int) LeatherWidget.getRectangle().getCenterX() +
                                    (OSRSUtilities.rand.nextInt(100) - 50),
                                    (int) LeatherWidget.getRectangle().getCenterY() +
                                    (OSRSUtilities.rand.nextInt(100) - 50));
            if(Mouse.click(Click, true) && Menu.contains(TanAllAction))
            {
                Logger.log("TanTask: Loop: Tanning All");
                Menu.clickAction(TanAllAction);
                Sleep.sleepTick();
                return super.Loop();
            }
        }

        return super.Loop();
    }

    private WidgetChild GetTargetWidget()
    {
        var widget = Widgets.getWidget(TannerWidgetID);

        if(widget != null)
        {
            var children = widget.getChildren();
            Logger.log(Arrays.toString(children.toArray()));
            var target = children.stream()
                                 .filter((t) -> t.getText().equalsIgnoreCase(LeatherName))
                                 .findFirst();
            return target.orElse(null);
        }

        return null;

    }

    private NPC GetTanner()
    {
        Logger.log("TanTask: GetTanner: TannerID" + TannerID);
        return NPCs.closest(TannerID);
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
     * @param graphics
     */
    @Override
    public void onDebugPaint(Graphics2D graphics)
    {
        int i = 0;
        for(var npc : NPCs.all())
        {
            if(npc == null) {continue;}

            graphics.drawString(npc.toString(), 10, 20 + i);
            i += 10;
        }
    }

    /**
     * @param Script
     *
     * @return return true if successful, false if we need more time, keep triggering start until it is ready
     */
    @Override
    public boolean onStartTask(IFScript Script)
    {
        return super.onStartTask(Script);
    }
}
