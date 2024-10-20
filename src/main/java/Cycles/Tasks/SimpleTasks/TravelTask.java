package Cycles.Tasks.SimpleTasks;

import Utilities.OSRSUtilities;
import Utilities.Patterns.Delegates.Delegate;
import Utilities.Scripting.IFScript;
import Utilities.Scripting.Obstacles;
import Utilities.Scripting.SimpleTask;
import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.input.mouse.MouseSettings;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.walking.pathfinding.impl.local.LocalPathFinder;
import org.dreambot.api.methods.walking.pathfinding.impl.obstacle.impl.DestructableObstacle;
import org.dreambot.api.utilities.Logger;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Random;

public class TravelTask extends SimpleTask
{
    // percentage chance
    final private Map<OSRSUtilities.ScriptIntenity, Integer> LoseFocusChance      = Map.of(
            OSRSUtilities.ScriptIntenity.Lax,
            20,
            OSRSUtilities.ScriptIntenity.Normal,
            10,
            OSRSUtilities.ScriptIntenity.Sweating,
            3,
            OSRSUtilities.ScriptIntenity.Bot,
            0);
    private final Tile                                       Destination;
    public        Delegate                                   onReachedDestination = new Delegate();
    private       Random                                     rand                 = new Random();

    public TravelTask(String Name, Tile Destination)
    {
        super(Name);
        this.Destination = Destination;
    }

    @Nonnull
    @Override
    public TaskType GetTaskType()
    {
        return TaskType.Travel;
    }

    @Override
    public boolean onStartTask(IFScript Script)
    {
        super.onStartTask(Script);
        MouseSettings                Setting   = Mouse.getMouseSettings();
        OSRSUtilities.ScriptIntenity Intensity = GetScriptIntensity();
        Obstacles.InitCustomObstacles();

        Logger.log("TravelTask: onStartTask: Current Tile: " + Players.getLocal().getTile() +
                   ", Destination: " + Destination);

        //        switch(Intensity)
        //        {
        //            case Lax ->
        //            {
        //                Setting.setClick(true);
        //                Setting.setOvershoot(true);
        //                Setting.setPreferredHand(MouseSettings.Hand.LEFT);
        //                Script.SetRandomizerParameters(5, 1.0f, 15);
        //            }
        //            case Normal ->
        //            {
        //                Setting.setClick(true);
        //                Setting.setOvershoot(true);
        //                Setting.setPreferredHand(MouseSettings.Hand.RIGHT);
        //                Script.SetRandomizerParameters(15, 1.0f, 20);
        //            }
        //            case Sweating ->
        //            {
        //                Setting.setClick(false);
        //                Setting.setOvershoot(false);
        //                Setting.setWalking(false);
        //                Setting.setPreferredHand(MouseSettings.Hand.RIGHT);
        //                Setting.setUseMiddleMouseInInteracts(true);
        //                Script.SetRandomizerParameters(40, 1.0f, 20);
        //            }
        //            case Bot ->
        //            {
        //                Setting.setClick(false);
        //                Setting.setOvershoot(false);
        //                Setting.setWalking(false);
        //                Setting.setPreferredHand(MouseSettings.Hand.RIGHT);
        //                Script.SetRandomizerParameters(80, 1.0f, 20);
        //            }
        //        }

        return true;
    }

    @Override
    public boolean onStopTask(IFScript Script)
    {
        //Script.ResetRandomizer();
        return true;
    }

    @Override
    public void onReplaced(IFScript Script, SimpleTask other)
    {
        super.onReplaced(Script, other);
        Script.StopTaskNow(this);
    }

