package Utilities.Scripting.Listeners.GrandExchangeListener;

import org.dreambot.api.methods.grandexchange.GrandExchangeItem;
import org.dreambot.api.methods.grandexchange.Status;

public final class GrandExchangeItemWrapper
{

    private final int    id;
    private final int    slot;
    private final int    amount;
    private final int    price;
    private final Status status;
    private final String name;

    public GrandExchangeItemWrapper(GrandExchangeItem item)
    {
        this.id     = item.getID();
        this.slot   = item.getSlot();
        this.amount = item.getAmount();
        this.price  = item.getPrice();
        this.name   = item.getName();
        this.status = item.getStatus() == null ? Status.EMPTY : item.getStatus();
    }

    public int getAmount()
    {
        return amount;
    }

    public int getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public int getPrice()
    {
        return price;
    }

    public int getSlot()
    {
        return slot;
    }

    public Status getStatus()
    {
        return status;
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o) {return true;}
        if(o == null || getClass() != o.getClass()) {return false;}

        GrandExchangeItemWrapper that = (GrandExchangeItemWrapper) o;

        if(that.status == null || this.status == null) {return false;}
        if(!name.equals(that.name)) {return false;}
        if(id != that.id) {return false;}
        if(slot != that.slot) {return false;}
        if(amount != that.amount) {return false;}
        if(price != that.price) {return false;}

        return status.equals(that.status);
    }

    @Override
    public String toString()
    {
        return "ID " + id + ", name: " + name + ", status: " + status.name() + ", slot: " + slot +
               ", amount: " + amount + ", price: " + price;
    }
}
