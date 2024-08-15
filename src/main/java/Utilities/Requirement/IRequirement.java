package Utilities.Requirement;

import Utilities.Serializers.RequirementSerializer;
import Utilities.Serializers.SerializableSupplier;
import Utilities.Serializers.SerializableSupplierSerializer;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import org.dreambot.api.methods.quest.book.FreeQuest;
import org.dreambot.api.methods.skills.Skill;

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
        for(var req : requirements)
        {
            if(req != null && req.isRequirementMet())
            {
                return true;
            }
        }
        return false;
    }

    static boolean IsAllRequirementMet(IRequirement... requirements)
    {
        for(var req : requirements)
        {
            if(req != null && !req.isRequirementMet())
            {
                return false;
            }
        }
        return true;
    }

}

