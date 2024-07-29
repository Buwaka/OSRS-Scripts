package Utilities.Scripting;


import Utilities.ECycleTags;
import Utilities.Patterns.Delegates.Delegate;
import Utilities.Requirement.IRequirement;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.utilities.Logger;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type")
@JsonTypeName("SimpleCycle")
public abstract class SimpleCycle implements ICycle, Serializable
{
    /**
     * When a cycle is completed, this is called, the goal might not have been met yet
     */
    public transient  Delegate            onCompleteCycle = new Delegate();
    /**
     * When the cycle is complete and will exit
     */
    public transient  Delegate            onCycleEnd      = new Delegate();
    /**
     * returns true when goal is met
     */
    private @Nullable List<IRequirement>  Goal            = null;
    private @Nullable List<IRequirement>  Requirements    = null;
    private           String              CycleName       = "";
    private           boolean             NeedsCachedBank = true;
    private           List<SimpleTask>    StartUpTasks    = null;
    private           List<SimpleTask>    EndTasks        = null;
    private transient CycleType           Type            = CycleType.NaturalEnd;
    private transient int                 CycleCount      = 0;
    private @Nullable Integer             CycleCountLimit = null;
    private transient boolean             Started         = false;
    private           EnumSet<ECycleTags> Tags            = EnumSet.noneOf(ECycleTags.class);

    private transient WeakReference<tpircSScript> ParentScript = null;

    private SimpleCycle()
    {
        onCompleteCycle = new Delegate();
        onCycleEnd      = new Delegate();
    }


    public SimpleCycle(String name)
    {
        CycleName = name;
    }

    public void AddEndTask(SimpleTask... Tasks)
    {
        if(EndTasks == null)
        {
            EndTasks = new ArrayList<>();
        }

        for(var task : Tasks)
        {
            task.onComplete.Subscribe(this, () -> EndTasks.remove(task));
        }

        EndTasks.addAll(List.of(Tasks));
    }

    public void AddGoal(IRequirement... requirement)
    {
        if(Goal == null)
        {
            Goal = new ArrayList<>();
        }
        if(requirement == null)
        {
            return;
        }
        Collections.addAll(Goal, requirement);
    }

    public void AddRequirement(IRequirement... requirement)
    {
        if(Requirements == null)
        {
            Requirements = new ArrayList<>();
        }
        if(requirement == null)
        {
            return;
        }
        Collections.addAll(Requirements, requirement);
    }

    public void AddStartUpTask(SimpleTask... Tasks)
    {
        if(StartUpTasks == null)
        {
            StartUpTasks = new ArrayList<>();
        }

        for(var task : Tasks)
        {
            task.onComplete.Subscribe(this, () -> StartUpTasks.remove(task));
        }

        StartUpTasks.addAll(List.of(Tasks));
    }

    public void AddTag(EnumSet<ECycleTags> tags)
    {
        Tags.addAll(tags);
    }

    public final boolean End(tpircSScript Script)
    {
        onCycleEnd.Fire();
        return onEnd(Script);
    }

    /**
     * No excuses, the cycle ends now, doesn't trigger any delegate, be sure to call End first in case you want it to fire
     */
    public final void EndNow(tpircSScript Script)
    {
        onEndNow(Script);
    }

    public int GetCycleLimit()
    {
        return CycleCountLimit;
    }

    public String GetName() {return CycleName;}

    public tpircSScript GetScript()
    {
        return ParentScript.get();
    }

    public boolean IsRequirementMet()
    {
        if(Requirements == null)
        {
            return true;
        }

        boolean result = true;
        for(var requirement : Requirements)
        {
            result &= requirement.isRequirementMet();
        }
        return result;
    }

    public void RemoveTag(EnumSet<ECycleTags> tags)
    {
        Tags.removeAll(tags);
    }

    public final void Reset(tpircSScript Script)
    {
        if(GetCycleType() == CycleType.byCount)
        {
            CycleCount = 0;
        }
        onReset(Script);
    }

    public final boolean Restart(tpircSScript Script)
    {
        return onRestart(Script);
    }

    public void SetCycleLimit(int Limit)
    {
        CycleCountLimit = Limit;
    }

    public void SetName(String name) {CycleName = name;}

