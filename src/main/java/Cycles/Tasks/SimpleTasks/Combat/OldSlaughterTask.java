package Cycles.Tasks.SimpleTasks.Combat;

import OSRSDatabase.MonsterDB;
import Utilities.Combat.CombatManager;
import Utilities.OSRSUtilities;
import Utilities.Scripting.IFScript;
import Utilities.Scripting.Logger;
import Utilities.Scripting.SimpleTask;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.Character;
import org.dreambot.api.wrappers.interactive.NPC;

import javax.annotation.Nonnull;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class OldSlaughterTask extends SimpleTask
{
    private final long                               _taskTimeoutStart = 0;
    private final ConcurrentHashMap<Integer, Thread> TargetListeners   = new ConcurrentHashMap<>();
    public PropertyChangeSupport onKill        = new PropertyChangeSupport(this);
    public AtomicInteger         TaskTimeout   = new AtomicInteger(120000);
    public AtomicInteger         CombatTimeout = new AtomicInteger(1000);
    public AtomicInteger         CacheTimeout  = new AtomicInteger(300);
    NPC _closestTarget = null;
    private       Area[]                             KillingAreas      = null;
    private       int[]                              TargetIDs         = null;

    public OldSlaughterTask(String Name, Area[] TargetAreas, String TargetName, boolean Exact)
    {
        super(Name);
        KillingAreas   = TargetAreas;
        this.TargetIDs = MonsterDB.GetMonsterIDsByName(TargetName, Exact);
    }

    public OldSlaughterTask(String Name, Area[] TargetAreas, int[] TargetIDs)
    {
        super(Name);
        KillingAreas   = TargetAreas;
        this.TargetIDs = TargetIDs;
    }

    public Area GetCurrentArea()
    {
        var result = Arrays.stream(KillingAreas)
                           .filter(t -> t.contains(Players.getLocal().getTile()))
                           .findAny();
        if(result.isPresent())
        {
            return result.get();
        }
        return null;
    }

    @Override
    public boolean Ready()
    {
        return GetTarget() != null;
    }

    @Override
    public int Loop()
    {
        var allTargets = OSRSUtilities.GetAllCharactersInteractingWith(Players.getLocal());
        for(var target : allTargets)
        {
            Integer hashcode = target.hashCode();
            if(!TargetListeners.containsKey(hashcode) && target.canAttack())
            {
                //TODO for some reason, the map gets reset after every update
                Logger.log("New Enemy found, listening, UID: " + hashcode);
                Thread TargetListener = new Thread(() -> TargetListener(hashcode));
                TargetListener.start();
                Thread th = new Thread(TargetListener);
                TargetListeners.put(hashcode, th);
            }
            Logger.log("OldSlaughterTask: Loop: EnemyCount " + TargetListeners.size());
        }

        var Target = GetTarget();

        if(Target != null && (Players.getLocal().getInteractingCharacter() != Target ||
                              !Players.getLocal().isHealthBarVisible()))
        {
            if(Target.distance() > 10)
            {
                Walking.walk(Target.getTile());
            }
            else
            {
                CombatManager.GetInstance(Players.getLocal()).Fight(Target);
            }
        }

        return super.Loop();
    }

    private void TargetListener(int TargetHash)
    {
        Character Target = NPCs.closest(t -> t.hashCode() == TargetHash);
        Logger.log("OldSlaughterTask: TargetListener: Starting to listen to target: " +
                   Target.toString() + " hashcode: " + Target.hashCode());
        if(Sleep.sleepUntil(() -> !Target.exists(), Long.MAX_VALUE))
        {
            Logger.log(
                    "OldSlaughterTask: TargetListener: Target has ceased to exist or has been defeated, waiting for end of animation for loot");
            Sleep.sleepUntil(() -> !Target.isAnimating(), 10000);
            Sleep.sleepTicks(3);
            onKill(Target.getID(), Target.getTile());
        }
        else
        {
            Logger.log("OldSlaughterTask: TargetListener: Target Timeout");
        }
        Logger.log("OldSlaughterTask: TargetListener: Stop listening to target: " + Target +
                   " hashcode: " + Target.hashCode());
        TargetListeners.remove(Target.hashCode());
    }

    private void onKill(int ID, Tile DeathTile)
    {
        onKill.firePropertyChange("Kill", ID, DeathTile);
    }

    public Character GetTarget()
    {
        if(!TargetListeners.isEmpty())
        {
            Character    weakest    = GetNearestTarget();
            int          healthperc = 100;
            Set<Integer> keys       = TargetListeners.keySet();
            var          Targets    = NPCs.all(y -> keys.contains(y.hashCode()));

            for(var target : Targets)
            {
                if(target.getHealthPercent() <= healthperc)
                {
                    weakest    = target;
                    healthperc = target.getHealthPercent();
                }
            }
            return weakest;
        }
        else
        {
            return GetNearestTarget();
        }
    }

    public NPC GetNearestTarget()
    {
        //        if(_closestTarget == null)
        //             // ||OSRSUtilities.IsTimeElapsed(Players.getLocal().getUID(), CacheTimeout.get()))
        //        {
        //            _closestTarget = OSRSUtilities.GetClosestAttackableEnemy(TargetIDs);
        //        }
        return OSRSUtilities.GetClosestAttackableEnemy(TargetIDs);
    }

    @Nonnull
    @Override
    public TaskType GetTaskType()
    {
        return TaskType.Slaughter;
    }

    @Override
    public boolean onStopTask(IFScript Script)
    {
        TargetListeners.clear();
        return super.onStopTask(Script);
    }
}
