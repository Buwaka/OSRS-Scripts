package Utilities.Requirement;

import org.dreambot.api.methods.quest.Quests;

public class QuestPointRequirement implements IRequirement
{
    int ReqQuestPoints;

    public QuestPointRequirement(int QuestPoints)
    {
        ReqQuestPoints = QuestPoints;
    }

    /**
     * @return
     */
    @Override
    public IRequirement.RequirementType GetRequirementType()
    {
        return IRequirement.RequirementType.QuestPoint;
    }

    /**
     * @return
     */
    @Override
    public boolean isRequirementMet()
    {
        return Quests.getQuestPoints() >= ReqQuestPoints;
    }
}
