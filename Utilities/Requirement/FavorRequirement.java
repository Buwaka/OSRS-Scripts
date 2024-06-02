package Utilities.Requirement;

public class FavorRequirement implements IRequirement
{
    public FavorRequirement()
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
        return IRequirement.RequirementType.Favor;
    }
}
