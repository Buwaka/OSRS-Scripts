package Utilities.Requirement;

import OSRSDatabase.ItemDB;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;

import javax.annotation.Nullable;
import java.io.Serial;

public class LevelRequirement implements IRequirement
{
    @Serial
    private static final long serialVersionUID = 855390922845163754L;
    Skill skill;
    int   level;
    @Nullable
    Integer maxLevel = null;


    public LevelRequirement(ItemDB.Skill skill, int level)
    {
        this.skill = skill.GetDreamBotSkill();
        this.level = level;
    }

    public LevelRequirement(ItemDB.Skill skill, int level, int MaxLevel)
    {
        this.skill = skill.GetDreamBotSkill();
        this.level = level;
        maxLevel   = MaxLevel;
    }

    public LevelRequirement(Skill skill, int level)
    {
        this.skill = skill;
        this.level = level;
    }

    public LevelRequirement(Skill skill, int level, int MaxLevel)
    {
        this.skill = skill;
        this.level = level;
        maxLevel   = MaxLevel;
    }

    /**
     * @return
     */
    @Override
    public IRequirement.RequirementType GetRequirementType()
    {
        return IRequirement.RequirementType.Level;
    }

    /**
     * @return
     */
    @Override
    public boolean isRequirementMet()
    {
        if(maxLevel != null)
        {
            return Skills.getRealLevel(skill) >= level && Skills.getRealLevel(skill) < maxLevel;
        }
        return Skills.getRealLevel(skill) >= level;
    }


}
