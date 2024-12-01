package IF.Utilities.GrandExchange.Orders;

import IF.Utilities.Serializers.SerializableSupplier;

public interface GEOrder
{
    enum OrderType
    {
        Market,
        Limit
    }

    enum TransactionType
    {
        Buy,
        Sell
    }

    void AddQuantity(int quantity);

    int GetID();

    OrderType GetOrderType();

    int GetPrice();

    SerializableSupplier<Integer> GetPriceGenerator();

    int GetQuantity();

    int GetTotalPrice();

    TransactionType GetTransactionType();
}
