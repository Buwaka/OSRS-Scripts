package Cycles.SimpleTasks;

import Utilities.OSRSUtilities;
import Utilities.Scripting.SimpleTask;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.wrappers.items.Item;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PrayTask extends SimpleTask
{
    final  String        BuryAction    = "Bury";
    final  String        ScatterAction = "Scatter";
    public AtomicInteger PrayTimeout   = new AtomicInteger(3000);
    int[] PrayExcepts = new int[0];

    public PrayTask()
    {
        super("Praying");
    }

    @Override
    public boolean accept()
    {
        return Inventory.contains(t -> t.hasAction(BuryAction, ScatterAction) &&
                                       Arrays.stream(PrayExcepts).anyMatch(x -> x != t.getID())) && super.accept();
    }

    @Override
    public int execute()
    {
        if(Inventory.contains(t -> t.hasAction(BuryAction, ScatterAction) &&
                                   Arrays.stream(PrayExcepts).anyMatch(x -> x != t.getID())))
        {
            List<Item> Items = Inventory.all(t -> t.hasAction(BuryAction, ScatterAction) &&
                                                  Arrays.stream(PrayExcepts).anyMatch(x -> x != t.getID()));
            OSRSUtilities.PrayAll(PrayTimeout.get(), Items.stream().distinct().mapToInt(Item::getID).toArray());
        }
        else
        {
            // Done
            return 0;
        }
        return OSRSUtilities.WaitTime(ScriptIntensity.get());
    }

    @Nonnull
    @Override
    public TaskType GetTaskType()
    {
        return TaskType.Pray;
    }
}
