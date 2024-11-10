package Cycles.General;

import Utilities.Scripting.IFScript;
import Utilities.Scripting.SimpleCycle;
import org.dreambot.api.script.ScriptManager;

public class ExternalScriptCycle extends SimpleCycle
{
    // WILL RESTART THE SCRIPT, SO INSTANCE VALUES WILL BE LOST!!!

    private final String   scriptName;
    private final String   CurrentScriptName;
    private       String[] args = null;

    public ExternalScriptCycle(String name, String ScriptName, String... Args)
    {
        super(name, null);
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
    public boolean onStart(IFScript Script)
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


    //    private final ScriptManager manager;
    //    private final String scriptName;
    //    private final String[] params;
    //    private final Lock managerLock = new ReentrantLock();
    //    private final ScriptServer scriptServer;
    //
    //    public ScriptLaunch(String scriptName, String[] params, ScriptServer scriptServer) {
    //        this.manager = Instance.getInstance().getScriptManager();
    //        this.scriptName = scriptName;
    //        this.params = params;
    //        this.scriptServer = scriptServer;
    //    }
    //
    //    @Override
    //    public synchronized void run() {
    //        managerLock.lock();
    //        try {
    //
    //            ensureScriptStopped();
    //            while (true) {
    //                if (!manager.isRunning()) {
    //                    if (scriptName != null) {
    //                        log("Attempting to start script: " + scriptName + " with params: " + Arrays.toString(params));
    //                        manager.start(scriptName, params);
    //                        scriptServer.setLastScript(scriptName, params);
    //                    } else {
    //                        log("No script name was provided.");
    //                    }
    //                }
    //                try {
    //                    Thread.sleep(5000);
    //                } catch (InterruptedException e) {
    //                    Thread.currentThread().interrupt();
    //                    return;
    //                }
    //            }
    //        } finally {
    //            managerLock.unlock();
    //        }
    //    }
}
