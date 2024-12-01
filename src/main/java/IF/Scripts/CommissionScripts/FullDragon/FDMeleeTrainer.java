package IF.Scripts.CommissionScripts.FullDragon;

import IF.Logic.Cycles.Combat.SlayerCycle;
import IF.Logic.Generators.MeleeTrainerCycleGenerator;
import IF.JFrames.FrameUtilities;
import IF.JFrames.MeleeTrainerConfig;
import IF.Utilities.OSRSUtilities;
import IF.Utilities.Scripting.Annotations.CommissionUnique;
import IF.Utilities.Scripting.Annotations.ScriptMetadata;
import IF.Utilities.Scripting.IFScript;
import IF.Utilities.Scripting.Logger;
import IF.Utilities.Scripting.SimpleCycle;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.combat.CombatStyle;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;

import javax.swing.*;
import java.util.SortedMap;
import java.util.TreeMap;


@ScriptMetadata(Name = "FullDragon's Melee Trainer", Description = "Kill NPCs")
public class FDMeleeTrainer extends IFScript
{
    public FDMeleeTrainer()
    {

    }

    @Override
    public void onStart()
    {
        Logger.log("MeleeTrainer start");
        SwingUtilities.invokeLater(() -> {
            var form  = new MeleeTrainerConfig();
            var frame = FrameUtilities.OpenGui("Melee Trainer Config", form.GetForm());

            form.onStart.Subscribe(this, (parameters) -> {
                FrameUtilities.CloseGui(frame);
                parameters.ChooseCombatStyle = this::ChooseCombatStyle;
                MeleeTrainerCycleGenerator generator = new MeleeTrainerCycleGenerator();
                AddCycle(() -> generator.Generate(parameters));
                Resume();
            });
        });

        Pause();

        super.onStart();


    }

    @Override
    public void onStart(String... params)
    {
        // file open link to melee data file
        MeleeTrainerCycleGenerator.MeleeTrainerData parameters = null; // check IFScripts folder by default, if path, follow path

        parameters.ChooseCombatStyle = this::ChooseCombatStyle;
        MeleeTrainerCycleGenerator generator = new MeleeTrainerCycleGenerator();

        AddCycle(() -> generator.Generate(parameters));


        super.onStart();
    }

    @CommissionUnique
    private void ChooseCombatStyle(SimpleCycle simpleCycle, MeleeTrainerCycleGenerator.MeleeTrainerData meleeTrainerData)
    {
        if(simpleCycle instanceof SlayerCycle SLCycle)
        {
            SLCycle.onKill.Subscribe(this, (cycle, npc) -> {
                Logger.log("FDMeleeTrainer: ChooseCombatStyle");
                if(OSRSUtilities.rand.nextInt(100) < 95)
                {
                    Logger.info("FDMeleeTrainer: ChooseCombatStyle: decided not to change");
                    return;
                }

                SortedMap<Integer, CombatStyle> NextChoice = new TreeMap();
                var                             att        = Skills.getRealLevel(Skill.ATTACK);
                if(att < meleeTrainerData.AttackGoal)
                {
                    NextChoice.put(att + OSRSUtilities.rand.nextInt(0, meleeTrainerData.AttackGoal),
                                   CombatStyle.ATTACK);
                }

                var str = Skills.getRealLevel(Skill.STRENGTH);
                if(str < meleeTrainerData.StrengthGoal)
                {
                    NextChoice.put(
                            str + OSRSUtilities.rand.nextInt(0, meleeTrainerData.StrengthGoal),
                            CombatStyle.STRENGTH);
                }

                var def = Skills.getRealLevel(Skill.DEFENCE);
                if(def < meleeTrainerData.DefenceGoal)
                {
                    NextChoice.put(
                            def + OSRSUtilities.rand.nextInt(0, meleeTrainerData.DefenceGoal),
                            CombatStyle.DEFENCE);
                }

                if(cycle instanceof SlayerCycle SLcycle)
                {
                    var skill = NextChoice.firstEntry().getValue();
                    Logger.warn("FDMeleeTrainer: ChooseCombatStyle: Current: " +
                                Combat.getCombatStyle() + " vs " + skill);
                    SLcycle.setCombatStyle(skill);
                }
            });
        }
    }
}
