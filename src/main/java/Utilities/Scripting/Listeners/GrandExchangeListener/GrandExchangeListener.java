package Utilities.Scripting.Listeners.GrandExchangeListener;

import java.util.EventListener;

public interface GrandExchangeListener extends EventListener
{
    void onItemBought(GrandExchangeItemWrapper item);

    void onBuyOrder(GrandExchangeItemWrapper item);

    void onSellOrder(GrandExchangeItemWrapper item);

    void onItemSold(GrandExchangeItemWrapper item);
}
