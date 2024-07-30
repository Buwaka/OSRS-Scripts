package Utilities.Requirement;

import io.vavr.Tuple2;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;

public class ItemRequirement implements IRequirement
{
    boolean CheckBank;

    Tuple2<Integer, Integer>[] Items;


    public ItemRequirement(boolean bank, Tuple2<Integer, Integer>... ItemIDAmpuntPair)
    {
        CheckBank = bank;
        Items     = ItemIDAmpuntPair;
    }

    /**
     * @return
     */
    @Override
    public IRequirement.RequirementType GetRequirementType()
    {
        return IRequirement.RequirementType.Item;
    }

    /**
     * @return
     */
    @Override
    public boolean isRequirementMet()
    {
        boolean[] Checks = new boolean[Items.length];
        for(int i = 0; i < Items.length; i++)
        {
            var item = Items[i];
            if(CheckBank)
            {
                if(item._2 <= 0)
                {
                    if(Bank.count(item._1) < Math.abs(item._2))
                    {
                        Checks[i] = true;
                        continue;
                    }
                }
                else if(Bank.count(item._1) > item._2)
                {
                    Checks[i] = true;
                    continue;
                }
            }
            if(item._2 <= 0)
            {
                if(Inventory.count(item._1) < Math.abs(item._2))
                {
                    Checks[i] = true;
                    continue;
                }
            }
            else if(Inventory.count(item._1) > item._2)
            {
                Checks[i] = true;
                continue;
            }
        }

        for(var check : Checks)
        {
            if(!check)
            {
                return false;
            }
        }
        return true;
    }
}
