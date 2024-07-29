package Utilities.Requirement;

public class KillRequirement implements IRequirement
{
    public KillRequirement()
    {
        //TODO
    }

    /**
     * @return
     */
    @Override
    public IRequirement.RequirementType GetRequirementType()
    {
        return IRequirement.RequirementType.Kill;
    }

    /**
     * @return
     */
    @Override
    public boolean isRequirementMet()
    {
        return false;
    }
}
