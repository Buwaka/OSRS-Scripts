package Cycles.SimpleTasks.ItemProcessing;

import Utilities.OSRSUtilities;
import Utilities.Scripting.SimpleTask;
import Utilities.Scripting.tpircSScript;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
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

public class InteractTask extends SimpleTask
{
    Integer[] ObjectIDs          = null;
    int       InteractTimeout    = 20000;
    int       Timeout            = 30000;
    Semaphore InventorySemaphore = new Semaphore(1);
    private HashMap<Tile, Integer> Objects            = new HashMap<>();
    private GameObject             Target             = null;
    private boolean                StartedInteracting = false;
    private boolean WaitForInventory = true;

    public InteractTask(String Name, int... IDs)
    {
        super(Name);
        ObjectIDs = Arrays.stream(IDs).boxed().toArray(Integer[]::new);
    }

    public void SetWaitForInventory(boolean wait) {
        WaitForInventory = wait;
    }

    @Nonnull
    @Override
    public TaskType GetTaskType()
    {
        return TaskType.InteractTask;
    }

    /**
     * @param Script
     *
     * @return return true if successful, false if we need more time, keep triggering start until it is ready
     */
    @Override
    public boolean onStartTask(tpircSScript Script)
    {
        Script.onInventory.Subscribe(this, InteractTask::onMine);
        return super.onStartTask(Script);
    }

    private static Boolean onMine(Object context, tpircSScript.ItemAction action, Item item, Item item1)
    {
        ((InteractTask) context).InventorySemaphore.release();
        Logger.log("released permit");
        return true;
    }

    //TODO check for pickaxe in equipment slot or inventory
    @Override
    protected boolean Ready()
    {
        var target = GetTarget();
        Logger.log("InteractTask: Ready: target = " + target);
        return target != null && super.Ready();
    }

    public GameObject GetTarget()
    {
        if(Target == null || OSRSUtilities.IsTimeElapsed(Players.getLocal().getUID(), 1000))
        {
            var rocks = GameObjects.all(ObjectIDs);
            rocks.sort((x, y) -> (int) (y.walkingDistance(Players.getLocal().getTile()) -
                                        x.walkingDistance(Players.getLocal().getTile())));
            for(var rock : rocks)
            {
                Logger.log("InteractTask: GetTarget: Possible Target: " + rock);
//                boolean NoPlayersPossiblyMining = Players.all(x -> rock.getTile().getArea(2).contains(x.getTile()) &&
//                                                                   x.isAnimating() &&
//                                                                   x != Players.getLocal()).isEmpty();
                boolean distance = rock.getTile().distance() < 10.0;
                boolean canReach = rock.canReach();
                if(distance && canReach)
                {
                    Objects.putIfAbsent(rock.getTile(), rock.getID());
                    Target = rock;
//                    if(NoPlayersPossiblyMining)
//                    {
//                        Target = rock;
//                    }
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
            if(Sleep.sleepUntil(() -> Bank.open(), 60000))
            {
                Bank.depositAllItems();
            }
        }

        if(Players.getLocal().isAnimating())
        {
            return super.Loop();
        }

        if(StartedInteracting && !ObjectsAvailable())
        {
            OSRSUtilities.JumpToOtherWorld(GetScript().onGameTick);
        }

        var target = GetTarget();
        if(target != null && Sleep.sleepUntil(target::interact, InteractTimeout))
        {
            Logger.log("InteractTask: Loop: interact successful");
            StartedInteracting = true;
            InventorySemaphore.tryAcquire();
            Logger.log("Acquired permit");
            if(WaitForInventory)
            {
                Sleep.sleepUntil(() -> InventorySemaphore.tryAcquire() || !target.exists(), Timeout);
            }

        }

        return super.Loop();
    }

    private boolean ObjectsAvailable()
    {
        for(var set : Objects.entrySet())
        {
            var rock = GameObjects.getTopObjectOnTile(set.getKey());
            if(rock != null && rock.getID() == set.getValue())
            {
                return true;
            }
        }
        return false;
    }
}
