package Utilities.Scripting;


import Utilities.Patterns.SimpleDelegate;
import org.dreambot.api.utilities.Logger;

import java.beans.PropertyChangeSupport;
import java.io.Serializable;


public class SimpleCycle implements ICycle, Serializable
{
    public transient  PropertyChangeSupport onCompleteCycle = new PropertyChangeSupport(this);
    public transient  SimpleDelegate        onCycleEnd      = new SimpleDelegate();
    private transient CycleType             Type            = CycleType.Null;
    private transient int                   CycleCount      = 0;
    /**
     * If the task is complete AND has been cleaned up, check CanRestart for whether its just complete
     */
    private transient boolean               Finished        = false;
    private           String                CycleName       = "";
    private transient boolean               Started         = false;

    private SimpleCycle()
    {
        onCompleteCycle = new PropertyChangeSupport(this);
        onCycleEnd      = new SimpleDelegate();
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
                Logger.log("Completed Cycle, is goal met:  " + Type.Goal.get());
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
        onCompleteCycle.firePropertyChange("CompletedCycle", CycleCount - 1, CycleCount);
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
        Started = true;
        return true;
    }

    protected final boolean Start(tpircSScript Script)
    {
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
                if(Type.Goal != null && !Type.Goal.get())
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

    protected final int Loop(tpircSScript Script)
    {
        int result = onLoop(Script);
        return result;
    }
}
