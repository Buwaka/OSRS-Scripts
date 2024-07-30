package Utilities.Requirement;

public class MemberRequirement implements IRequirement
{

    // TODO

    /**
     * @return
     */
    @Override
    public RequirementType GetRequirementType()
    {
        return RequirementType.Member;
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
