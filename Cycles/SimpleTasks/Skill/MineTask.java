package Cycles.SimpleTasks.Skill;

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
    private HashMap<Tile, Integer> Rocks = new HashMap<>();

    public MineTask(String Name, int... IDs)
    {
        super(Name);
        MineObjects = Arrays.stream(IDs).boxed().toArray(Integer[]::new);
    }

    private static Boolean onMine(Object context, tpircSScript.ItemAction action, Item item, Item item1)
    {
        ((MineTask) context).InventorySemaphore.release();
        Logger.log("released permit");
        return true;
    }

    public GameObject GetTarget()
    {
        var Rock = GameObjects.closest(t -> {
            boolean MatchID = Arrays.stream(MineObjects).anyMatch(x -> x == t.getID());
            boolean NoPlayersPossiblyMining = Players.all(x -> t.getTile().getArea(2).contains(x.getTile()) &&
                                                               x.isAnimating() && x != Players.getLocal()).isEmpty();
            boolean distance = t.getTile().distance() < 10.0;
            return MatchID && NoPlayersPossiblyMining && distance;
        });
        if(Rock != null)
        {
            Rocks.putIfAbsent(Rock.getTile(), Rock.getID());
        }

        return Rock;
    }

    //TODO check for pickaxe in equipment slot or inventory
    @Override
    protected boolean Ready()
    {
        var target = GetTarget();
//        if(target == null && !Rocks.isEmpty())
//        {
//            OSRSUtilities.JumpToOtherWorld();
//            target = GetTarget();
//        }
        return target != null && super.Ready();
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

        var target = GetTarget();
        if(Sleep.sleepUntil(() -> target.interact("Mine"), MineInteractTimeout))
        {
            Logger.log("Mine interact succesful");
            InventorySemaphore.tryAcquire();
            Logger.log("Acquired permit");
            //this.GetScript().onGameTick.WaitTicks(3);
            Sleep.sleepUntil(() -> InventorySemaphore.tryAcquire() || !target.exists(), MineTimeout);
            Logger.log("Acquired permit");
        }

        return super.Loop();
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
}
