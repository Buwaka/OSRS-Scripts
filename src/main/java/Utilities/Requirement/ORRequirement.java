package Utilities.Requirement;

import org.dreambot.api.utilities.Logger;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

public class ORRequirement implements IRequirement
{
    @Serial
    private static final long serialVersionUID = 8672484086162215074L;
    List<IRequirement> Requirements = new ArrayList<>();

    public ORRequirement(IRequirement... reqs)
    {
        if(reqs != null)
        {
            Requirements.addAll(List.of(reqs));
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
        for(var req : Requirements)
        {
            Logger.log("ORRequirements: isRequirementMet: " + req.toString());
            if(req.isRequirementMet())
            {
                Logger.log("ORRequirements: isRequirementMet: true");
                return true;
            }
        }
        return false;
    }
}
