package Cycles.SimpleTasks.Combat;

import Database.OSRSDataBase;
import Utilities.OSRSUtilities;
import Utilities.Scripting.SimpleTask;
import org.dreambot.api.Client;
import org.dreambot.api.data.consumables.Food;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.utilities.Logger;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.function.Supplier;

public class MinimumHealthTask extends SimpleTask
{
    private int               MinimumHealth;
    private Supplier<Boolean> CompleteCondition;

    public MinimumHealthTask(String Name, int MinHealth)
    {
        super(Name);
        MinimumHealth = MinHealth;
    }

    public int GetMinimumHealth() {return MinimumHealth;}

    @Nonnull
    @Override
    public TaskType GetTaskType()
    {
        return TaskType.MinimumHealth;
    }

    @Override
    public boolean Ready()
    {
        var foods = OSRSDataBase.GetCommonFoods(Client.isMembers());
        var min   = OSRSUtilities.HPtoPercent(MinimumHealth);
        Logger.log("Player Healthpercent: " + Players.getLocal().getHealthPercent() + " Minimum Healthpercent: " + min +
                   " MinimumHealth " + MinimumHealth);
        return Players.getLocal().getHealthPercent() < min &&
               OSRSUtilities.InventoryContainsAny(Arrays.stream(foods).mapToInt(t -> t.id).toArray()) && super.Ready();
    }

    @Override
    public int Loop()
    {
        OSRSUtilities.ScriptIntenity Intensity = ScriptIntensity.get();

        Food.eat(OSRSUtilities.HPtoPercent(MinimumHealth), true);
        return OSRSUtilities.WaitTime(Intensity);
    }
}
