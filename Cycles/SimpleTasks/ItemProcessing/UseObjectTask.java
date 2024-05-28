package Cycles.SimpleTasks.ItemProcessing;

import Cycles.SimpleTasks.TravelTask;
import Utilities.OSRSUtilities;
import Utilities.Scripting.SimpleTask;
import Utilities.Scripting.tpircSScript;
import org.dreambot.api.methods.dialogues.Dialogues;
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
    private GameObject ObjectToUeOn   = null;
    private Tile       BackupTile     = null;
    private String     InteractAction = null;
    private       String        Choice                 = null;
    private       Integer       Count                  = null;
    private       AtomicInteger TimeoutTicker          = new AtomicInteger();
    private       boolean       StartedProcessing      = false;
    private       int           Attempts               = 0;


    /**
     * @param Name   Name of the task
     * @param Target Object to interact with
     * @param Choice Action to perform on object
     */
    public UseObjectTask(String Name, GameObject Target, String Choice)
    {
        super(Name);
        ObjectToUeOn = Target;
        this.Choice  = Choice;
    }

    /**
     * @param Name   Name of the task
     * @param Target Object to interact with
     * @param Choice Action to perform on object
     * @param Amount the amount that we want to make
     */
    public UseObjectTask(String Name, GameObject Target, String Choice, int Amount)
    {
        super(Name);
        ObjectToUeOn = Target;
        this.Choice  = Choice;
        Count        = Amount;
    }

    /**
     * @param Name   Name of the task
     * @param Target Object to interact with
     * @param Choice Action to perform on object
     * @param Action What action to perform on object
     */
    public UseObjectTask(String Name, GameObject Target, String Choice, String Action)
    {
        super(Name);
        ObjectToUeOn   = Target;
        this.Choice    = Choice;
        InteractAction = Action;
    }

    /**
     * @param Name   Name of the task
     * @param Target Object to interact with
     * @param Choice What menu item to choose
     * @param Action What action to perform on object
     * @param Amount the amount that we want to make
     */
    public UseObjectTask(String Name, GameObject Target, String Choice, String Action, int Amount)
    {
        super(Name);
        ObjectToUeOn   = Target;
        this.Choice    = Choice;
        Count          = Amount;
        InteractAction = Action;
    }

    void SetBackupTile(Tile BackupTile)
    {
        this.BackupTile = BackupTile;
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
        return ObjectToUeOn.canReach() && super.Ready();
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

    /**
     * @return
     */
    @Override
    protected int Loop()
    {
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
        }
        else
        {
            Logger.log("UseObjectTask: Interact with " + ObjectToUeOn + ( InteractAction == null ? "" : " With Action " + InteractAction));
            boolean result;
            if(InteractAction == null)
            {
                result = ObjectToUeOn.interact();
            }
            else
            {
                result = ObjectToUeOn.interact(InteractAction);
            }
            if(!result)
            {
                if(BackupTile != null)
                {
                    Walking.walk(BackupTile);
                }
                else
                {
                    Walking.walk(ObjectToUeOn.getTile().getArea(3).getRandomTile());
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
}
