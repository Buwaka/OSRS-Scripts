package Utilities.Requirement;

import io.vavr.Tuple2;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.utilities.Logger;

import java.io.Serial;
import java.util.Arrays;

public class ItemRequirement implements IRequirement
{
    @Serial
    private static final long serialVersionUID = -7005824736241585885L;
    boolean CheckBank;

    Tuple2<Integer, Integer>[] Items;


    /**
     * @param bank             Whether to check the bank as well
     * @param ItemIDAmpuntPair ID and amount set, negative amount for reverse requirement (aka item count needs to be less than count)
     */
    public ItemRequirement(boolean bank, Tuple2<Integer, Integer>... ItemIDAmpuntPair)
    {
        CheckBank = bank;
        Logger.log("ItemRequirement: " + Arrays.toString(ItemIDAmpuntPair));
        Items = ItemIDAmpuntPair;
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
        Arrays.fill(Checks, false);
        Logger.log("ItemRequirement: isRequirementMet: CheckBank: " + CheckBank);
        for(int i = 0; i < Items.length; i++)
        {
            var item = Items[i];
            if(CheckBank)
            {
                if(item._2 <= 0)
                {
                    if(Bank.count(item._1) <= Math.abs(item._2))
                    {
                        Logger.log(
                                "ItemRequirement: isRequirementMet: less than requirement bank, true " +
                                item._1);
                        Logger.log("count: " + Bank.count(item._1) + " < " + Math.abs(item._2));
                        Checks[i] = true;
                        continue;
                    }
                }
                else if(Bank.count(item._1) > item._2)
                {
                    Logger.log(
                            "ItemRequirement: isRequirementMet: more than requirement bank, true " +
                            item._1);
                    Logger.log("count: " + Bank.count(item._1) + " > " + Math.abs(item._2));
                    Checks[i] = true;
                    continue;
                }
            }
            else if(item._2 <= 0)
            {
                if(Inventory.count(item._1) <= Math.abs(item._2))
                {
                    Logger.log(
                            "ItemRequirement: isRequirementMet: less than requirement Inventory, true " +
                            item._1);
                    Logger.log("count: " + Inventory.count(item._1) + " < " + Math.abs(item._2));
                    Checks[i] = true;
                    continue;
                }
            }
            else if(Inventory.count(item._1) > item._2)
            {
                Logger.log(
                        "ItemRequirement: isRequirementMet: more than requirement Inventory, true " +
                        item._1);
                Logger.log("count: " + Inventory.count(item._1) + " > " + item._2);
                Checks[i] = true;
                continue;
            }
        }

        for(var check : Checks)
        {
            if(!check)
            {
                Logger.log("ItemRequirement: failed a check, exiting, false");
                return false;
            }
        }
        Logger.log("ItemRequirement: Succeeded all checks, exiting, true");
        return true;
    }
}
