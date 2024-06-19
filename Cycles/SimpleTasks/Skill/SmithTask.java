package Cycles.SimpleTasks.Skill;

import Utilities.Scripting.SimpleTask;
import Utilities.Scripting.tpircSScript;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.filter.impl.IdFilter;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.widget.helpers.Smithing;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.items.Item;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicInteger;

public class SmithTask extends SimpleTask
{
    private final int           MaxAttempts            = 10;
    public        int           DefaultProcessTickTime = 7;
    private       int[]         ObjectID               = null;
    private       String        InteractAction         = null;
    private       String        Choice                 = null;
    private       Integer       Count                  = null;
    private       AtomicInteger TimeoutTicker          = new AtomicInteger();
    private       boolean       StartedProcessing      = false;
    private       int           Attempts               = 0;
    private       Tile          BackupTile             = null;


    /**
     * @param Name      Name of the task
     * @param ObjectIDs Object to interact with
     * @param Choice    Action to perform on object
     */
    public SmithTask(String Name, String Choice, int... ObjectIDs)
    {
        super(Name);
        ObjectID    = ObjectIDs;
        this.Choice = Choice;
    }

    /**
     * @param Name      Name of the task
     * @param ObjectIDs Object to interact with
     * @param Choice    Action to perform on object
     * @param Action    What action to perform on object
     */
    public SmithTask(String Name, String Choice, String Action, int... ObjectIDs)
    {
        super(Name);
        ObjectID       = ObjectIDs;
        this.Choice    = Choice;
        InteractAction = Action;
    }

    public Integer getCount()
    {
        return Count;
    }

    public void setCount(Integer count)
    {
        Count = count;
    }

    void SetBackupTile(Tile BackupTile)
    {
        this.BackupTile = BackupTile;
    }

    /**
     * @return
     */
    @Override
    protected boolean Ready()
    {
        var Obj = GetObject(ObjectID);
        return Obj != null && Obj.canReach() && super.Ready();
    }

    @SafeVarargs
    public static GameObject GetObject(int... ID)
    {
        return GameObjects.closest(new IdFilter<>(ID));
    }

    /**
     * @return
     */
    @Override
    protected int Loop()
    {
        if(ObjectID == null)
        {
            Logger.log("SmithTask: Gameobject is null");
            return 0;
        }

        if(Dialogues.inDialogue())
        {
            Logger.log("SmithTask: In Dialogue");
            StartedProcessing = false;
        }

        if(Attempts > MaxAttempts)
        {
            Logger.log("SmithTask: Too many failed attempts");
            return 0;
        }

        if(Smithing.isOpen())
        {
            Logger.log("SmithTask: Process");
            if(Count == null)
            {
                Smithing.makeAll(Choice);
            }
            else
            {
                Smithing.make(Choice, Count);
            }
            StartedProcessing = true;
            Attempts          = 0;
            return super.Loop();
        }

        if(StartedProcessing)
        {
            Logger.log("SmithTask: Ticker: " + TimeoutTicker.get());
            if(TimeoutTicker.get() < 0)
            {
                Logger.log("SmithTask: Timeout");
                return 0;
            }
            return super.Loop();
        }
        else
        {
            GameObject Obj = GetObject(ObjectID);
            Logger.log("SmithTask: Interact with " + Obj +
                       (InteractAction == null ? "" : " With Action " + InteractAction));
            boolean result;
            if(InteractAction == null)
            {
                result = Sleep.sleepUntil(() -> Obj.interact(), 10000, 2000);
            }
            else
            {
                result = Sleep.sleepUntil(() -> Obj.interact(InteractAction), 10000, 2000);
            }
            if(!result)
            {
                if(BackupTile != null)
                {
                    Walking.walk(BackupTile);
                }
                else
                {
                    Walking.walk(Obj.getTile().getArea(3).getRandomTile());
                }
            }
            else
            {
                Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 3000);
            }
            Logger.log("SmithTask: Interaction result: " + result);
            Attempts++;
            TimeoutTicker.set(DefaultProcessTickTime);
            GetScript().onGameTick.AddUpdateTicker(this, TimeoutTicker);
        }
        return super.Loop();
    }

    /**
     * @return
     */
    @Nonnull
    @Override
    public TaskType GetTaskType()
    {
        return TaskType.UseOnObjectTask;
    }

    /**
     * @param Script
     *
     * @return return true if successful, false if we need more time, keep triggering start until it is ready
     */
    @Override
    public boolean onStartTask(tpircSScript Script)
    {
        Script.onInventory.Subscribe(this, SmithTask::CheckInventory);
        return super.onStartTask(Script);
    }

    private static Boolean CheckInventory(Object context, tpircSScript.ItemAction Action, Item item1, Item item2)
    {
        ((SmithTask) context).TimeoutTicker.set(((SmithTask) context).DefaultProcessTickTime);
        return true;
    }
}
