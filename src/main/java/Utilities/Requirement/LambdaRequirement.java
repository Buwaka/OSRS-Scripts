package Utilities.Requirement;

import java.util.function.Supplier;

public interface LambdaRequirement extends Supplier<Boolean>, IRequirement
{
    /**
     * @return
     */
    @Override
    public default RequirementType GetRequirementType()
    {
        return RequirementType.Lambda;
    }

    /**
     * @return
     */
    @Override
    public default boolean isRequirementMet()
    {
        return this.get();
    }
}
