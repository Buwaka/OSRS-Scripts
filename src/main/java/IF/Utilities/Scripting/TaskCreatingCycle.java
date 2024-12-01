package IF.Utilities.Scripting;

public abstract class TaskCreatingCycle extends SimpleCycle
{
    public TaskCreatingCycle(String name, String authKey)
    {
        super(name, authKey);
    }

    public abstract SimpleTask[] CreateTasks();
}
