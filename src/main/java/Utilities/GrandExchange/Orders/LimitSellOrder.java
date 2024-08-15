package Utilities.GrandExchange.Orders;

import OSRSDatabase.OSRSPrices;
import Utilities.Patterns.SYMaths;
import Utilities.Scripting.ExternalLambdaUsage;
import Utilities.Serializers.SerializableSupplier;

import java.io.Serial;
import java.util.function.Supplier;

public class LimitSellOrder extends BaseOrder
{
    @Serial
    private static final long serialVersionUID = -1748286596079852740L;

    LimitSellOrder(int id, int count, int price)
    {
        super(id, count, TransactionType.Sell, OrderType.Limit);
        PriceGenerator = () -> price;
    }

    LimitSellOrder(int id, int count, SerializableSupplier<Integer> price)
    {
        super(id, count, TransactionType.Sell, OrderType.Limit);
        PriceGenerator = price;
    }

    public static LimitSellOrder SellAtAvgPrice(int id, int count)
    {
        return new LimitSellOrder(id, count, () -> OSRSPrices.GetAveragePrice(id));
    }

    public static LimitSellOrder SellAtHighPrice(int id, int count)
    {
        return new LimitSellOrder(id, count, () -> OSRSPrices.GetAverageHighPrice(id));
    }

    public static LimitSellOrder SellAtLowPrice(int id, int count)
    {
        return new LimitSellOrder(id, count, () -> OSRSPrices.GetAverageLowPrice(id));
    }

    public static LimitSellOrder SellAtPercentage(int id, int count, float percentage)
    {
        return new LimitSellOrder(id, count, () -> GetPercentageSellPrice(id, percentage));
    }

    @ExternalLambdaUsage
    public static int GetPercentageSellPrice(int id, float percentage)
    {
        int price = OSRSPrices.GetAveragePrice(id);
        return SYMaths.AddPercentage(price, percentage);
    }
}
