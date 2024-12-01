package IF.Utilities.Scripting;

import IF.Utilities.GrandExchange.GEInstance;
import IF.Utilities.OSRSUtilities;
import IF.Utilities.Patterns.Delegates.Delegate1;
import IF.Utilities.Patterns.Delegates.Delegate2;
import IF.Utilities.Patterns.Delegates.Delegate3;
import IF.Utilities.Patterns.GameTickDelegate;
import IF.Utilities.Scripting.Annotations.DontEncrypt;
import org.dreambot.api.data.GameState;
import org.dreambot.api.randoms.RandomSolver;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.Item;

import java.awt.*;
import java.util.List;

@DontEncrypt
public class DummyScript implements IScript
{

    @Override
    public void onGameTick()
    {

    }

    @Override
    public void onGameStateChange(GameState gameState)
    {

    }

    @Override
    public void onStart()
    {

    }

    @Override
    public void onStart(String... params)
    {

    }

    @Override
    public boolean onSolverStart(RandomSolver solver)
    {
        return false;
    }

    @Override
    public void onSolverEnd(RandomSolver solver)
    {

    }

    @Override
    public int onLoop()
    {
        return 0;
    }

    @Override
    public boolean onScheduledStop()
    {
        return false;
    }

    @Override
    public void onExit()
    {

    }

    @Override
    public void onPause()
    {

    }

    @Override
    public void onResume()
    {

    }

    @Override
    public void onPaint(Graphics graphics)
    {

    }

    @Override
    public void onPaint(Graphics2D graphics)
    {

    }

    @Override
    public void stop()
    {

    }

    @Override
    public boolean isPaused()
    {
        return false;
    }

    @Override
    public GEInstance GetGEInstance()
    {
        return null;
    }

    @Override
    public void addNodes(ITask... node)
    {

    }

    @Override
    public void removeNodes(ITask... node)
    {

    }

    @Override
    public boolean IsActiveTaskLeft()
    {
        return false;
    }

    @Override
    public OSRSUtilities.ScriptIntenity GetScriptIntensity()
    {
        return null;
    }

    @Override
    public void removePersistentNodes(ITask... nodes)
    {

    }

    @Override
    public GameTickDelegate onGameTicked()
    {
        return null;
    }

    @Override
    public List<ITask> getPersistentNodes()
    {
        return null;
    }

    @Override
    public void StopTaskNow(ITask task)
    {

    }

    @Override
    public Delegate1<NPC> onNpcDespawn()
    {
        return null;
    }

    @Override
    public void StopCurrentCycle()
    {

    }

    @Override
    public Delegate3<ItemAction, Item, Item> onInventory()
    {
        return null;
    }

    @Override
    public GameState GetCurrentGameState()
    {
        return null;
    }

    @Override
    public Delegate2<GameState, GameState> onGameStateChange()
    {
        return null;
    }

    @Override
    public PlayerConfig GetConfig()
    {
        return null;
    }
}
