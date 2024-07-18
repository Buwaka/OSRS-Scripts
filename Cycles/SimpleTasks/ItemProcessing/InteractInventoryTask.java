package Cycles.SimpleTasks.ItemProcessing;

import Utilities.Scripting.ITask;
import Utilities.Scripting.SimpleTask;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.wrappers.items.Item;

import javax.annotation.Nonnull;

public class InteractInventoryTask extends SimpleTask
{
    private int[]  ItemID = null;
    private String Action = null;

    public InteractInventoryTask(String Name, String InteractAction, int... ItemIDs)
    {
        super(Name);
        ItemID = ItemIDs;
        Action = InteractAction;
    }

    public InteractInventoryTask(String Name, int... ItemIDs)
    {
        super(Name);
        ItemID = ItemIDs;
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
    protected boolean Ready()
    {
        return Inventory.contains(ItemID);
    }

    /**
     * @return
     */
    @Override
    protected int Loop()
    {
        boolean success = false;
        Item    item    = Inventory.get(ItemID);

        if(item != null)
        {
            if(Action != null)
            {
                success = item.interact(Action);
            }
            else
            {
                success = item.interact();
            }
        }

        return success ? 0 : super.Loop();
    }
}
