package Utilities.GrandExchange.Orders;

public class BuyOrder implements GEOrder
{
    int ID;
    int Count;
    int Price;

    BuyOrder(int id, int count, int price)
    {
        ID    = id;
        Count = count;
        Price = price;
    }

    /**
     * @return
     */
    @Override
    public int GetID()
    {
        return ID;
    }

    /**
     * @return
     */
    @Override
    public int GetPrice()
    {
        return Price;
    }

    /**
     * @return
     */
    @Override
    public int GetQuantity()
    {
        return Count;
    }

    /**
     * @return
     */
    @Override
    public OrderType GetType()
    {
        return OrderType.Buy;
    }
}
