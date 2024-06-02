package Utilities.Requirement;

public class LevelRequirement implements IRequirement
{
    public LevelRequirement()
    {
        //TODO
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
    public IRequirement.RequirementType GetRequirementType()
    {
        return IRequirement.RequirementType.Level;
    }


}
