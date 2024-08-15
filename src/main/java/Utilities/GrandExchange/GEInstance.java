package Utilities.GrandExchange;

import Utilities.GrandExchange.Orders.BaseOrder;
import Utilities.GrandExchange.Orders.GEOrder;
import Utilities.OSRSUtilities;
import Utilities.Scripting.tpircSScript;
import io.vavr.Tuple2;
import org.dreambot.api.Client;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.Item;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class GEInstance implements Serializable
{
    public static final  int                      CoinID                 = 995;
    public static final  String                   ConfigID               = "GrandExchangeOrders";
    @Serial
    private static final long                     serialVersionUID       = 6064461425258339028L;
    private final        long                     MarketOrderGracePeriod = TimeUnit.SECONDS.toNanos(
            10);
    private final        long                     LimitOrderGracePeriod  = TimeUnit.DAYS.toNanos(7);
    private final        int                      MaxAttempts            = 5;
    private              PriorityQueue<BaseOrder> Orders                 = new PriorityQueue<>();
    private              Map<Integer, GESlot>     ActiveOrders           = new HashMap<>();
    private              List<BaseOrder> OrdersToCancel                = new ArrayList<>();
    private transient    int                      Attempts               = 0;
    private transient    tpircSScript             OwnerScript;

    public GEInstance(tpircSScript owner)
    {
        init(owner);
    }

    public void init(tpircSScript owner)
    {
        OwnerScript = owner;
        Logger.log("GEInstance: init: Orders: " + Orders.toString());
        Logger.log("GEInstance: init: ActiveOrders: " + ActiveOrders.entrySet());
    }

    public static class GESlot implements Serializable
    {
        @Serial
        private static final long      serialVersionUID = 320916992060472575L;
        public               int       Slot;
        public               BaseOrder Order;
        public               long      Timestamp;

        public GESlot(int slot, BaseOrder order, long timestamp)
        {
            Slot      = slot;
            Order     = order;
            Timestamp = timestamp;
        }

        public GEOrder.OrderType GetType()
        {
            return Order.GetOrderType();
        }
    }

    public <T extends BaseOrder> void AddUniqueOrder(T order)
    {
        if(order.GetQuantity() == 0)
        {
            return;
        }

        var existingOrder = GetOrderWithID(order.GetID(),
                                           order.GetOrderType(),
                                           order.GetTransactionType());
        if(existingOrder != null)
        {
            AddOrder(order);
        }
    }

    public <T extends BaseOrder> void AddOrder(T order)
    {
        if(order.GetQuantity() == 0)
        {
            return;
        }

        var existingActiveOrder = CancelOrder(order);
        var existingOrder = GetOrderWithID(order.GetID(),
                                           order.GetOrderType(),
                                           order.GetTransactionType());
        if(existingActiveOrder != null)
        {
            order.AddQuantity(existingActiveOrder.GetQuantity());
            var base = new BaseOrder(order);
            Logger.log("GEInstance: AddOrder: Overwrite and update existing order: " + base);
            Orders.add(base);
        }
        else if(existingOrder != null)
        {
            Logger.log("GEInstance: AddOrder: Update existing order " + existingOrder);
            existingOrder.AddQuantity(order.GetQuantity());
        }
        else
        {
            var base = new BaseOrder(order);
            Logger.log("GEInstance: AddOrder: " + base);
            Orders.add(base);
        }

        SaveState();
    }

    public BaseOrder GetOrderWithID(int ID, GEOrder.OrderType OType, GEOrder.TransactionType TType)
    {
        for(var order : Orders)
        {
            if(order != null && order.GetID() == ID && order.GetOrderType() == OType &&
               order.GetTransactionType() == TType)
            {
                return order;
            }
        }
        return null;
    }

    /**
     * @param order: based on the item ID of the order, an existing order will be cancelled
     *
     * @return the order that has been cancelled, null if there was no order
     */
    public BaseOrder CancelOrder(GEOrder order)
    {
        for(var activeOrder : ActiveOrders.entrySet())
        {
            if(activeOrder.getValue().Order.GetID() == order.GetID())
            {
                if(GrandExchange.isOpen())
                {
                    boolean result = GrandExchange.cancelOffer(activeOrder.getKey());
                    if(result)
                    {
                        ActiveOrders.remove(activeOrder.getKey());
                        return activeOrder.getValue().Order;
                    }
                    else
                    {
                        return null;
                    }
                }
                else
                {
                    OrdersToCancel.add(activeOrder.getValue().Order);
                    return activeOrder.getValue().Order;
                }
            }
        }
        return null;
    }

    public int tick()
    {
        int SlotCount = Client.isMembers() ? 10 : 3;

        if(!GrandExchange.isOpen())
        {
            GrandExchange.open();
        }

        if(GrandExchange.isReadyToCollect())
        {
            GrandExchange.collectToBank();
        }

        while(!OrdersToCancel.isEmpty())
        {
            CancelOrder(OrdersToCancel.removeFirst());
        }

        if(Orders.isEmpty() && !HasActiveMarketOrders())
        {
            //done for now
            Logger.log("GEInstance: Tick: No orders left, quiting");
            GrandExchange.collectToBank();
            return 0;
        }

        var order    = Orders.peek();
        int freeSlot = GrandExchange.getFirstOpenSlot();
        Logger.log("GEInstance: Tick: " + order);

        while(!Orders.isEmpty())
        {
            if(freeSlot == -1)
            {
                for(var i = 0; i < SlotCount; i++)
                {
                    if(!ActiveOrders.containsKey(i))
                    {
                        CancelOrder(i);
                        freeSlot = i;
                        break;
                    }
                    else
                    {
                        var CurrentSlot = ActiveOrders.get(i);
                        if(CurrentSlot.GetType() == GEOrder.OrderType.Limit ||
                           ExceedGracePeriod(CurrentSlot))
                        {
                            GrandExchange.cancelOffer(i);
                            Orders.add(CurrentSlot.Order);
                            freeSlot = i;
                            SaveState();
                            break;
                        }
                    }
                }
            }

            if(freeSlot == -1)
            {
                return GetGEWaitTime();
            }

            GESlot NewSlot = ProcessOrder(freeSlot, order);

            if(NewSlot != null)
            {
                Orders.poll();
                ActiveOrders.put(freeSlot, NewSlot);
                SaveState();
                Attempts = 0;
            }
            else
            {
                Logger.log("GEInstance: Tick: Attempt " + Attempts);
                Attempts++;
                if(Attempts >= MaxAttempts)
                {
                    Logger.log("GEInstance: Tick: Too many attempts, removing order");
                    Orders.poll();
                    break;
                }
            }
        }

        return GetGEWaitTime();
    }

    private GESlot ProcessOrder(int slot, BaseOrder order)
    {
        int       Attempts    = 0;
        final int MaxAttempts = 5;
        GESlot    NewSlot     = new GESlot(slot, order, System.nanoTime());

        boolean result = false;
        while(!result && Attempts < MaxAttempts)
        {
            Logger.log("GEInstance: ProcessOrder: " + order);
            switch(order.GetTransactionType())
            {
                case Buy ->
                {
                    if(GetLiquidMoney() < order.GetTotalPrice())
                    {
                        Logger.log(
                                "GEInstance: ProcessOrder: Not enough money to complete this order, exiting, needs " +
                                order.GetTotalPrice());
                        return null;
                    }
                    result = GrandExchange.buyItem(order.GetID(),
                                                   order.GetQuantity(),
                                                   order.GetPrice());
                    Logger.log("GEInstance: ProcessOrder: buy result: " + result);
                }
                case Sell ->
                {
                    result = GrandExchange.sellItem(order.GetID(),
                                                    order.GetQuantity(),
                                                    order.GetPrice());
                    Logger.log("GEInstance: ProcessOrder: sell result: " + result);
                }
            }
            Logger.log("GEInstance: ProcessOrder: Attempt: " + Attempts);
            Attempts++;
            Sleep.sleepTicks(1);
        }

        return result ? NewSlot : null;
    }

    public static int GetLiquidMoney()
    {
        final int CoincID        = CoinID;
        int       inventoryMoney = Inventory.count(CoincID);
        int       BankMoney      = Bank.count(CoincID);
        Logger.log("GEInstance: GetLiquidMoney: " + (inventoryMoney + BankMoney));

        return inventoryMoney + BankMoney;
    }

    public static List<Item> GetAllItems()
    {
        var AllItems = Bank.all();
        AllItems.addAll(Inventory.all());
        AllItems.addAll(Equipment.all());
        return AllItems;
    }

    public static int[] GetAllItemsID()
    {
        var AllItems = Bank.all();
        AllItems.addAll(Inventory.all());
        AllItems.addAll(Equipment.all());
        return AllItems.stream().mapToInt(t -> t != null ? t.getID() : 0).toArray();
    }

    public Tuple2<Integer, Integer>[] GetAllOrderRequirements()
    {
        List<Tuple2<Integer, Integer>> out = new ArrayList<>();
        for(var order : Orders)
        {
            if(order.GetTransactionType() == GEOrder.TransactionType.Sell)
            {
                out.add(new Tuple2<>(order.GetID(), order.GetQuantity()));

            }
        }

        return out.toArray(new Tuple2[]{});
    }

    /**
     * @param slot: slot to cancel
     *
     * @return the order that was cancelled if the order was inside the GEInstance, otherwise null
     */
    public boolean CancelOrder(int slot)
    {
        return GrandExchange.cancelOffer(slot);
    }

    private boolean ExceedGracePeriod(GESlot slot)
    {
        switch(slot.GetType())
        {
            case Market ->
            {
                if(System.nanoTime() - slot.Timestamp > MarketOrderGracePeriod)
                {
                    return true;
                }
            }
            case Limit ->
            {
                if(System.nanoTime() - slot.Timestamp > LimitOrderGracePeriod)
                {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean HasActiveMarketOrders()
    {
        for(var order : ActiveOrders.entrySet())
        {
            if(order.getValue().GetType() == GEOrder.OrderType.Market)
            {
                return true;
            }
        }
        return false;
    }

    public boolean HasQueuedActions()
    {
        return !Orders.isEmpty() || !OrdersToCancel.isEmpty();
    }

    public int GetGEWaitTime()
    {
        return OSRSUtilities.rand.nextInt(40000);
    }

    public void SaveState()
    {
        OwnerScript.GetConfig().SaveState(ConfigID, this);
    }

}
