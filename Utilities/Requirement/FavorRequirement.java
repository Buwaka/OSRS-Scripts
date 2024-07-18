package Utilities.Requirement;

import org.dreambot.api.methods.favour.House;

public class FavorRequirement implements IRequirement
{
    House house;
    int   favour;

    public FavorRequirement(House house, int favour)
    {
        this.house  = house;
        this.favour = favour;
    }

    /**
     * @return
     */
    @Override
    public boolean isRequirementMet()
    {
        return favour >= house.getValue();
    }

    /**
     * @return
     */
    @Override
    public IRequirement.RequirementType GetRequirementType()
    {
        return IRequirement.RequirementType.Favor;
    }
}
