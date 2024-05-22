package Cycles.SimpleTasks.Combat;

import Utilities.Scripting.SimpleTask;

import javax.annotation.Nonnull;

public class LootLookoutTask extends SimpleTask
{
    //TODO
    public LootLookoutTask(String Name)
    {
        super(Name);
    }

    @Nonnull
    @Override
    public TaskType GetTaskType()
    {
        return TaskType.LootLookout;
    }
}
