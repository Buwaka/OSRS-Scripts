package Utilities.Combat;

import OSRSDatabase.ItemDB;
import Utilities.GrandExchange.GEInstance;
import org.dreambot.api.Client;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.items.Item;

import java.util.*;

public class EquipmentHelper
{
    public enum StatFocus
    {
        Melee,
        StabAtt,
        SlashAtt,
        CrushAtt,
        Ranged,
        Magic,
        Defence,
        StabDef,
        SlashDef,
        CrushDef,
        RangedDef,
        MagicDef,
        Prayer,
        MinMax
    }

    public static class EquipmentSlotFocus
    {
        Map<ItemDB.EquipmentData.EquipmentSlot, StatFocus> slots;

        EquipmentSlotFocus(EquipmentSlotFocus other)
        {
            slots = new HashMap<>(other.slots);
        }

        EquipmentSlotFocus(Map<ItemDB.EquipmentData.EquipmentSlot, StatFocus> other)
        {
            slots = other;
        }
    }


    public static class Equipment
    {
        public Map<ItemDB.EquipmentData.EquipmentSlot, ItemDB.ItemData> equip = new HashMap<>();

        public Equipment()
        {
        }

        public Equipment(Map<ItemDB.EquipmentData.EquipmentSlot, ItemDB.ItemData> mapEquipment)
        {
            equip = mapEquipment;
        }

        public Equipment(Equipment other)
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
                    Logger.log(
                            "EquipmentHelper: isEquipped: Current item (" + item + ") for slot " +
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

        public static Equipment GetCurrentEquipment()
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
            return new Equipment(out);
        }
    }