    public boolean hasEndTasks()
    {
        if(EndTasks == null)
        {
            return false;
        }

        for(var task : EndTasks)
        {
            if(!task.isFinished())
            {
                return true;
            }
        }
        return false;
    }

    public boolean hasStartUpTasks()
    {
        if(StartUpTasks == null)
        {
            return false;
        }

        for(var task : StartUpTasks)
        {
            if(!task.isFinished())
            {
                return true;
            }
        }
        return false;
    }

    public boolean isNeedsCachedBank()
    {
        return NeedsCachedBank;
    }

    public void setNeedsCachedBank(boolean needsCachedBank)
    {
        NeedsCachedBank = needsCachedBank;
    }

    protected final void CompleteCycle()
    {
        switch(Type)
        {
            case byCount ->
            {
                Logger.log("Completed Cycle " + (CycleCount + 1) + " of " + CycleCountLimit);
            }
            case byGoal ->
            {
                Logger.log("Completed Cycle, is goal met:  " + isGoalMet());
            }
            case Endless ->
            {
                Logger.log("Completed Cycle, Cycle is endless");
            }
            case NaturalEnd ->
            {
                Logger.log("Completed Cycle, Cycle is NaturalEnd");
            }
        }

        CycleCount++;
        onCompleteCycle.Fire();
    }

    protected SimpleTask[] GetEndTasks()
    {
        return EndTasks.toArray(new SimpleTask[0]);
    }

    protected SimpleTask[] GetStartupTasks()
    {
        return StartUpTasks.toArray(new SimpleTask[0]);
    }

    protected final int Loop(tpircSScript Script)
    {
        int result = onLoop(Script);
        return result;
    }

    protected boolean Ready()
    {
        return IsRequirementMet();
    }

    protected final void ResetCycleCount()
    {
        CycleCount = 0;
    }

    protected final boolean Start(tpircSScript Script)
    {
        Started      = true;
        ParentScript = new WeakReference<>(Script);
        return onStart(Script);
    }

    public String toString()
    {
        return CycleName + " type: " + Type.name() + " CycleCount: " + CycleCount;
    }

    @Override
    public final int GetCycleCount()
    {
        return CycleCount;
    }

    @Override
    public CycleType GetCycleType()
    {
        return Type;
    }

    @Override
    public void SetCycleType(CycleType Type)
    {
        this.Type = Type;
    }

    /**
     * will be called once there are no active tasks anymore, aka a single cycle has been completed
     *
     * @param Script
     *
     * @return true when Cycle is completed, ready for a restart
     */
    @Override
    public boolean isCycleComplete(tpircSScript Script)
    {
        return !Script.IsActiveTaskLeft();
    }

    /**
     * @param Script
     *
     * @return true when Cycle is completely done and should/will be terminated, typically the same as isCycleComplete
     */
    @Override
    public boolean isCycleFinished(tpircSScript Script)
    {
        return isCycleComplete(Script);
    }

    @Override
    public boolean isStarted()
    {
        return Started;
    }

    /**
     * @param Script
     *
     * @return if cycle has successfully started
     */
    @Override
    public boolean onStart(tpircSScript Script)
    {
        return true;
    }

    /**
     * @return Whether the goal of this cycle has been met, based on CycleType
     */

    boolean isGoalMet()
    {
        if(NeedsCachedBank && !Bank.isCached() || !Started)
        {
            return false;
        }

        switch(GetCycleType())
        {
            case byCount ->
            {
                if(CycleCountLimit != null && CycleCount >= CycleCountLimit)
                {
                    Logger.log("SimpleCycle: isGoalMet: byCount true");
                    return true;
                }
            }
            case byGoal ->
            {
                {
                    if(Goal == null)
                    {
                        Logger.log("SimpleCycle: isGoalMet: byGoal true cuz there is no goal");
                        return true;
                    }

                    boolean result = true;
                    for(var goal : Goal)
                    {
                        result &= goal.isRequirementMet();
                    }
                    Logger.log("SimpleCycle: isGoalMet: byGoal result: " + result);
                    return result;
                }
            }
            case Endless ->
            {
                Logger.log("SimpleCycle: isGoalMet: Endless false");
                return false;
            }
            case NaturalEnd ->
            {
                boolean result = isCycleFinished(GetScript());
                Logger.log("SimpleCycle: isGoalMet: natural end: " + result);
                return result;
            }
        }
        return false;
    }
}
