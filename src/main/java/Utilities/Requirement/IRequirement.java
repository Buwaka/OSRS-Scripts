package Utilities.Requirement;

import Utilities.Serializers.RequirementSerializer;
import Utilities.Serializers.SerializableSupplier;
import Utilities.Serializers.SerializableSupplierSerializer;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import org.dreambot.api.methods.quest.book.FreeQuest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Logger;

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

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
                Logger.log("IRequirement: IsAllRequirementMet: requirement is not met, exiting, " + req);
                return false;
            }
        }
        Logger.log("IRequirement: IsAllRequirementMet: all requirements are met");
        return true;
    }

}

