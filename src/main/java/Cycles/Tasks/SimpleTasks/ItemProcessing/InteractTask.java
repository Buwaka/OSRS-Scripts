package Cycles.Tasks.SimpleTasks.ItemProcessing;

import Utilities.OSRSUtilities;
import Utilities.Scripting.IFScript;
import Utilities.Scripting.SimpleTask;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.Entity;
import org.dreambot.api.wrappers.items.Item;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Semaphore;

public class InteractTask extends SimpleTask
{
    Integer[] ObjectIDs          = null;
    String    Action             = null;
    int       InteractTimeout    = 20000;
    int       Timeout            = 30000;
    long      TargetTimout       = 1000;
    long      TargetTimeStamp    = 0;
    Semaphore InventorySemaphore = new Semaphore(0);
    private EnumSet<InteractableFilter> TargetFilter       = EnumSet.of(InteractableFilter.GameObjects);
    private Entity                      Target             = null;
    private boolean                     StartedInteracting = false;
    private boolean                     WaitForInventory   = true;


    public enum InteractableFilter
    {
        GameObjects,
        NPCs,
        GroundItems,
        Players
    }


    public InteractTask(String Name, int... IDs)
    {
        super(Name);
        ObjectIDs = Arrays.stream(IDs).boxed().toArray(Integer[]::new);
    }

    public InteractTask(String Name, String Action, int... IDs)
    {
        super(Name);
        this.Action = Action;
        ObjectIDs   = Arrays.stream(IDs).boxed().toArray(Integer[]::new);
    }

    public void AddFilter(InteractableFilter... Filter)
    {
        TargetFilter.addAll(List.of(Filter));
    }

    public void SetFilter(InteractableFilter... Filter)
    {
        TargetFilter = EnumSet.noneOf(InteractableFilter.class);
        TargetFilter.addAll(Arrays.asList(Filter));
    }

    public void SetFilter(EnumSet<InteractableFilter> Filter)
    {
        TargetFilter = Filter;
    }

    public void SetWaitForInventory(boolean wait)
    {
        WaitForInventory = wait;
    }

    public static Entity GetTargetByActionStatic(String Action)
    {
        return GetTargetStatic(EnumSet.allOf(InteractableFilter.class), Action, null);
    }

    public static Entity GetTargetStatic(EnumSet<InteractableFilter> TargetFilter, String Action, Integer... IDs)
    {
        List<Entity> toFilter = new ArrayList<>();

        if(TargetFilter.contains(InteractableFilter.GameObjects))
        {
            toFilter.addAll(GameObjects.all());
        }
        if(TargetFilter.contains(InteractableFilter.NPCs))
        {
            toFilter.addAll(NPCs.all());
        }
        if(TargetFilter.contains(InteractableFilter.GroundItems))
        {
            toFilter.addAll(GroundItems.all());
        }
        if(TargetFilter.contains(InteractableFilter.Players))
        {
            toFilter.addAll(Players.all());
        }


        Logger.log(toFilter);
        var first = toFilter.stream()
                            .filter(t -> (IDs == null ||
                                          Arrays.stream(IDs).anyMatch((x) -> x == t.getID())) &&
                                         (Action == null || t.hasAction(Action)) && t.canReach() &&
                                         t.distance() < 10)
                            .sorted((x, y) -> (int) (
                                    x.walkingDistance(Players.getLocal().getTile()) -
                                    y.walkingDistance(Players.getLocal().getTile())))
                            .findFirst();
        Logger.log(first);
        return first.orElse(null);
    }

    public static Entity GetTargetStatic(Integer... IDs)
    {
        return GetTargetStatic(EnumSet.allOf(InteractableFilter.class), null, IDs);
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
    public boolean onStartTask(IFScript Script)
    {
        Script.onInventory.Subscribe(this, this::onItem);
        return super.onStartTask(Script);
    }

    private Boolean onItem(IFScript.ItemAction action, Item item, Item item1)
    {
        InventorySemaphore.release();
        Logger.log("InteractTask: onItem: released permit");
        return true;
    }

    //TODO check for pickaxe in equipment slot or inventory
    @Override
    public boolean Ready()
    {
        var target = GetTarget();
        Logger.log("InteractTask: Ready: target = " + target);
        return target != null && super.Ready();
    }

    @Override
    protected int Loop()
    {
        if(Inventory.isFull())
        {
            return 0;
        }

        if(Players.getLocal().isAnimating() && !Dialogues.inDialogue())
        {
            return super.Loop();
        }

        // only hop world when we are in the right place and circumstances, aka after a successful interact
        if(StartedInteracting && GetTarget() == null)
        {
            OSRSUtilities.JumpToOtherWorld(GetScript().onGameTick);
        }

        var target = GetTarget();

        if(target != null)
        {
            if(target.distance() > 10)
            {
                Walking.walk(target.getTile().getArea(5).getRandomTile());
                return super.Loop();
            }

            if(Sleep.sleepUntil(target::interact, InteractTimeout))
            {
                Logger.log("InteractTask: Loop: interact successful");
                StartedInteracting = true;
                Logger.log("InteractTask: Loop: Acquired permit");
                Sleep.sleepUntil(() -> (!WaitForInventory || InventorySemaphore.tryAcquire()) ||
                                       !target.exists() || Arrays.stream(ObjectIDs)
                                                                 .noneMatch(t -> t ==
                                                                                 target.getID()) ||
                                       Dialogues.inDialogue(), Timeout);


            }
        }


        return super.Loop();
    }

    public Entity GetTarget()
    {
        return GetTarget(false);
    }

    public Entity GetTarget(boolean Regenerate)
    {
        if(Target != null && Target.exists() && !Regenerate &&
           System.nanoTime() - TargetTimeStamp < TargetTimout)
        {
            return Target;
        }

        Target = GetTargetStatic(TargetFilter, Action, ObjectIDs);
        return Target;
    }
}
