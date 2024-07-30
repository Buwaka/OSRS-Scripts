package Cycles.SimpleTasks.Combat;

import OSRSDatabase.MonsterDB;
import Utilities.OSRSUtilities;
import Utilities.Scripting.SimpleTask;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.items.GroundItem;

import javax.annotation.Nonnull;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LootKillsTask extends SimpleTask implements PropertyChangeListener
{
    //TODO priority based on rarity from itemdb
    private final ConcurrentLinkedQueue<GroundItem> LootItems = new ConcurrentLinkedQueue<>();

    private int[] IgnoreLoot = null;

    public LootKillsTask()
    {
        super("Loot items after kill");
    }

    public LootKillsTask(int... IgnoreItems)
    {
        super("Loot items after kill");
        IgnoreLoot = IgnoreItems;
    }

    private void Cleanup()
    {
        for(var loot : LootItems)
        {
            if(!loot.exists())
            {
                LootItems.remove(loot);
                Logger.log("LootKillsTask: Cleanup: " + loot.toString() + " removing from loot");
            }
            if(IgnoreLoot != null && Arrays.stream(IgnoreLoot).anyMatch((t) -> t == loot.getID()))
            {
                LootItems.remove(loot);
                Logger.log("LootKillsTask: Cleanup: " + loot.toString() +
                           " removing from loot because of ignore");
            }
            else
            {
                Logger.log(
                        "LootKillsTask: Cleanup: " + loot.toString() + " exists:" + loot.exists() +
                        " isonscreen:" + loot.isOnScreen());
            }

        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        int  ID       = (int) evt.getOldValue();
        Tile lootTile = (Tile) evt.getNewValue();

        Logger.log("LootKillsTask: propertyChange: Loot found:" + ID + " " + lootTile);

        var LootTable = MonsterDB.GetMonsterLootTable(ID);
        var size      = MonsterDB.GetMonsterSize(ID);
        size = size == null ? 3 : size;
        if(LootTable != null && LootTable.length > 0)
        {
            var drops    = Arrays.stream(LootTable).mapToInt(t -> t.id).toArray();
            var newItems = OSRSUtilities.GetLootItemsInclude(lootTile.getArea(size), drops);
            LootItems.addAll(newItems);
        }
    }

    @Override
    public boolean Ready()
    {
        return !LootItems.isEmpty() && super.Ready();
    }

    @Override
    public int Loop()
    {
        Cleanup();

        if(LootItems.isEmpty())
        {
            return 0;
        }
        GroundItem Item = null;
        while(Item == null)
        {
            var first = LootItems.peek();
            Logger.log("LootKillTask: Removing Loot " + Arrays.toString(IgnoreLoot));
            if(!first.exists() ||
               (IgnoreLoot != null && Arrays.stream(IgnoreLoot).anyMatch(t -> t == first.getID())))
            {
                Logger.log("LootKillTask: Removing Loot " + first);
                LootItems.remove(first);
                continue;
            }
            if(LootItems.isEmpty())
            {
                return 0;
            }
            else
            {
                Item = first;
            }
        }

        if(Item.distance() > 10)
        {
            Walking.walk(Item.getTile());
        }
        else
        {
            Item.interact();
        }

        return super.Loop();
    }

    @Nonnull
    @Override
    public TaskType GetTaskType()
    {
        return TaskType.LootKills;
    }
}
