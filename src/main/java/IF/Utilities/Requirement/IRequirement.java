package IF.Utilities.Requirement;

import IF.Utilities.Scripting.Logger;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public interface IRequirement extends Serializable
{
    enum RequirementType
    {
        Equipment,
        Favor,
        Item,
        Kill,
        Kudos,
        Level,
        CombatLevel,
        QuestPoint,
        Quest,
        Member,
        OR
    }

    @SerializedName("RequirementType")
    RequirementType GetRequirementType();

    static boolean IsAllRequirementMet(IRequirement... requirements)
    {
        if(requirements == null)
        {
            Logger.log("IRequirement: IsAllRequirementMet: requirements is null, exiting");
            return true;
        }

        for(var req : requirements)
        {
            if(req != null && !req.isRequirementMet())
            {
                Logger.log("IRequirement: IsAllRequirementMet: requirement is not met, exiting, " +
                           req);
                return false;
            }
        }
        Logger.log("IRequirement: IsAllRequirementMet: all requirements are met");
        return true;
    }

    boolean isRequirementMet();

    static boolean IsAnyRequirementMet(IRequirement... requirements)
    {
        if(requirements == null)
        {
            Logger.log("IRequirement: IsAnyRequirementMet: requirements is null, exiting");
            return true;
        }

        for(var req : requirements)
        {
            if(req != null && req.isRequirementMet())
            {
                Logger.log("IRequirement: IsAnyRequirementMet: true, " + req);
                return true;
            }
        }
        Logger.log("IRequirement: IsAnyRequirementMet: false, none are met");
        return false;
    }

}

