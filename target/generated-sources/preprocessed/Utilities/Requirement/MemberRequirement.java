package Utilities.Requirement;

import org.dreambot.api.Client;

import java.io.Serial;

public class MemberRequirement implements IRequirement
{
    @Serial
    private static final long serialVersionUID = 5829362324941237304L;

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
        return Client.isMembers();
    }
}
