package Utilities.Requirement;

public class QuestRequirement implements IRequirement
{
    private String QuestName;

    public QuestRequirement(String Name)
    {
        QuestName = Name;
    }

    /**
     * @return
     */
    @Override
    public boolean isRequirementMet()
    {
        return false;
    }

    /**
     * @return
     */
    @Override
    public RequirementType GetRequirementType()
    {
        return RequirementType.Quest;
    }
}
