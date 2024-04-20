package Cycles.SimpleTasks;

import Database.OSRSDataBase;
import Utilities.OSRSUtilities;
import Utilities.Scripting.SimpleTask;
import org.dreambot.api.Client;
import org.dreambot.api.data.consumables.Food;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.utilities.Logger;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.function.Supplier;

public class MinimumHealthTask extends SimpleTask
{
    private int MinimumHealth;
    private Supplier<Boolean> CompleteCondition;

    public MinimumHealthTask(String Name, int MinHealth)
    {
        super(Name);
        MinimumHealth = MinHealth;
        SetPassive(true);
    }

    public int GetMinimumHealth() {return MinimumHealth;}

    public int GetMinimumHealthAsPercent()
    {
        return (int) (((float) MinimumHealth / (float) Skills.getRealLevel(Skill.HITPOINTS)) * 100);
    }

    @Nonnull
    @Override
    public TaskType GetTaskType()
    {
        return TaskType.MinimumHealth;
    }

    @Override
    public boolean accept()
    {
        var foods = OSRSDataBase.GetCommonFoods(!Client.isMembers());
        var min   = GetMinimumHealthAsPercent();
        Logger.log("Player Healthpercent: " + Players.getLocal().getHealthPercent() + " Minimum Healthpercent: " + min +
                   " MinimumHealth " + MinimumHealth);
        return Players.getLocal().getHealthPercent() < min &&
               OSRSUtilities.InventoryContainsAny(Arrays.stream(foods).mapToInt(t -> t.id).toArray()) && super.accept();
    }

    @Override
    public int execute()
    {
        OSRSUtilities.ScriptIntenity Intensity = ScriptIntensity.get();

        Food.eat(GetMinimumHealthAsPercent(), true);
        return accept() ? OSRSUtilities.WaitTime(Intensity) : 0;
    }
}
