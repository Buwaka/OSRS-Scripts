package Cycles.Tasks.SimpleTasks.ItemProcessing;

import Utilities.Scripting.IFScript;
import Utilities.Scripting.Logger;
import Utilities.Scripting.SimpleTask;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.magic.Magic;
import org.dreambot.api.methods.magic.Normal;
import org.dreambot.api.wrappers.items.Item;

import javax.annotation.Nonnull;

public class AlchTask extends SimpleTask
{
    private final int     AlchItemID;
    private       Item    AlchItem;
    private       int     TotalToAlch = -1;
    private       boolean Ready       = true;

    public AlchTask(String Name, int ToAlch, int count)
    {
        super(Name);
        AlchItemID  = ToAlch;
        TotalToAlch = count;
    }

    public AlchTask(String Name, int ToAlch)
    {
        super(Name);
        AlchItemID = ToAlch;
    }

    /**
     * @return
     */
    @Nonnull
    @Override
    public TaskType GetTaskType()
    {
        return TaskType.AlchTask;
    }

    /**
     * @param Script
     *
     * @return return true if successful, false if we need more time, keep triggering start until it is ready
     */
    @Override
    public boolean onStartTask(IFScript Script)
    {
        AlchItem = Inventory.get(AlchItemID);
        Script.onInventory.Subscribe(this, (a, b, c) -> Ready = true);
        Logger.log("AlchTask: onStartTask: Starting Alch of item " + AlchItem);
        return super.onStartTask(Script);
    }

    /**
     * @return
     */
    @Override
    public boolean Ready()
    {
        return Magic.canCast(Normal.HIGH_LEVEL_ALCHEMY) && super.Ready();
    }

    /**
     * @return
     */
    @Override
    protected int Loop()
    {
        Logger.log("AlchTask: Loop: Alching now");
        if(!Inventory.contains(AlchItemID))
        {
            return 0;
        }

        if(Ready)
        {
            if(Magic.castSpellOn(Normal.HIGH_LEVEL_ALCHEMY, AlchItem))
            {
                Ready = false;
            }
        }

        return super.Loop();
    }
    //TODO
}
