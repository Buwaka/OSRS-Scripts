package Scripts.Private;

import org.dreambot.api.methods.settings.Varcs;
import org.dreambot.api.methods.settings.vars.VarBit;
import org.dreambot.api.script.AbstractScript;

public class TutorialIsland extends AbstractScript
{
    final static int TutProgressVarBit = 281;
    /**
     * @return
     */
    @Override
    public int onLoop()
    {
        int TutProgress = Varcs.getInt(TutProgressVarBit);
        switch(TutProgress)
        {
            case 1: // Name and Outfit
        }

        return 0;
    }
}
