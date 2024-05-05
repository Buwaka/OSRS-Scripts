package Utilities.Scripting.Listeners;

import Utilities.Patterns.Delegates.Delegate3;
import org.dreambot.api.script.listener.ItemContainerListener;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.items.Item;

public class InventoryListener implements ItemContainerListener
{
    public Delegate3<ItemAction, Item, Item> onInventoryAdded = new Delegate3<>();

    public static void main(String[] args)
    {
        InventoryListener testt = new InventoryListener();
        var               a     = 5;
        a += 5;
        testt.onInventoryAdded.Subscribe(((InventoryListener.ItemAction A, Item B, Item C) -> true));
        testt.onInventoryAdded.Fire(ItemAction.Added, null, null);
    }

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

    public enum ItemAction
    {
        Added,
        Removed,
        Changed,
        Swapped
    }
}
