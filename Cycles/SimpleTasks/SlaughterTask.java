package Cycles.SimpleTasks;

import Database.OSRSDataBase;
import Utilities.Combat.CombatManager;
import Utilities.OSRSUtilities;
import Utilities.Scripting.SimpleTask;
import Utilities.Scripting.tpircSScript;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.Character;
import org.dreambot.api.wrappers.interactive.NPC;

import javax.annotation.Nonnull;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SlaughterTask extends SimpleTask
{
    public PropertyChangeSupport onKill        = new PropertyChangeSupport(this);
    public AtomicInteger         TaskTimeout   = new AtomicInteger(120000);
    public AtomicInteger         CombatTimeout = new AtomicInteger(1000);
    public AtomicInteger         CacheTimeout  = new AtomicInteger(300);
    NPC _closestTarget = null;
    private Area[]                     KillingAreas      = null;
    private int[]                      TargetIDs         = null;
    private long                       _taskTimeoutStart = 0;
    private HashMap<Character, Thread> TargetListeners   = new HashMap<>();

    public SlaughterTask(String Name)
    {
        super(Name);
    }

    // Whether Names have to exactly the same
    public void Init(Area[] TargetAreas, String TargetName, boolean Exact)
    {
        KillingAreas   = TargetAreas;
        this.TargetIDs = OSRSDataBase.GetMonsterIDsByName(TargetName, Exact);
    }

    public void Init(Area[] TargetAreas, int[] TargetIDs)
    {
        KillingAreas   = TargetAreas;
        this.TargetIDs = TargetIDs;
    }

    public NPC GetNearestTarget()
    {
        if(_closestTarget == null || OSRSUtilities.IsTimeElapsed(Players.getLocal().getUID(), CacheTimeout.get()))
        {
            _closestTarget = OSRSUtilities.GetClosestAttackableEnemy(TargetIDs);
        }
        return _closestTarget;
    }

    public Character GetTarget()
    {
        if(!TargetListeners.isEmpty())
        {
            Character weakest    = null;
            int       healthperc = 100;
            for(var terget : TargetListeners.entrySet())
            {
                if(terget.getKey().getHealthPercent() <= healthperc)
                {
                    weakest    = terget.getKey();
                    healthperc = terget.getKey().getHealthPercent();
                }
            }
            return weakest;
        }
        else
        {
            return GetNearestTarget();
        }
    }

    public Area GetCurrentArea()
    {
        var result = Arrays.stream(KillingAreas).filter(t -> t.contains(Players.getLocal().getTile())).findAny();
        if(result.isPresent())
        {
            return result.get();
        }
        return null;
    }

    @Override
    public boolean accept()
    {
        return GetTarget() != null;
    }

    private void onKill(int ID, Tile DeathTile)
    {
        onKill.firePropertyChange("Kill", ID, DeathTile);
    }

    private void TargetListener(Character Target)
    {
        Logger.log("Starting to listen to target: " + Target.toString());
        if(Sleep.sleepUntil(() -> !Target.exists(), Long.MAX_VALUE))
        {
            Logger.log("Target has ceased to exist or has been defeated, waiting for end of animation for loot");
            Sleep.sleepUntil(() -> !Target.isAnimating(), 10000);
            Sleep.sleepTicks(3);
            onKill(Target.getID(), Target.getTile());

        }
        else
        {
            Logger.log("Target Timeout");
        }
        Logger.log("Stop listening to target: " + Target.toString());
        TargetListeners.remove(Target);
    }

    @Override
    public boolean onStopTask(tpircSScript Script)
    {
        TargetListeners.clear();
        return super.onStopTask(Script);
    }

    @Override
    public int execute()
    {
        var allTargets = OSRSUtilities.GetAllCharactersInteractingWith(Players.getLocal());
        for(var target : allTargets)
        {
            if(!TargetListeners.containsKey(target))
            {
                Logger.log("New Enemy found, listening, UID: " + target.toString());
                Thread TargetListener = new Thread(() -> TargetListener(target));
                TargetListener.start();
                TargetListeners.put(target, TargetListener);
            }
            Logger.log("EnemyCount " + TargetListeners.size());
        }

        var Target = GetTarget();

        if(Target != null && (Players.getLocal().getInteractingCharacter() != Target || !Players.getLocal().isHealthBarVisible() ))
        {
            CombatManager.GetInstance(Players.getLocal()).Fight(Target);
        }


        return super.execute();
    }

    @Nonnull
    @Override
    public TaskType GetTaskType()
    {
        return TaskType.Slaughter;
    }
}
