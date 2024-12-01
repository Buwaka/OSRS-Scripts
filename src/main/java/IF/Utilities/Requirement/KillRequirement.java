package IF.Utilities.Requirement;

import java.io.Serial;

public class KillRequirement implements IRequirement
{
    @Serial
    private static final long serialVersionUID = 8664327205259346103L;

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
