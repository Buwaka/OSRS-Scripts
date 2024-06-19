package Cycles;

import Cycles.SimpleTasks.ItemProcessing.AlchTask;
import OSRSDatabase.ItemDB;
import OSRSDatabase.OSRSDataBase;
import Utilities.Scripting.SimpleCycle;
import Utilities.Scripting.tpircSScript;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.grandexchange.LivePrices;
import org.dreambot.api.wrappers.items.Item;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class AlchCycle extends SimpleCycle
{
    private final int NatureRuneID = 561;
    private Instant StartTime = null;
    public int ProfitMargin = 50;
    public AlchCycle(String name)
    {
        super(name);
    }

    public Item[] GetProfitableAlchs()
    {
        List<Item> ItemsToAlch = new ArrayList<>();
        int RunePrice = LivePrices.get(NatureRuneID);
        for(var item : Bank.all())
        {
            if(ItemDB.IsAlchable(item.getID()))
            {
                ItemDB.ItemData itemData = ItemDB.GetItemData(item.getID());
                int GEPrice = LivePrices.get(item.getID());
                int AlchPrice = itemData.highalch;
                int profit = (AlchPrice - RunePrice) - GEPrice;

                if(profit >= ProfitMargin)
                {
                    ItemsToAlch.add(item);
                }
            }
        }

        return ItemsToAlch.toArray(new Item[0]);
    }

    /**
     * @param Script
     *
     * @return if cycle has successfully started
     */
    @Override
    public boolean onStart(tpircSScript Script)
    {
        var alchs = GetProfitableAlchs();

        for(var task : alchs)
        {
            Script.addNodes(new AlchTask("Alching " + task.getName() ,task));
        }

        StartTime = Instant.now();

        return super.onStart(Script);
    }

    /**
     * @return Whether the goal of this cycle has been met, based on CycleType
     */
    @Override
    public boolean isGoalMet()
    {
        var alchs = GetProfitableAlchs();
        return alchs == null || alchs.length == 0;
    }

    /**
     * @param Script
     *
     * @return
     */
    @Override
    public boolean CanRestart(tpircSScript Script)
    {
        return false;
    }

    /**
     * will be called once there are no active tasks anymore, aka a single cycle has been completed
     *
     * @param Script
     *
     * @return Cycle completed, ready for a restart
     */
    @Override
    public boolean isCycleComplete(tpircSScript Script)
    {
        if(GetCycleLimit() == -1)
        {
            return true;
        }

        return (Instant.now().getEpochSecond() - StartTime.getEpochSecond()) > (long) GetCycleLimit();
    }

    /**
     * @param Script
     *
     * @return
     */
    @Override
    public int onLoop(tpircSScript Script)
    {
        return super.onLoop(Script);
    }
}
