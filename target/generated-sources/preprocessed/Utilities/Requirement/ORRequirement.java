package Utilities.Requirement;

import java.io.Serial;

public class ORRequirement implements IRequirement
{
    @Serial
    private static final long serialVersionUID = 8672484086162215074L;
    IRequirement[] Requirements;

    public ORRequirement(IRequirement... reqs)
    {
        if(reqs != null)
        {
            Requirements = reqs;
        }
    }

    /**
     * @return
     */
    @Override
    public RequirementType GetRequirementType()
    {
        return RequirementType.OR;
    }

    /**
     * @return
     */
    @Override
    public boolean isRequirementMet()
    {
        return IRequirement.IsAnyRequirementMet(Requirements);
    }
}
