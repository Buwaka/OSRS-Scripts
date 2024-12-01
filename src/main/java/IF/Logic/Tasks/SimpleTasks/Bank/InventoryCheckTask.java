package IF.Logic.Tasks.SimpleTasks.Bank;

import IF.OSRSDatabase.ItemDB;
import IF.Utilities.Scripting.IScript;
import IF.Utilities.Scripting.Logger;
import io.vavr.Tuple2;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.equipment.Equipment;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InventoryCheckTask extends BankItemsTask
{
    List<Tuple2<Integer, Integer>> Items             = new ArrayList<>();
    boolean                        DepositEverything = true;

    public InventoryCheckTask(String Name, Tuple2<Integer, Integer>... items)
    {
        super(Name);
        Items = List.of(items);
        SetTaskPriority(-1);
    }

    public InventoryCheckTask(String Name, List<Tuple2<Integer, Integer>> items)
    {
        super(Name);
        Items = items;
        SetTaskPriority(-1);
    }

    public InventoryCheckTask(String Name, int... items)
    {
        super(Name);
        for(var item : items)
        {
            Items.add(new Tuple2<>(item, 1));
        }
        SetTaskPriority(-1);
    }

    public void setDepositEverything(boolean depositEverything)
    {
        DepositEverything = depositEverything;
    }

    /**
     * @return
     */
    @Override
    public boolean Ready()
    {
        var result = HasRequirements();
        Logger.log("InventoryCheck: Ready: result: " + result);

        if(!result)
        {
            return true;
        }
        else
        {
            //we have all requirements
            this.StopTask(GetScript());
            return false;
        }
    }

    @Nonnull
    @Override
    public TaskType GetTaskType()
    {
        return TaskType.InventoryCheck;
    }

    /**
     * @return false if a Bank visit is necessary, true if we're good
     */
    public boolean HasRequirements()
    {
        //        for(var item : Inventory.all())
        //        {
        //            if(item != null && Items.stream().anyMatch(t -> t._1 != item.getID()))
        //            {
        //                return false;
        //            }
        //        }

        for(var item : Items)
        {
            if(item._2 == Integer.MAX_VALUE)
            {
                var itemData = ItemDB.GetItemData(item._1);
                if((itemData != null && itemData.stackable) && Bank.contains(item._1))
                {
                    Logger.log("InventoryCheck: HasRequirements: false, stackabale item");
                    return false;
                }
                else if(!Inventory.contains(item._1) ||
                        (!Inventory.isFull() && Bank.contains(item._1)))
                {
                    Logger.log("InventoryCheck: HasRequirements: false, still inventory space");
                    return false;
                }
            }
            else if(Inventory.count(item._1) < item._2 && !Equipment.contains(item._1))
            {
                Logger.log("InventoryCheck: HasRequirements: false, item not present");
                return false;
            }


        }
        Logger.log("InventoryCheck: HasRequirements: true");
        return true;
    }

    @Override
    public boolean onStartTask(IScript Script)
    {
        var difference = GetDifference();
        if(DepositEverything || (difference.length > 0 && Inventory.getEmptySlots() >
                                                          Arrays.stream(difference)
                                                                .mapToInt((t) -> t._2)
                                                                .sum()))
        {
            AddDepositAll();
            for(var item : Items)
            {
                AddWithdraw(item._1, item._2);
            }
        }
        else
        {
            for(var item : Items)
            {
                int count = Inventory.count(item._1);
                if(count < item._2)
                {
                    AddWithdraw(item._1, item._2 - count);
                }
            }
        }
        return super.onStartTask(Script);
    }

    public Tuple2<Integer, Integer>[] GetDifference()
    {
        List<Tuple2<Integer, Integer>> out = new ArrayList<>();
        for(var req : Items)
        {
            if(req == null)
            {
                continue;
            }
            if(Inventory.count(req._1) < req._2)
            {
                var item = ItemDB.GetItemData(req._1);
                if(item != null && item.stackable)
                {
                    out.add(new Tuple2<>(req._1, 1));
                }
                else
                {
                    out.add(new Tuple2<>(req._1, req._2 - Inventory.count(req._1)));
                }
            }
        }
        return out.toArray(new Tuple2[0]);
    }
}
