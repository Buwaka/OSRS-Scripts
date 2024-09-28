package Utilities.GrandExchange.Orders;

import Utilities.Scripting.ExternalLambdaUsage;
import org.dreambot.api.methods.container.impl.bank.Bank;

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

    public MarketSellOrder(int id)
    {
        super(id, -1, TransactionType.Sell, OrderType.Market);
        PriceGenerator = () -> GetInstaSellPrice(id);
    }

    /**
     * @return
     */
    @Override
    public int GetQuantity()
    {
        if(super.GetQuantity() == -1)
        {
            return Bank.count(GetID());
        }
        return super.GetQuantity();
    }

}
