package Utilities.Scripting;

import Utilities.OSRSUtilities;
import org.dreambot.api.Client;
import org.dreambot.api.data.GameState;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.script.impl.TaskScript;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import randomhandler.RandomHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class tpircSScript extends TaskScript
{
    private final List<SimpleTask>            PersistentTasks        = new ArrayList<>();
    private final Lock                        PersistentTaskListLock = new ReentrantLock();
    private final List<SimpleTask>            Tasks                  = new ArrayList<>();
    private final Lock                        TaskListLock           = new ReentrantLock();
    public        AtomicReference<SimpleTask> CurrentTask            = new AtomicReference<>(null);
    public        AtomicInteger               FailLimit              = new AtomicInteger(-1);
    private       int                         FailCount              = 0;
    private       Thread                      Randomizer;
    private       SimpleCycle                 Cycle                  = null;

    public OSRSUtilities.ScriptIntenity GetScriptIntensity() {return OSRSUtilities.ScriptIntenity.Normal;}

    private void KillRandomizer()
    {
        if(Randomizer != null && Randomizer.isAlive())
        {
            Randomizer.interrupt();
        }
        Randomizer = null;
    }

    public final void ResetRandomizer()
    {
        KillRandomizer();
        Randomizer = OSRSUtilities.StartRandomizerThread();
    }

    public final void SetRandomizerParameters(int minSpeed, float SpeedMultiplier, int Variance)
    {
        KillRandomizer();
        Randomizer = OSRSUtilities.StartRandomizerThread(minSpeed, SpeedMultiplier, Variance);
    }

    public final void PauseRandomizerThread()
    {
        if(Randomizer != null && Randomizer.isAlive())
        {
            Randomizer.interrupt();
        }
    }

    public final void UnPauseRandomizerThread()
    {
        if(Randomizer != null && Randomizer.isAlive())
        {
            Randomizer.start();
        }
        else
        {
            ResetRandomizer();
        }
    }

    public void SetCycle(SimpleCycle Cycle, int count)
    {
        Cycle.Start(this, count);
        this.Cycle = Cycle;
    }

    @Override
    public void onExit()
    {
        super.onExit();
        Randomizer.interrupt();
        RandomHandler.loadRandoms();
        if(Cycle != null)
        {
            Cycle.EndNow(this);
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Randomizer = OSRSUtilities.StartRandomizerThread();
        RandomHandler.clearRandoms();
    }

    public boolean IsActiveTaskLeft()
    {
        for(var task : Tasks)
        {
            Logger.log(task.toString() + " is passive: " + task.IsPassive());
            if(!task.IsPassive())
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public TaskNode[] getNodes()
    {
        return Tasks.toArray(new SimpleTask[0]);
    }

    public List<SimpleTask> getSimpleTasks()
    {
        return Tasks;
    }

    public List<SimpleTask> getPersistentNodes()
    {
        return PersistentTasks;
    }


    @Override
    public final void addNodes(TaskNode... nodes)
    {
        if(nodes == null)
        {return;}

        Logger.log("Adding nodes: " + Arrays.toString(nodes));
        for(var node : nodes)
        {
            if(SimpleTask.class.isAssignableFrom(node.getClass()))
            {
                SimpleTask Task = (SimpleTask) node;
                Logger.log("Adding task: " + Task.GetTaskName());
                TaskListLock.lock();
                Tasks.add(Task);
                TaskListLock.unlock();
            }
            else
            {
                Logger.log("Trying to add Tasknode instead of SimpleTask: " + node.getClass().getName());
            }
        }
    }

    public final void addPersistentNodes(TaskNode... nodes)
    {
        if(nodes == null)
        {return;}

        Logger.log("Adding persistent nodes: " + Arrays.toString(nodes));
        for(var node : nodes)
        {
            if(SimpleTask.class.isAssignableFrom(node.getClass()))
            {
                SimpleTask Task = (SimpleTask) node;
                Logger.log("Adding Persistent task: " + Task.GetTaskName());
                PersistentTaskListLock.lock();
                PersistentTasks.add(Task);
                PersistentTaskListLock.unlock();
            }
            else
            {
                Logger.log("Trying to add Tasknode instead of SimpleTask: " + node.getClass().getName());
            }
        }
    }


    @Override
    public final void removeNodes(TaskNode... nodes)
    {
        if(nodes == null)
        {return;}

        for(var node : nodes)
        {
            if(SimpleTask.class.isAssignableFrom(node.getClass()))
            {
                SimpleTask Task = (SimpleTask) node;
                TaskListLock.lock();
                Logger.log("Removing task: " + Task.GetTaskName());
                Tasks.remove(Task);
                TaskListLock.unlock();
                Logger.log(Tasks.size() + " Tasks left");
                for(var task : Tasks)
                {
                    Logger.log(task.toString());
                }
            }
            else
            {
                Logger.log("Trying to remove Tasknode instead of SimpleTask: " + node.getClass().getName());
            }
        }
    }

    public final void removePersistentNodes(TaskNode... nodes)
    {
        if(nodes == null)
        {return;}

        for(var node : nodes)
        {
            if(SimpleTask.class.isAssignableFrom(node.getClass()))
            {
                SimpleTask Task = (SimpleTask) node;
                PersistentTaskListLock.lock();
                Logger.log("Removing persistent task: " + Task.GetTaskName());
                PersistentTasks.remove(Task);
                PersistentTaskListLock.unlock();
                Logger.log(PersistentTasks.size() + " Tasks left");
            }
            else
            {
                Logger.log("Trying to remove Tasknode instead of SimpleTask: " + node.getClass().getName());
            }
        }
    }

    public void StopTask(SimpleTask task)
    {
        Logger.log("Stopping task: " + task.GetTaskName());
        task.StopTask(this);
        if(!task.IsPersistent())
        {
            removeNodes(task);
        }
    }

    List<SimpleTask> GetSortedTasks()
    {
        return Tasks.stream().sorted((a, b) -> a.priority() - b.priority()).filter(t -> !t.IsPaused() && t.IsAlive() &&
                                                                                        t.accept()).toList();
    }

    @Override
    public int onLoop()
    {
        if(Client.getGameState() != GameState.LOGGED_IN)
        {
            Sleep.sleepUntil(() -> Client.getGameState() == GameState.LOGGED_IN, 60000);
        }

        if(Cycle != null)
        {
            Cycle.Loop(this);
        }

        if(FailLimit.get() > 0)
        {
            if(FailCount > FailLimit.get())
            {
                Logger.log("Fail limit exceeded, exiting, " + this.getClass().getName());
                return -1;
            }
        }

        if(!Tasks.isEmpty())
        {
            var sorted = GetSortedTasks();

            // Check if there are any non passive tasks left
            if(!IsActiveTaskLeft())
            {
                Cycle.CompleteCycle();
                Cycle.Restart(this);
                return OSRSUtilities.WaitTime(GetScriptIntensity());
            }

            if(!sorted.isEmpty())
            {
                FailCount = 0;
                var task = sorted.getFirst();

                // Starting new task
                if(!task.StartTask(this))
                {
                    return OSRSUtilities.WaitTime(GetScriptIntensity());
                }

                // Notifying task if they've been replaced
                if(CurrentTask.get() != null && !CurrentTask.get().equals(task))
                {
                    Logger.log("Replacing old task (" + CurrentTask.toString() + " with new task(" + task.toString() +
                               ")");
                    CurrentTask.get().onReplaced(this, task);
                }
                CurrentTask.set(task);

                // Executing task
                Logger.log("Executing task: " + task.GetTaskName() + " (" + task.getClass().getName() + ")");
                int result = task.execute();
                if(result <= 0)
                {
                    // Stopping task
                    Logger.log(task.toString() + " return " + result + ", stopping task");
                    StopTask(task);
                    CurrentTask.set(null);
                    return OSRSUtilities.WaitTime(GetScriptIntensity());
                }
                return result;
            }
        }
        else if(Cycle.CanRestart(this))
        {
            Cycle.CompleteCycle();
            Cycle.Restart(this);
            return OSRSUtilities.WaitTime(GetScriptIntensity());
        }
        FailCount++;
        return OSRSUtilities.WaitTime(GetScriptIntensity());
    }
}
