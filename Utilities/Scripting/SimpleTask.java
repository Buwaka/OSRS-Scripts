package Utilities.Scripting;

import Utilities.OSRSUtilities;
import Utilities.Patterns.Delegates.Delegate;
import Utilities.Patterns.Delegates.Delegate1;
import org.dreambot.api.script.TaskNode;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public abstract class SimpleTask extends TaskNode implements ITask
{
    private final     OSRSUtilities.ScriptIntenity                  _scriptIntensity  = OSRSUtilities.ScriptIntenity.Lax;
    // TODO replace with serializable Requirement
    public            Supplier<Boolean>                             AcceptCondition   = () -> true;
    public            Supplier<Boolean>                             CompleteCondition = null;
    public            SimpleTask[]                                  ChildTasks        = null; // TODO, basically start these tasks when this task is complete
    public transient  AtomicReference<OSRSUtilities.ScriptIntenity> ScriptIntensity   = new AtomicReference<>(
            _scriptIntensity);
    public transient  AtomicInteger                                 TaskPriority      = new AtomicInteger(1);
    public transient  Delegate                                      onComplete        = new Delegate();
    public transient  Delegate1<SimpleTask>                         onReplaced        = new Delegate1<>();
    public transient  Delegate                                      onAccept          = new Delegate();
    public transient  Delegate1<Integer>                            onExecute         = new Delegate1<>();
    public transient  Delegate                                      onStart           = new Delegate();
    public transient  Delegate                                      onStop            = new Delegate();
    public transient  Delegate                                      onPause           = new Delegate();
    public transient  Delegate                                      onUnPause         = new Delegate();
    private           String                                        TaskName          = "";
    private transient boolean                                       Active            = true;
    private transient boolean                                       Finished          = false;
    private transient boolean                                       Paused            = false;
    private transient WeakReference<tpircSScript>                   ParentScript      = null;

    public SimpleTask(String Name)
    {
        TaskName = Name;
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
     * @return Whether this task is still ongoing
     */
    public final boolean isActive()
    {
        return Active;
    }

    public final boolean isPaused() {return Paused;}

    public boolean isFinished()     {return Finished;}

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

    /**
     * @param Script Caller script, this basically
     *
     * @return return true if successful, false if we need more time, keep triggering start until it is ready
     */
    protected final boolean StartTask(tpircSScript Script)
    {
        Active = true;
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
     * @return return true if successful, false if we need more time, keep triggering start until it is ready
     */
    protected final boolean StopTask(tpircSScript Script)
    {
        boolean result = onStopTask(Script);
        if(result)
        {
            Active   = false;
            Finished = true;
            onStop.Fire();
        }
        return result;
    }

    protected final void StopTaskNOW(tpircSScript Script)
    {
        onStopTask(Script);
        Active   = false;
        Finished = true;
        onStop.Fire();
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
}
