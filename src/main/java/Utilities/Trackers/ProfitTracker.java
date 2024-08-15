package Utilities.Trackers;

import Utilities.Scripting.Listeners.GrandExchangeListener.GrandExchangeItemWrapper;
import Utilities.Scripting.Listeners.GrandExchangeListener.GrandExchangeListener;
import Utilities.Scripting.Listeners.InventoryListener;
import org.dreambot.api.script.listener.ItemContainerListener;
import org.dreambot.api.script.listener.PaintListener;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.items.Item;

import java.awt.*;

public class ProfitTracker implements PaintListener, ItemContainerListener, GrandExchangeListener
{


    /**
     * @param graphics
     */
    @Override
    public void onPaint(Graphics graphics)
    {
        PaintListener.super.onPaint(graphics);
    }

    /**
     * @param graphics
     */
    @Override
    public void onPaint(Graphics2D graphics)
    {
        PaintListener.super.onPaint(graphics);
    }


    /**
     * @param incoming
     * @param existing
     */
    @Override
    public void onInventoryItemChanged(Item incoming, Item existing)
    {
        ItemContainerListener.super.onInventoryItemChanged(incoming, existing);
    }

    /**
     * @param item
     */
    @Override
    public void onInventoryItemAdded(Item item)
    {
        ItemContainerListener.super.onInventoryItemAdded(item);
    }

    /**
     * @param item
     */
    @Override
    public void onInventoryItemRemoved(Item item)
    {
        ItemContainerListener.super.onInventoryItemRemoved(item);
    }

    /**
     * @param incoming
     * @param outgoing
     */
    @Override
    public void onInventoryItemSwapped(Item incoming, Item outgoing)
    {
        ItemContainerListener.super.onInventoryItemSwapped(incoming, outgoing);
    }

    /**
     * @param incoming
     * @param existing
     */
    @Override
    public void onBankItemChanged(Item incoming, Item existing)
    {
        ItemContainerListener.super.onBankItemChanged(incoming, existing);
    }

    /**
     * @param item
     */
    @Override
    public void onBankItemAdded(Item item)
    {
        ItemContainerListener.super.onBankItemAdded(item);
    }

    /**
     * @param item
     */
    @Override
    public void onBankItemRemoved(Item item)
    {
        ItemContainerListener.super.onBankItemRemoved(item);
    }

    /**
     * @param incoming
     * @param outgoing
     */
    @Override
    public void onBankItemSwapped(Item incoming, Item outgoing)
    {
        ItemContainerListener.super.onBankItemSwapped(incoming, outgoing);
    }

    /**
     * @param item
     */
    @Override
    public void onItemBought(GrandExchangeItemWrapper item)
    {
        Logger.log("GE, onItemBought " + item);
    }

    /**
     * @param item
     */
    @Override
    public void onBuyOrder(GrandExchangeItemWrapper item)
    {
        Logger.log("GE, onBuyOrder " + item);
    }

    /**
     * @param item
     */
    @Override
    public void onSellOrder(GrandExchangeItemWrapper item)
    {
        Logger.log("GE, onSellOrder " + item);
    }

    /**
     * @param item
     */
    @Override
    public void onItemSold(GrandExchangeItemWrapper item)
    {
        Logger.log("GE, onItemSold " + item);
    }

    // basically when we get an item, get prices, add to unrealized profit
    // remove profit when when item is removed
    // expenditures on GE purchases or removal of money (taht isn't the bank)
    // when we sell on the GE, we turn the unrealized profit into real profit

}
