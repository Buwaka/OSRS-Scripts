package Cycles.SimpleTasks.Misc;

import Utilities.Scripting.SimpleTask;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.utilities.Logger;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class EquipmentTask extends SimpleTask
{
    HashMap<EquipmentSlot, Integer> ToEquip    = new HashMap<>();
    Set<EquipmentSlot>              ToUnEquip  = new HashSet<>();
    boolean                         UnEquipAll = false;
    private int Fails   = 0;
    private int Retries = 5;

    public EquipmentTask(String Name)
    {
        super(Name);
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
    protected boolean Ready()
    {
        boolean result = true;
        for(var equip : ToEquip.values())
        {
            result &= Inventory.contains(equip);
        }

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
            if(Equipment.unequip(slot))
            {
                ToUnEquip.remove(slot);
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
            Logger.log("EquipmentTask: Loop: Equip ID " + set.getValue() + " for slot " + set.getKey());
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
            // Done
            return 0;
        }

        return super.Loop();
    }
}
