package Scripts.AccountManagement;

import DataBase.PerformanceDatabase;
import Utilities.OSRSUtilities;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.AccountManager;
import org.dreambot.api.utilities.Logger;

import java.util.concurrent.TimeUnit;

@ScriptManifest(name = "PerformanceReport", description = "Upload current performance", author = "Semanresu", version = 1.0, category = Category.MISC, image = "")

public class PerformanceReport extends AbstractScript
{
    long startTime = 0;
    long wait      = 0;

    String Activity = "";


    @Override
    public void onStart(String... params)
    {
        if(params.length > 0)
        {
            Activity = params[0];
        }
        // wait a random amount of secs, so we don't send these reports all at once
        long seed = AccountManager.getAccountNickname().hashCode();
        OSRSUtilities.rand.setSeed(seed);
        startTime = System.nanoTime();
        wait      = OSRSUtilities.rand.nextLong(TimeUnit.MINUTES.toMillis(5)) * 1000;
    }

    @Override
    public void onStart()
    {
        // wait a random amount of secs, so we don't send these reports all at once
        long seed = AccountManager.getAccountNickname().hashCode();
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
        Logger.log(System.nanoTime()  + "-" +  startTime + "<" + wait);
        if(System.nanoTime() - startTime < wait)
        {
            Logger.log("Waiting to prevent concurrency, " + TimeUnit.NANOSECONDS.toSeconds(wait));
            return 10;
        }


        if(Bank.isCached())
        {
            var report = PerformanceDatabase.GeneratePerformanceReport(Activity);
            Logger.log(report);

            if(PerformanceDatabase.UploadPerformanceData(report))
            {
                this.stop();
            }
        }
        else
        {
            Bank.open();
        }


        return 0;
    }
}
