package Cycles.SimpleTasks.Misc;

import Cycles.SimpleTasks.Bank.BankItemsTask;
import Utilities.Combat.EquipmentHelper;
import Utilities.Combat.EquipmentManager;
import Utilities.Scripting.SimpleTask;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.combat.CombatStyle;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.items.Item;

import javax.annotation.Nonnull;
import java.util.*;

public class EquipmentTask extends SimpleTask
{
    HashMap<EquipmentSlot, Integer> ToEquip    = new HashMap<>();
    Set<EquipmentSlot> ToUnEquip = new HashSet<>();
    List<Integer>      ToDeposit = new ArrayList<>();
    boolean                         UnEquipAll = false;
    private int Fails   = 0;
    private int Retries = 5;
    private boolean DepositPreviousEquipment = false;

    public EquipmentTask(String Name)
    {
        super(Name);
    }

    public EquipmentTask(String Name, EquipmentHelper.Equipment equipment)
    {
        super(Name);

        for(var slot : equipment.equip.entrySet())
        {
            if(slot == null || slot.getKey() == null ||
               (Equipment.contains(slot.getValue().id) && !slot.getValue().stackable))
            {
                continue;
            }
            ToEquip.put(slot.getKey().GetDreamBotSkill(), slot.getValue().id);
        }

        Logger.log(ToEquip);
    }

    public static EquipmentTask SimpleEquip(String name, int ID, EquipmentSlot slot)
    {
        EquipmentTask out = new EquipmentTask(name);
        out.Equip(slot, ID);
        return out;
    }

    public void Equip(EquipmentSlot Slot, int id)
    {
        ToEquip.put(Slot, id);
        ToUnEquip.remove(Slot);
    }

    //    public void Equip(int id)
    //    {
    //        var slot = Equipment.slot(id);
    //        ToEquip.put(EquipmentSlot.forOriginalSlotId(slot), id);
    //    }

    public void UnEquip(EquipmentSlot Slot)
    {
        if(ToEquip.get(Slot) == null)
        {
            ToUnEquip.add(Slot);
        }
    }

    public void UnEquipAll()
    {
        UnEquipAll = true;
    }

    public void SetAtackType(CombatStyle type)
    {
        Combat.setCombatStyle(type);
    }

    public void SetAtackType()
    {
        Combat.setCombatStyle(EquipmentManager.GetDBCombatStyle());
    }

    /**
     * @return
     */
    @Nonnull
    @Override
    public TaskType GetTaskType()
    {
        return TaskType.EquipmentTask;
    }

    /**
     * @return
     */
    @Override
    public boolean Ready()
    {
        boolean result = true;
        for(var equip : ToEquip.values())
        {
            result &= Inventory.contains(equip) || Equipment.contains(equip);
        }
        Logger.log("EquipmentTask: Ready: EquipCheck: " + result);

        return result && super.Ready();
    }

    /**
     * @return
     */
    @Override
    protected int Loop()
    {
        if(Fails > Retries)
        {
            Logger.log("EquipmentTask: Loop: Failed too many times, exiting");
            return 0;
        }

        if(!DepositPreviousEquipment)
        {
            if(UnEquipAll || !ToUnEquip.isEmpty())
            {
                DepositPreviousEquipment = true;
            }
        }

        if(UnEquipAll)
        {
            Logger.log("EquipmentTask: Loop: UnEquipAll");
            for(var slot : EquipmentSlot.values())
            {
                Equipment.unequip(slot);
            }
        }
        else if(!ToUnEquip.isEmpty())
        {
            var slot = ToUnEquip.iterator().next();
            Logger.log("EquipmentTask: Loop: UnEquip slot " + slot.name());
            Item item = Equipment.getItemInSlot(slot);
            if(item != null && Equipment.unequip(slot))
            {
                ToUnEquip.remove(slot);
                ToDeposit.add(item.getID());
            }
            else
            {
                Fails++;
            }
        }
        else if(!ToEquip.isEmpty())
        {
            if(Inventory.isFull())
            {
                Bank.depositAllItems();
            }
            var set = ToEquip.entrySet().iterator().next();
            Logger.log("EquipmentTask: Loop: Equip ID " + set.getValue() + " for slot " +
                       set.getKey());
            if(Equipment.equip(set.getKey(), set.getValue()))
            {
                ToEquip.remove(set.getKey());
            }
            else
            {
                Fails++;
            }
        }
        else
        {
            SetAtackType();
            if(DepositPreviousEquipment)
            {
                GetScript().addNodes( BankItemsTask.SimpleDeposit(ToDeposit.stream().mapToInt(Integer::intValue).toArray()));
            }
            // Done
            return 0;
        }

        return super.Loop();
    }
}
