package Utilities.Combat;

import OSRSDatabase.ItemDB;
import Utilities.Scripting.Logger;
import org.dreambot.api.methods.container.impl.Inventory;

import java.util.*;

public class EquipmentLoadout
{
    public Map<ItemDB.EquipmentData.EquipmentSlot, ItemDB.ItemData> equip = new HashMap<>();

    public EquipmentLoadout()
    {
    }

    public EquipmentLoadout(Map<ItemDB.EquipmentData.EquipmentSlot, ItemDB.ItemData> mapEquipment)
    {
        equip = mapEquipment;
    }

    public EquipmentLoadout(EquipmentLoadout other)
    {
        equip = other.equip;
    }

    public int[] GetBankEquipment()
    {
        List<Integer> out = new ArrayList<>();
        for(var slot : equip.entrySet())
        {
            if(slot.getValue() != null)
            {
                var item = org.dreambot.api.methods.container.impl.equipment.Equipment.getItemInSlot(
                        slot.getKey().GetDreamBotEquipmentSlot());
                if((item == null || item.getID() != slot.getValue().id) &&
                   !Inventory.contains(slot.getValue().id))
                {
                    out.add(slot.getValue().id);
                }
            }
        }

        Logger.log("EquipmentHelper: GetBankEquipment: " + Arrays.toString(out.toArray()));

        return out.stream().mapToInt(Integer::intValue).toArray();
    }

    public boolean isEquipped()
    {
        for(var slot : equip.entrySet())
        {
            if(slot.getValue() != null)
            {
                var item = org.dreambot.api.methods.container.impl.equipment.Equipment.getItemInSlot(
                        slot.getKey().GetDreamBotEquipmentSlot());
                Logger.log("EquipmentHelper: isEquipped: Current item (" + item + ") for slot " +
                           slot.getKey().name());
                Logger.log("EquipmentHelper: isEquipped: want to equip " + slot.getValue());
                if(item == null || item.getID() != slot.getValue().id)
                {
                    Logger.log("EquipmentHelper: isEquipped: item " + slot.getValue().name +
                               " is not equipped");
                    return false;
                }
            }
        }
        return true;
    }

    public static EquipmentLoadout GetCurrentEquipment()
    {
        Map<ItemDB.EquipmentData.EquipmentSlot, ItemDB.ItemData> out = new HashMap<>();
        for(var slot : org.dreambot.api.methods.container.impl.equipment.EquipmentSlot.values())
        {
            var item = org.dreambot.api.methods.container.impl.equipment.Equipment.getItemInSlot(
                    slot);
            if(item != null && item.isValid())
            {
                out.put(ItemDB.EquipmentData.EquipmentSlot.FromDreamBotEquipSlot(slot),
                        ItemDB.GetItemData(item.getID()));
            }

        }
        return new EquipmentLoadout(out);
    }
}
