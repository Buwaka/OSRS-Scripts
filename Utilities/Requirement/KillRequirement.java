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
        return IRequirement.RequirementType.Kill;
    }
}
