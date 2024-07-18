package Utilities.Requirement;

import java.io.Serializable;

public interface IRequirement extends Serializable
{
    enum RequirementType
    {
        Equipment,
        Favor,
        Item,
        BankItem,
        Kill,
        Kudos,
        Level,
        QuestPoint,
        Quest,
        Lambda
    }

    boolean isRequirementMet();

    RequirementType GetRequirementType();
}
