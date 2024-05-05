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
    private transient boolean               Finished        = false;
    private           String                CycleName       = "";

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
    public boolean IsFinished()
    {
        return Finished;
    }

    public void SetCycleLimit(int Limit)
    {
        Type.Count = Limit;
    }

    protected final void CompleteCycle()
    {
        Logger.log("Completed Cycle " + CycleCount);
        CycleCount++;
        onCompleteCycle.firePropertyChange("CompletedCycle", CycleCount - 1, CycleCount);
    }

    protected final void ResetCycleCount()
    {
        CycleCount = 0;
    }

    //protected boolean CanStart(tpircSScript Script) { return true;}

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

    public boolean CanRestart(tpircSScript Script) {return true;}

    public final boolean Restart(tpircSScript Script)
    {
        return onRestart(Script);
    }

    protected final int Loop(tpircSScript Script)
    {
        int result = onLoop(Script);

        switch(Type)
        {
            case byCount ->
            {
                if(CycleCount >= Type.Count && End(Script))
                {
                    EndNow(Script);
                }
            }
            case byGoal ->
            {
                if(Type.Goal != null && Type.Goal.get() && End(Script))
                {
                    EndNow(Script);
                }
            }
            case Endless ->
            {

            }
            case Null ->
            {
                Logger.log("Forgot to set Cycle type");
            }
        }
        return result;
    }
}
