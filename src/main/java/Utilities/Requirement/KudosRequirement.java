package Utilities.Requirement;

import java.io.Serial;

public class KudosRequirement implements IRequirement
{
    @Serial
    private static final long serialVersionUID = 3339472580663162929L;

    public KudosRequirement()
    {
        //TODO
    }

    /**
     * @return
     */
    @Override
    public IRequirement.RequirementType GetRequirementType()
    {
        return IRequirement.RequirementType.Kudos;
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
