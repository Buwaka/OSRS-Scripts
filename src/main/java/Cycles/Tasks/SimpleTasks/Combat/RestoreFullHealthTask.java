package Cycles.Tasks.SimpleTasks.Combat;

import Cycles.Tasks.SimpleTasks.Bank.GetCombatRationsTask;
import Utilities.OSRSUtilities;
import Utilities.Scripting.SimpleTask;
import org.dreambot.api.Client;
import org.dreambot.api.data.consumables.Food;
import org.dreambot.api.methods.interactive.Players;

import javax.annotation.Nonnull;

public class RestoreFullHealthTask extends SimpleTask
{

    private GetCombatRationsTask Rations = null;

    public RestoreFullHealthTask(String Name)
    {
        super(Name);
    }

    @Nonnull
    @Override
    public TaskType GetTaskType()
    {
        return TaskType.RestoreFullHealth;
    }

    @Override
    public boolean Ready()
    {
        return OSRSUtilities.CanReachBank() && super.Ready();
    }

    @Override
    public int Loop()
    {
        if(IsFullHealth())
        {
            return 0;
        }

        OSRSUtilities.ScriptIntenity Intensity = GetScriptIntensity();

        if(Rations == null && OSRSUtilities.InventoryContainsAnyFoods(Client.isMembers()))
        {
            Food.eat(99, true);
            return !IsFullHealth() ? OSRSUtilities.WaitTime(Intensity) : 0;
        }
        else
        {
            if(Rations == null)
            {
                Rations = new GetCombatRationsTask("Restore Full Health",
                                                   OSRSUtilities.GetMissingHP());
            }
            if(Rations.Loop() == 0)
            {
                Rations = null;
            }
        }

        return super.Loop();
    }

    public boolean IsFullHealth()
    {
        return Players.getLocal().getHealthPercent() == 100;
    }
}
