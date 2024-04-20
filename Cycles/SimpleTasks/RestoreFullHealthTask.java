package Cycles.SimpleTasks;

import Database.OSRSDataBase;
import Utilities.OSRSUtilities;
import Utilities.Scripting.SimpleTask;
import org.dreambot.api.Client;
import org.dreambot.api.data.consumables.Food;
import org.dreambot.api.methods.interactive.Players;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.function.Supplier;

public class RestoreFullHealthTask extends SimpleTask
{

    public RestoreFullHealthTask(String Name)
    {
        super(Name);
        SetPassive(true);
    }

    @Nonnull
    @Override
    public TaskType GetTaskType()
    {
        return TaskType.RestoreFullHealth;
    }

    @Override
    public boolean accept()
    {
        var foods = OSRSDataBase.GetCommonFoods(!Client.isMembers());
        return Players.getLocal().getHealthPercent() < 100 &&
               OSRSUtilities.InventoryContainsAny(Arrays.stream(foods).mapToInt(t -> t.id).toArray()) && super.accept();
    }

    @Override
    public int execute()
    {
        OSRSUtilities.ScriptIntenity Intensity = ScriptIntensity.get();

        Food.eat(99, true);
        return accept() ? OSRSUtilities.WaitTime(Intensity) : 0;
    }
}
