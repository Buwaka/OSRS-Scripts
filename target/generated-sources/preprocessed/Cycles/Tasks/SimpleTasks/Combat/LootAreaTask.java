package Cycles.Tasks.SimpleTasks.Combat;

import OSRSDatabase.OSRSPrices;
import Utilities.OSRSUtilities;
import Utilities.Scripting.SimpleTask;
import io.vavr.Tuple2;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.items.Item;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class LootAreaTask extends SimpleTask
{
    List<Area>            LootAreas   = new ArrayList<>();
    List<GroundItem>      LootItems   = new ArrayList<>();
    Predicate<GroundItem> LootChecker = null;

    int CheckCount    = 0;
    int MaxCheckCount = 3;

    public LootAreaTask(String Name, Area area)
    {
        super(Name);
        LootAreas.add(area);
    }

    public void AddArea(Area area)
    {
        LootAreas.add(area);
    }

    public void setLootChecker(Predicate<GroundItem> lootChecker)
    {
        LootChecker = lootChecker;
    }

    @Override
    public boolean Ready()
    {
        CheckAreas();
        return !LootItems.isEmpty() && super.Ready();
    }

    @Override
    public int Loop()
    {
        CheckAreas();
        if(LootItems.isEmpty())
        {
            return 0;
        }

        GroundItem Item = null;
        while(!LootItems.isEmpty())
        {
            while(Item == null)
            {
                var first = LootItems.getFirst();
                if(!first.exists())
                {
                    LootItems.removeFirst();
                    if(LootItems.isEmpty())
                    {
                        return 0;
                    }
                }
                else
                {
                    Item = first;
                }
            }

            if(Inventory.isFull())
            {
                var price             = OSRSPrices.GetLatestPrice(Item.getID());
                var LowestInInventory = LowestInInventory();
                if(price == null || LowestInInventory == null || price < LowestInInventory._1)
                {
                    Item = null;
                }
                else
                {
                    Inventory.drop(LowestInInventory._2.getID());
                }
                continue;
            }

            if(Item.distance() > 10)
            {
                Walking.walk(Item.getTile().getRandomized(2));
                continue;
            }

            Item.interact();

            GroundItem finalItem1 = Item;
            Sleep.sleepUntil(() -> !finalItem1.exists(),
                             OSRSUtilities.WaitTime(GetScriptIntensity()));

            if(!Item.exists())
            {
                Item = null;
            }
        }

        return super.Loop();
    }

    Tuple2<Integer, Item> LowestInInventory()
    {
        if(Inventory.isEmpty())
        {
            return null;
        }

        var item = Inventory.all()
                            .stream()
                            .filter((t) -> t != null)
                            .min(Comparator.comparingInt(x -> OSRSPrices.GetLatestPrice(x.getID()) *
                                                              x.getAmount()))
                            .get();


        return new Tuple2<>(OSRSPrices.GetLatestPrice(item.getID()), item);
    }

    private void CheckAreas()
    {
        if(CheckCount > MaxCheckCount)
        {
            return;
        }
        CheckCount++;

        for(var area : LootAreas)
        {
            List<GroundItem> Items;


            if(LootChecker == null)
            {
                Items = GroundItems.all((t) -> area.contains(t));
            }
            else
            {
                Items = GroundItems.all((t) -> area.contains(t) && LootChecker.test(t));
            }

            Items.sort(Comparator.comparingDouble(p -> {
                double dist = p.walkingDistance(Players.getLocal().getTile());

                return Math.abs(dist);
            }));

            LootItems.addAll(Items);
        }
    }

    @Nonnull
    @Override
    public TaskType GetTaskType()
    {
        return TaskType.LootArea;
    }
}
