package IF.Utilities.Requirement;

import org.dreambot.api.methods.favour.House;

import java.io.Serial;

public class FavorRequirement implements IRequirement
{
    @Serial
    private static final long serialVersionUID = -3503527546478455791L;
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
    public IRequirement.RequirementType GetRequirementType()
    {
        return IRequirement.RequirementType.Favor;
    }

    /**
     * @return
     */
    @Override
    public boolean isRequirementMet()
    {
        return favour >= house.getValue();
    }
}
