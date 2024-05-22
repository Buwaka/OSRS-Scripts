package Cycles.AdvanceTasks;

import Cycles.SimpleTasks.Combat.LootKillsTask;
import Cycles.SimpleTasks.Combat.MinimumHealthTask;
import Cycles.SimpleTasks.Combat.SlaughterTask;
import Cycles.SimpleTasks.TravelTask;
import Database.OSRSDataBase;
import Utilities.OSRSUtilities;
import Utilities.Scripting.SimpleTask;
import Utilities.Scripting.tpircSScript;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.utilities.Logger;

import javax.annotation.Nonnull;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SlaughterAndLoot extends SimpleTask
{
    private List<AbstractMap.SimpleEntry<Integer, Integer>> ItemRequirements = new ArrayList<>();
    private int[]                                           TargetIDs        = null;
    private Area[]                                          Areas            = null;
    private int                                             FailCount        = 0;
    private int                                             FailMax          = 10;
    private SlaughterTask                                   SlaughterTask    = null;
    private LootKillsTask                                   LootTask         = null;
    private TravelTask                                      Travel           = null;
    private MinimumHealthTask                               MinimumHealth    = null;
    private int                                             Retries          = 5;
    private int                                             AttemptCount     = 0;


    public SlaughterAndLoot(String Name, Area[] Areas, int[] Targets, List<AbstractMap.SimpleEntry<Integer, Integer>> ItemRequirements)
    {
        super(Name);
        SetAreas(Areas);
        SetTarget(Targets);
        this.ItemRequirements = ItemRequirements;
    }

    void SetTarget(String target)
    {
        TargetIDs = OSRSDataBase.GetMonsterIDsByName(target, false);
    }

    void SetTarget(int... target)
    {
        TargetIDs = target;
    }

    void SetAreas(Area... Areas)
    {
        this.Areas = Areas;
    }

    @Override
    public boolean Ready()
    {
        return OSRSUtilities.GetClosestAttackableEnemy(TargetIDs) != null && super.Ready();
    }

    public int GetMaxHit()
    {
        int HighestHit = 0;
        for(int i : TargetIDs)
        {
            var monster = OSRSDataBase.GetMonsterData(i);
            if(monster != null && monster.max_hit > HighestHit)
            {
                HighestHit = monster.max_hit;
            }
        }

        return HighestHit;
    }

    @Nonnull
    @Override
    public TaskType GetTaskType()
    {
        return TaskType.SlaughterAndLoot;
    }

    @Override
    public boolean onStartTask(tpircSScript Script)
    {
        Logger.log("StartSlaughterLoot task");
        // Loot
        var possibleLootTask = Script.getPersistentNodes().stream().filter(t -> t.GetTaskType() ==
                                                                                TaskType.LootKills).findAny();
        if(possibleLootTask.isPresent())
        {
            LootTask = (LootKillsTask) possibleLootTask.get();
        }
        else
        {
            LootTask = new LootKillsTask();
            LootTask.TaskPriority.set(TaskPriority.get() - 1);
            LootTask.Init(Script);
            //Script.addPersistentNodes(LootTask);
        }

        // Slaughter
        SlaughterTask = new SlaughterTask("Slaughter", Areas, TargetIDs);
        SlaughterTask.TaskPriority.set(TaskPriority.get());
        SlaughterTask.onKill.addPropertyChangeListener(LootTask);
        SlaughterTask.Init(Script);

        MinimumHealth = new MinimumHealthTask("Prevent dying", GetMaxHit() + 2);

        return true;
    }

    @Override
    public boolean onStopTask(tpircSScript Script)
    {
        Script.removePersistentNodes(LootTask);
        return super.onStopTask(Script);
    }

    @Override
    public int Loop()
    {
        if(Inventory.isFull())
        {
            Logger.log("Inventory full, stopping task");
            return 0;
        }

        if(MinimumHealth.Ready())
        {
            Logger.log("SLA: Heal");
            MinimumHealth.Loop();
        }
        else if(Players.getLocal().getHealthPercent() < OSRSUtilities.HPtoPercent(MinimumHealth.GetMinimumHealth()))
        {
            Logger.log("Too low health and no more food, exiting task");
            return 0;
        }

        if(Travel != null && Travel.Loop() != 0)
        {
            Logger.log("SLA: Travel");
            return super.Loop();
        }

        if(LootTask.Ready())
        {
            Logger.log("SLA: Loot");
            AttemptCount = 0;
            LootTask.Loop();
        }
        else if(SlaughterTask.Ready())
        {
            Logger.log("SLA: Slaughter");
            AttemptCount = 0;
            SlaughterTask.Loop();
        }
        else
        {
            Logger.log("Can't find enemy");
            if(AttemptCount > Retries)
            {
                Logger.log("No retries left, exiting script");
                return 0;
            }
            else
            {
                AttemptCount++;
                Travel = new TravelTask("Travel to different spot in Killing Area",
                                        Arrays.stream(Areas).findAny().get().getRandomTile());
                Travel.onReachedDestination.Subscribe(() -> Travel = null);
            }
        }

        return super.Loop();
    }
}
