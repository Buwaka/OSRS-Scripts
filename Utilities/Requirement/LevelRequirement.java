package Utilities.Requirement;

import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;

public class LevelRequirement implements IRequirement
{
    Skill   skill;
    int     level;
    Integer maxLevel = null;

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
    public boolean isRequirementMet()
    {
        if(maxLevel != null)
        {
            return Skills.getRealLevel(skill) >= level && Skills.getRealLevel(skill) < maxLevel;
        }
        return Skills.getRealLevel(skill) >= level;
    }

    /**
     * @return
     */
    @Override
    public IRequirement.RequirementType GetRequirementType()
    {
        return IRequirement.RequirementType.Level;
    }


}
