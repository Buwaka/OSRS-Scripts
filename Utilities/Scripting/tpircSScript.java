package Utilities.Scripting;

import Utilities.OSRSUtilities;
import Utilities.Patterns.Delegates.Delegate3;
import Utilities.Patterns.GameTickDelegate;
import org.dreambot.api.Client;
import org.dreambot.api.data.GameState;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.script.impl.TaskScript;
import org.dreambot.api.script.listener.GameStateListener;
import org.dreambot.api.script.listener.GameTickListener;
import org.dreambot.api.script.listener.ItemContainerListener;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.items.Item;
import randomhandler.RandomHandler;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class tpircSScript extends TaskScript implements GameTickListener,
                                                                 GameStateListener,
                                                                 ItemContainerListener//,ActionListener,AnimationListener, ChatListener,ExperienceListener,HitSplatListener,ItemContainerListener,LoginListener, MenuRowListener,PaintListener,ProjectileListener,RegionLoadListener,SpawnListener
{
    //TODO make this listen to everything and make it accessible through the script variable, perhaps make this calss include subcalsses that implement the listener, perhaps that may also trigger them
    // Simple Delegates for each listener
    // also don't forget to unsubscribe tasks to make sure all objects are cleaned up, perhaps automatically somehow
    private final List<SimpleTask>                  PersistentTasks        = new ArrayList<>();
    private final Lock                              PersistentTaskListLock = new ReentrantLock();
    private final List<SimpleTask>                  Tasks                  = new ArrayList<>();
    private final Lock                              TaskListLock           = new ReentrantLock();
    public        AtomicReference<SimpleTask>       CurrentTask            = new AtomicReference<>(null);
    public        AtomicInteger                     FailLimit   = new AtomicInteger(-1);
    public        Delegate3<ItemAction, Item, Item> onInventory = new Delegate3<>();
    public        GameTickDelegate                  onGameTick  = new GameTickDelegate();
    private       int                               FailCount              = 0;
    private       int                               CycleCounter           = 0;
    private       boolean                           IsReady                = false;
    private       Thread                            Randomizer;
    private       Set<SimpleCycle>                  Cycles                 = new HashSet<>();
    private       WeakReference<SimpleCycle>        CurrentCycle           = null;
    private       AtomicBoolean                     isLooping              = new AtomicBoolean(false);
    private       AtomicBoolean                     isSolving              = new AtomicBoolean(false);
    private       AtomicBoolean                     isGameStateChanging    = new AtomicBoolean(false);
    private       AtomicBoolean                     GameTicked             = new AtomicBoolean(false);
    private       GameState                         LastState;

    public tpircSScript()
    {
        LastState = Client.getGameState();
        if(Client.getGameState() != GameState.LOGGED_IN)
        {
            isGameStateChanging.set(true);
        }
        onInventory.Subscribe((A, B, C) -> {
            Logger.log(A.name() + B + C);
            return true;
        });
    }

    @Override
    public void onInventoryItemChanged(Item incoming, Item existing)
    {
        onInventory.Fire(ItemAction.Changed, incoming, existing);
        Logger.log("onInventoryItemChanged");
    }

    @Override
    public void onInventoryItemAdded(Item item)
    {
        onInventory.Fire(ItemAction.Added, item, null);
        Logger.log("onInventoryItemAdded");
    }

    @Override
    public void onInventoryItemRemoved(Item item)
    {
        onInventory.Fire(ItemAction.Removed, item, null);
        Logger.log("onInventoryItemRemoved");
    }

    @Override
    public void onInventoryItemSwapped(Item incoming, Item outgoing)
    {
        onInventory.Fire(ItemAction.Swapped, incoming, outgoing);
        Logger.log("onInventoryItemSwapped");
    }

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
        //KillRandomizer();
        //Randomizer = OSRSUtilities.StartRandomizerThread();
    }

    public final void SetRandomizerParameters(int minSpeed, float SpeedMultiplier, int Variance)
    {
        //KillRandomizer();
        //Randomizer = OSRSUtilities.StartRandomizerThread(minSpeed, SpeedMultiplier, Variance);
    }

    public void AddCycle(SimpleCycle Cycle)
    {
        Cycles.add(Cycle);
        if(CurrentCycle == null)
        {
            CurrentCycle = new WeakReference<>(Cycle);
            _startCycle();
        }
    }

    //refactor this
    private void _startCycle()
    {
        if(Cycles.isEmpty())
        {
            this.stop();
            return;
        }

        if(CurrentCycle == null)
        {
            CurrentCycle = new WeakReference<>(Cycles.iterator().next());
        }

        CurrentCycle.get().Start(this);
        IsReady = true;
    }

    public void StopCurrentCycle()
    {
        Cycles.remove(CurrentCycle.get());
        CurrentCycle = null;
        _startCycle();
    }

    @Override
    public void onExit()
    {
        super.onExit();
        Randomizer.interrupt();
        RandomHandler.loadRandoms();
        if(CurrentCycle != null && CurrentCycle.get() != null)
        {
            CurrentCycle.get().EndNow(this);
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Randomizer = OSRSUtilities.StartRandomizerThread();
        RandomHandler.clearRandoms();
        if(!Cycles.isEmpty() && !isGameStateChanging.get() && !isSolving.get())
        {
            Logger.log("Starting onstart procedure");
            _startCycle();
        }
    }

    public boolean IsActiveTaskLeft()
    {
        var tasks = GetSortedTasks();
        return !tasks.isEmpty();
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
                Task.Init(this);
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
                Task.Init(this);
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

    @Override
    public void onGameStateChange(GameState gameState)
    {
        Logger.log(gameState.name());
        if(gameState != GameState.LOGGED_IN)
        {
            Logger.log("Starting gamestate transition " + Client.getGameState().name());
            isGameStateChanging.set(true);
        }
        else
        {
            Logger.log("Completing Gamestate transition");
            isGameStateChanging.set(false);
            if(!IsReady)
            {
                _startCycle();
            }
        }
        LastState = gameState;
    }

//    @Override
//    public boolean onSolverStart(RandomSolver solver)
//    {
//        // to prevent the next loop
//        isSolving.set(true);
//        if(isLooping.get())
//        {
//            Logger.log("LoopLock is locked, preventing solver from starting");
//            return false;
//        }
//
//        Logger.log("Locking Looplock, starting solver");
//        return true;
//    }
//
//    @Override
//    public void onSolverEnd(RandomSolver solver)
//    {
//        Logger.log("Completing Solver Condition");
//        isSolving.set(false);
//    }

    List<SimpleTask> GetSortedTasks()
    {
        return Tasks.stream().sorted((a, b) -> a.priority() - b.priority()).filter(t -> !t.IsPaused() && t.IsAlive() &&
                                                                                        t.accept()).toList();
    }

    @Override
    public int onLoop()
    {
        if(isSolving.get() || isGameStateChanging.get() || !GameTicked.get())
        {
            return 0;
        }

        if(FailLimit.get() > 0)
        {
            if(FailCount > FailLimit.get())
            {
                Logger.log("Fail limit exceeded, exiting, " + this.getClass().getName());
                return -1;
            }
        }

        Logger.log("Starting loop");
        isLooping.set(true);
        GameTicked.set(false);

        if(CurrentCycle != null && CurrentCycle.get() != null)
        {
            CurrentCycle.get().Loop(this);
        }

        if(IsActiveTaskLeft())
        {
            var sorted = GetSortedTasks();
            var task   = sorted.getFirst();
            if(CurrentTask.get() != task)
            {
                // Starting new task
                if(!task.StartTask(this))
                {
                    isLooping.set(false);
                    GameTicked.set(false);
                    return OSRSUtilities.WaitTime(GetScriptIntensity());
                }

                // Notifying task if they've been replaced
                if(CurrentTask.get() != null && !CurrentTask.get().equals(task))
                {
                    Logger.log("Replacing old task (" + CurrentTask.toString() + " with new task(" + task + ")");
                    CurrentTask.get().ReplaceTask(this, task);
                }
                CurrentTask.set(task);
            }

            if(CurrentTask.get() != null)
            {
                FailCount = 0;

                // Executing task
                Logger.log("Executing task: " + CurrentTask.get().GetTaskName() + " (" +
                           CurrentTask.get().getClass().getName() + ")");
                int result = CurrentTask.get().execute();
                //Logger.log("Task result is: " + result);
                if(result <= 0)
                {
                    // Stopping task
                    Logger.log(CurrentTask.get().toString() + " return " + result + ", stopping task");
                    StopTask(CurrentTask.get());
                    CurrentTask.set(null);
                    isLooping.set(false);
                    GameTicked.set(false);
                    return OSRSUtilities.WaitTime(GetScriptIntensity());
                }
                isLooping.set(false);
                GameTicked.set(false);
                return result;
            }
        }
        else if(CurrentCycle != null && CurrentCycle.get() != null && CurrentCycle.get().IsCycleComplete(this))
        {
            CurrentCycle.get().CompleteCycle();
            if(CurrentCycle.get().CanRestart(this))
            {
                CurrentCycle.get().Restart(this);
                isLooping.set(false);
                GameTicked.set(false);
                return OSRSUtilities.WaitTime(GetScriptIntensity());
            }
            while(!CurrentCycle.get().End(this))
            {
                OSRSUtilities.Wait(GetScriptIntensity());
            }
            Cycles.remove(CurrentCycle.get());
            _startCycle();
        }
        FailCount++;
        isLooping.set(false);
        GameTicked.set(false);
        return OSRSUtilities.WaitTime(GetScriptIntensity());
    }

    @Override
    public void onGameTick()
    {
        onGameTick.Fire();
        if(!isLooping.get())
        {
            GameTicked.set(true);
        }
    }

    public enum ItemAction
    {
        Added,
        Removed,
        Changed,
        Swapped
    }
}
