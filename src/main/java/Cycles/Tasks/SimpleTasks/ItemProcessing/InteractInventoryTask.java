package Cycles.Tasks.SimpleTasks.ItemProcessing;

import Utilities.Scripting.ITask;
import Utilities.Scripting.SimpleTask;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.items.Item;

import javax.annotation.Nonnull;

public class InteractInventoryTask extends SimpleTask
{
    private int[]  ItemID = null;
    private String Action = null;

    private Integer Tool = null;

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
            if(Tool == null)
            {
                if(Action != null)
                {
                    Logger.log(
                            "InteractInventoryTask: Loop: Interact on " + item + " with Action " +
                            Action);
                    success = item.interact(Action);
                }
                else
                {
                    Logger.log("InteractInventoryTask: Loop: Interact on " + item);
                    success = item.interact();
                }
            }
            else
            {
                var tool = Inventory.get(Tool);
                if(tool != null)
                {
                    Logger.log("InteractInventoryTask: Loop: Use tool " + tool + " on " + item);
                    success = tool.useOn(item);
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