    @Override
    public int Loop()
    {
        OSRSUtilities.ScriptIntenity Intensity = GetScriptIntensity();


        LocalPathFinder.getLocalPathFinder().addObstacle(new DestructableObstacle("Web", "Slash"));

        Walking.walk(Destination);
        //        LocalPathFinder.getLocalPathFinder().addObstacle(new PassableObstacle("Web",
        //                                                                              "Slash"));
        //
        //        var Path = LocalPathFinder.getLocalPathFinder().calculate(Players.getLocal().getTile(), Destination);
        //        var Web = WebFinder.getWebFinder().calculate(Players.getLocal().getTile(), Destination);
        //
        //        Tile WebObstacleTile = new Tile(3210, 9898, 0);
        //
        //        Web.add( new EntranceWebNode(WebObstacleTile, "Web", "Slash"));
        //        //WebFinder.getWebFinder().addWebNode();
        //        if(!Path.isEmpty())
        //        {
        //            Logger.log("Path");
        //            Path.walk();
        //        }
        //        else if(Web.next().getTile().canReach())
        //        {
        //            Logger.log("Web");
        //            Web.walk();
        //        }
        //        else
        //        {
        //            Logger.log("Path Else");
        //            Path.walk();
        //        }


        //        if(rand.nextInt(100) < LoseFocusChance.get(Intensity))
        //        {
        //            switch(rand.nextInt(7))
        //            {
        //                case 0:
        //                    // Move outside screen
        //                    Mouse.moveOutsideScreen();
        //                    OSRSUtilities.Wait(Intensity, 1.0f);
        //                    break;
        //                case 1:
        //                    // Examine random item
        //                    OSRSUtilities.ExamineRandomInventoryItem();
        //                    OSRSUtilities.Wait(Intensity, 1.0f);
        //                    break;
        //                case 2:
        //                {
        //                    // hover over random object
        //                    var all = GameObjects.all();
        //                    var obj = all.get(rand.nextInt(all.size() - 1));
        //                    OSRSUtilities.RandomizeClick(obj.getCenterPoint(), 20, 20);
        //                    OSRSUtilities.Wait(Intensity, 1.0f);
        //                }
        //                break;
        //                case 3:
        //                {
        //                    // follow random object
        //                    var  all       = GameObjects.all();
        //                    var  obj       = all.get(rand.nextInt(all.size() - 1));
        //                    long startTime = System.nanoTime();
        //                    long HoverTime = rand.nextLong(TimeUnit.MILLISECONDS.toNanos(5000));
        //                    while(System.nanoTime() - startTime < HoverTime)
        //                    {
        //                        Mouse.move(OSRSUtilities.RandomizeClick(obj.getCenterPoint(), 3, 3));
        //                        OSRSUtilities.Wait(Intensity, 5.0f);
        //                    }
        //                    OSRSUtilities.Wait(Intensity, 1.0f);
        //                }
        //                break;
        //                case 4:
        //                    // switch tab
        //                    if(Intensity == OSRSUtilities.ScriptIntenity.Sweating ||
        //                       Intensity == OSRSUtilities.ScriptIntenity.Bot)
        //                    {
        //                        break;
        //                    }
        //                    Tab OpenTab = Tabs.getOpen();
        //                    var allTabs = Tab.values();
        //                    Tabs.open(allTabs[rand.nextInt(allTabs.length - 1)]);
        //                    OSRSUtilities.Wait(Intensity, 1.0f);
        //                    Tabs.open(OpenTab);
        //                    break;
        //                case 5:
        //                    //just wait
        //                    OSRSUtilities.Wait(Intensity, 10.0f);
        //                    break;
        //                case 6:
        //                    // reset camera
        //                    OSRSUtilities.ResetCameraRandom(3000);
        //                    OSRSUtilities.Wait(Intensity, 3.0f);
        //                    break;
        //                case 7:
        //                    // TODO Open minimap and wait
        //                    break;
        //            }
        //        }

        //        if(CompleteCondition != null)
        //        {
        //            Logger.log("CompleteCondition result: " + CompleteCondition.get());
        //        }
        if(Destination.getArea(5).contains(Players.getLocal().getServerTile()))
        {
            Logger.log("Reached Destination");
            onReachedDestination.Fire();
            return 0;
        }
        if((CompleteCondition != null && CompleteCondition.get()))
        {
            Logger.log("Complete Condition fulfilled");
            return 0;
        }
        return super.Loop();
    }
}
