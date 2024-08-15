package Utilities.GrandExchange.Orders;

import Utilities.Scripting.ExternalLambdaUsage;

import java.io.Serial;

public class MarketSellOrder extends BaseOrder
{
    @Serial
    private static final long serialVersionUID = 181712629328118089L;

    public MarketSellOrder(int id, int count)
    {
        super(id, count, TransactionType.Sell, OrderType.Market);
        PriceGenerator = () -> GetInstaSellPrice(id);
    }

    @ExternalLambdaUsage
    public static int GetInstaSellPrice(int id)
    {
        return 1;
    }

}