    public static Equipment GetBestEquipment(EquipmentSlotFocus focus)
    {
        if(!Bank.isCached())
        {
            Logger.log("EquipmentHelper: GetBestEquipment: Bank is not cached, exiting");
            return null;
        }

        var       AllItems = GEInstance.GetAllItems();
        Equipment equip    = Equipment.GetCurrentEquipment();

        for(var item : AllItems)
        {
            if(item == null)
            {
                continue;
            }

            var itemData = ItemDB.GetItemData(item.getID());

            if(itemData.equipable_by_player && itemData.equipment != null &&
               (Client.isMembers() || !itemData.members))
            {
                var itemEquipmentSlot = itemData.equipment.slot;
                if(itemEquipmentSlot == null)
                {
                    Logger.log(itemData);
                }
                if(itemData.equipment.slot == ItemDB.EquipmentData.EquipmentSlot.two_handed ||
                   itemEquipmentSlot == null)
                {
                    //itemEquipmentSlot = ItemDB.EquipmentData.EquipmentSlot.weapon;
                    continue;
                }

                if(itemEquipmentSlot == ItemDB.EquipmentData.EquipmentSlot.ammo &&
                   (focus.slots.get(itemEquipmentSlot) != StatFocus.Ranged &&
                    focus.slots.get(itemEquipmentSlot) != StatFocus.RangedDef))
                {
                    continue;
                }

                //                Logger.log(item);
                if(equip.equip.containsKey(itemEquipmentSlot))
                {
                    if(itemData.equipment.requirements != null &&
                       !itemData.equipment.requirements.isMet())
                    {
                        continue;
                    }

                    int NewStat = 0;
                    int OldStat = 0;
                    Logger.log("EquipmentHelper: GetBestEquipment: DataSlot: " + itemEquipmentSlot);
                    switch(focus.slots.get(itemEquipmentSlot))
                    {
                        case Melee ->
                        {
                            NewStat = itemData.equipment.GetMaxMelee();
                            OldStat = equip.equip.get(itemEquipmentSlot).equipment.GetMaxMelee();
                            if(itemData.equipment.slot ==
                               ItemDB.EquipmentData.EquipmentSlot.two_handed &&
                               equip.equip.containsKey(ItemDB.EquipmentData.EquipmentSlot.shield))
                            {
                                OldStat += equip.equip.get(ItemDB.EquipmentData.EquipmentSlot.shield).equipment.GetMaxMelee();
                            }
                        }
                        case StabAtt ->
                        {
                            NewStat = itemData.equipment.attack_stab;
                            OldStat = equip.equip.get(itemEquipmentSlot).equipment.attack_stab;
                            if(itemData.equipment.slot ==
                               ItemDB.EquipmentData.EquipmentSlot.two_handed &&
                               equip.equip.containsKey(ItemDB.EquipmentData.EquipmentSlot.shield))
                            {
                                OldStat += equip.equip.get(ItemDB.EquipmentData.EquipmentSlot.shield).equipment.attack_stab;
                            }

                        }
                        case SlashAtt ->
                        {
                            NewStat = itemData.equipment.attack_slash;
                            OldStat = equip.equip.get(itemEquipmentSlot).equipment.attack_slash;
                            if(itemData.equipment.slot ==
                               ItemDB.EquipmentData.EquipmentSlot.two_handed &&
                               equip.equip.containsKey(ItemDB.EquipmentData.EquipmentSlot.shield))
                            {
                                OldStat += equip.equip.get(ItemDB.EquipmentData.EquipmentSlot.shield).equipment.attack_slash;
                            }
                        }
                        case CrushAtt ->
                        {
                            NewStat = itemData.equipment.attack_crush;
                            OldStat = equip.equip.get(itemEquipmentSlot).equipment.attack_crush;
                            if(itemData.equipment.slot ==
                               ItemDB.EquipmentData.EquipmentSlot.two_handed &&
                               equip.equip.containsKey(ItemDB.EquipmentData.EquipmentSlot.shield))
                            {
                                OldStat += equip.equip.get(ItemDB.EquipmentData.EquipmentSlot.shield).equipment.attack_crush;
                            }
                        }
                        case Ranged ->
                        {
                            NewStat = itemData.equipment.ranged_strength;
                            OldStat = equip.equip.get(itemEquipmentSlot).equipment.ranged_strength;
                            if(itemData.equipment.slot ==
                               ItemDB.EquipmentData.EquipmentSlot.two_handed &&
                               equip.equip.containsKey(ItemDB.EquipmentData.EquipmentSlot.shield))
                            {
                                OldStat += equip.equip.get(ItemDB.EquipmentData.EquipmentSlot.shield).equipment.ranged_strength;
                            }
                        }
                        case Magic ->
                        {
                            NewStat = itemData.equipment.magic_damage;
                            OldStat = equip.equip.get(itemEquipmentSlot).equipment.magic_damage;
                            if(itemData.equipment.slot ==
                               ItemDB.EquipmentData.EquipmentSlot.two_handed &&
                               equip.equip.containsKey(ItemDB.EquipmentData.EquipmentSlot.shield))
                            {
                                OldStat += equip.equip.get(ItemDB.EquipmentData.EquipmentSlot.shield).equipment.magic_damage;
                            }
                        }
                        case Defence ->
                        {
                            NewStat = itemData.equipment.GetMaxDef();
                            OldStat = equip.equip.get(itemEquipmentSlot).equipment.GetMaxDef();
                            if(itemData.equipment.slot ==
                               ItemDB.EquipmentData.EquipmentSlot.two_handed &&
                               equip.equip.containsKey(ItemDB.EquipmentData.EquipmentSlot.shield))
                            {
                                OldStat += equip.equip.get(ItemDB.EquipmentData.EquipmentSlot.shield).equipment.GetMaxDef();
                            }

                        }
                        case StabDef ->
                        {
                            NewStat = itemData.equipment.defence_stab;
                            OldStat = equip.equip.get(itemEquipmentSlot).equipment.defence_stab;
                            if(itemData.equipment.slot ==
                               ItemDB.EquipmentData.EquipmentSlot.two_handed &&
                               equip.equip.containsKey(ItemDB.EquipmentData.EquipmentSlot.shield))
                            {
                                OldStat += equip.equip.get(ItemDB.EquipmentData.EquipmentSlot.shield).equipment.defence_stab;
                            }
                        }
                        case SlashDef ->
                        {
                            NewStat = itemData.equipment.defence_slash;
                            OldStat = equip.equip.get(itemEquipmentSlot).equipment.defence_slash;
                            if(itemData.equipment.slot ==
                               ItemDB.EquipmentData.EquipmentSlot.two_handed &&
                               equip.equip.containsKey(ItemDB.EquipmentData.EquipmentSlot.shield))
                            {
                                OldStat += equip.equip.get(ItemDB.EquipmentData.EquipmentSlot.shield).equipment.defence_slash;
                            }
                        }
                        case CrushDef ->
                        {
                            NewStat = itemData.equipment.defence_crush;
                            OldStat = equip.equip.get(itemEquipmentSlot).equipment.defence_crush;
                            if(itemData.equipment.slot ==
                               ItemDB.EquipmentData.EquipmentSlot.two_handed &&
                               equip.equip.containsKey(ItemDB.EquipmentData.EquipmentSlot.shield))
                            {
                                OldStat += equip.equip.get(ItemDB.EquipmentData.EquipmentSlot.shield).equipment.defence_crush;
                            }
                        }
                        case RangedDef ->
                        {
                            NewStat = itemData.equipment.defence_ranged;
                            OldStat = equip.equip.get(itemEquipmentSlot).equipment.defence_ranged;
                            if(itemData.equipment.slot ==
                               ItemDB.EquipmentData.EquipmentSlot.two_handed &&
                               equip.equip.containsKey(ItemDB.EquipmentData.EquipmentSlot.shield))
                            {
                                OldStat += equip.equip.get(ItemDB.EquipmentData.EquipmentSlot.shield).equipment.defence_ranged;
                            }
                        }
                        case MagicDef ->
                        {
                            NewStat = itemData.equipment.defence_magic;
                            OldStat = equip.equip.get(itemEquipmentSlot).equipment.defence_magic;
                            if(itemData.equipment.slot ==
                               ItemDB.EquipmentData.EquipmentSlot.two_handed &&
                               equip.equip.containsKey(ItemDB.EquipmentData.EquipmentSlot.shield))
                            {
                                OldStat += equip.equip.get(ItemDB.EquipmentData.EquipmentSlot.shield).equipment.defence_magic;
                            }
                        }
                        case Prayer ->
                        {
                            NewStat = itemData.equipment.prayer;
                            OldStat = equip.equip.get(itemEquipmentSlot).equipment.prayer;
                            if(itemData.equipment.slot ==
                               ItemDB.EquipmentData.EquipmentSlot.two_handed &&
                               equip.equip.containsKey(ItemDB.EquipmentData.EquipmentSlot.shield))
                            {
                                OldStat += equip.equip.get(ItemDB.EquipmentData.EquipmentSlot.shield).equipment.prayer;
                            }
                        }
                        case MinMax ->
                        {
                            NewStat = itemData.equipment.GetTotal();
                            OldStat = equip.equip.get(itemEquipmentSlot).equipment.GetTotal();
                            if(itemData.equipment.slot ==
                               ItemDB.EquipmentData.EquipmentSlot.two_handed &&
                               equip.equip.containsKey(ItemDB.EquipmentData.EquipmentSlot.shield))
                            {
                                OldStat += equip.equip.get(ItemDB.EquipmentData.EquipmentSlot.shield).equipment.GetTotal();
                            }
                        }
                    }
                    Logger.log("EquipmentHelper: " + itemData.name + " " + NewStat + " <-> " +
                               OldStat + equip.equip.get(itemEquipmentSlot).name);
                    Item CurrentForSlot = org.dreambot.api.methods.container.impl.equipment.Equipment.getItemInSlot(
                            itemEquipmentSlot.GetDreamBotEquipmentSlot());
                    if(NewStat > OldStat)
                    {
                        Logger.log("EquipmentHelper: Chosen " + itemData.name);
                        equip.equip.put(itemEquipmentSlot, itemData);
                    }
                    //                    else if(NewStat == OldStat && CurrentForSlot != null &&
                    //                            CurrentForSlot.getID() == itemData.id)
                    //                    {
                    //                        Logger.log("EquipmentHelper: same stat, preferring already equipped " +
                    //                                   itemData.name);
                    //                        equip.equip.put(itemEquipmentSlot, itemData);
                    //                    }
                }
                else
                {
                    Logger.log("EquipmentHelper: Chosen " + itemData.name);
                    equip.equip.put(itemData.equipment.slot, itemData);
                }
            }
        }

        return equip;
    }

}
