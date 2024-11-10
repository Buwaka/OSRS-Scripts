package Utilities.GrandExchange.Orders;

import OSRSDatabase.OSRSPrices;
import Utilities.GrandExchange.GEInstance;
import Utilities.Patterns.SYMaths;
import Utilities.Scripting.ExternalLambdaUsage;

import java.io.Serial;

@ExternalLambdaUsage
public class MarketBuyOrder extends BaseOrder
{

    @Serial
    private static final long serialVersionUID = 642092741145280019L;

    public MarketBuyOrder(int id, int count)
    {
        super(id, count, TransactionType.Buy, OrderType.Market);
        PriceGenerator = () -> GetInstaBuyPrice(id);
    }

    @ExternalLambdaUsage
    public static int GetInstaBuyPrice(int id)
    {
        int price;
        int highprice = OSRSPrices.GetAverageHighPrice(id) * 2;
        int digits    = SYMaths.DigitCount(highprice);

        if(digits > 1)
        {
            double decimal     = (int) Math.pow(10, digits - 1);
            double significant = highprice / decimal;
            int    newPrice    = (int) (Math.ceil(significant) * decimal);
            if((double) (newPrice - highprice) / (double) highprice < 0.1)
            {
                price = Math.min(newPrice * 2, GEInstance.GetLiquidMoney());
            }
            else
            {
                price = newPrice;
            }
        }
        else
        {
            price = 100;
        }

        return price;
    }

}
