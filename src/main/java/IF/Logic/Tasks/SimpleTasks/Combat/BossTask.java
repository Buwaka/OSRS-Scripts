package IF.Logic.Tasks.SimpleTasks.Combat;

import IF.Utilities.Scripting.SimpleTask;

import javax.annotation.Nonnull;

public class BossTask extends SimpleTask
{
    public BossTask(String Name)
    {
        super(Name);
    }

    /**
     * @return
     */
    @Nonnull
    @Override
    public TaskType GetTaskType()
    {
        return TaskType.BossTask;
    }
}
