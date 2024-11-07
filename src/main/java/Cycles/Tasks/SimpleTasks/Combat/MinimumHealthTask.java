package Cycles.Tasks.SimpleTasks.Combat;

import OSRSDatabase.FoodDB;
import Utilities.OSRSUtilities;
import Utilities.Scripting.Logger;
import Utilities.Scripting.SimpleTask;
import org.dreambot.api.Client;
import org.dreambot.api.data.consumables.Food;
import org.dreambot.api.methods.interactive.Players;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class MinimumHealthTask extends SimpleTask
{
    private final int MinimumHealth;

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
        var foods = FoodDB.GetCommonFoods(Client.isMembers());
        var min   = OSRSUtilities.HPtoPercent(MinimumHealth);
        Logger.log("Player Healthpercent: " + Players.getLocal().getHealthPercent() +
                   " Minimum Healthpercent: " + min + " MinimumHealth " + MinimumHealth);
        return Players.getLocal().getHealthPercent() < min &&
               OSRSUtilities.InventoryContainsAny(Arrays.stream(foods)
                                                        .mapToInt(t -> t.id)
                                                        .toArray()) && super.Ready();
    }

    @Override
    public int Loop()
    {
        OSRSUtilities.ScriptIntenity Intensity = GetScriptIntensity();

        Food.eat(OSRSUtilities.HPtoPercent(MinimumHealth), true);
        return OSRSUtilities.WaitTime(Intensity);
    }
}
