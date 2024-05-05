package Utilities.Scripting;

import Utilities.OSRSUtilities;
import Utilities.Patterns.Delegates.Delegate1;
import Utilities.Patterns.SimpleDelegate;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.utilities.Logger;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public abstract class SimpleTask extends TaskNode implements ITask
{
    private final OSRSUtilities.ScriptIntenity                  _scriptIntensity  = OSRSUtilities.ScriptIntenity.Lax;
    public        AtomicReference<OSRSUtilities.ScriptIntenity> ScriptIntensity   = new AtomicReference<>(
            _scriptIntensity);
    public        AtomicInteger                                 TaskPriority      = new AtomicInteger(1);
    public        Supplier<Boolean>                             AcceptCondition   = () -> true;
    public        Supplier<Boolean>                             CompleteCondition = null;
    public        SimpleDelegate                                onComplete        = new SimpleDelegate();
    public        Delegate1<SimpleTask>                         onReplaced        = new Delegate1<>();
    public        SimpleDelegate                                onAccept          = new SimpleDelegate();
    public        Delegate1<Integer>                            onExecute         = new Delegate1<>();
    public        SimpleDelegate                                onStart           = new SimpleDelegate();
    public        SimpleDelegate                                onStop            = new SimpleDelegate();
    public        SimpleDelegate                                onPause           = new SimpleDelegate();
    public        SimpleDelegate                                onUnPause         = new SimpleDelegate();
    private       boolean                                       Alive             = true;
    private       boolean                                       Passive           = false;
    private       boolean                                       Paused            = false;
    private       boolean                                       Persist           = false;
    private       String                                        TaskName          = "";
    private       WeakReference<tpircSScript>                   ParentScript      = null;

    public SimpleTask(String Name)
    {
        TaskName = Name;
        Persist  = false;
    }

    /**
     * Be sure to call this in case you don't add the task to the script using any of the addnode(), aka advanced tasks
     */
    public void Init(tpircSScript Script)
    {
        ParentScript = new WeakReference<>(Script);
    }

    public tpircSScript GetScript()
    {
        return ParentScript.get();
    }

    /**
     * @return Whether this task hasn't been stopped yet
     */
    public final boolean IsAlive()
    {
        return Alive;
    }

    /**
     * @return Whether this task is necessary to be completed
     */
    public final boolean IsPassive() {return Passive;}

    public final boolean IsPaused() {return Paused;}

    /**
     * @return whether this task should go back into the task list once its completed
     */
    public final boolean IsPersistent() {return Persist;}

    public void SetTaskName(String Name)
    {
        TaskName = Name;
    }

    @Override
    public String GetTaskName()
    {
        return TaskName;
    }

    @Override
    public String toString()
    {
        return GetTaskType().name() + ": " + TaskName;
    }

    /**
     * @return whether this task should go back into the task list once its completed
     */
    public void SetPersistant(boolean persist) {Persist = persist;}

    /**
     * @param passive Whether a task has to be completed before a cycle can end
     */
    public void SetPassive(boolean passive) {Passive = passive;}

    /**
     * @param Script Caller script, this basically
     *
     * @return return true if successful, false if we need more time, keep triggering start until it is ready
     */
    protected final boolean StartTask(tpircSScript Script)
    {
        Alive = true;
        ScriptIntensity.set(Script.GetScriptIntensity());
        boolean result = onStartTask(Script);
        if(result)
        {
            onStart.Fire();
        }
        return result;
    }

    /**
     * @param Script Caller script, this basically
     *
     * @return return true if successfully paused, otherwise false
     */
    public final boolean PauseTask(tpircSScript Script)
    {
        Paused = true;
        boolean result = onPauseTask(Script);
        onPause.Fire();// see if we need to enclose this by a if check as well
        return result;
    }

    /**
     * @param Script Caller script, this basically
     *
     * @return return true if successfully paused, otherwise false
     */
    public final boolean UnPauseTask(tpircSScript Script)
    {
        Paused = false;
        boolean result = onUnPauseTask(Script);
        onUnPause.Fire(); // see if we need to enclose this by a if check as well
        return result;
    }

    /**
     * @param Script Caller script, this basically
     *
     * @return return true if successful, false if we need more time, keep triggering start until it is ready
     */
    protected final boolean StopTask(tpircSScript Script)
    {
        Alive = false;
        boolean result = onStopTask(Script);
        if(result)
        {
            onStop.Fire();
        }
        return result;
    }

    protected final void ReplaceTask(tpircSScript Script, SimpleTask other)
    {
        onReplaced(Script, other);
        onReplaced.Fire(other);
    }

    /**
     * @param Script caller script
     * @param other  Task that is replacing this one
     *               Is called after the task has been stopped
     */
    public void onReplaced(tpircSScript Script, SimpleTask other) {}


    @Override
    public int priority()
    {
        return TaskPriority.get();
    }

    @Override
    public final boolean accept()
    {
        boolean result = Ready();
        if(result)
        {
            onAccept.Fire();
        }
        return result;
    }

    protected boolean Ready()
    {
        return AcceptCondition.get();
    }

    @Override
    public final int execute()
    {
        int result = Loop();
        onExecute.Fire(result);
        if(result == 0)
        {
            onComplete.Fire();
        }
        return result;
    }

    protected int Loop()
    {
        return CompleteCondition != null && CompleteCondition.get() ? 0 : OSRSUtilities.WaitTime(ScriptIntensity.get());
    }
}
