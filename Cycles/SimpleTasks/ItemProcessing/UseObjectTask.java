package Cycles.SimpleTasks.ItemProcessing;

import Utilities.OSRSUtilities;
import Utilities.Scripting.SimpleTask;
import Utilities.Scripting.tpircSScript;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.filter.impl.IdFilter;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.widget.helpers.ItemProcessing;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.items.Item;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicInteger;

public class UseObjectTask extends SimpleTask
{
    private final int           MaxAttempts            = 10;
    public        int           DefaultProcessTickTime = 7;
    private       int[]         ObjectID               = null;
    private       Tile          BackupTile             = null;
    private       String        InteractAction         = null;
    private       String        Choice                 = null;
    private       Integer       Count                  = null;
    private       AtomicInteger TimeoutTicker          = new AtomicInteger();
    private       boolean       StartedProcessing      = false;
    private       int           Attempts               = 0;


    /**
     * @param Name      Name of the task
     * @param ObjectIDs Object IDs to search and interact with
     * @param Choice    Action to perform on object
     */
    public UseObjectTask(String Name, String Choice, int... ObjectIDs)
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
    public UseObjectTask(String Name, String Choice, String Action, int... ObjectIDs)
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
        Script.onInventory.Subscribe(this, UseObjectTask::CheckInventory);
        return super.onStartTask(Script);
    }

    private static Boolean CheckInventory(Object context, tpircSScript.ItemAction Action, Item item1, Item item2)
    {
        ((UseObjectTask) context).TimeoutTicker.set(((UseObjectTask) context).DefaultProcessTickTime);
        return true;
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

    public GameObject GetObject(int... IDs)
    {
        var closest = GetObjectStatic(IDs);
        if(closest != null && !closest.getInteractableFrom().isEmpty())
        {
            BackupTile = closest.getInteractableFrom().getFirst();
        }
        return closest;
    }

    public static GameObject GetObjectStatic(int... IDs)
    {
        return GameObjects.closest(new IdFilter<>(IDs));
    }

    /**
     * @return
     */
    @Override
    protected int Loop()
    {
        if(ObjectID == null)
        {
            Logger.log("UseObjectTask: Gameobject is not set");
            return 0;
        }

        if(Dialogues.inDialogue())
        {
            Logger.log("UseObjectTask: In Dialogue");
            StartedProcessing = false;
        }

        if(Attempts > MaxAttempts)
        {
            Logger.log("UseObjectTask: Too many failed attempts");
            OSRSUtilities.ResetCameraRandom(100);
            return 0;
        }

        if(ItemProcessing.isOpen())
        {
            Logger.log("UseObjectTask: Process");
            if(Count == null)
            {
                ItemProcessing.makeAll(Choice);
            }
            else
            {
                ItemProcessing.make(Choice, Count);
            }
            StartedProcessing = true;
            Attempts          = 0;
            return super.Loop();
        }

        if(StartedProcessing)
        {
            Logger.log("UseObjectTask: Ticker: " + TimeoutTicker.get());
            if(TimeoutTicker.get() < 0)
            {
                Logger.log("UseObjectTask: Timeout");
                return 0;
            }
            return super.Loop();
        }
        else
        {
            GameObject Obj = GetObject(ObjectID);
            if(Obj == null)
            {
                Logger.log("UseObjectTask: Object not found, quiting");
                return 0;
            }
            Logger.log("UseObjectTask: Interact with " + Obj +
                       (InteractAction == null ? "" : " With Action " + InteractAction));
            boolean result;
            if(InteractAction == null)
            {
                result = Sleep.sleepUntil(() -> Obj.interact(), 10000, 2000);
            }
            else
            {
                result = Sleep.sleepUntil(() -> Obj.interact(InteractAction), 10000, 2000);
                if(!result)
                {
                    result = Sleep.sleepUntil(() -> Obj.interact(InteractAction, true, false), 10000, 2000);
                }
                else if(!result)
                {
                    result = Sleep.sleepUntil(() -> Obj.interactForceLeft(InteractAction), 10000, 2000);
                }
                else if(!result)
                {
                    result = Sleep.sleepUntil(() -> Obj.interactForceRight(InteractAction), 10000, 2000);
                }
            }

            if(!result)
            {
                if(BackupTile != null)
                {
                    Walking.walk(BackupTile);
                }
                else if(!Obj.getInteractableFrom().isEmpty())
                {
                    Walking.walk(Obj.getInteractableFrom().getFirst());
                }
                else
                {
                    Logger.log("UseObjectTask: Failed both interaction and walking to the object, quiting");
                    return 0;
                }
            }
            else
            {
                Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 3000);
            }

            Logger.log("UseObjectTask: Interaction result: " + result);
            Attempts++;
            TimeoutTicker.set(DefaultProcessTickTime);
            GetScript().onGameTick.AddUpdateTicker(this, TimeoutTicker);
        }
        return super.Loop();
    }

    void SetBackupTile(Tile BackupTile)
    {
        this.BackupTile = BackupTile;
    }
}
