package Cycles.SimpleTasks;

import Database.OSRSDataBase;
import Utilities.OSRSUtilities;
import Utilities.Scripting.SimpleTask;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.GroundItem;

import javax.annotation.Nonnull;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LootKillsTask extends SimpleTask implements PropertyChangeListener
{
    private final List<GroundItem> LootItems = new ArrayList<>();

    public LootKillsTask()
    {
        super("Loot items after kill");
        SetPersistant(true);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {

        int  ID       = (int) evt.getOldValue();
        Tile lootTile = (Tile) evt.getNewValue();

        Logger.log("Loot found:" + ID + " " + lootTile);

        var LootTable = OSRSDataBase.GetMonsterLootTable(ID);
        var size      = OSRSDataBase.GetMonsterSize(ID);
        size = size == null ? 3 : size;
        if(LootTable != null && LootTable.length > 0)
        {
            var drops    = Arrays.stream(LootTable).mapToInt(t -> t.id).toArray();
            var newItems = OSRSUtilities.GetLootItemsInclude(lootTile.getArea(size), drops);
            LootItems.addAll(newItems);
        }
    }

    @Override
    public boolean accept()
    {
        return !LootItems.isEmpty() && super.accept();
    }

    @Override
    public int execute()
    {
        for(var loot : LootItems)
        {
            Logger.log(loot.toString() + " exists:" + loot.exists() + " isonscreen:" + loot.isOnScreen() );
        }

        if(LootItems.isEmpty())
        {
            return 0;
        }
        GroundItem Item = null;
        while(Item == null)
        {
            var first = LootItems.getFirst();
            if(!first.exists())
            {
                LootItems.remove(first);
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

        if(!Item.isOnScreen())
        {
            Camera.mouseRotateToTile(Item.getTile());
            GroundItem finalItem = Item;
            Sleep.sleepUntil(() -> finalItem.isOnScreen(), OSRSUtilities.WaitTime(ScriptIntensity.get()));
        }

        Item.interact();

        Sleep.sleepTicks(3);
        if(!Item.exists())
        {
            LootItems.remove(Item);
        }

        return super.execute();
    }

    @Nonnull
    @Override
    public TaskType GetTaskType()
    {
        return TaskType.LootKills;
    }
}
