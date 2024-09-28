package Cycles.Tasks.SimpleTasks.ItemProcessing;

import Utilities.Scripting.ITask;
import Utilities.Scripting.SimpleTask;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.items.Item;

import javax.annotation.Nonnull;

public class InteractInventoryTask extends SimpleTask
{
    private int[]  ItemID  = null;
    private Item   ItemRef = null;
    private String Action  = null;

    private Integer Tool = null;

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
        if(ItemRef == null)
        {
            ItemRef = Inventory.get(ItemID);
        }

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

        return success ? 0 : super.Loop();
    }
}
