package IF.Logic.Tasks.AdvanceTasks;

import IF.Logic.Tasks.SimpleTasks.Combat.LootKillsTask;
import IF.Logic.Tasks.SimpleTasks.Combat.MinimumHealthTask;
import IF.Logic.Tasks.SimpleTasks.Combat.SlaughterTask;
import IF.Logic.Tasks.SimpleTasks.TravelTask;
import IF.OSRSDatabase.MonsterDB;
import IF.Utilities.OSRSUtilities;
import IF.Utilities.Scripting.IScript;
import IF.Utilities.Scripting.Logger;
import IF.Utilities.Scripting.SimpleTask;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.wrappers.items.Item;

import javax.annotation.Nonnull;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SlaughterAndLootTask extends SimpleTask
{
    private final int                                             FailCount        = 0;
    private final int                                             FailMax          = 10;
    private final int                                             Retries          = 10;
    private       List<AbstractMap.SimpleEntry<Integer, Integer>> ItemRequirements = new ArrayList<>();
    private       int[]                                           TargetIDs        = null;
    private Area[]                                          Areas         = null;
    private IF.Logic.Tasks.SimpleTasks.Combat.SlaughterTask SlaughterTask = null;
    private LootKillsTask                                   LootTask      = null;
    private TravelTask                                      Travel         = null;
    private MinimumHealthTask                               MinimumHealth  = null;
    private int                                             RerouteRetries = 5;
    private       int                                             AttemptCount     = 0;
    private       int[]                                           IgnoreLoot       = null;
    private       boolean                                         PrayBones        = false;
    private       boolean                                         EscapeLowHP      = true;

    public SlaughterAndLootTask(String Name, Area[] Areas, int[] Targets, List<AbstractMap.SimpleEntry<Integer, Integer>> ItemRequirements)
    {
        super(Name);
        SetAreas(Areas);
        SetTarget(Targets);
        this.ItemRequirements = ItemRequirements;
    }

    void SetAreas(Area... Areas)
    {
        this.Areas = Areas;
    }

    void SetTarget(int... target)
    {
        TargetIDs = target;
    }

    public void setEscapeLowHP(boolean escapeLowHP)
    {
        EscapeLowHP = escapeLowHP;
    }

    public void setIgnoreLoot(int[] ignoreLoot)
    {
        IgnoreLoot = ignoreLoot;
    }

    public void setPrayBones(boolean prayBones)
    {
        PrayBones = prayBones;
    }

    @Override
    public boolean Ready()
    {
        return OSRSUtilities.GetClosestAttackableEnemy(TargetIDs) != null && super.Ready();
    }

    @Override
    public int Loop()
    {
        final String BuryAction    = "Bury";
        final String ScatterAction = "Scatter";
        if((Inventory.contains(t -> t.hasAction(BuryAction)) ||
            Inventory.contains(t -> t.hasAction(ScatterAction)) && PrayBones))
        {
            List<Item> Items = Inventory.all(t -> t.hasAction(BuryAction) ||
                                                  t.hasAction(ScatterAction));
            OSRSUtilities.PrayAll(3000, Items.stream().distinct().mapToInt(Item::getID).toArray());
        }

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
        else if(Players.getLocal().getHealthPercent() <
                OSRSUtilities.HPtoPercent(MinimumHealth.GetMinimumHealth()) && EscapeLowHP)
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
            GetScript().onGameTicked().WaitRandomTicks(3);
            if(AttemptCount > Retries)
            {
                RerouteRetries++;
                AttemptCount = 0;
                Travel       = new TravelTask("Travel to different spot in Killing Area",
                                              Arrays.stream(Areas).findAny().get().getRandomTile());
                Travel.onReachedDestination.Subscribe(this, () -> Travel = null);

                if(RerouteRetries > Retries)
                {
                    Logger.log("No retries left, exiting script");
                    return 0;
                }
            }
            else
            {
                AttemptCount++;
            }
        }

        return super.Loop();
    }

    @Nonnull
    @Override
    public TaskType GetTaskType()
    {
        return TaskType.SlaughterAndLoot;
    }

    @Override
    public boolean onStartTask(IScript Script)
    {
        Logger.log("StartSlaughterLoot task");
        // Loot
        var possibleLootTask = Script.getPersistentNodes()
                                     .stream()
                                     .filter(t -> t.GetTaskType() == TaskType.LootKills)
                                     .findAny();
        if(possibleLootTask.isPresent())
        {
            LootTask = (LootKillsTask) possibleLootTask.get();
        }
        else
        {
            LootTask = new LootKillsTask(IgnoreLoot);
            LootTask.SetTaskPriority(priority() - 1);
            LootTask.Init(Script);
            //Script.addPersistentNodes(LootTask);
        }

        // Slaughter
        SlaughterTask = new SlaughterTask("Slaughter", TargetIDs);
        SlaughterTask.SetTaskPriority(priority());
        SlaughterTask.onKill.Subscribe(LootTask, LootTask::onKill);
        SlaughterTask.Init(Script);

        MinimumHealth = new MinimumHealthTask("Prevent dying", GetMaxHit() + 2);

        return true;
    }

    public int GetMaxHit()
    {
        int                   HighestHit = 0;
        MonsterDB.MonsterData Strongest  = null;
        for(int i : TargetIDs)
        {
            var monster = MonsterDB.GetMonsterData(i);
            if(monster != null && monster.max_hit > HighestHit)
            {
                Strongest  = monster;
                HighestHit = monster.max_hit;
            }
        }

        Logger.log("SlaughterAndLootTask: GetMaxHit: " + HighestHit + " by " + Strongest);

        return HighestHit;
    }

    @Override
    public boolean onStopTask(IScript Script)
    {
        Script.removePersistentNodes(LootTask);
        return super.onStopTask(Script);
    }

    void SetTarget(String target)
    {
        TargetIDs = MonsterDB.GetMonsterIDsByName(target, false);
    }
}
