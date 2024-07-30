package Cycles.SimpleTasks.Bank;

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
    public boolean Ready()
    {
        return false;
    }

    @Override
    public int Loop()
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
