package Utilities.Scripting;

import Cycles.CycleGenerators.CycleLibrary;
import Cycles.Tasks.AdvanceTasks.GraveStoneTask;
import Cycles.Tasks.AdvanceTasks.OpenBankTask;
import Utilities.GrandExchange.GEInstance;
import Utilities.OSRSUtilities;
import Utilities.Patterns.Delegates.Delegate;
import Utilities.Patterns.Delegates.Delegate3;
import Utilities.Patterns.GameTickDelegate;
import Utilities.Scripting.Listeners.GraveStoneListener;
import org.dreambot.api.Client;
import org.dreambot.api.data.GameState;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.script.ScriptManager;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.script.impl.TaskScript;
import org.dreambot.api.script.listener.GameStateListener;
import org.dreambot.api.script.listener.GameTickListener;
import org.dreambot.api.script.listener.ItemContainerListener;
import org.dreambot.api.script.listener.PaintListener;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.Item;
import randomhandler.RandomHandler;

import java.awt.*;
import java.util.List;
import java.util.Queue;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public abstract class tpircSScript extends TaskScript implements GameTickListener,
        GameStateListener,
        ItemContainerListener,
        PaintListener//,ActionListener,AnimationListener, ChatListener,ExperienceListener,HitSplatListener,ItemContainerListener,LoginListener, MenuRowListener,PaintListener,ProjectileListener,RegionLoadListener,SpawnListener
{
    private static Random                            rand                   = new Random();
    //TODO make this listen to everything and make it accessible through the script variable, perhaps make this calss include subcalsses that implement the listener, perhaps that may also trigger them
    // Simple Delegates for each listener
    // also don't forget to unsubscribe tasks to make sure all objects are cleaned up, perhaps automatically somehow
    private final  List<SimpleTask>                  PersistentTasks        = new ArrayList<>();
    private final  Lock                              PersistentTaskListLock = new ReentrantLock();
    private final  List<SimpleTask>                  Tasks                  = new ArrayList<>();
    private final  Lock                              TaskListLock           = new ReentrantLock();
    private final  List<Supplier<SimpleCycle[]>>     Cycles                 = new ArrayList<>();
    public         AtomicReference<SimpleTask>       CurrentTask            = new AtomicReference<>(
            null);
    public         AtomicInteger                     FailLimit              = new AtomicInteger(-1);
    public         Delegate3<ItemAction, Item, Item> onInventory            = new Delegate3<>();
    public         GameTickDelegate                  onGameTick             = new GameTickDelegate();
    public         Delegate                          onTaskRemoved          = new Delegate();
    public         Delegate                          onTaskAdded            = new Delegate();
    public         Delegate                          onBankCached           = new Delegate();
    private        int                               FailCount              = 0;
    private        int                               CycleCounter           = 0;
    private        Thread                            Randomizer;
    private        SimpleCycle                       CycleSetup             = null;
    private        SimpleCycle                       CurrentCycle           = null;
    private        Queue<SimpleCycle>                CycleQueue             = null;
    private        AtomicBoolean                     isLooping              = new AtomicBoolean(
            false);
    private        AtomicBoolean                     isSolving              = new AtomicBoolean(
            false);
    private        AtomicBoolean                     isGameStateChanging    = new AtomicBoolean(
            false);
    private        AtomicBoolean                     GameTicked             = new AtomicBoolean(
            false);
    private        AtomicBoolean                     isPaused               = new AtomicBoolean(true);
    private        AtomicInteger                     PauseTime              = new AtomicInteger(
            GetRandom().nextInt(5000) + 5000); // is pause on the start
    private        long                              StopTaskTimeout        = 10000;
    private        OpenBankTask                      CacheBank              = null;
    public         GraveStoneListener                GraveListener          = new GraveStoneListener();
    private        GEInstance                        GrandExchangeInstance  = null;
    private        PlayerConfig                      Config                 = new PlayerConfig();
    //private              ProfitTracker      PTracker              = new ProfitTracker();
    private        boolean                           DebugPaint             = true;


    public enum ItemAction
    {
        Added,
        Removed,
        Changed,
        Swapped
    }

    public tpircSScript()
    {
        Logger.log("Levels at init: " + Skills.getRealLevel(Skill.HITPOINTS));
        //        org.burningwave.core.assembler.StaticComponentContainer.Modules.exportAllToAll();
        ResetRandomizer();
        //ScriptManager.getScriptManager().addListener(PTracker);
        ScriptManager.getScriptManager().addListener(GraveListener);
        GraveListener.onDeath.Subscribe(this, this::onDeath);
        CycleLibrary.init(this);
        if(Client.getGameState() != GameState.LOGGED_IN)
        {
            isGameStateChanging.set(true);
        }
        onInventory.Subscribe(this, (A, B, C) -> {
            Logger.log(A.name() + B + C);
            return true;
        });
    }

    // For Legacy sake
    public void AddCycle(SimpleCycle Cycle)
    {
        Cycles.add(() -> {return new SimpleCycle[]{Cycle};});
    }

    public void AddCycle(Supplier<SimpleCycle[]> Cycle)
    {
        Cycles.add(Cycle);
    }

    public void Delay(int ms)
    {
        PauseTime.set(ms);
    }

    public PlayerConfig GetConfig()
    {
        return Config;
    }

    /**
     * @return Returns current GE instance, creates one if none exists, do make sure to cleanup afterwards
     */
    public GEInstance GetGEInstance()
    {
        if(GrandExchangeInstance == null)
        {
            Logger.log("Script: GetGEInstance: Loading GEInstance from file");
            var instance = Config.LoadState(GEInstance.ConfigID, GEInstance.class);
            if(instance != null)
            {
                Logger.log("Script: GetGEInstance: GEInstance file found and loaded");
                instance.init(this);
                GrandExchangeInstance = instance;
                return instance;
            }
            else
            {
                Logger.log("Script: GetGEInstance: new GEInstance");
                GrandExchangeInstance = new GEInstance(this);
                return GrandExchangeInstance;
            }
        }
        return GrandExchangeInstance;
    }

    public final void ResetRandomizer()
    {
        KillRandomizer();
        Randomizer = OSRSUtilities.StartRandomizerThread();
    }

    private void KillRandomizer()
    {
        if(Randomizer != null && Randomizer.isAlive())
        {
            Randomizer.interrupt();
        }
        Randomizer = null;
    }

    public final void SetRandomizerParameters(int minSpeed, float SpeedMultiplier, int Variance)
    {
        KillRandomizer();
        Randomizer = OSRSUtilities.StartRandomizerThread(minSpeed, SpeedMultiplier, Variance);
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
                Logger.log("Trying to add Tasknode instead of SimpleTask: " +
                           node.getClass().getName());
            }
        }
    }

    public List<SimpleTask> getPersistentNodes()
    {
        return PersistentTasks;
    }

    public List<SimpleTask> getSimpleTasks()
    {
        return Tasks;
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
                Logger.log("Trying to remove Tasknode instead of SimpleTask: " +
                           node.getClass().getName());
            }
        }
    }

    public void setDebugPaint(boolean debugPaint)
    {
        DebugPaint = debugPaint;
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

    private boolean onDeath(Tile tile)
    {
        Logger.log(Color.red, "You died, going to collect items from gravestone");
        var collect = new GraveStoneTask("Collect GraveStone items", tile);
        collect.onComplete.Subscribe(this, () -> CurrentCycle.Restart(this));
        collect.SetTaskPriority(-5);
        addNodes(collect);

        return true;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        getScriptManager().addListener(Config);
        Config.onAccountChanged.Subscribe(this, this::onAccountChange);

        Randomizer = OSRSUtilities.StartRandomizerThread();
        RandomHandler.clearRandoms();

        if(!Bank.isCached() && !Bank.isOpen())
        {
            Logger.log("Bank is not cached and needs to be, visiting bank");
            CacheBank = new OpenBankTask();
            CacheBank.onComplete.Subscribe(CacheBank, () -> {
                CacheBank = null;
                onBankCached.Fire();
            });
            addNodes(CacheBank);
            return;
        }

        if(!Cycles.isEmpty() && !isGameStateChanging.get() && !isSolving.get())
        {
            Logger.log("Starting onstart procedure");
            _startCycle();
        }
    }

    private void onAccountChange()
    {
        GrandExchangeInstance = null;
    }

    //refactor this
    private void _startCycle()
    {
        if(CacheBank != null)
        {
            Logger.log("Script: _startCycle: BankCaching in progress");
            return;
        }

        if(Cycles.isEmpty())
        {
            Logger.log("Script: _startCycle: No cycles to generate, quiting");
            this.stop();
            return;
        }

        if(CycleSetup != null)
        {
            HandleStartUpTasks();
            return;
        }

        Logger.log("Script: _startCycle: Start next cycle in queue");
        if(CycleQueue != null && !CycleQueue.isEmpty())
        {
            int length = CycleQueue.size();
            for(int i = 0; i < length; i++)
            {
                if(CycleQueue.peek() == null)
                {
                    CycleQueue.poll();
                    continue;
                }

                SimpleCycle cycle = CycleQueue.poll();
                cycle.init(this);
                Logger.log("Script: _startCycle: Checking if we can start " + cycle.GetName());
                if(_setCurrentCycle(cycle))
                {
                    Logger.log("Script: _startCycle: starting " + cycle.GetName());
                    return;
                }
                //CycleQueue.add(cycle);
            }
        }
        else
        {
            GenerateNewCycles();
        }
    }

    private void HandleStartUpTasks()
    {
        Logger.log("Script: HandleStartUpTasks: Start Cycle setup");
        if(CycleSetup.hasStartUpTasks())
        {
            Logger.log("Script: HandleStartUpTasks: Cycle has Startup Tasks");
            if(!IsActiveTaskLeft())
            {
                Logger.log("Script: HandleStartUpTasks: Startup tasks are not active, rejecting cycle, " +
                           CycleSetup.toString());
                CycleSetup = null;
            }
        }
        else
        {
            Logger.log(
                    "Script: HandleStartUpTasks: Startup tasks have been completed, trying to start cycle, " +
                    CycleSetup.toString());
            if(CycleSetup.Start(this))
            {
                Logger.log("Script: HandleStartUpTasks: Starting Cycle, " + CycleSetup.toString());
                CurrentCycle = CycleSetup;
                CycleSetup   = null;
            }
        }
    }


    private static int CycleGenerationAttempts = 0;
    private static int CycleGenerationMaxAttempts = 10;

    private void GenerateNewCycles()
    {
        if(CycleGenerationAttempts > CycleGenerationMaxAttempts)
        {
            Logger.log("Script: GenerateNewCycles: Failed too many times trying to generate new cycles, exiting");
            this.stop();
            return;
        }


        Logger.log("Script: GenerateNewCycles: generating new cycles");
        for(var ToGen : Cycles)
        {
            if(ToGen == null)
            {
                continue;
            }

            SimpleCycle[] nextCycles = ToGen.get();
            if(nextCycles == null)
            {
                CycleGenerationAttempts++;
                continue;
            }

            CycleQueue = new ArrayDeque<>();
            for(int i = 0; i < nextCycles.length; i++)
            {
                var cycle = nextCycles[i];
                cycle.init(this);
                Logger.log("Script: GenerateNewCycles: Trying to start newly generated cycle " + cycle);
                if(_setCurrentCycle(cycle))
                {
                    Logger.log("Script: GenerateNewCycles: cycle " + cycle +
                               " has started, adding remaining cycles to the queue");
                    if(i != nextCycles.length - 1)
                    {
                        CycleQueue.addAll(List.of(Arrays.copyOfRange(nextCycles,
                                                                     i,
                                                                     nextCycles.length)));
                    }
                    CycleGenerationAttempts = 0;
                    return;
                }
                CycleGenerationAttempts++;
                CycleQueue.add(cycle);
            }
        }
    }

    public boolean IsActiveTaskLeft()
    {
        var tasks = GetSortedTasks();
        Logger.log("tpircSScript: IsActiveTaskLeft: " + tasks.size());
        return !tasks.isEmpty();
    }

    List<SimpleTask> GetSortedTasks()
    {
        return Tasks.stream()
                    .sorted((a, b) -> a.priority() - b.priority())
                    .filter(t -> !t.isPaused() && t.isActive() && t.accept())
                    .toList();
    }

    private boolean _setCurrentCycle(SimpleCycle next)
    {
        if(next.Ready() && !next.isGoalMet())
        {
            Logger.log("Script: _setCurrentCycle: " + next.GetName() +
                       " goal hasn't been met, starting");

            if(next.hasStartUpTasks())
            {
                Logger.log("Script: _startCycle: Cycle has StartupRequirements, adding tasks, " +
                           next.toString());
                addNodes(next.GenerateStartupTasks());
                CycleSetup = next;
                return true;
            }

            if(next.Start(this))
            {
                Logger.log("Script: _startCycle: Start Cycle, " + next.toString());
                CurrentCycle = next;
            }
            return true;
        }
        return false;
    }

    @Override
    public void onExit()
    {
        super.onExit();
        Randomizer.interrupt();
        RandomHandler.loadRandoms();
        if(CurrentCycle != null)
        {
            CurrentCycle.EndNow(this);
        }
    }

    @Override
    public void onPaint(Graphics graphics)
    {
        //Logger.log("Paint3D");
        if(CurrentCycle != null)
        {
            CurrentCycle.onPaint(graphics);
        }
        for(var task : Tasks)
        {
            if(task != null)
            {
                task.onPaint(graphics);
            }
        }

        if(DebugPaint)
        {
            if(CurrentCycle != null)
            {
                CurrentCycle.onDebugPaint(graphics);
            }
            for(var task : Tasks)
            {
                if(task != null)
                {
                    task.onDebugPaint(graphics);
                }
            }
        }

        super.onPaint(graphics);
    }

    @Override
    public void onPaint(Graphics2D graphics)
    {
        if(CurrentCycle != null)
        {
            CurrentCycle.onPaint(graphics);
        }
        for(var task : Tasks)
        {
            if(task != null)
            {
                task.onPaint(graphics);
            }
        }

        if(DebugPaint)
        {
            if(CurrentCycle != null)
            {
                CurrentCycle.onDebugPaint(graphics);
            }
            for(var task : Tasks)
            {
                if(task != null)
                {
                    task.onDebugPaint(graphics);
                }
            }
        }
        super.onPaint(graphics);
    }

    @Override
    public final void addNodes(TaskNode... nodes)
    {
        if(nodes == null)
        {return;}

        Logger.log("Script: addNodes: Adding nodes: " + Arrays.toString(nodes));
        for(var node : nodes)
        {
            if(node == null)
            {
                continue;
            }

            if(SimpleTask.class.isAssignableFrom(node.getClass()))
            {
                SimpleTask Task = (SimpleTask) node;
                Task.Init(this);
                Logger.log("Script: addNodes: Adding task: " + Task.GetTaskName());
                TaskListLock.lock();
                Tasks.add(Task);
                TaskListLock.unlock();
                onTaskAdded.Fire();
            }
            else
            {
                Logger.log("Script: addNodes: Trying to add Tasknode instead of SimpleTask: " +
                           node.getClass().getName());
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
            if(node == null)
            {
                continue;
            }

            if(SimpleTask.class.isAssignableFrom(node.getClass()))
            {
                SimpleTask Task = (SimpleTask) node;
                TaskListLock.lock();
                Logger.log("Script: removeNodes: Removing task: " + Task.GetTaskName());
                Tasks.remove(Task);
                Task = null;
                TaskListLock.unlock();
                onTaskRemoved.Fire();
                Logger.log("Script: removeNodes: " + Tasks.size() + " Tasks left");
                for(var task : Tasks)
                {
                    Logger.log("Script: removeNodes: " + task.toString());
                }
            }
            else
            {
                Logger.log(
                        "Script: removeNodes: Trying to remove Tasknode instead of SimpleTask: " +
                        node.getClass().getName());
            }
        }
    }

    @Override
    public TaskNode[] getNodes()
    {
        return Tasks.toArray(new SimpleTask[0]);
    }

    @Override
    public int onLoop()
    {
        if(isPaused.get())
        {
            isPaused.set(false);
            Logger.log("Script: onLoop: Pausing script");
            return PauseTime.get();
        }

        if(isSolving.get() || isGameStateChanging.get() || !GameTicked.get())
        {
            Logger.log("Script: onLoop: client is busy " + isSolving.get() +
                       isGameStateChanging.get() + !GameTicked.get());
            return 50;
        }

        if(FailLimit.get() > 0)
        {
            if(FailCount > FailLimit.get())
            {
                Logger.log("Script: onLoop: Fail limit exceeded, exiting, " +
                           this.getClass().getName());
                GameTicked.set(true);
                isLooping.set(false);
                return -1;
            }
        }

        Logger.log("Script: onLoop: Starting loop");
        isLooping.set(true);
        GameTicked.set(false);

        int CycleResult = 0;
        if(CurrentCycle != null)
        {
            Logger.log("Script: onLoop: ticking Cycle");
            CycleResult = CurrentCycle.Loop(this);
        }

        Logger.log("Script: onLoop: CycleNullCheck: " + (CurrentCycle != null));
        if(CurrentCycle != null && (CurrentCycle != null))
        {
            Logger.log("Script: onLoop: CycleIsStarted Check: " + CurrentCycle.isStarted() +
                       " CycleResult: " + CycleResult + " CycleIsComplete: " +
                       CurrentCycle.isCycleComplete(this));
        }
        if(IsActiveTaskLeft())
        {
            // Task loop
            Logger.log("Script: onLoop: Start Task Loop");
            var sorted = GetSortedTasks();
            var task   = sorted.getFirst();
            if(CurrentTask.get() != task)
            {
                Logger.log("Script: onLoop: starting new task " + task);
                if(!task.StartTask(this))
                {
                    Logger.log("Script: onLoop: task is not ready to be started");
                    isLooping.set(false);
                    GameTicked.set(false);
                    return OSRSUtilities.WaitTime(GetScriptIntensity());
                }

                // Notifying task if they've been replaced
                if(CurrentTask.get() != null && !CurrentTask.get().equals(task))
                {
                    Logger.log("Script: onLoop: Replacing old task (" + CurrentTask.toString() +
                               " with new task(" + task + ")");
                    CurrentTask.get().ReplaceTask(this, task);
                }
                CurrentTask.set(task);
            }

            if(CurrentTask.get() != null)
            {
                FailCount = 0;

                // Executing task
                Logger.log("Script: onLoop: Executing task: " + CurrentTask.get().GetTaskName() +
                           " (" + CurrentTask.get().getClass().getName() + ")");

                int result = CurrentTask.get().execute();
                Logger.log("Script: onLoop: Task result is: " + result);
                if(result <= 0)
                {
                    // Stopping task
                    Logger.log("Script: onLoop: " + CurrentTask.get().toString() + " return " +
                               result + ", stopping task");
                    if(!Sleep.sleepUntil(() -> StopTask(CurrentTask.get()), StopTaskTimeout))
                    {
                        StopTaskNow(CurrentTask.get());
                    }
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
        else if(CurrentCycle != null && CurrentCycle.isStarted() &&
                (CycleResult <= 0 || CurrentCycle.isCycleComplete(this)))
        {
            Logger.log("Script: onLoop: Cycle is complete, cleaning up, " + CurrentCycle);
            CurrentCycle.CompleteCycle();
            CleanUpCycle();

            // restart
            if(!CurrentCycle.isGoalMet())
            {
                if(CurrentCycle.hasEndTasks())
                {
                    Logger.log("Script: onLoop: Cycle has end tasks, restart " + CurrentCycle);
                    addNodes(CurrentCycle.GenerateEndTasks());
                }
                else
                {
                    Logger.log("Script: onLoop: Restarting cycle " + CurrentCycle);
                    CurrentCycle.Restart(this);
                }

                isLooping.set(false);
                GameTicked.set(false);
                return OSRSUtilities.WaitTime(GetScriptIntensity());
            }

            Logger.log("Script: onLoop: ending cycle " + CurrentCycle);
            // end
            while(!CurrentCycle.End(this))
            {
                Logger.log("Script: onLoop: Cycle is not ready to be ended " + CurrentCycle);
                OSRSUtilities.Wait(GetScriptIntensity());
            }
            if(CurrentCycle.hasEndTasks())
            {
                Logger.log("Script: onLoop: Cycle has end tasks " + CurrentCycle);
                addNodes(CurrentCycle.GenerateEndTasks());
            }
            StopCurrentCycle();
        }
        else if(CurrentCycle == null && !Cycles.isEmpty())
        {
            // Start Cycle
            Logger.log("Script: onLoop: Starting new cycle because current cycle is empty");
            _startCycle();
        }
        //        else if(CurrentCycle != null && CurrentCycle.isCycleComplete(this))
        //        {
        //            Logger.log("Script: onLoop: something went wrong");
        //            FailCount++;
        //            StopCurrentCycle();
        //        }

        isLooping.set(false);
        GameTicked.set(false);
        return OSRSUtilities.WaitTime(GetScriptIntensity());
    }

    public OSRSUtilities.ScriptIntenity GetScriptIntensity() {return OSRSUtilities.ScriptIntenity.Normal;}

    public void StopCurrentCycle()
    {
        Logger.log("Script: StopCurrentCycle: Stopping current cycle");
        if(CurrentCycle != null)
        {
            Logger.log("Script: StopCurrentCycle: Reseting cycle " + CurrentCycle);
            CurrentCycle.Reset(this);
            CurrentCycle = null;
        }
        else if(!CycleQueue.isEmpty())
        {
            Logger.log("Script: StopCurrentCycle: moving on to the next cycle");
            CycleQueue.poll();
        }
        else
        {
            Logger.log("Script: StopCurrentCycle: Cycle queue is empty, rotating cycle generators");
            Collections.rotate(Cycles, -1);
            _startCycle();
        }
    }

    public boolean StopTask(SimpleTask task)
    {
        Logger.log("Script: StopTask: Stopping task: " + task.GetTaskName());
        boolean result = task.StopTask(this);
        if(result)
        {
            removeNodes(task);
        }
        return result;
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

    public void StopTaskNow(SimpleTask task)
    {
        Logger.log("Script: StopTaskNow: Stopping task: " + task.GetTaskName());
        task.StopTaskNOW(this);
        removeNodes(task);
    }

    private void CleanUpCycle()
    {
        Tasks.clear();
        System.gc();
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
        }
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

    static Random GetRandom()
    {
        return rand;
    }
}
