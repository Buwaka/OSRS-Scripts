package Utilities.Combat;

import OSRSDatabase.ItemDB;
import org.dreambot.api.Client;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.utilities.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MeleeCombat extends CombatManager
{
    private static HashMap<ItemDB.EquipmentData.EquipmentSlot, StatFocus> _defaultFocus = new HashMap<>()
    {{
        put(ItemDB.EquipmentData.EquipmentSlot.head, StatFocus.Defence);
        put(ItemDB.EquipmentData.EquipmentSlot.cape, StatFocus.MinMax);
        put(ItemDB.EquipmentData.EquipmentSlot.neck, StatFocus.Melee);
        put(ItemDB.EquipmentData.EquipmentSlot.weapon, StatFocus.Melee);
        put(ItemDB.EquipmentData.EquipmentSlot.body, StatFocus.Defence);
        put(ItemDB.EquipmentData.EquipmentSlot.shield, StatFocus.Defence);
        put(ItemDB.EquipmentData.EquipmentSlot.legs, StatFocus.Defence);
        put(ItemDB.EquipmentData.EquipmentSlot.hands, StatFocus.Defence);
        put(ItemDB.EquipmentData.EquipmentSlot.feet, StatFocus.Defence);
        put(ItemDB.EquipmentData.EquipmentSlot.ring, StatFocus.Melee);
        put(ItemDB.EquipmentData.EquipmentSlot.ammo, StatFocus.Melee);
        put(ItemDB.EquipmentData.EquipmentSlot.two_handed, StatFocus.Melee);
    }};
    public static  Map<ItemDB.EquipmentData.EquipmentSlot, StatFocus>     DefaultFocus  = Collections.unmodifiableMap(
            _defaultFocus);

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

    public static class Equipment
    {
        public Map<ItemDB.EquipmentData.EquipmentSlot, ItemDB.ItemData> equip = new HashMap<>();
        public Map<ItemDB.EquipmentData.EquipmentSlot, StatFocus>       Focus = DefaultFocus;

        public Equipment()
        {
            for(var slot : EquipmentSlot.values())
            {
                var item = org.dreambot.api.methods.container.impl.equipment.Equipment.getItemInSlot(
                        slot);
                if(item != null && item.isValid())
                {
                    equip.put(ItemDB.EquipmentData.EquipmentSlot.FromDreamBotEquipSlot(slot),
                              ItemDB.GetItemData(item.getID()));
                }

            }
        }
    }

    public static Equipment GetBestEquipment()
    {
        if(!Bank.isCached())
        {
            Logger.log("MeleeCombat: GetBestEquipment: Bank is not cached, exiting");
            return null;
        }

        var AllItems = Bank.all();
        AllItems.addAll(org.dreambot.api.methods.container.impl.equipment.Equipment.all());
        AllItems.addAll(Inventory.all());
        Equipment equip = new Equipment();

        for(var item : AllItems)
        {
            if(item == null)
            {
                continue;
            }

            var data = ItemDB.GetItemData(item.getID());

            if(data.equipable_by_player && data.equipment != null &&
               (Client.isMembers() || !data.members))
            {
                var dataSlot = data.equipment.slot;
                if(dataSlot == null)
                {
                    Logger.log(data);
                }
                if(data.equipment.slot == ItemDB.EquipmentData.EquipmentSlot.two_handed ||
                   dataSlot == null)
                {
                    //dataSlot = ItemDB.EquipmentData.EquipmentSlot.weapon;
                    continue;
                }


                //                Logger.log(item);
                if(equip.equip.containsKey(dataSlot))
                {
                    if(data.equipment.requirements != null && !data.equipment.requirements.isMet())
                    {
                        continue;
                    }

                    int NewStat = 0;
                    int OldStat = 0;
                    Logger.log("MeleeCombat: GetBestEquipment: DataSlot: " + dataSlot);
                    switch(equip.Focus.get(dataSlot))
                    {
                        case Melee ->
                        {
                            NewStat = data.equipment.GetMaxMelee();
                            OldStat = equip.equip.get(dataSlot).equipment.GetMaxMelee();
                            if(data.equipment.slot ==
                               ItemDB.EquipmentData.EquipmentSlot.two_handed &&
                               equip.equip.containsKey(ItemDB.EquipmentData.EquipmentSlot.shield))
                            {
                                OldStat += equip.equip.get(ItemDB.EquipmentData.EquipmentSlot.shield).equipment.GetMaxMelee();
                            }
                        }
                        case StabAtt ->
                        {
                            NewStat = data.equipment.attack_stab;
                            OldStat = equip.equip.get(dataSlot).equipment.attack_stab;
                            if(data.equipment.slot ==
                               ItemDB.EquipmentData.EquipmentSlot.two_handed &&
                               equip.equip.containsKey(ItemDB.EquipmentData.EquipmentSlot.shield))
                            {
                                OldStat += equip.equip.get(ItemDB.EquipmentData.EquipmentSlot.shield).equipment.attack_stab;
                            }

                        }
                        case SlashAtt ->
                        {
                            NewStat = data.equipment.attack_slash;
                            OldStat = equip.equip.get(dataSlot).equipment.attack_slash;
                            if(data.equipment.slot ==
                               ItemDB.EquipmentData.EquipmentSlot.two_handed &&
                               equip.equip.containsKey(ItemDB.EquipmentData.EquipmentSlot.shield))
                            {
                                OldStat += equip.equip.get(ItemDB.EquipmentData.EquipmentSlot.shield).equipment.attack_slash;
                            }
                        }
                        case CrushAtt ->
                        {
                            NewStat = data.equipment.attack_crush;
                            OldStat = equip.equip.get(dataSlot).equipment.attack_crush;
                            if(data.equipment.slot ==
                               ItemDB.EquipmentData.EquipmentSlot.two_handed &&
                               equip.equip.containsKey(ItemDB.EquipmentData.EquipmentSlot.shield))
                            {
                                OldStat += equip.equip.get(ItemDB.EquipmentData.EquipmentSlot.shield).equipment.attack_crush;
                            }
                        }
                        case Ranged ->
                        {
                            NewStat = data.equipment.ranged_strength;
                            OldStat = equip.equip.get(dataSlot).equipment.ranged_strength;
                            if(data.equipment.slot ==
                               ItemDB.EquipmentData.EquipmentSlot.two_handed &&
                               equip.equip.containsKey(ItemDB.EquipmentData.EquipmentSlot.shield))
                            {
                                OldStat += equip.equip.get(ItemDB.EquipmentData.EquipmentSlot.shield).equipment.ranged_strength;
                            }
                        }
                        case Magic ->
                        {
                            NewStat = data.equipment.magic_damage;
                            OldStat = equip.equip.get(dataSlot).equipment.magic_damage;
                            if(data.equipment.slot ==
                               ItemDB.EquipmentData.EquipmentSlot.two_handed &&
                               equip.equip.containsKey(ItemDB.EquipmentData.EquipmentSlot.shield))
                            {
                                OldStat += equip.equip.get(ItemDB.EquipmentData.EquipmentSlot.shield).equipment.magic_damage;
                            }
                        }
                        case Defence ->
                        {
                            NewStat = data.equipment.GetMaxDef();
                            OldStat = equip.equip.get(dataSlot).equipment.GetMaxDef();
                            if(data.equipment.slot ==
                               ItemDB.EquipmentData.EquipmentSlot.two_handed &&
                               equip.equip.containsKey(ItemDB.EquipmentData.EquipmentSlot.shield))
                            {
                                OldStat += equip.equip.get(ItemDB.EquipmentData.EquipmentSlot.shield).equipment.GetMaxDef();
                            }

                        }
                        case StabDef ->
                        {
                            NewStat = data.equipment.defence_stab;
                            OldStat = equip.equip.get(dataSlot).equipment.defence_stab;
                            if(data.equipment.slot ==
                               ItemDB.EquipmentData.EquipmentSlot.two_handed &&
                               equip.equip.containsKey(ItemDB.EquipmentData.EquipmentSlot.shield))
                            {
                                OldStat += equip.equip.get(ItemDB.EquipmentData.EquipmentSlot.shield).equipment.defence_stab;
                            }
                        }
                        case SlashDef ->
                        {
                            NewStat = data.equipment.defence_slash;
                            OldStat = equip.equip.get(dataSlot).equipment.defence_slash;
                            if(data.equipment.slot ==
                               ItemDB.EquipmentData.EquipmentSlot.two_handed &&
                               equip.equip.containsKey(ItemDB.EquipmentData.EquipmentSlot.shield))
                            {
                                OldStat += equip.equip.get(ItemDB.EquipmentData.EquipmentSlot.shield).equipment.defence_slash;
                            }
                        }
                        case CrushDef ->
                        {
                            NewStat = data.equipment.defence_crush;
                            OldStat = equip.equip.get(dataSlot).equipment.defence_crush;
                            if(data.equipment.slot ==
                               ItemDB.EquipmentData.EquipmentSlot.two_handed &&
                               equip.equip.containsKey(ItemDB.EquipmentData.EquipmentSlot.shield))
                            {
                                OldStat += equip.equip.get(ItemDB.EquipmentData.EquipmentSlot.shield).equipment.defence_crush;
                            }
                        }
                        case RangedDef ->
                        {
                            NewStat = data.equipment.defence_ranged;
                            OldStat = equip.equip.get(dataSlot).equipment.defence_ranged;
                            if(data.equipment.slot ==
                               ItemDB.EquipmentData.EquipmentSlot.two_handed &&
                               equip.equip.containsKey(ItemDB.EquipmentData.EquipmentSlot.shield))
                            {
                                OldStat += equip.equip.get(ItemDB.EquipmentData.EquipmentSlot.shield).equipment.defence_ranged;
                            }
                        }
                        case MagicDef ->
                        {
                            NewStat = data.equipment.defence_magic;
                            OldStat = equip.equip.get(dataSlot).equipment.defence_magic;
                            if(data.equipment.slot ==
                               ItemDB.EquipmentData.EquipmentSlot.two_handed &&
                               equip.equip.containsKey(ItemDB.EquipmentData.EquipmentSlot.shield))
                            {
                                OldStat += equip.equip.get(ItemDB.EquipmentData.EquipmentSlot.shield).equipment.defence_magic;
                            }
                        }
                        case Prayer ->
                        {
                            NewStat = data.equipment.prayer;
                            OldStat = equip.equip.get(dataSlot).equipment.prayer;
                            if(data.equipment.slot ==
                               ItemDB.EquipmentData.EquipmentSlot.two_handed &&
                               equip.equip.containsKey(ItemDB.EquipmentData.EquipmentSlot.shield))
                            {
                                OldStat += equip.equip.get(ItemDB.EquipmentData.EquipmentSlot.shield).equipment.prayer;
                            }
                        }
                        case MinMax ->
                        {
                            NewStat = data.equipment.GetTotal();
                            OldStat = equip.equip.get(dataSlot).equipment.GetTotal();
                            if(data.equipment.slot ==
                               ItemDB.EquipmentData.EquipmentSlot.two_handed &&
                               equip.equip.containsKey(ItemDB.EquipmentData.EquipmentSlot.shield))
                            {
                                OldStat += equip.equip.get(ItemDB.EquipmentData.EquipmentSlot.shield).equipment.GetTotal();
                            }
                        }
                    }
                    Logger.log(data.name + " " + NewStat + " <-> " + OldStat +
                               equip.equip.get(dataSlot).name);
                    if(NewStat > OldStat)
                    {
                        Logger.log("Chosen " + data.name);
                        equip.equip.put(dataSlot, data);
                    }
                }
                else
                {
                    Logger.log("Chosen " + data.name);
                    equip.equip.put(data.equipment.slot, data);
                }
            }
        }

        return equip;
    }

    public static Equipment GetWeaponOnly()
    {
        Equipment equp = new Equipment();
        var       best = GetBestEquipment();
        equp.equip.put(ItemDB.EquipmentData.EquipmentSlot.weapon,
                       best.equip.get(ItemDB.EquipmentData.EquipmentSlot.weapon));
        return equp;
    }


}
