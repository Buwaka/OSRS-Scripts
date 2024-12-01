package IF.Scripts;

import IF.JFrames.FrameUtilities;
import IF.Scripts.CommissionScripts.FullDragon.FDMeleeTrainer;
import IF.Utilities.Encryption.EncryptedClassLoader;
import IF.Utilities.Scripting.DummyScript;
import IF.Utilities.Scripting.IScript;
import IF.Utilities.Scripting.Logger;
import org.dreambot.api.randoms.RandomManager;
import org.dreambot.api.randoms.RandomSolver;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManager;
import org.dreambot.api.script.ScriptManifest;

import java.awt.*;

@ScriptManifest(name = "DevScript", category = Category.UTILITY, description = "IFScript Dev script", author = "Semanresu", version = 1.0)
public class DevScript extends AbstractScript
{
   private IScript ScriptInstance = new FDMeleeTrainer();


    @Override
    public void onStart()
    {
        ScriptInstance.onStart();
    }

    @Override
    public void onStart(String... params)
    {
        ScriptInstance.onStart(params);
    }

    @Override
    public boolean onSolverStart(RandomSolver solver)
    {
        return ScriptInstance.onSolverStart(solver);
    }

    @Override
    public void onSolverEnd(RandomSolver solver)
    {
        ScriptInstance.onSolverEnd(solver);
    }

    @Override
    public int onLoop()
    {
//        Logger.log("Loop");
        return ScriptInstance.onLoop();
    }

    @Override
    public boolean onScheduledStop()
    {
        return ScriptInstance.onScheduledStop();
    }

    @Override
    public void onExit()
    {
        ScriptInstance.onExit();
    }

    @Override
    public void onPause()
    {
        ScriptInstance.onPause();
    }

    @Override
    public void onResume()
    {
        ScriptInstance.onResume();
    }

    @Override
    public void onPaint(Graphics graphics)
    {
        ScriptInstance.onPaint(graphics);
    }

    @Override
    public void onPaint(Graphics2D graphics)
    {
        ScriptInstance.onPaint(graphics);
    }

    @Override
    public void stop()
    {
        ScriptInstance.stop();
    }

    @Override
    public boolean isPaused()
    {
        return ScriptInstance.isPaused();
    }

    @Override
    public void setState(ScriptManager.State state)
    {
        super.setState(state);
    }

    @Override
    public double getVersion()
    {
        return super.getVersion();
    }

    @Override
    public ScriptManager.State getCurrentState()
    {
        return super.getCurrentState();
    }

    @Override
    public RandomManager getRandomManager()
    {
        return super.getRandomManager();
    }

    @Override
    public Thread getRandomThread()
    {
        return super.getRandomThread();
    }

}
