package Utilities.GrandExchange;

import OSRSDatabase.OSRSPrices;
import Utilities.GrandExchange.Orders.GEOrder;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.utilities.Sleep;

import java.util.ArrayList;
import java.util.List;

public class GEInstance
{
    List<GEOrder> Actions = new ArrayList<>();

    public static boolean MarketOrderBuy(int ID, int count)
    {
        boolean result = Sleep.sleepUntil(GrandExchange::open, 20000);
        if(result)
        {
            int high = OSRSPrices.GetAverageHighPrice(ID);
            if(high == -1)
            {
                return false;
            }
            result = GrandExchange.buyItem(ID, count, high);
            if(result && Sleep.sleepUntil(GrandExchange::isReadyToCollect, 20000))
            {
                return GrandExchange.collectToBank();
            }
        }

        return result;
    }

    public static boolean MarketOrderSell(int ID, int count)
    {
        boolean result = Sleep.sleepUntil(GrandExchange::open, 20000);
        if(result)
        {
            int low = OSRSPrices.GetAverageLowPrice(ID);
            if(low == -1)
            {
                return false;
            }
            result = GrandExchange.sellItem(ID, count, low);
            if(result && Sleep.sleepUntil(GrandExchange::isReadyToCollect, 20000))
            {
                return GrandExchange.collectToBank();
            }
        }

        return result;
    }

}
