package IF.Utilities.Scripting;

import IF.Utilities.GrandExchange.GEInstance;
import IF.Utilities.OSRSUtilities;
import IF.Utilities.Patterns.Delegates.Delegate1;
import IF.Utilities.Patterns.Delegates.Delegate2;
import IF.Utilities.Patterns.Delegates.Delegate3;
import IF.Utilities.Patterns.GameTickDelegate;
import org.dreambot.api.data.GameState;
import org.dreambot.api.randoms.RandomSolver;
import org.dreambot.api.script.listener.*;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.Item;

import java.awt.*;
import java.util.List;

public interface IScript extends GameTickListener,
        GameStateListener,
        ItemContainerListener,
        PaintListener,
        HitSplatListener,
        SpawnListener,
        ExperienceListener
{

    enum ItemAction
    {
        Added,
        Removed,
        Changed,
        Swapped
    }

    void onStart();

    void onStart(String... params);

    boolean onSolverStart(RandomSolver solver);

    void onSolverEnd(RandomSolver solver);

    int onLoop();

    boolean onScheduledStop();

    void onExit();

    void onPause();

    void onResume();

    void onPaint(Graphics graphics);

    void onPaint(Graphics2D graphics);

    void stop();

    boolean isPaused();

    GEInstance GetGEInstance();

    void addNodes(ITask... node);

    void removeNodes(ITask... node);

    boolean IsActiveTaskLeft();

    OSRSUtilities.ScriptIntenity GetScriptIntensity();

    void removePersistentNodes(ITask... nodes);

    GameTickDelegate onGameTicked();

    List<ITask> getPersistentNodes();

    void StopTaskNow(ITask task);

    Delegate1<NPC> onNpcDespawn();

    void StopCurrentCycle();

    Delegate3<IScript.ItemAction, Item, org.dreambot.api.wrappers.items.Item> onInventory();

    GameState GetCurrentGameState();

    Delegate2<GameState, GameState> onGameStateChange();

    PlayerConfig GetConfig();


    //    double getVersion();

    //    ScriptManager.State getCurrentState(); // paused, running, solver, etc
    //    void setState(ScriptManager.State state);

    //    RandomManager getRandomManager();
    //
    //
    //    Thread getRandomThread();
}
