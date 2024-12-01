package IF.Logic.Tasks.SimpleTasks.Bank;

import IF.Utilities.OSRSUtilities;
import IF.Utilities.Scripting.Logger;
import IF.Utilities.Scripting.SimpleTask;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class GetCombatRationsTask extends SimpleTask
{
    public  AtomicInteger                 HPToCarry     = new AtomicInteger(20);
    public  AtomicInteger                 MaxItems      = new AtomicInteger(10);
    private boolean                       FirstCheck    = true;
    private List<OSRSUtilities.BankEntry> ItemsToPickup = null;
    private Supplier<Boolean>             CompleteCondition;

    public GetCombatRationsTask(String Name, int HPtoCarry)
    {
        super(Name);
        HPToCarry.set(HPtoCarry);
    }

    @Nonnull
    @Override
    public TaskType GetTaskType()
    {
        return TaskType.GetCombatRations;
    }

    @Override
    public boolean Ready()
    {
        return OSRSUtilities.CanReachBank() && OSRSUtilities.InventoryHPCount() < HPToCarry.get() &&
               super.Ready();
    }

    @Override
    public int Loop()
    {
        if(FirstCheck)
        {
            var HPtoGet = HPToCarry.get();
            var temp    = OSRSUtilities.GetBestFoodChoice(HPtoGet);
            Logger.log("GetCombatRations: " + temp);
            if(temp == null)
            {
                Logger.log("Failed to gather optimal food choices from bank");
                return 0;
            }
            AtomicInteger MaxCounter = new AtomicInteger(MaxItems.get());
            ItemsToPickup = temp.stream().map(t -> {
                int max = MaxCounter.addAndGet(-t.getValue());
                return new OSRSUtilities.BankEntry(t.getKey(), Math.min(max, t.getValue()));
            }).toList();
            for(var item : ItemsToPickup)
            {
                Logger.log("Combat Rations, picking up: " + item.toString());
            }

            FirstCheck = false;
            return OSRSUtilities.WaitTime(GetScriptIntensity());
        }
        else
        {
            OSRSUtilities.ProcessBankEntries(GetScript(),
                                             null,
                                             ItemsToPickup,
                                             OSRSUtilities.WaitTime(GetScriptIntensity()));
        }

        return 0;
    }
}
