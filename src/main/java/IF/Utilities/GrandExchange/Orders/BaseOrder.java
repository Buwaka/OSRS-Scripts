package IF.Utilities.GrandExchange.Orders;

import IF.Utilities.Serializers.SerializableSupplier;
import org.json.JSONPropertyName;

import javax.annotation.Nonnull;
import java.io.Serial;
import java.io.Serializable;

public class BaseOrder implements GEOrder, Comparable<BaseOrder>, Serializable
{

    @Serial
    private static final long                          serialVersionUID = 3343306652600915000L;
    private final        int                           ID; // item ID
    private final        TransactionType               TType;
    private final        OrderType                     OType;
    public               SerializableSupplier<Integer> PriceGenerator;
    private              int                           Count;


    BaseOrder(int id, int count, TransactionType ttype, OrderType otype)
    {
        ID    = id;
        Count = count;
        TType = ttype;
        OType = otype;
    }

    public <T extends GEOrder> BaseOrder(T other)
    {
        ID             = other.GetID();
        Count          = other.GetQuantity();
        TType          = other.GetTransactionType();
        OType          = other.GetOrderType();
        PriceGenerator = other.GetPriceGenerator();
    }

    @Override
    public int compareTo(@Nonnull BaseOrder o)
    {
        return this.GetOrderType().ordinal() - o.GetOrderType().ordinal();
    }

    /**
     * @param quantity
     */
    @Override
    public void AddQuantity(int quantity)
    {
        Count += quantity;
    }

    /**
     * @return
     */
    @JSONPropertyName("ID")
    @Override
    public int GetID()
    {
        return ID;
    }

    /**
     * @return
     */
    @JSONPropertyName("OType")
    @Override
    public final OrderType GetOrderType()
    {
        return OType;
    }

    /**
     * @return
     */
    @Override
    public int GetPrice()
    {
        return PriceGenerator.get();
    }

    /**
     * @return
     */
    @Override
    public SerializableSupplier<Integer> GetPriceGenerator()
    {
        return PriceGenerator;
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
    public int GetTotalPrice()
    {
        return GetPrice() * Count;
    }

    /**
     * @return
     */
    @JSONPropertyName("TType")
    @Override
    public final TransactionType GetTransactionType()
    {
        return TType;
    }

    public String toString()
    {
        return "ID: " + ID + "\nCount: " + Count + "\nTType: " + TType + "\nOType: " + OType;
    }
}
