package IF.Scripts;

import IF.JFrames.FrameUtilities;
import IF.Utilities.Encryption.EncryptedClassLoader;
import IF.Utilities.Scripting.DummyScript;
import IF.Utilities.Scripting.IScript;
import IF.Utilities.Scripting.Logger;
import org.checkerframework.common.reflection.qual.GetClass;
import org.dreambot.api.randoms.RandomManager;
import org.dreambot.api.randoms.RandomSolver;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManager;
import org.dreambot.api.script.ScriptManifest;

import java.awt.*;
import java.util.Arrays;

@ScriptManifest(name = "IFScript", category = Category.UTILITY, description = "IFScript master script", author = "Semanresu", version = 1.0)
public class ScriptTemplate extends AbstractScript
{
   private IScript ScriptInstance = new DummyScript();


    @Override
    public void onStart()
    {
        EncryptedClassLoader.Init(getClass().getClassLoader());
        FrameUtilities.Authenticate(() -> {
            FrameUtilities.ScriptSelector((select) -> {
                Logger.log("Creating Script");
                try
                {
                    ScriptInstance = select.getConstructor().newInstance();
                    ScriptManager.getScriptManager().addListener(ScriptInstance);
                    Logger.log(ScriptInstance);
                    ScriptInstance.onStart();
                }catch(Exception e)
                {
                    Logger.error("Failed to create script instance");
                }

//                script.whenCompleteAsync((result, throwa) -> {
//                    Logger.log("Loading complete: " + result + " " +
//                               Arrays.toString(throwa.getStackTrace()));
//                    if(result != null)
//                    {
//                        Logger.log("Loading success");
//                        ScriptInstance = result;
//                        ScriptInstance.onStart();
//                    }
//                });
            }, this::stop);

        }, this::stop);


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
