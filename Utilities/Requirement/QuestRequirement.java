package Utilities.Requirement;

import org.dreambot.api.methods.quest.book.Quest;

public class QuestRequirement implements IRequirement
{
    private Quest quest;

    public QuestRequirement(Quest quest)
    {
        this.quest = quest;
    }

    /**
     * @return
     */
    @Override
    public RequirementType GetRequirementType()
    {
        return RequirementType.Quest;
    }

    /**
     * @return
     */
    @Override
    public boolean isRequirementMet()
    {
        return quest.isFinished();
    }
}
