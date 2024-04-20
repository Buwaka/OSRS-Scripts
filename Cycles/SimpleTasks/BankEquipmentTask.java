package Cycles.SimpleTasks;

import Utilities.Scripting.SimpleTask;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class BankEquipmentTask extends SimpleTask
{
    private Supplier<Boolean> CompleteCondition;

    //TODO
    public BankEquipmentTask(String Name)
    {
        super(Name);
    }

    @Override
    public boolean accept()
    {
        return false;
    }

    @Override
    public int execute()
    {
        return 0;
    }

    @Nonnull
    @Override
    public TaskType GetTaskType()
    {
        return TaskType.BankEquipment;
    }
}
