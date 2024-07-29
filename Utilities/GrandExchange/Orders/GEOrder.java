package Utilities.GrandExchange.Orders;

public interface GEOrder
{
    enum OrderType
    {
        Buy,
        Sell
    }

    int GetID();

    int GetPrice();

    int GetQuantity();

    OrderType GetType();
}
