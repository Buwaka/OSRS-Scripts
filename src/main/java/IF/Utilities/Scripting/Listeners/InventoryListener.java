package IF.Utilities.Scripting.Listeners;

import IF.Utilities.Patterns.Delegates.Delegate1;
import IF.Utilities.Patterns.Delegates.Delegate2;
import IF.Utilities.Scripting.Logger;
import org.dreambot.api.script.listener.ItemContainerListener;
import org.dreambot.api.wrappers.items.Item;

public class InventoryListener implements ItemContainerListener
{
    public Delegate1<Item> onNewItem  = new Delegate1<>();
    public Delegate1<Item> onLostItem = new Delegate1<>();


    public Delegate2</*incoming*/Item,/*existing*/ Item> onInventoryItemChanged = new Delegate2<>();
    public Delegate2</*incoming*/Item,/*outgoing*/ Item> onInventoryItemSwapped = new Delegate2<>();
    public Delegate2</*incoming*/Item,/*outgoing*/ Item> onEquipmentItemChanged = new Delegate2<>();
    public Delegate2</*incoming*/Item,/*outgoing*/ Item> onEquipmentItemSwapped = new Delegate2<>();
    public Delegate2</*incoming*/Item,/*outgoing*/ Item> onBankItemChanged      = new Delegate2<>();
    public Delegate2</*incoming*/Item,/*outgoing*/ Item> onBankItemSwapped      = new Delegate2<>();
    public Delegate2</*incoming*/Item,/*outgoing*/ Item> onLootBagItemUpdated   = new Delegate2<>();

    public Delegate1<Item> onInventoryAdded       = new Delegate1<>();
    public Delegate1<Item> onInventoryItemRemoved = new Delegate1<>();
    public Delegate1<Item> onEquipmentItemAdded   = new Delegate1<>();
    public Delegate1<Item> onEquipmentItemRemoved = new Delegate1<>();
    public Delegate1<Item> onBankItemAdded        = new Delegate1<>();
    public Delegate1<Item> onBankItemRemoved      = new Delegate1<>();
    public Delegate1<Item> onLootBagItemAdded     = new Delegate1<>();
    public Delegate1<Item> onLootBagItemRemoved   = new Delegate1<>();


    @Override
    public void onInventoryItemChanged(Item incoming, Item existing)
    {
        //onInventoryAdded.Fire(ItemAction.Changed, incoming, existing);
        Logger.log("onInventoryItemChanged");
    }

    @Override
    public void onInventoryItemAdded(Item item)
    {
        //onInventoryAdded.Fire(ItemAction.Added, item, null);
        Logger.log("onInventoryItemAdded");
    }

    @Override
    public void onInventoryItemRemoved(Item item)
    {
        //onInventoryAdded.Fire(ItemAction.Removed, item, null);
        Logger.log("onInventoryItemRemoved");
    }

    @Override
    public void onInventoryItemSwapped(Item incoming, Item outgoing)
    {
        //onInventoryAdded.Fire(ItemAction.Swapped, incoming, outgoing);
        Logger.log("onInventoryItemSwapped");
    }

    @Override
    public void onEquipmentItemChanged(Item incoming, Item existing)
    {
    }

    @Override
    public void onEquipmentItemAdded(Item item)
    {
    }

    @Override
    public void onEquipmentItemRemoved(Item item)
    {
    }

    @Override
    public void onEquipmentItemSwapped(Item incoming, Item outgoing)
    {
    }

    @Override
    public void onBankItemChanged(Item incoming, Item existing)
    {
    }

    @Override
    public void onBankItemAdded(Item item)
    {
    }

    @Override
    public void onBankItemRemoved(Item item)
    {
    }

    @Override
    public void onBankItemSwapped(Item incoming, Item outgoing)
    {
    }

    @Override
    public void onLootBagItemAdded(Item item)
    {
    }

    @Override
    public void onLootBagItemRemoved(Item item)
    {
    }

    @Override
    public void onLootBagItemUpdated(Item incoming, Item existing)
    {
    }
}
