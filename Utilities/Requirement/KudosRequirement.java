package Utilities.Requirement;

public class KudosRequirement implements IRequirement
{
    public KudosRequirement()
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
        return IRequirement.RequirementType.Kudos;
    }

}
