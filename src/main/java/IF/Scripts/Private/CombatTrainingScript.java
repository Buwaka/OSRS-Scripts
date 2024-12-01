package IF.Scripts.Private;

import IF.Logic.Cycles.Combat.CombatLootBankCycle;
import IF.OSRSDatabase.ItemDB;
import IF.OSRSDatabase.MonsterDB;
import IF.Utilities.Scripting.IFScript;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

@ScriptManifest(name = "SoloScripts.CombatTrainingScript", description = "Kill stuff", author = "Semanresu", version = 1.0, category = Category.COMBAT, image = "")
public class CombatTrainingScript extends IFScript
{


    @Override
    public void onStart()
    {


        AddCycle(() -> {
            CombatLootBankCycle Cycle = new CombatLootBankCycle("Moss Giant slaughter",
                                                                new Area[]{
                                                                        new Area(3264,
                                                                                 3256,
                                                                                 3254,
                                                                                 3296),
                                                                        new Area(3253,
                                                                                 3296,
                                                                                 3244,
                                                                                 3281)},
                                                                MonsterDB.GetMonsterIDsByName("Cow",
                                                                                              true));
            Cycle.SetPray(true);
            Cycle.SetEscapeLowHP(false);
            Cycle.setIgnoreLoot(2132);

            int attackLV   = Skills.getRealLevel(Skill.ATTACK);
            int strengthLV = Skills.getRealLevel(Skill.STRENGTH);
            int defenseLV  = Skills.getRealLevel(Skill.DEFENCE);

            if(attackLV < strengthLV && attackLV < defenseLV)
            {
                Cycle.SetEXPType(ItemDB.StanceData.ExperienceType.attack);
            }
            else if(strengthLV < defenseLV)
            {
                Cycle.SetEXPType(ItemDB.StanceData.ExperienceType.strength);
            }
            else
            {
                Cycle.SetEXPType(ItemDB.StanceData.ExperienceType.defence);
            }


            return new CombatLootBankCycle[]{Cycle};
        });

        if(!Combat.isAutoRetaliateOn())
        {
            Combat.toggleAutoRetaliate(true);
        }


        super.onStart();
    }
}
