package IF.Utilities.GrandExchange.Orders;

import IF.OSRSDatabase.OSRSPrices;
import IF.Utilities.Patterns.SYMaths;
import IF.Utilities.Scripting.ExternalLambdaUsage;
import IF.Utilities.Serializers.SerializableSupplier;

import java.io.Serial;

@ExternalLambdaUsage
public class LimitBuyOrder extends BaseOrder
{
    @Serial
    private static final long serialVersionUID = 4480625565946032108L;

    public LimitBuyOrder()
    {
        super(0,0,TransactionType.Buy, OrderType.Limit);
    }

    LimitBuyOrder(int id, int count, int price)
    {
        super(id, count, TransactionType.Buy, OrderType.Limit);
        PriceGenerator = () -> price;
    }

    LimitBuyOrder(int id, int count, SerializableSupplier<Integer> price)
    {
        super(id, count, TransactionType.Buy, OrderType.Limit);
        PriceGenerator = price;
    }

    public static LimitBuyOrder BuyAtAvgPrice(int id, int count)
    {
        return new LimitBuyOrder(id, count, () -> OSRSPrices.GetAveragePrice(id));
    }

    public static LimitBuyOrder BuyAtHighPrice(int id, int count)
    {
        return new LimitBuyOrder(id, count, () -> OSRSPrices.GetAverageHighPrice(id));
    }

    public static LimitBuyOrder BuyAtLowPrice(int id, int count)
    {
        return new LimitBuyOrder(id, count, () -> OSRSPrices.GetAverageLowPrice(id));
    }

    public static LimitBuyOrder BuyAtPercentage(int id, int count, float percentage)
    {
        return new LimitBuyOrder(id, count, () -> GetPercentageBuyPrice(id, percentage));
    }

    @ExternalLambdaUsage
    public static int GetPercentageBuyPrice(int id, float percentage)
    {
        int price = OSRSPrices.GetAveragePrice(id);
        return SYMaths.AddPercentage(price, percentage);
    }

    @Override
    public int GetPrice()
    {
        return PriceGenerator.get();
    }
}
