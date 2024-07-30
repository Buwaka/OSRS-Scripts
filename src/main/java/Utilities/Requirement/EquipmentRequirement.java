package Utilities.Requirement;

public class EquipmentRequirement implements IRequirement
{
    public EquipmentRequirement()
    {
        //TODO
    }

    /**
     * @return
     */
    @Override
    public RequirementType GetRequirementType()
    {
        return RequirementType.Equipment;
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
