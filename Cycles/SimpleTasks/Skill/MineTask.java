package Cycles.SimpleTasks.Skill;

import Utilities.OSRSUtilities;
import Utilities.Scripting.SimpleTask;
import Utilities.Scripting.tpircSScript;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.items.Item;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

public class MineTask extends SimpleTask
{
    Integer[] MineObjects         = null;
    int       MineInteractTimeout = 10000;
    int       MineTimeout         = 20000;
    Semaphore InventorySemaphore  = new Semaphore(1);
    private HashMap<Tile, Integer> Rocks         = new HashMap<>();
    private GameObject             Target        = null;
    private boolean                StartedMining = false;

    public MineTask(String Name, int... IDs)
    {
        super(Name);
        MineObjects = Arrays.stream(IDs).boxed().toArray(Integer[]::new);
    }

    //TODO check for pickaxe in equipment slot or inventory
    @Override
    protected boolean Ready()
    {
        var target = GetTarget();
        return target != null && super.Ready();
    }

    public GameObject GetTarget()
    {
        if(Target == null || OSRSUtilities.IsTimeElapsed(Players.getLocal().getUID(), 1000))
        {
            var rocks = GameObjects.all(MineObjects);
            rocks.sort((x, y) -> (int) (y.walkingDistance(Players.getLocal().getTile()) -
                                        x.walkingDistance(Players.getLocal().getTile())));
            for(var rock : rocks)
            {
                boolean NoPlayersPossiblyMining = Players.all(x -> rock.getTile().getArea(2).contains(x.getTile()) &&
                                                                   x.isAnimating() &&
                                                                   x != Players.getLocal()).isEmpty();
                boolean distance = rock.getTile().distance() < 10.0;
                boolean canReach = rock.canReach();
                if(distance && canReach)
                {
                    Rocks.putIfAbsent(rock.getTile(), rock.getID());
                    if(NoPlayersPossiblyMining)
                    {
                        Target = rock;
                    }
                }
            }
        }
        return Target;
    }

    @Override
    protected int Loop()
    {
        if(Inventory.isFull())
        {
            return 0;
        }

        if(Players.getLocal().isAnimating())
        {
            return super.Loop();
        }

        if(StartedMining && !RockAvailable())
        {
            OSRSUtilities.JumpToOtherWorld(GetScript().onGameTick);
        }

        var target = GetTarget();
        if(target != null && Sleep.sleepUntil(() -> target.interact("Mine"), MineInteractTimeout))
        {
            Logger.log("Mine interact succesful");
            InventorySemaphore.tryAcquire();
            Logger.log("Acquired permit");
            //this.GetScript().onGameTick.WaitTicks(3);
            Sleep.sleepUntil(() -> InventorySemaphore.tryAcquire() || !target.exists(), MineTimeout);
            Logger.log("Acquired permit");
            StartedMining = true;
        }

        return super.Loop();
    }

    private boolean RockAvailable()
    {
        for(var set : Rocks.entrySet())
        {
            var rock = GameObjects.getTopObjectOnTile(set.getKey());
            if(rock != null && rock.getID() == set.getValue())
            {
                return true;
            }
        }
        return false;
    }

    @Nonnull
    @Override
    public TaskType GetTaskType()
    {
        return TaskType.MineTask;
    }

    /**
     * @param Script
     *
     * @return return true if successful, false if we need more time, keep triggering start until it is ready
     */
    @Override
    public boolean onStartTask(tpircSScript Script)
    {
        Script.onInventory.Subscribe(this, MineTask::onMine);
        return super.onStartTask(Script);
    }

    private static Boolean onMine(Object context, tpircSScript.ItemAction action, Item item, Item item1)
    {
        ((MineTask) context).InventorySemaphore.release();
        Logger.log("released permit");
        return true;
    }
}
