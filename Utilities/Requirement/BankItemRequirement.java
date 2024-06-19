package Utilities.Requirement;

import org.dreambot.api.methods.container.impl.bank.Bank;

public class BankItemRequirement implements IRequirement
{
    int ID;
    int Count;

    /**
     * @return
     */
    @Override
    public boolean isRequirementMet()
    {
        return Bank.count(ID) >= Count;
    }

    /**
     * @return
     */
    @Override
    public RequirementType GetRequirementType()
    {
        return RequirementType.BankItem;
    }
}
