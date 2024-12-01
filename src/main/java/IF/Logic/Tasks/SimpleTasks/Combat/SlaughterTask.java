package IF.Logic.Tasks.SimpleTasks.Combat;

import IF.OSRSDatabase.MonsterDB;
import IF.Utilities.Patterns.Delegates.Delegate;
import IF.Utilities.Patterns.Delegates.Delegate1;
import IF.Utilities.Scripting.IScript;
import IF.Utilities.Scripting.Logger;
import IF.Utilities.Scripting.SimpleTask;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.dreambot.api.data.consumables.Food;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.combat.CombatStyle;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.wrappers.interactive.NPC;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class SlaughterTask extends SimpleTask
{
    private final int[]       TargetIDs;
    /**
     * bool: has supplies
     */
    public Delegate1<Boolean> onLowHealth  = new Delegate1<>();
    public Delegate           onNoSupplies = new Delegate();
    /**
     * int: TargetID
     * Tile: death position
     */
    public Delegate1<NPC>     onKill       = new Delegate1<>();
    Cache<Integer, Integer> Targets = CacheBuilder.newBuilder()
                                                  .expireAfterAccess(1, TimeUnit.MINUTES)
                                                  .build();
    private       Integer     CurrentTargetHash = null;
    private       CombatStyle Style             = Combat.getCombatStyle();

    private Supplier<Integer> MinimumHPCondition    = null;
    private Supplier<Integer> EatThresholdCondition = null;
    private boolean           isLowHealth           = false;
    private boolean           isNoSupplies          = false;

    public SlaughterTask(String Name, String TargetName, boolean Exact)
    {
        super(Name);
        this.TargetIDs = MonsterDB.GetMonsterIDsByName(TargetName, Exact);
    }

    public SlaughterTask(String Name, int[] TargetIDs)
    {
        super(Name);
        this.TargetIDs = TargetIDs;
    }

    public boolean isLowHealth()
    {
        return isLowHealth;
    }

    public boolean isNoSupplies()
    {
        return isNoSupplies;
    }

    public boolean isTargetInSight()
    {
        return !GetAllTargets().isEmpty();
    }

    public void setCombatStyle(CombatStyle style)
    {
        Style = style;
    }

    public void setEatThresholdCondition(int eatThresholdCondition)
    {
        EatThresholdCondition = () -> eatThresholdCondition;
    }

    public void setEatThresholdCondition(Supplier<Integer> eatThresholdCondition)
    {
        EatThresholdCondition = eatThresholdCondition;
    }

    public void setMinimumHPCondition(int minimumHPCondition)
    {
        MinimumHPCondition = () -> minimumHPCondition;
    }

    public void setMinimumHPCondition(Supplier<Integer> minimumHPCondition)
    {
        MinimumHPCondition = minimumHPCondition;
    }

    @Override
    public boolean Ready()
    {
        return !GetTargetList().isEmpty();
    }

    PriorityQueue<NPC> GetTargetList()
    {
        // create a list of all viable targets, ordered by their distance
        var AllOfID = GetAllTargets();


        PriorityQueue<NPC> list = new PriorityQueue<>((x, y) -> {
            int HealthComp = Integer.compare(x.getHealthPercent(), y.getHealthPercent());
            int DistComp   = Double.compare(x.distance(), y.distance());
            int InteractComp = Boolean.compare(x.isInteracting(Players.getLocal()),
                                               y.isInteracting(Players.getLocal()));

            if(HealthComp == 0)
            {
                if(DistComp == 0)
                {
                    return InteractComp;
                }
                else
                {
                    return DistComp;
                }
            }
            else
            {
                return HealthComp;
            }
        });

        list.addAll(AllOfID);
        list.removeIf(t -> t == null);
        return list;
    }

    List<NPC> GetAllTargets()
    {
        return NPCs.all(t -> {
            boolean IDComp = Arrays.stream(TargetIDs).anyMatch((x) -> x == t.getID());
            return t.canAttack() && IDComp;
        });
    }

    @Override
    public int Loop()
    {
        if(MinimumHPCondition != null &&
           Skills.getBoostedLevel(Skill.HITPOINTS) <= MinimumHPCondition.get())
        {
            Logger.log("SlaughterTask: Loop: Low Health, MinimumHPCondition");
            if(!Heal(true))
            {
                onLowHealth.Fire(false);
            }
            else
            {
                onLowHealth.Fire(true);
            }
            isLowHealth = true;
            return super.Loop();
        }
        if(EatThresholdCondition != null &&
           Skills.getBoostedLevel(Skill.HITPOINTS) <= EatThresholdCondition.get())
        {
            Logger.log("SlaughterTask: Loop: Low Health, EatThresholdCondition");
            if(!Heal(true))
            {
                onLowHealth.Fire(false);
            }
            else
            {
                onLowHealth.Fire(true);
            }
            return super.Loop();
        }

        if(Combat.getCombatStyle() != Style)
        {
            Logger.log("SlaughterTask: Loop: Change combat style");
            Combat.setCombatStyle(Style);
            return super.Loop();
        }

        if(!Combat.isInMultiCombat() && Players.getLocal().getInteractingCharacter() != null &&
           Players.getLocal().getInteractingCharacter().canAttack())
        {
            Logger.log("SlaughterTask: Loop: nuisance detected");
            var nuisance = Players.getLocal().getInteractingCharacter();
            if(GetCurrentTarget() == null)
            {
                CurrentTargetHash = nuisance.hashCode();
            }
        }

        NPC Target = GetCurrentTarget();
        if(Target == null)
        {
            Logger.log("SlaughterTask: Loop: target is null, finding new target");
            Target = GetTargetList().peek();
            if(Target == null)
            {
                return super.Loop();
            }
            CurrentTargetHash = Target.hashCode();
            Targets.put(Target.hashCode(), Target.hashCode());
        }

        if(!Players.getLocal().isInteracting(Target))
        {
            if(Target.canReach())
            {
                Logger.log("SlaughterTask: Loop: not interacting, taking action, interact");
                Target.interact();
            }
            else
            {
                Logger.log("SlaughterTask: Loop: not interacting, taking action, walk");
                Walking.walk(Target.getTile().getRandomized(3));
            }
        }

        return super.Loop();
    }

    private boolean Heal(boolean overeat)
    {
        var food = Food.getBestOnHand(overeat);
        if(food != null)
        {
            isNoSupplies = false;
            if(food.eat())
            {
                isLowHealth = false;
            }
            return true;
        }
        isNoSupplies = true;
        isLowHealth  = true;
        onNoSupplies.Fire();
        return false;
    }

    public NPC GetCurrentTarget()
    {
        return CurrentTargetHash == null
                ? null
                : NPCs.closest(t -> t.hashCode() == CurrentTargetHash);
    }

    @Nonnull
    @Override
    public TaskType GetTaskType()
    {
        return TaskType.Slaughter;
    }

    @Override
    public boolean onStartTask(IScript Script)
    {
        Logger.info("SlaughterTask: onStartTask:");
        Script.onNpcDespawn().Subscribe(this, this::DespawnChecker);
        return super.onStartTask(Script);
    }

    private Boolean DespawnChecker(NPC npc)
    {
        if(Targets.getIfPresent(npc.hashCode()) != null)
        {
            Targets.invalidate(npc.hashCode());
            Logger.log("SlaughterTask: onKill " + npc);
            onKill.Fire(npc);
        }
        return true;
    }

    @Override
    public boolean onStopTask(IScript Script)
    {
        return super.onStopTask(Script);
    }
}
