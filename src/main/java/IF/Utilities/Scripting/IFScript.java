package IF.Utilities.Scripting;

import IF.Logic.Tasks.AdvanceTasks.GraveStoneTask;
import IF.Logic.Tasks.AdvanceTasks.OpenBankTask;
import IF.Utilities.GrandExchange.GEInstance;
import IF.Utilities.OSRSUtilities;
import IF.Utilities.Patterns.Delegates.*;
import IF.Utilities.Patterns.GameTickDelegate;
import IF.Utilities.Scripting.Annotations.EncyptedInjection;
import IF.Utilities.Scripting.Annotations.Internal;
import IF.Utilities.Scripting.Listeners.GraveStoneListener;
import IF.randomhandler.RandomHandler;
import org.dreambot.api.Client;
import org.dreambot.api.ClientSettings;
import org.dreambot.api.data.GameState;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.randoms.RandomSolver;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.script.event.impl.ExperienceEvent;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.Entity;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.Item;

import java.awt.*;
import java.time.Duration;
import java.util.List;
import java.util.Queue;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import static org.dreambot.api.script.ScriptManager.getScriptManager;

@EncyptedInjection
public abstract class IFScript implements IScript//,ActionListener,AnimationListener, ChatListener,ExperienceListener,,ItemContainerListener,LoginListener, MenuRowListener,PaintListener,ProjectileListener,RegionLoadListener,
{
    private final Random                                                         rand                       = new Random();
    private final int                                                            CycleGenerationMaxAttempts = 10;
    private       int                                                            CycleGenerationAttempts    = 0;
    //TODO make this listen to everything and make it accessible through the script variable, perhaps make this calss include subcalsses that implement the listener, perhaps that may also trigger them
    // Simple Delegates for each listener
    // also don't forget to unsubscribe tasks to make sure all objects are cleaned up, perhaps automatically somehow
    private final List<ITask>                                               PersistentTasks            = new ArrayList<>();
    private final Lock                                                           PersistentTaskListLock     = new ReentrantLock();
    private final List<ITask>                                               Tasks                      = new ArrayList<>();
    private final Lock                                                           TaskListLock               = new ReentrantLock();
    private final List<Supplier<SimpleCycle[]>>                                  Cycles                     = new ArrayList<>();
    private final int                                                            CycleCounter               = 0;
    private final AtomicBoolean                                                  isLooping                  = new AtomicBoolean(
            false);
    private final AtomicBoolean                                                  isGameStateChanging        = new AtomicBoolean(
            false);
    private final AtomicBoolean                                                  GameTicked                 = new AtomicBoolean(
            false);
    private final AtomicBoolean                                                  isPaused                   = new AtomicBoolean(
            true);
    private       AtomicInteger                                                  PauseTime                  = new AtomicInteger(
            rand.nextInt(5000) + 5000); // is pause on the start
    private final long                                                           StopTaskTimeout            = 10000;
    private final PlayerConfig                                                   Config                     = new PlayerConfig();
    private       AtomicReference<ITask>                                    CurrentTask                = new AtomicReference<>(
            null);
    private       AtomicInteger                                                  FailLimit                  = new AtomicInteger(
            -1);
    private       int                                                            FailCount                  = 0;
    private       SimpleCycle                                                    CycleSetup                 = null;
    private       SimpleCycle                                                    CurrentCycle               = null;
    private       Queue<SimpleCycle>                                             CycleQueue                 = null;
    private       GameState                                                      LastGameState              = GameState.LOADING;
    private       OpenBankTask                                                   CacheBank                  = null;
    private       GEInstance                                                     GrandExchangeInstance      = null;
    //private              ProfitTracker      PTracker              = new ProfitTracker();
    private       boolean                                                        DebugPaint                 = true;
    private       Long                                                           BackupTimer                = null;
    final private long                                                           BackupDuration             = Duration.ofMinutes(
                                                                                                                              1)
                                                                                                                      .toNanos(); // minimum of 1 game tick, aka 0.6sec
    private       Delegate3<ItemAction, Item, Item>                              onInventory                = new Delegate3<>();
    private       GameTickDelegate                                               onGameTick                 = new GameTickDelegate();
    private       Delegate                                                       onTaskRemoved              = new Delegate();
    private       Delegate                                                       onTaskAdded                = new Delegate();
    private       Delegate                                                       onBankCached               = new Delegate();
    private       Delegate1<NPC>                                                 onNpcDespawn               = new Delegate1<>();
    private       Delegate1<NPC>                                                 onNpcSpawn                 = new Delegate1<>();
    // Entity, type, damage, id, special, gameCycle
    private       Delegate2<GameState/*last*/, GameState/*current*/>             onGameStateChange          = new Delegate2<>();
    private       Delegate6<Entity, Integer, Integer, Integer, Integer, Integer> onHitSplat                 = new Delegate6<>();
    private       Delegate1<ExperienceEvent>                                     onEXPGained                = new Delegate1<>();
    private       Delegate1<ExperienceEvent>                                     onLevelUp                  = new Delegate1<>();
    private       Delegate1<ExperienceEvent>                                     onLevelChange              = new Delegate1<>();
    private       GraveStoneListener                                             GraveListener              = new GraveStoneListener();
    private       AtomicBoolean                                                  isSolving                  = new AtomicBoolean(
            false);


