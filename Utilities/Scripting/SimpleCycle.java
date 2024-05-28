package Utilities.Scripting;


import Utilities.Patterns.Delegates.Delegate;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.utilities.Logger;

import java.io.Serializable;
import java.util.function.Supplier;


public class SimpleCycle implements ICycle, Serializable
{
    /**
     * returns true when goal is met
     */
    public transient  Supplier<Boolean> Goal            = null;
    public transient  Delegate          onCompleteCycle = new Delegate();
    public transient  Delegate          onCycleEnd      = new Delegate();
    private transient CycleType         Type            = CycleType.Null;
    private transient int                   CycleCount      = 0;
    /**
     * If the task is complete AND has been cleaned up, check CanRestart for whether its just complete
     */
    private transient boolean               Finished        = false;
    private           String                CycleName       = "";
    private transient boolean               Started         = false;
    private boolean          NeedsCachedBank = true;
    public Supplier<Boolean> Requirement     = null;

    private SimpleCycle()
    {
        onCompleteCycle = new Delegate();
        onCycleEnd      = new Delegate();
    }

    public boolean isNeedsCachedBank()
    {
        return NeedsCachedBank;
    }

    public void setNeedsCachedBank(boolean needsCachedBank)
    {
        NeedsCachedBank = needsCachedBank;
    }

    public SimpleCycle(String name)
    {
        CycleName = name;
    }

    public String GetName()          {return CycleName;}

    public void SetName(String name) {CycleName = name;}

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
    public void SetCycleType(CycleType Type)
    {
        this.Type = Type;
    }

    @Override
    public CycleType GetCycleType()
    {
        return Type;
    }

    @Override
    public boolean isFinished()
    {
        return Finished;
    }

    @Override
    public boolean isStarted()
    {
        return Started;
    }

    public void SetCycleLimit(int Limit)
    {
        Type.Count = Limit;
    }

    protected final void CompleteCycle()
    {
        switch(Type)
        {
            case byCount ->
            {
                Logger.log("Completed Cycle " + (CycleCount + 1) + " of " + Type.Count);
            }
            case byGoal ->
            {
                Logger.log("Completed Cycle, is goal met:  " + Goal.get());
            }
            case Endless ->
            {
                Logger.log("Completed Cycle, Cycle is endless");
            }
            case Null ->
            {
                Logger.log("Completed Cycle, Cycle is null?");
            }
        }

        CycleCount++;
        onCompleteCycle.Fire();
    }

    protected final void ResetCycleCount()
    {
        CycleCount = 0;
    }

    //protected boolean CanStart(tpircSScript Script) { return true;}


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
    @Override
    public boolean isGoalMet()
    {
        if(NeedsCachedBank && !Bank.isCached())
        {
            return false;
        }

        switch(GetCycleType())
        {
            case byCount ->
            {
                if(CycleCount >= Type.Count)
                {
                    Logger.log("SimpleCycle: isGoalMet: byCount true");
                    return true;
                }
            }
            case byGoal ->
            {
                if(Goal != null && Goal.get())
                {
                    Logger.log("SimpleCycle: isGoalMet: byGoal true");
                    return true;
                }
            }
            case Endless ->
            {
                Logger.log("SimpleCycle: isGoalMet: Endless false");
                return false;
            }
            case Null ->
            {
                Logger.log("Forgot to set Cycle type");
            }
        }
        return false;
    }

    protected final boolean Start(tpircSScript Script)
    {
        Started = true;
        return onStart(Script);
    }

    public final boolean End(tpircSScript Script)
    {
        Finished = true;
        onCycleEnd.Fire();
        return true;
    }

    /**
     * No excuses, the cycle ends now, doesn't trigger any delegate, be sure to call End first in case you want it to fire
     */
    public final void EndNow(tpircSScript Script)
    {
        onEndNow(Script);
    }

    public boolean CanRestart(tpircSScript Script)
    {
        switch(Type)
        {
            case byCount ->
            {
                if(CycleCount < Type.Count)
                {
                    return true;
                }
            }
            case byGoal ->
            {
                if(Goal != null && !Goal.get())
                {
                    return true;
                }
            }
            case Endless ->
            {
                return true;
            }
            case Null ->
            {
                Logger.log("Forgot to set Cycle type");
            }
        }
        return false;
    }

    public final boolean Restart(tpircSScript Script)
    {
        return onRestart(Script);
    }

    public final void Reset(tpircSScript Script)
    {
        if(GetCycleType() == CycleType.byCount)
        {
            CycleCount = 0;
        }
        onReset(Script);
    }

    protected final int Loop(tpircSScript Script)
    {
        int result = onLoop(Script);
        return result;
    }

    protected boolean Ready()
    {
        if(Requirement != null)
        {
            return Requirement.get();
        }
        return true;
    }
}
