package Utilities.Requirement;

import java.io.Serializable;

public interface IRequirement extends Serializable
{
    enum RequirementType
    {
        Equipment,
        Favor,
        Item,
        Kill,
        Kudos,
        Level,
        QuestPoint,
        Quest
    }

    boolean isRequirementMet();

    RequirementType GetRequirementType();
}
