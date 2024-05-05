package Cycles.SimpleTasks;

import Utilities.Scripting.SimpleTask;

import javax.annotation.Nonnull;

public class RestorePrayerTask extends SimpleTask
{
    public RestorePrayerTask(String Name)
    {
        super(Name);
    }

    @Nonnull
    @Override
    public TaskType GetTaskType()
    {
        return TaskType.RestorePrayer;
    }
}