    public IFScript(boolean Dummy)
    {

    }

    public IFScript()
    {
        Logger.log("Levels at init: " + Skills.getRealLevel(Skill.HITPOINTS));
        //        org.burningwave.core.assembler.StaticComponentContainer.Modules.exportAllToAll();
        //ScriptManager.getScriptManager().addListener(PTracker);
        getScriptManager().addListener(GraveListener);
        GraveListener.onDeath.Subscribe(this, this::onDeath);
//        CycleLibrary.init(this);
        if(Client.getGameState() != GameState.LOGGED_IN)
        {
            isGameStateChanging.set(true);
        }
        onInventory.Subscribe(this, (A, B, C) -> Logger.log(A.name() + B + C));
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
    public final void addNodes(ITask... nodes)
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

            node.Init(this);
            Logger.log("Script: addNodes: Adding task: " + node.GetTaskName());
            TaskListLock.lock();
            Tasks.add(node);
            TaskListLock.unlock();
            onTaskAdded.Fire();
        }
    }

    @Override
    public final void removeNodes(ITask... nodes)
    {
        if(nodes == null)
        {return;}

        for(var node : nodes)
        {
            if(node == null)
            {
                continue;
            }

            if(ITask.class.isAssignableFrom(node.getClass()))
            {
                ITask Task = (ITask) node;
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
                        "Script: removeNodes: Trying to remove TaskNode instead of ITask: " +
                        node.getClass().getName());
            }
        }
    }


    public ITask[] getNodes()
    {
        return Tasks.toArray(new ITask[0]);
    }

    @Internal
    public int onLoop()
    {
        if(isPaused.get())
        {
            GameTicked.set(true);
            if(PauseTime == null)
            {
                Logger.log("Script: onLoop: Pausing script indefinitely");
                return 1000;
            }
            Logger.log("Script: onLoop: Pausing script for " + PauseTime.get());
            isPaused.set(false);
            return PauseTime.get();
        }

        if(isGameStateChanging.get() || !GameTicked.get())
        {
            Logger.log("Script: onLoop: client is busy " + isGameStateChanging.get() +
                       !GameTicked.get());

            if(BackupTimer == null)
            {
                BackupTimer = System.nanoTime();
            }
            else if(System.nanoTime() - BackupTimer > BackupDuration)
            {
                Logger.log("Script: onLoop: Backup time elapsed, force enabling loop");
                GameTicked.set(true);
            }

            return 50;
        }
        BackupTimer = null;

        if(FailLimit.get() > 0)
        {
            if(FailCount > FailLimit.get())
            {
                Logger.error("Script: onLoop: Fail limit exceeded, exiting, " +
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
        else if(CurrentCycle != null)
        {
            Logger.log("Script: onLoop: ticking Cycle");
            CycleResult = CurrentCycle.Loop(this);

            Logger.log("Script: onLoop: CycleNullCheck: " + (CurrentCycle != null));
            Logger.log("Script: onLoop: CycleIsStarted Check: " + CurrentCycle.isStarted() +
                       " CycleResult: " + CycleResult + " CycleIsComplete: " +
                       CurrentCycle.isCycleComplete(this));

            if(CurrentCycle.isStarted() && (CycleResult <= 0 || CurrentCycle.isCycleComplete(this)))
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

    @Override
    public boolean onScheduledStop()
    {
        return false;
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

    public void PauseDelay(int ms)
    {
        PauseTime.set(ms);
    }

    public PlayerConfig GetConfig()
    {
        return Config;
    }

    public GameState GetCurrentGameState()
    {
        return LastGameState;
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

    public boolean StopTask(ITask task)
    {
        Logger.log("Script: StopTask: Stopping task: " + task.GetTaskName());
        boolean result = task.StopTask(this);
        if(result)
        {
            removeNodes(task);
        }
        return result;
    }

    public void StopTaskNow(ITask task)
    {
        Logger.log("Script: StopTaskNow: Stopping task: " + task.GetTaskName());
        task.StopTaskNOW(this);
        removeNodes(task);
    }

    public final void addPersistentNodes(TaskNode... nodes)
    {
        if(nodes == null)
        {return;}

        Logger.log("Adding persistent nodes: " + Arrays.toString(nodes));
        for(var node : nodes)
        {
            if(ITask.class.isAssignableFrom(node.getClass()))
            {
                ITask Task = (ITask) node;
                Task.Init(this);
                Logger.log("Adding Persistent task: " + Task.GetTaskName());
                PersistentTaskListLock.lock();
                PersistentTasks.add(Task);
                PersistentTaskListLock.unlock();
            }
            else
            {
                Logger.log("Trying to add TaskNode instead of ITask: " +
                           node.getClass().getName());
            }
        }
    }

    public void Resume()
    {
        isPaused.set(false);
    }

    public void Pause()
    {
        isPaused.set(true);
        PauseTime = null;
    }

    public void Pause(int ms)
    {
        isPaused.set(true);
        PauseTime = new AtomicInteger(ms);
    }

    public List<ITask> getPersistentNodes()
    {
        return PersistentTasks;
    }

    public List<ITask> getITasks()
    {
        return Tasks;
    }

    @Override
    public final void removePersistentNodes(ITask... nodes)
    {
        if(nodes == null)
        {return;}

        for(var node : nodes)
        {
            if(ITask.class.isAssignableFrom(node.getClass()))
            {
                ITask Task = (ITask) node;
                PersistentTaskListLock.lock();
                Logger.log("Removing persistent task: " + Task.GetTaskName());
                PersistentTasks.remove(Task);
                PersistentTaskListLock.unlock();
                Logger.log(PersistentTasks.size() + " Tasks left");
            }
            else
            {
                Logger.log("Trying to remove TaskNode instead of ITask: " +
                           node.getClass().getName());
            }
        }
    }

    public void setDebugPaint(boolean debugPaint)
    {
        DebugPaint = debugPaint;
    }

    private void CleanUpCycle()
    {
        Tasks.clear();
        System.gc();
    }

    @Internal
    public void onStart(String... params)
    {
        onStart();
    }

    @Internal
    public void onStart()
    {
        ClientSettings.toggleLevelUpInterface(false);

        getScriptManager().addListener(Config);
        Config.onAccountChanged.Subscribe(this, this::onAccountChange);

        RandomHandler.loadRandoms();

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

        if(!Cycles.isEmpty() && !isGameStateChanging.get())
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
                Logger.log(
                        "Script: HandleStartUpTasks: Startup tasks are not active, rejecting cycle, " +
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

    public boolean IsActiveTaskLeft()
    {
        var tasks = GetSortedTasks();
        Logger.log("Script: IsActiveTaskLeft: " + tasks.size());
        return !tasks.isEmpty();
    }

    List<ITask> GetSortedTasks()
    {
        return Tasks.stream()
                    .sorted((a, b) -> a.priority() - b.priority())
                    .filter(t -> !t.isPaused() && t.isActive() && t.accept())
                    .toList();
    }

    private void GenerateNewCycles()
    {
        if(CycleGenerationAttempts > CycleGenerationMaxAttempts)
        {
            Logger.warn(
                    "Script: GenerateNewCycles: Failed too many times trying to generate new cycles, Goal has been reached or an error occurred, exiting");
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
                Logger.log("Script: GenerateNewCycles: Trying to start newly generated cycle " +
                           cycle);
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

    public void stop()
    {
        // TODO
    }

    @Override
    public boolean isPaused()
    {
        return isPaused.get();
    }


    public boolean onSolverStart(RandomSolver solver)
    {
        // to prevent the next loop
        isSolving.set(true);
        if(isLooping.get())
        {
            Logger.log("LoopLock is locked, preventing solver from starting");
            return false;
        }

        Logger.log("Locking Looplock, starting solver");
        return true;
    }


    public void onSolverEnd(RandomSolver solver)
    {
        Logger.log("Completing Solver Condition");
        isSolving.set(false);
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
                           next);
                addNodes(next.GenerateStartupTasks());
                CycleSetup = next;
                return true;
            }

            if(next.Start(this))
            {
                Logger.log("Script: _startCycle: Start Cycle, " + next);
                CurrentCycle = next;
            }
            return true;
        }
        return false;
    }

    @Internal
    public void onExit()
    {
        if(CurrentCycle != null)
        {
            CurrentCycle.EndNow(this);
        }
    }

    @Override
    public void onPause()
    {

    }

    @Override
    public void onResume()
    {

    }

    @Internal
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
    }

    @Internal
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
    }

    @Internal
    @Override
    public void onGameStateChange(GameState gameState)
    {
        Logger.log(gameState.name());
        onGameStateChange.Fire(LastGameState, gameState);
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
        LastGameState = gameState;
    }

    @Internal
    @Override
    public void onGameTick()
    {
        onGameTick.Fire();
        if(!isLooping.get())
        {
            GameTicked.set(true);
        }
    }

    @Internal
    @Override
    public void onGained(ExperienceEvent event)
    {
        onEXPGained.Fire(event);
        if(CurrentCycle != null)
        {
            CurrentCycle.onEXPGained.Fire(CurrentCycle, event);
        }
    }

    @Internal
    @Override
    public void onLevelUp(ExperienceEvent event)
    {
        onLevelUp.Fire(event);
        if(CurrentCycle != null)
        {
            CurrentCycle.onLevelUp.Fire(CurrentCycle, event);
        }
    }

    @Internal
    @Override
    public void onLevelChange(ExperienceEvent event)
    {
        onLevelChange.Fire(event);
        if(CurrentCycle != null)
        {
            CurrentCycle.onLevelChange.Fire(CurrentCycle, event);
        }
    }

    @Internal
    @Override
    public void onNpcSpawn(NPC npc)
    {
        onNpcSpawn.Fire(npc);
        if(CurrentCycle != null)
        {
            CurrentCycle.onNpcSpawn.Fire(CurrentCycle, npc);
        }
    }

    @Internal
    @Override
    public void onNpcDespawn(NPC npc)
    {
        onNpcDespawn.Fire(npc);
        if(CurrentCycle != null)
        {
            CurrentCycle.onNpcDespawn.Fire(CurrentCycle, npc);
        }
    }

    @Internal
    @Override
    public void onHitSplatAdded(Entity entity, int type, int damage, int id, int special, int gameCycle)
    {
        Logger.info("Script: Hitsplat: " + entity + ", " + type + ", " + damage + ", " + special +
                    ", " + gameCycle + ", " + entity.hashCode());
        onHitSplat.Fire(entity, type, damage, id, special, gameCycle);
        if(CurrentCycle != null)
        {
            CurrentCycle.onHitSplat.Fire(CurrentCycle,
                                         entity,
                                         type,
                                         damage,
                                         id,
                                         special,
                                         gameCycle);
        }
    }

    @Internal
    @Override
    public void onInventoryItemChanged(Item incoming, Item existing)
    {
        onInventory.Fire(ItemAction.Changed, incoming, existing);
        Logger.log("onInventoryItemChanged");
        if(CurrentCycle != null)
        {
            CurrentCycle.onInventory.Fire(CurrentCycle, ItemAction.Changed, incoming, existing);
        }
    }

    @Internal
    @Override
    public void onInventoryItemAdded(Item item)
    {
        onInventory.Fire(ItemAction.Added, item, null);
        Logger.log("onInventoryItemAdded");
        if(CurrentCycle != null)
        {
            CurrentCycle.onInventory.Fire(CurrentCycle, ItemAction.Added, item, null);
        }
    }

    @Internal
    @Override
    public void onInventoryItemRemoved(Item item)
    {
        onInventory.Fire(ItemAction.Removed, item, null);
        Logger.log("onInventoryItemRemoved");
        if(CurrentCycle != null)
        {
            CurrentCycle.onInventory.Fire(CurrentCycle, ItemAction.Removed, item, null);
        }
    }

    @Internal
    @Override
    public void onInventoryItemSwapped(Item incoming, Item outgoing)
    {
        onInventory.Fire(ItemAction.Swapped, incoming, outgoing);
        Logger.log("onInventoryItemSwapped");
        if(CurrentCycle != null)
        {
            CurrentCycle.onInventory.Fire(CurrentCycle, ItemAction.Swapped, incoming, outgoing);
        }
    }

    public Delegate3<ItemAction, Item, Item> onInventory()
    {
        return onInventory;
    }

    public Delegate onTaskRemoved()
    {
        return onTaskRemoved;
    }

    public Delegate onTaskAdded()
    {
        return onTaskAdded;
    }

    public Delegate onBankCached()
    {
        return onBankCached;
    }

    public Delegate1<NPC> onNpcDespawn()
    {
        return onNpcDespawn;
    }

    public Delegate1<NPC> onNpcSpawn()
    {
        return onNpcSpawn;
    }

    public Delegate2<GameState, GameState> onGameStateChange()
    {
        return onGameStateChange;
    }

    public Delegate6<Entity, Integer, Integer, Integer, Integer, Integer> onHitSplat()
    {
        return onHitSplat;
    }

    public Delegate1<ExperienceEvent> onEXPGained()
    {
        return onEXPGained;
    }

    public Delegate1<ExperienceEvent> onLevelUp()
    {
        return onLevelUp;
    }

    public Delegate1<ExperienceEvent> onLevelChange()
    {
        return onLevelChange;
    }

    public GameTickDelegate onGameTicked()
    {
        return onGameTick;
    }

    public GraveStoneListener GraveListener()
    {
        return GraveListener;
    }

   public Random GetRandom()
    {
        return rand;
    }
}
