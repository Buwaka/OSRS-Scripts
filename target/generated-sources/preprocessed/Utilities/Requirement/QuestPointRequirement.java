package Utilities.Requirement;

import org.dreambot.api.methods.quest.Quests;

import java.io.Serial;

public class QuestPointRequirement implements IRequirement
{
    @Serial
    private static final long serialVersionUID = -7697006643976060405L;
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
