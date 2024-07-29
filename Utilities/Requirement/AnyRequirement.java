package Utilities.Requirement;

import java.util.ArrayList;
import java.util.List;

public class AnyRequirement implements IRequirement
{
    List<IRequirement> Requirements = new ArrayList<>();


    AnyRequirement(IRequirement... requirements)
    {
        Requirements.addAll(List.of(requirements));
    }

    /**
     * @return
     */
    @Override
    public RequirementType GetRequirementType()
    {
        return RequirementType.Any;
    }

    /**
     * @return
     */
    @Override
    public boolean isRequirementMet()
    {
        return Requirements.stream().anyMatch(IRequirement::isRequirementMet);
    }
}
