package Utilities;

import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.ScriptManager;
import org.dreambot.api.script.listener.GameTickListener;

public class ScriptStarter implements Runnable, GameTickListener
{

    public Class  ClassClass  = null;
    public String ClassString = "";

    public ScriptStarter(Class<? extends AbstractScript> className)
    {
        ClassClass = className;
    }

    public ScriptStarter(String className)
    {
        ClassString = className;
    }

    @Override
    public void run()
    {

        ScriptManager manager = ScriptManager.getScriptManager();
        try
        {
            Thread.sleep(1000);
        } catch(InterruptedException e)
        {
            throw new RuntimeException(e);
        }
        if(ClassClass != null)
        {
            //manager.start(ClassClass);
        }
        else
        {
            //manager.start(ClassString, null);
        }
    }

    @Override
    public void onGameTick()
    {

    }
}
