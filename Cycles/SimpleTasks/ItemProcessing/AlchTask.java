package Cycles.SimpleTasks.ItemProcessing;

import Utilities.Scripting.SimpleTask;
import org.dreambot.api.wrappers.items.Item;

import javax.annotation.Nonnull;

public class AlchTask extends SimpleTask
{
    private Item AlchItem;
    public AlchTask(String Name, Item ToAlch)
    {
        super(Name);
        AlchItem = ToAlch;
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
    //TODO
}
