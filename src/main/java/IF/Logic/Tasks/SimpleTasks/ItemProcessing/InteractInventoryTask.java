package IF.Logic.Tasks.SimpleTasks.ItemProcessing;

import IF.Utilities.Scripting.ITask;
import IF.Utilities.Scripting.Logger;
import IF.Utilities.Scripting.SimpleTask;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.wrappers.items.Item;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class InteractInventoryTask extends SimpleTask
{
    private int[]   ItemID        = null;
    private Item    ItemRef       = null;
    private String  Action        = null;
    private Integer Tool          = null;
    private int     InteractCount = 1;
    private int     count         = 0;


    public InteractInventoryTask(String Name, String InteractAction, int... ItemIDs)
    {
        super(Name);
        ItemID = ItemIDs;
        Action = InteractAction;
    }

    public InteractInventoryTask(String Name, Item Item)
    {
        super(Name);
        ItemRef = Item;
    }

    public InteractInventoryTask(String Name, String InteractAction, Item Item)
    {
        super(Name);
        ItemRef = Item;
        Action  = InteractAction;
    }

    public InteractInventoryTask(String Name, int... ItemIDs)
    {
        super(Name);
        ItemID = ItemIDs;
    }

    public void setTool(int tool)
    {
        Tool = tool;
    }

    public static InteractInventoryTask[] PrayBonesAndAshes()
    {
        final String BuryAction    = "Bury";
        final String ScatterAction = "Scatter";


        var Bury = Inventory.all(t -> t.hasAction(BuryAction))
                            .stream()
                            .mapToInt((t) -> t.getID())
                            .distinct()
                            .toArray();
        var Scatter = Inventory.all(t -> t.hasAction(ScatterAction))
                               .stream()
                               .mapToInt((t) -> t.getID())
                               .distinct()
                               .toArray();

        List<InteractInventoryTask> out = new ArrayList<>();
        if(Bury != null && Bury.length > 0)
        {
            var task = new InteractInventoryTask("Bury", BuryAction, Bury);
            task.setInteractCount(-1);
            out.add(task);
        }
        if(Scatter != null && Scatter.length > 0)
        {
            var task = new InteractInventoryTask("Bury", ScatterAction, Scatter);
            task.setInteractCount(-1);
            out.add(task);
        }

        return out.toArray(new InteractInventoryTask[0]);
    }

    public void setInteractCount(int interactCount)
    {
        if(interactCount < 0)
        {
            InteractCount = Integer.MAX_VALUE;
        }
        else
        {
            InteractCount = interactCount;
        }
    }

    @Nonnull
    @Override
    public TaskType GetTaskType()
    {
        return ITask.TaskType.InteractInventoryTask;
    }

    /**
     * @return
     */
    @Override
    public boolean Ready()
    {
        return ItemRef != null || (ItemID != null && Inventory.contains(ItemID));
    }

    /**
     * @return
     */
    @Override
    protected int Loop()
    {
        boolean success = false;
        ItemRef = Inventory.get(ItemID);

        if(ItemRef != null)
        {
            if(Tool == null)
            {
                if(Action != null)
                {
                    Logger.log("InteractInventoryTask: Loop: Interact on " + ItemRef +
                               " with Action " + Action);
                    success = ItemRef.interact(Action);
                }
                else
                {
                    Logger.log("InteractInventoryTask: Loop: Interact on " + ItemRef);
                    success = ItemRef.interact();
                }
            }
            else
            {
                var tool = Inventory.get(Tool);
                if(tool != null)
                {
                    Logger.log("InteractInventoryTask: Loop: Use tool " + tool + " on " + ItemRef);
                    success = tool.useOn(ItemRef);
                }
                else
                {
                    Logger.log("InteractInventoryTask: Loop: Tool not found");
                    success = false;
                }

            }

        }
        if(success)
        {
            count++;
        }

        if(count >= InteractCount || ItemRef == null)
        {
            return 0;
        }
        return super.Loop();
    }
}
