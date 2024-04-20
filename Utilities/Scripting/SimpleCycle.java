package Utilities.Scripting;


import Utilities.Patterns.SimpleDelegate;
import org.dreambot.api.utilities.Logger;

import java.beans.PropertyChangeSupport;


public class SimpleCycle implements ICycle
{
    public  PropertyChangeSupport onCompleteCycle = new PropertyChangeSupport(this);
    public  SimpleDelegate        onCycleEnd      = new SimpleDelegate();
    private CycleType             Type            = CycleType.Null;
    private int                   CycleCount      = 0;
    private int                   CycleLimit      = -1; //for byCount type cycles

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

    public void SetCycleLimit(int Limit)
    {
        CycleLimit = Limit;
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

    protected final boolean Start(tpircSScript Script, int CycleCount)
    {
        return onStart(Script, CycleCount);
    }

    protected final boolean Start(tpircSScript Script)
    {
        return onStart(Script, -1);
    }

    public final boolean End(tpircSScript Script)
    {
        return onEnd(Script);
    }

    public final void EndNow(tpircSScript Script)
    {
        onEndNow(Script);
        onCycleEnd.Fire();
    }

    public boolean CanRestart(tpircSScript Script) {return true;}

    public final boolean Restart(tpircSScript Script)
    {
        return onRestart(Script);
    }

    protected final int Loop(tpircSScript Script)
    {
        int result = onLoop(Script);

//        if(!Script.IsActiveTaskLeft())
//        {
//            Logger.log("Inside Cycle");
//            CompleteCycle();
//            Restart(Script);
//        }

        switch(Type)
        {
            case byCount ->
            {
                if(CycleCount >= CycleLimit && End(Script))
                {
                    onCycleEnd.Fire();
                }
            }
            case byGoal ->
            {
                if(GoalIsMet() && End(Script))
                {
                    onCycleEnd.Fire();
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
