package IF.Utilities.Scripting.Listeners.GrandExchangeListener;

import IF.Utilities.Patterns.Delegates.Delegate1;
import org.dreambot.api.Client;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.grandexchange.GrandExchangeItem;
import org.dreambot.api.methods.grandexchange.Status;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.script.listener.ChatListener;
import org.dreambot.api.script.listener.ItemContainerListener;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.wrappers.widgets.message.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class GrandExchangeListener implements ChatListener, ItemContainerListener
{
    public Delegate1<GrandExchangeItemWrapper> onItemBought = new Delegate1<>();
    public Delegate1<GrandExchangeItemWrapper> onBuyOrder   = new Delegate1<>();
    public Delegate1<GrandExchangeItemWrapper> onSellOrder  = new Delegate1<>();
    public Delegate1<GrandExchangeItemWrapper> onItemSold   = new Delegate1<>();

    private Map<Integer, GrandExchangeItemWrapper> current, next = null;

    private boolean shouldStop()
    {
        return !Client.getInstance().getScriptManager().isRunning();
    }

    /**
     * @param incoming
     * @param existing
     */
    @Override
    public final void onInventoryItemChanged(Item incoming, Item existing)
    {
        Tick();
    }

    private void Tick()
    {
        List<GrandExchangeItemWrapper> difference;
        if(canVerify())
        {
            current = fetchItems();
            if(current != null)
            {
                difference = getDifference(next, current);
                if(difference != null && !difference.isEmpty())
                {
                    for(GrandExchangeItemWrapper i : difference)
                    {
                        switch(i.getStatus())
                        {
                            case BUY ->
                            {
                                onBuyOrder.Fire(i);
                                onBuyOrder(i);
                            }
                            case BUY_COLLECT ->
                            {
                                onItemBought.Fire(i);
                                onItemBought(i);
                            }
                            case SELL ->
                            {
                                onSellOrder.Fire(i);
                                onSellOrder(i);
                            }
                            case SELL_COLLECT ->
                            {
                                onItemSold.Fire(i);
                                onItemSold(i);
                            }
                        }
                    }
                }
                next = new HashMap<>(current);
            }
        }
    }

    protected abstract void onItemBought(GrandExchangeItemWrapper item);

    protected abstract void onBuyOrder(GrandExchangeItemWrapper item);

    protected abstract void onSellOrder(GrandExchangeItemWrapper item);

    protected abstract void onItemSold(GrandExchangeItemWrapper item);

    private List<GrandExchangeItemWrapper> getDifference(Map<Integer, GrandExchangeItemWrapper> before, Map<Integer, GrandExchangeItemWrapper> after)
    {
        if(before == null || after == null)
        {
            return null;
        }

        List<GrandExchangeItemWrapper> list = new ArrayList<>();
        for(Map.Entry<Integer, GrandExchangeItemWrapper> entry : before.entrySet())
        {
            int                      slot  = entry.getKey();
            GrandExchangeItemWrapper item1 = entry.getValue();
            GrandExchangeItemWrapper item2 = after.get(slot);

            if(!item1.equals(item2))
            {
                if(item1.getStatus() == Status.EMPTY)
                {
                    list.add(item2);
                }
                else
                {
                    list.add(item1);
                }
            }
        }

        return list;
    }

    private Map<Integer, GrandExchangeItemWrapper> fetchItems()
    {
        GrandExchangeItem[] items = GrandExchange.getItems();
        if(items != null && items.length > 0)
        {
            Map<Integer, GrandExchangeItemWrapper> map = new HashMap<>();
            for(int i = 0; i < items.length; i++)
            {
                map.put(i, new GrandExchangeItemWrapper(items[i]));
            }

            return map;
        }

        return null;
    }

    private boolean canVerify()
    {
        return Client.isLoggedIn() && !Client.getInstance().getRandomManager().isSolving() &&
               Players.getLocal() != null && Players.getLocal().exists();
    }

    /**
     * @param item
     */
    @Override
    public final void onInventoryItemAdded(Item item)
    {
        Tick();
    }

    /**
     * @param item
     */
    @Override
    public final void onInventoryItemRemoved(Item item)
    {
        Tick();
    }

    /**
     * @param incoming
     * @param outgoing
     */
    @Override
    public final void onInventoryItemSwapped(Item incoming, Item outgoing)
    {
        Tick();
    }

    /**
     * @param incoming
     * @param existing
     */
    @Override
    public final void onEquipmentItemChanged(Item incoming, Item existing)
    {
        Tick();
    }

    /**
     * @param item
     */
    @Override
    public final void onEquipmentItemAdded(Item item)
    {
        Tick();
    }

    /**
     * @param item
     */
    @Override
    public final void onEquipmentItemRemoved(Item item)
    {
        Tick();
    }

    /**
     * @param incoming
     * @param outgoing
     */
    @Override
    public final void onEquipmentItemSwapped(Item incoming, Item outgoing)
    {
        Tick();
    }

    /**
     * @param incoming
     * @param existing
     */
    @Override
    public final void onBankItemChanged(Item incoming, Item existing)
    {
        Tick();
    }

    /**
     * @param item
     */
    @Override
    public final void onBankItemAdded(Item item)
    {
        Tick();
    }

    /**
     * @param item
     */
    @Override
    public final void onBankItemRemoved(Item item)
    {
        Tick();
    }

    /**
     * @param incoming
     * @param outgoing
     */
    @Override
    public final void onBankItemSwapped(Item incoming, Item outgoing)
    {
        Tick();
    }

    /**
     * @param item
     */
    @Override
    public final void onLootBagItemAdded(Item item)
    {
        Tick();
    }

    /**
     * @param item
     */
    @Override
    public final void onLootBagItemRemoved(Item item)
    {
        Tick();
    }

    /**
     * @param incoming
     * @param existing
     */
    @Override
    public final void onLootBagItemUpdated(Item incoming, Item existing)
    {
        Tick();
    }

    /**
     * @param message
     */
    @Override
    public final void onAutoMessage(Message message)
    {
        Tick();
    }

    /**
     * @param message
     */
    @Override
    public final void onPrivateInfoMessage(Message message)
    {
        Tick();
    }

    /**
     * @param message
     */
    @Override
    public final void onClanMessage(Message message)
    {
        Tick();
    }

    /**
     * @param message
     */
    @Override
    public final void onMessage(Message message)
    {
        Tick();
    }

    /**
     * @param message
     */
    @Override
    public final void onGameMessage(Message message)
    {
        Tick();
    }

    /**
     * @param message
     */
    @Override
    public final void onPlayerMessage(Message message)
    {
        Tick();
    }

    /**
     * @param message
     */
    @Override
    public final void onTradeMessage(Message message)
    {
        Tick();
    }

    /**
     * @param message
     */
    @Override
    public final void onPrivateInMessage(Message message)
    {
        Tick();
    }

    /**
     * @param message
     */
    @Override
    public final void onPrivateOutMessage(Message message)
    {
        Tick();
    }
}
