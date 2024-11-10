package Scripts.AccountManagement;

import DataBase.PerformanceDatabase;
import Utilities.OSRSUtilities;
import Utilities.Scripting.Logger;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.magic.Magic;
import org.dreambot.api.methods.magic.Normal;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.listener.ActionListener;
import org.dreambot.api.utilities.AccountManager;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.widgets.MenuRow;

import java.util.concurrent.TimeUnit;

@ScriptManifest(name = "PerformanceReport", description = "Upload current performance", author = "Semanresu", version = 1.0, category = Category.MISC, image = "")

public class PerformanceReport extends AbstractScript implements ActionListener
{
    private final int MaxUploadAttempts = 10;
    long startTime = 0;
    long wait      = 0;
    String Activity = "";
    long   LastAction;
    private       int UploadAttempt     = 0;

    @Override
    public void onAction(MenuRow eventRow, int mouseX, int mouseY)
    {
        LastAction = System.nanoTime();
    }

    @Override
    public void onStart()
    {
        // wait a random amount of secs, so we don't send these reports all at once
        long seed = AccountManager.getAccountNickname().hashCode();
        LastAction = System.nanoTime();
        OSRSUtilities.rand.setSeed(seed);
        startTime = System.nanoTime();
        wait      = OSRSUtilities.rand.nextLong(TimeUnit.MINUTES.toMillis(5)) * 1000;
    }

    @Override
    public void onStart(String... params)
    {
        if(params.length > 0)
        {
            Activity = params[0];
        }
        // wait a random amount of secs, so we don't send these reports all at once
        long seed = AccountManager.getAccountNickname().hashCode();
        LastAction = System.nanoTime();
        OSRSUtilities.rand.setSeed(seed);
        startTime = System.nanoTime();
        wait      = OSRSUtilities.rand.nextLong(TimeUnit.MINUTES.toMillis(5)) * 1000;
    }

    /**
     * @return
     */
    @Override
    public int onLoop()
    {
        Logger.log(System.nanoTime() + "-" + startTime + "<" + wait);
        if(System.nanoTime() - startTime < wait)
        {
            Logger.log("Waiting to prevent concurrency, " + TimeUnit.NANOSECONDS.toSeconds(wait));
            return 10;
        }

        if(System.nanoTime() - LastAction > TimeUnit.MINUTES.toNanos(3))
        {
            Magic.castSpell(Normal.HOME_TELEPORT);
            Sleep.sleepTicks(25);
            return 10;
        }

        if(System.nanoTime() - LastAction > TimeUnit.MINUTES.toNanos(31))
        {
            Logger.log("Player is stuck, cannot reach bank");
            //TODO minimal report with only available info
            this.stop();
        }

        if(UploadAttempt > MaxUploadAttempts)
        {
            Logger.log("Failed to generate and/or upload performance report too many times, exiting");
            this.stop();
        }


        if(Bank.isCached())
        {
            var report = PerformanceDatabase.GeneratePerformanceReport(Activity);
            Logger.log(report);

            if(PerformanceDatabase.UploadPerformanceData(report))
            {
                this.stop();
            }
            else
            {
                UploadAttempt++;
            }
        }
        else
        {
            Bank.open();
        }


        return OSRSUtilities.WaitTime(OSRSUtilities.ScriptIntenity.Lax);
    }
}
