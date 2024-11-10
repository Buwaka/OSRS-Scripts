package Scripts.CommissionScripts.FullDragon;

import Cycles.Combat.SlayerCycle;
import Cycles.CycleGenerators.MeleeTrainerCycleGenerator;
import Cycles.Tasks.SimpleTasks.Combat.LootAreaTask;
import Cycles.Tasks.SimpleTasks.ItemProcessing.InteractInventoryTask;
import JFrames.EquipmentConfiguration;
import JFrames.FrameUtilities;
import OSRSDatabase.MonsterDB;
import OSRSDatabase.OSRSPrices;
import Utilities.OSRSUtilities;
import Utilities.Requirement.IRequirement;
import Utilities.Requirement.LevelRequirement;
import Utilities.Scripting.*;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.combat.CombatStyle;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.NPC;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;


@ScriptManifest(category = Category.COMBAT, name = "FullDragon's Melee Trainer", description = "Kill NPCs", author = "Semanresu", version = 0.1)
public class FDMeleeTrainer extends IFScript
{
    @Override
    public void onStart()
    {
        var form = new EquipmentConfiguration();
        var frame = FrameUtilities.OpenGui("Melee Trainer Config", form.GetForm());

        form.onSave.Subscribe(this, (parameters) -> {
            FrameUtilities.CloseGui(frame);
            //MeleeTrainerCycleGenerator.MeleeTrainerData parameters = null;
            //parameters.ChooseCombatStyle = this::ChooseCombatStyle;
            MeleeTrainerCycleGenerator generator = new MeleeTrainerCycleGenerator();
            //AddCycle(() -> generator.Generate(parameters));
            Resume();
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
            SLCycle.onKill.Subscribe(this, (cycle, npc) ->
            {
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
                    NextChoice.put(att + OSRSUtilities.rand.nextInt(0, meleeTrainerData.AttackGoal), CombatStyle.ATTACK);
                }

                var str = Skills.getRealLevel(Skill.STRENGTH);
                if(str < meleeTrainerData.StrengthGoal)
                {
                    NextChoice.put(str + OSRSUtilities.rand.nextInt(0, meleeTrainerData.StrengthGoal), CombatStyle.STRENGTH);
                }

                var def = Skills.getRealLevel(Skill.DEFENCE);
                if(def < meleeTrainerData.DefenceGoal)
                {
                    NextChoice.put(def + OSRSUtilities.rand.nextInt(0, meleeTrainerData.DefenceGoal), CombatStyle.DEFENCE);
                }

                if(cycle instanceof SlayerCycle SLcycle)
                {
                    var skill = NextChoice.firstEntry().getValue();
                    Logger.warn("FDMeleeTrainer: ChooseCombatStyle: Current: " + Combat.getCombatStyle() + " vs " + skill);
                    SLcycle.setCombatStyle(skill);
                }
            });
        }
    }
}
