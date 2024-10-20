package OSRSDatabase;

import DataBase.DataBaseUtilities;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;

import java.io.Serial;
import java.io.Serializable;


public class SkillsDB implements Serializable
{
    @Serial
    private static final long serialVersionUID = 593562862959996456L;

    static
    {
        DataBaseUtilities.RegisterPOJO(SkillsDB.class);
    }

    public int ATTACK       = Skills.getRealLevel(Skill.ATTACK);
    public int DEFENCE      = Skills.getRealLevel(Skill.DEFENCE);
    public int STRENGTH     = Skills.getRealLevel(Skill.STRENGTH);
    public int COMBAT       = Combat.getCombatLevel();
    public int HITPOINTS    = Skills.getRealLevel(Skill.HITPOINTS);
    public int RANGED       = Skills.getRealLevel(Skill.RANGED);
    public int PRAYER       = Skills.getRealLevel(Skill.PRAYER);
    public int MAGIC        = Skills.getRealLevel(Skill.MAGIC);
    public int COOKING      = Skills.getRealLevel(Skill.COOKING);
    public int WOODCUTTING  = Skills.getRealLevel(Skill.WOODCUTTING);
    public int FLETCHING    = Skills.getRealLevel(Skill.FLETCHING);
    public int FISHING      = Skills.getRealLevel(Skill.FISHING);
    public int FIREMAKING   = Skills.getRealLevel(Skill.FIREMAKING);
    public int CRAFTING     = Skills.getRealLevel(Skill.CRAFTING);
    public int SMITHING     = Skills.getRealLevel(Skill.SMITHING);
    public int MINING       = Skills.getRealLevel(Skill.MINING);
    public int HERBLORE     = Skills.getRealLevel(Skill.HERBLORE);
    public int AGILITY      = Skills.getRealLevel(Skill.AGILITY);
    public int THIEVING     = Skills.getRealLevel(Skill.THIEVING);
    public int SLAYER       = Skills.getRealLevel(Skill.SLAYER);
    public int FARMING      = Skills.getRealLevel(Skill.FARMING);
    public int RUNECRAFTING = Skills.getRealLevel(Skill.RUNECRAFTING);
    public int HUNTER       = Skills.getRealLevel(Skill.HUNTER);
    public int CONSTRUCTION = Skills.getRealLevel(Skill.CONSTRUCTION);

}
