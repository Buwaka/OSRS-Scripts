package Scripts.AccountManagement;

import DataBase.PerformanceDatabase;
import OSRSDatabase.OSRSPrices;
import Utilities.GrandExchange.GEInstance;
import Utilities.Patterns.Playtime;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.quest.Quests;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.AccountManager;
import org.dreambot.api.utilities.Logger;

@ScriptManifest(name = "PerformanceReport", description = "Upload current performance", author = "Semanresu", version = 1.0, category = Category.MISC, image = "")

public class PerformanceReport extends AbstractScript
{


    /**
     * @return
     */
    @Override
    public int onLoop()
    {
        if(Bank.isCached())
        {
            var data = new PerformanceDatabase.PerformanceData(AccountManager.getAccountUsername(),
                                                               Playtime.GetPlaytimeLong(),
                                                               Playtime.GetPlaytimeLong(),
                                                               GEInstance.GetLiquidMoney(),
                                                               OSRSPrices.GetNetWorth(),
                                                               0,
                                                               null,
                                                               Quests.getQuestPoints(),
                                                               0);

            PerformanceDatabase.UploadPerformanceData(data);

            Logger.log(data);
            this.stop();
        }
        else
        {
            Bank.open();
        }


        return 0;
    }
}
