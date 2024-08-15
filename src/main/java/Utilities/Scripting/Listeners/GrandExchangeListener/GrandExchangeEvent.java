package Utilities.Scripting.Listeners.GrandExchangeListener;

import org.dreambot.api.Client;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.grandexchange.GrandExchangeItem;
import org.dreambot.api.methods.grandexchange.Status;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.event.ScriptEvent;
import org.dreambot.api.utilities.Logger;

import java.io.Serial;
import java.util.*;

public final class GrandExchangeEvent extends ScriptEvent implements EventListener
{
    @Serial
    private static final long serialVersionUID = -5073112575276005212L;
    Map<Integer, GrandExchangeItemWrapper> current, next = null;


    /**
     * @param eventListener
     */
    @Override
    public void dispatch(EventListener eventListener)
    {
        Logger.log("GrandExchangeEvent: dispatch: ");
        if(eventListener instanceof GrandExchangeListener)
        {
            GrandExchangeListener GEListener = ((GrandExchangeListener) eventListener);
            List<GrandExchangeItemWrapper>         difference;
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
                                case BUY -> {
                                    GEListener.onBuyOrder(i);
                                }
                                case BUY_COLLECT -> {
                                    GEListener.onItemBought(i);
                                }
                                case SELL -> {
                                    GEListener.onSellOrder(i);
                                }
                                case SELL_COLLECT -> {
                                    GEListener.onItemSold(i);
                                }
                            }
                        }
                    }
                    next = new HashMap<>(current);
                }
            }
        }
    }

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

    private boolean shouldStop()
    {
        return !Client.getInstance().getScriptManager().isRunning();
    }
}
