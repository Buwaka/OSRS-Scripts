package Utilities.Scripting;

import Utilities.OSRSUtilities;
import Utilities.Patterns.Delegates.Delegate;
import Utilities.Patterns.Delegates.Delegate1;
import com.google.gson.Gson;
import org.dreambot.api.script.TaskNode;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.function.Supplier;

public abstract class SimpleTask extends TaskNode implements ITask, Serializable, Cloneable
{
    // TODO replace with serializable Requirement
    public transient  Supplier<Boolean>            AcceptCondition   = () -> true;
    public transient  Supplier<Boolean>            CompleteCondition = null;
    public transient  Delegate                     onComplete        = new Delegate();
    public transient  Delegate1<SimpleTask>        onReplaced        = new Delegate1<>();
    public transient  Delegate                     onAccept          = new Delegate();
    public transient  Delegate1<Integer>           onExecute         = new Delegate1<>();
    public transient  Delegate                     onStart           = new Delegate();
    public transient  Delegate                     onStop            = new Delegate();
    public transient  Delegate                     onPause           = new Delegate();
    public transient  Delegate                     onUnPause         = new Delegate();
    // public            SimpleTask[]                                  ChildTasks        = null; // TODO, basically start these tasks when this task is complete
    private           OSRSUtilities.ScriptIntenity ScriptIntensity   = OSRSUtilities.ScriptIntenity.Sweating;
    private           int                          TaskPriority      = 1;
    private           String                       TaskName          = "";
    private transient boolean                      Active            = true;
    private transient boolean                      Finished          = false;
    private transient boolean                      Paused            = false;
    private transient WeakReference<IFScript>      ParentScript      = null;

    public SimpleTask(String Name)
    {
        TaskName = Name;
    }

    public SimpleTask Copy()
    {
        Gson       gson     = OSRSUtilities.OSRSGsonBuilder.create();
        SimpleTask deepCopy = gson.fromJson(gson.toJson(this), this.getClass());
        return deepCopy;
    }

    public IFScript GetScript()
    {
        return ParentScript.get();
    }

    /**
     * Be sure to call this in case you don't add the task to the script using any of the addnode(), aka advanced tasks
     */
    public void Init(IFScript Script)
    {
        ParentScript = new WeakReference<>(Script);
    }

    /**
     * @param Script Caller script, this basically
     *
     * @return return true if successfully paused, otherwise false
     */
    public final boolean PauseTask(IFScript Script)
    {
        Paused = true;
        boolean result = onPauseTask(Script);
        onPause.Fire();// see if we need to enclose this by a if check as well
        return result;
    }

    public void SetTaskName(String Name)
    {
        TaskName = Name;
    }

    public void SetTaskPriority(int taskPriority)
    {
        TaskPriority = taskPriority;
    }

    /**
     * @param Script Caller script, this basically
     *
     * @return return true if successfully paused, otherwise false
     */
    public final boolean UnPauseTask(IFScript Script)
    {
        Paused = false;
        boolean result = onUnPauseTask(Script);
        onUnPause.Fire(); // see if we need to enclose this by a if check as well
        return result;
    }

    /**
     * @return Whether this task is still ongoing
     */
    public final boolean isActive()
    {
        return Active;
    }

    public boolean isFinished()     {return Finished;}

    public final boolean isPaused() {return Paused;}

    protected final void ReplaceTask(IFScript Script, SimpleTask other)
    {
        onReplaced(Script, other);
        onReplaced.Fire(other);
    }

    /**
     * @param Script caller script
     * @param other  Task that is replacing this one
     *               Is called after the task has been stopped
     */
    public void onReplaced(IFScript Script, SimpleTask other) {}

    /**
     * @param Script Caller script, this basically
     *
     * @return return true if successful, false if we need more time, keep triggering start until it is ready
     */
    protected final boolean StartTask(IFScript Script)
    {
        Active = true;
        SetScriptIntensity(Script.GetScriptIntensity());
        boolean result = onStartTask(Script);
        if(result)
        {
            onStart.Fire();
        }
        return result;
    }

    public void SetScriptIntensity(OSRSUtilities.ScriptIntenity scriptIntensity)
    {
        ScriptIntensity = scriptIntensity;
    }

    /**
     * @param Script Caller script, this basically
     *
     * @return return true if successful, false if we need more time, keep triggering start until it is ready
     */
    protected final boolean StopTask(IFScript Script)
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

    protected final void StopTaskNOW(IFScript Script)
    {
        onStopTask(Script);
        Active   = false;
        Finished = true;
        onStop.Fire();
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

    @Override
    public int priority()
    {
        return TaskPriority;
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

    public boolean Ready()
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
        return CompleteCondition != null && CompleteCondition.get()
                ? 0
                : OSRSUtilities.WaitTime(GetScriptIntensity());
    }

    public OSRSUtilities.ScriptIntenity GetScriptIntensity()
    {
        return ScriptIntensity;
    }
}
