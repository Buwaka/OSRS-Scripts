package Cycles.General;

import Utilities.Scripting.SimpleCycle;
import Utilities.Scripting.tpircSScript;
import org.dreambot.api.script.ScriptManager;

public class ExternalScriptCycle extends SimpleCycle
{
    // WILL RESTART THE SCRIPT, SO INSTANCE VALUES WILL BE LOST!!!

    private String   scriptName;
    private String[] args = null;
    private String   CurrentScriptName;

    public ExternalScriptCycle(String name, String ScriptName, String... Args)
    {
        super(name);
        scriptName        = ScriptName;
        args              = Args;
        CurrentScriptName = ScriptManager.getScriptManager().getCurrentScript().getSDNName();
    }


    /**
     * @param Script
     *
     * @return if cycle has successfully started
     */
    @Override
    public boolean onStart(tpircSScript Script)
    {
        ScriptManager Manager = ScriptManager.getScriptManager();

        Manager.stop();

        Thread newScript = new Thread(() -> {
            ScriptManager.getScriptManager().start(scriptName, args);
        });

        Thread newScriptListener = new Thread(() -> {
            while(newScript.isAlive())
            {
                try
                {
                    Thread.sleep(1000);
                } catch(InterruptedException e)
                {
                    break;
                }
            }

            //ScriptManager.getScriptManager().start(CurrentScriptName, null);
        });

        Script.stop();

        return true;
    }
}
