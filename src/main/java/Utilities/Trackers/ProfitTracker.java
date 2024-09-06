package Utilities.Trackers;

import Utilities.Scripting.Listeners.GrandExchangeListener.GrandExchangeItemWrapper;
import Utilities.Scripting.Listeners.GrandExchangeListener.GrandExchangeListener;
import Utilities.Scripting.Listeners.InventoryListener;
import org.dreambot.api.script.listener.PaintListener;
import org.dreambot.api.wrappers.items.Item;

import java.awt.*;

public class ProfitTracker implements PaintListener
{
    private long UnrealizedProfit = 0;
    private long RealizedProfit   = 0;
    private long NetWorth         = 0;

    private GrandExchangeListener GEListener   = null;
    private InventoryListener     ItemListener = null;


    public ProfitTracker(GrandExchangeListener GEListener, InventoryListener ItemListener)
    {
        this.GEListener   = GEListener;
        this.ItemListener = ItemListener;
    }

    private void Init()
    {
//        GEListener.onItemBought.Subscribe(this, this::onItemBought);
//        GEListener.onItemSold.Subscribe(this, this::onItemSold);

//        ItemListener.
    }

//    private boolean onItemNew(Item item)
//    {
//
//    }
//
//    private boolean onItemRemoved(Item item)
//    {
//
//    }
//
//    private boolean onItemSold(GrandExchangeItemWrapper item)
//    {
//
//    }
//
//    private boolean onItemBought(GrandExchangeItemWrapper item)
//    {
//
//    }

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


    // basically when we get an item, get prices, add to unrealized profit
    // remove profit when item is removed
    // expenditures on GE purchases or removal of money (taht isn't the bank)
    // when we sell on the GE, we turn the unrealized profit into real profit

}
