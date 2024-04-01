package Utilities;

import org.dreambot.api.script.AbstractScript;

public abstract class tpircSScript extends AbstractScript
{
    Thread Ranomizer;

    @Override
    public void onExit()
    {
        super.onExit();
        Ranomizer.interrupt();
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Ranomizer = OSRSUtilities.StartRandomizerThread();
    }


}
