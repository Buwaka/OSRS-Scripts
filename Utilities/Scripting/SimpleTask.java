package Utilities.Scripting;

import Utilities.OSRSUtilities;
import org.dreambot.api.script.TaskNode;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public abstract class SimpleTask extends TaskNode implements ITask
{
    private final OSRSUtilities.ScriptIntenity                  _scriptIntensity  = OSRSUtilities.ScriptIntenity.Lax;
    public        AtomicReference<OSRSUtilities.ScriptIntenity> ScriptIntensity   = new AtomicReference<>(
            _scriptIntensity);
    public        AtomicInteger     TaskPriority      = new AtomicInteger(1);
    public    Supplier<Boolean> AcceptCondition   = () -> true;
    public        Supplier<Boolean> CompleteCondition = null;
    private       boolean                                       Alive             = true;
    private       boolean                                       Passive           = false;
    private       boolean                                       Paused           = false;
    private       boolean                                       Persist          = false;
    private       String                                        TaskName         = "";

    public SimpleTask(String Name)
    {
        TaskName = Name;
        Persist  = false;
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
    public final boolean IsPassive()    {return Passive;}

    public final boolean IsPaused()     {return Paused;}

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

    @Override
    public int priority()
    {
        return TaskPriority.get();
    }

    @Override
    public boolean accept()
    {
        return AcceptCondition.get();
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
        Alive  = true;
        ScriptIntensity.set(Script.GetScriptIntensity());
        return onStartTask(Script);
    }

    /**
     * @param Script Caller script, this basically
     *
     * @return return true if successfully paused, otherwise false
     */
    public final boolean PauseTask(tpircSScript Script)
    {
        Paused = true;
        return onPauseTask(Script);
    }

    /**
     * @param Script Caller script, this basically
     *
     * @return return true if successfully paused, otherwise false
     */
    public final boolean UnPauseTask(tpircSScript Script)
    {
        Paused = false;
        return onUnPauseTask(Script);
    }

    /**
     * @param Script Caller script, this basically
     *
     * @return return true if successful, false if we need more time, keep triggering start until it is ready
     */
    protected final boolean StopTask(tpircSScript Script)
    {
        Alive  = false;
        return onStopTask(Script);
    }

    /**
     * @param Script caller script
     * @param other  Task that is replacing this one
     *               Is called after the task has been stopped
     */
    public void onReplaced(tpircSScript Script, SimpleTask other) {}

    @Override
    public int execute()
    {
        return CompleteCondition != null && CompleteCondition.get() ? 0 : OSRSUtilities.WaitTime(ScriptIntensity.get());
    }
}
