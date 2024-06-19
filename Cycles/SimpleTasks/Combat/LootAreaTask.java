package Cycles.SimpleTasks.Combat;

import Utilities.OSRSUtilities;
import Utilities.Scripting.SimpleTask;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.GroundItem;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class LootAreaTask extends SimpleTask
{
    List<Area>       LootAreas = new ArrayList<>();
    List<GroundItem> LootItems = new ArrayList<>();

    public LootAreaTask(String Name)
    {
        super(Name);
    }

    public void AddArea(Area area)
    {
        LootAreas.add(area);
    }

    @Override
    public boolean Ready()
    {
        CheckAreas();
        return !LootItems.isEmpty() && super.Ready();
    }

    private void CheckAreas()
    {
        for(var area : LootAreas)
        {
            LootItems.addAll(OSRSUtilities.GetLootItems(area));
        }
    }

    @Override
    public int Loop()
    {
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
                LootItems.removeFirst();
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
            LootItems.removeFirst();
        }

        return super.Loop();
    }

    @Nonnull
    @Override
    public TaskType GetTaskType()
    {
        return TaskType.LootArea;
    }
}
