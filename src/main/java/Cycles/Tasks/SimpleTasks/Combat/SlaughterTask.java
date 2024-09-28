package Cycles.Tasks.SimpleTasks.Combat;

import OSRSDatabase.MonsterDB;
import Utilities.Patterns.Delegates.Delegate2;
import Utilities.Scripting.SimpleTask;
import Utilities.Scripting.tpircSScript;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.wrappers.interactive.Entity;
import org.dreambot.api.wrappers.interactive.NPC;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.PriorityQueue;

public class SlaughterTask extends SimpleTask
{
    public  Delegate2<Integer, Tile> onKill = new Delegate2<>();
    private Area[]                   KillingAreas;
    private int[]                    TargetIDs;

    private Integer CurrentTargetHash = null;

    public SlaughterTask(String Name, Area[] TargetAreas, String TargetName, boolean Exact)
    {
        super(Name);
        KillingAreas   = TargetAreas;
        this.TargetIDs = MonsterDB.GetMonsterIDsByName(TargetName, Exact);
    }

    public SlaughterTask(String Name, Area[] TargetAreas, int[] TargetIDs)
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

    PriorityQueue<NPC> GetTargetList()
    {
        // create a list of all viable targets, ordered by their distance
        var AllOfID = NPCs.all(t -> {
            boolean IDComp = Arrays.stream(TargetIDs).anyMatch((x) -> x == t.getID());
            return t.canAttack() && IDComp;
        });


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

    NPC GetCurrentTarget()
    {
        return CurrentTargetHash == null
                ? null
                : NPCs.closest(t -> t.hashCode() == CurrentTargetHash);
    }

    private Boolean HitSplatChecker(Entity entity, int type, int damage, int id, int special, int gameCycle)
    {
        if(entity instanceof NPC)
        {
            NPC npc = (NPC) entity;
            if(npc.isInteracting(Players.getLocal()) && npc.getHealthPercent() == 0)
            {
                onKill(npc.getID(), npc.getTile());
                CurrentTargetHash = null;
            }
        }
        return true;
    }

    private void onKill(int ID, Tile DeathTile)
    {
        onKill.Fire(ID, DeathTile);
    }

    @Override
    public boolean Ready()
    {
        return !GetTargetList().isEmpty();
    }

    @Override
    public int Loop()
    {
        NPC Target = GetCurrentTarget();
        if(Target == null)
        {
            Target            = GetTargetList().peek();
            CurrentTargetHash = Target.hashCode();
        }

        if(!Players.getLocal().isInteracting(Target))
        {
            Target.interact();
        }

        return super.Loop();
    }

    @Nonnull
    @Override
    public TaskType GetTaskType()
    {
        return TaskType.Slaughter;
    }

    @Override
    public boolean onStartTask(tpircSScript Script)
    {
        GetScript().onHitSplat.Subscribe(this, this::HitSplatChecker);
        return super.onStartTask(Script);
    }

    @Override
    public boolean onStopTask(tpircSScript Script)
    {
        return super.onStopTask(Script);
    }
}
