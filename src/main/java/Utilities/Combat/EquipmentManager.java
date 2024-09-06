package Utilities.Combat;

import OSRSDatabase.ItemDB;
import Utilities.GrandExchange.GEInstance;
import io.vavr.Tuple4;
import org.dreambot.api.methods.combat.CombatStyle;
import org.dreambot.api.utilities.Logger;

import java.util.HashMap;

public class EquipmentManager
{
    private static final EquipmentHelper.EquipmentSlotFocus DefaultEquipmentFocus = new EquipmentHelper.EquipmentSlotFocus(
            new HashMap<>()
            {{
                put(ItemDB.EquipmentData.EquipmentSlot.head, EquipmentHelper.StatFocus.Defence);
                put(ItemDB.EquipmentData.EquipmentSlot.cape, EquipmentHelper.StatFocus.MinMax);
                put(ItemDB.EquipmentData.EquipmentSlot.neck, EquipmentHelper.StatFocus.Melee);
                put(ItemDB.EquipmentData.EquipmentSlot.weapon, EquipmentHelper.StatFocus.Melee);
                put(ItemDB.EquipmentData.EquipmentSlot.body, EquipmentHelper.StatFocus.Defence);
                put(ItemDB.EquipmentData.EquipmentSlot.shield, EquipmentHelper.StatFocus.Defence);
                put(ItemDB.EquipmentData.EquipmentSlot.legs, EquipmentHelper.StatFocus.Defence);
                put(ItemDB.EquipmentData.EquipmentSlot.hands, EquipmentHelper.StatFocus.Defence);
                put(ItemDB.EquipmentData.EquipmentSlot.feet, EquipmentHelper.StatFocus.Defence);
                put(ItemDB.EquipmentData.EquipmentSlot.ring, EquipmentHelper.StatFocus.Melee);
                put(ItemDB.EquipmentData.EquipmentSlot.ammo, EquipmentHelper.StatFocus.Melee);
                put(ItemDB.EquipmentData.EquipmentSlot.two_handed, EquipmentHelper.StatFocus.Melee);
            }});


    private static ItemDB.StanceData.ExperienceType   EXPFocus          = ItemDB.StanceData.ExperienceType.attack;
    private static ItemDB.StanceData.Attacktype       CurrentAttackType = ItemDB.StanceData.Attacktype.slash;
    private static CombatStyle                        CurrentDBCStyle   = CombatStyle.ATTACK;
    private static EquipmentHelper.EquipmentSlotFocus EquipmentFocus    = new EquipmentHelper.EquipmentSlotFocus(
            DefaultEquipmentFocus);
    private static EquipmentHelper.Equipment          CurrentEquipment  = EquipmentHelper.Equipment.GetCurrentEquipment();


    public static EquipmentHelper.Equipment GetWeaponOnly()
    {
        EquipmentHelper.Equipment empty = new EquipmentHelper.Equipment();
        var                       best  = EquipmentHelper.GetBestEquipment(EquipmentFocus);
        empty.equip.put(ItemDB.EquipmentData.EquipmentSlot.weapon,
                        best.equip.get(ItemDB.EquipmentData.EquipmentSlot.weapon));
        return empty;
    }

    public static EquipmentHelper.Equipment GetEquipment()
    {
        return CurrentEquipment;
    }

    public static EquipmentHelper.Equipment GetBestEquipment()
    {
        UpdateEquipment();
        return CurrentEquipment;
    }

    public static EquipmentHelper.Equipment SetEXPFocus(ItemDB.StanceData.ExperienceType focus)
    {
        UpdateEquipment();
        EXPFocus = focus;
        var                                                                            allItems      = GEInstance.GetAllItems();
        Tuple4<Integer, ItemDB.StanceData, EquipmentHelper.StatFocus, ItemDB.ItemData> CurrentChoice = null;

        for(var item : allItems)
        {
            if(item == null)
            {
                continue;
            }

            var itemData = ItemDB.GetItemData(item.getID());
            if(itemData != null && itemData.equipable_by_player && itemData.equipable_weapon &&
               itemData.weapon != null && itemData.equipment != null)
            {
                var weaponData    = itemData.weapon;
                var equipmentData = itemData.equipment;
                for(var stance : weaponData.stances)
                {
                    if(stance.experience == focus)
                    {
                        int                       CurrentStat  =
                                CurrentChoice != null ? CurrentChoice._1 : 0;
                        int                       NewStat      = 0;
                        EquipmentHelper.StatFocus CurrentFocus = EquipmentHelper.StatFocus.StabAtt;
                        switch(stance.attack_type)
                        {
                            case stab ->
                            {
                                NewStat      = equipmentData.attack_stab;
                                CurrentFocus = EquipmentHelper.StatFocus.StabAtt;
                            }
                            case slash ->
                            {
                                NewStat      = equipmentData.attack_slash;
                                CurrentFocus = EquipmentHelper.StatFocus.SlashAtt;
                            }
                            case crush ->
                            {
                                NewStat      = equipmentData.attack_crush;
                                CurrentFocus = EquipmentHelper.StatFocus.CrushAtt;
                            }
                            case magic, defensive_casting, spellcasting ->
                            {
                                NewStat      = equipmentData.magic_damage;
                                CurrentFocus = EquipmentHelper.StatFocus.Magic;
                            }
                            case ranged ->
                            {
                                NewStat      = equipmentData.ranged_strength;
                                CurrentFocus = EquipmentHelper.StatFocus.Ranged;
                            }
                        }

                        var current = CurrentEquipment.equip.get(ItemDB.EquipmentData.EquipmentSlot.weapon);
                        if(NewStat > CurrentStat)
                        {
                            CurrentChoice = new Tuple4<>(NewStat, stance, CurrentFocus, itemData);
                        }
                        else if(NewStat == CurrentStat &&
                                (current != null && current.id == itemData.id))
                        {
                            CurrentChoice = new Tuple4<>(NewStat, stance, CurrentFocus, itemData);
                        }
                    }
                }
            }
        }

        if(CurrentChoice != null)
        {
            CurrentAttackType = CurrentChoice._2.attack_type;
            EquipmentFocus.slots.put(ItemDB.EquipmentData.EquipmentSlot.weapon, CurrentChoice._3);
            UpdateEquipment();
            CurrentEquipment.equip.put(ItemDB.EquipmentData.EquipmentSlot.weapon, CurrentChoice._4);
            CurrentDBCStyle = CurrentChoice._2.experience.GetDBCStyle();
        }

        return CurrentEquipment;
    }

    public static CombatStyle GetDBCombatStyle()
    {
        return CurrentDBCStyle;
    }


    private static void UpdateEquipment()
    {
        Logger.log("EquipmentManager: UpdateEquipment:");
        CurrentEquipment = EquipmentHelper.GetBestEquipment(EquipmentFocus);
    }
}
