package Cycles.CycleGenerators;

import Cycles.Combat.SlayerCycle;
import Cycles.Tasks.SimpleTasks.Combat.LootAreaTask;
import Cycles.Tasks.SimpleTasks.ItemProcessing.InteractInventoryTask;
import OSRSDatabase.ItemDB;
import OSRSDatabase.MonsterDB;
import OSRSDatabase.OSRSPrices;
import Utilities.OSRSUtilities;
import Utilities.Patterns.Runables.Runable2;
import Utilities.Requirement.IRequirement;
import Utilities.Requirement.LevelRequirement;
import Utilities.Scripting.ICycle;
import Utilities.Scripting.Logger;
import Utilities.Scripting.SimpleCycle;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.wrappers.interactive.NPC;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

public class MeleeTrainerCycleGenerator implements CycleGenerator
{

    public class MeleeTrainerData extends CycleData implements Serializable
    {
        @Serial
        private static final long serialVersionUID = -1420834627078592327L;

        public int                AttackGoal;
        public int                StrengthGoal;
        public int                DefenceGoal;
        public List<TrainingArea> TrainingAreas = new ArrayList<>();
        public int MinimumGPLoot          = 100;
        public int MinimumGPStackableLoot = 1;
        public boolean PrayBones;
        public Runable2<SimpleCycle, MeleeTrainerData> ChooseCombatStyle = null;
        public  Runable2<SimpleCycle, MeleeTrainerData> LootKillStyle = null;
        public  Runable2<SimpleCycle, MeleeTrainerData> ExtraActions = null;


        public class TrainingArea
        {
            public Area   area;
            public String targetName;
            List<IRequirement> requirements = new ArrayList<>();
        }
    }

    @Override
    public SimpleCycle[] Generate(@Nullable CycleGenerator.CycleData param)
    {
        if(param instanceof MeleeTrainerData meleeData)
        {
            List<IRequirement> Goals = new ArrayList<IRequirement>();
            Goals.add(new LevelRequirement(Skill.ATTACK, meleeData.AttackGoal));
            Goals.add(new LevelRequirement(Skill.STRENGTH, meleeData.StrengthGoal));
            Goals.add(new LevelRequirement(Skill.DEFENCE, meleeData.DefenceGoal));

            Area  KillingArea = new Area(new Tile(3183, 3300, 0), new Tile(3171, 3290, 0));
            int[] TargetIDs   = MonsterDB.GetMonsterIDsByName("Chicken", false);

            var cycle = new SlayerCycle("Kill Chickens", KillingArea, TargetIDs); // TODO
            cycle.AddGoal(Goals);
            cycle.SetCycleType(ICycle.CycleType.byGoal);
            //        cycle.onLevelUp.Subscribe(this, this::ChooseCombatStyle);
            //        cycle.onEXPGained.Subscribe(this, this::ChooseCombatStyle);

            if(meleeData.ChooseCombatStyle == null)
            {
                cycle.onKill.Subscribe(this, this::DefaultChooseCombatStyle);
            }
            else
            {
                meleeData.ChooseCombatStyle.Run(cycle, meleeData);
            }

            if(meleeData.LootKillStyle == null)
            {
                cycle.onKill.Subscribe(this, (c, npc) -> DefaultLootKills(meleeData, c, npc));
            }
            else
            {
                meleeData.LootKillStyle.Run(cycle, meleeData);
            }

            if(meleeData.PrayBones)
            {
                cycle.onKill.Subscribe(this, (c, npc) -> {
                    Logger.log("FDMeleeTrainer: Pray");
                    if(OSRSUtilities.InventoryContainsPrayables())
                    {
                        c.AddNode(InteractInventoryTask.PrayBonesAndAshes());
                    }
                });
            }

            return new SlayerCycle[]{cycle};
        }

        Logger.error("MeleeTrainerCycleGenerator: Generate: failed to read data, something went wrong, " + param);
        return null;
    }

    @Override
    public String GetName()
    {
        return "Melee Trainer";
    }

    @Override
    public Set<CycleRequirement> GetRequirements()
    {
        return Set.of();
    }

    @Override
    public EnumSet<ItemDB.Skill> GetTags()
    {
        return EnumSet.of(ItemDB.Skill.ATTACK,
                          ItemDB.Skill.STRENGTH,
                          ItemDB.Skill.DEFENCE,
                          ItemDB.Skill.HITPOINTS);
    }

    void DefaultChooseCombatStyle(SimpleCycle cycle, NPC npc)
    {
        // TODO
    }

    void DefaultLootKills(MeleeTrainerData data, SimpleCycle cycle, NPC npc)
    {
        Logger.log("FDMeleeTrainer: LootKills");
        var task = new LootAreaTask("Loot from " + npc.getName(), npc.getTile().getArea(3));
        task.setLootChecker((loot) -> {
            Integer price = OSRSPrices.GetLatestPrice(loot.getID());
            Logger.info("LootKills: prices: " + OSRSPrices.GetLatestPrice(loot.getID()) + " > " +
                        data.MinimumGPStackableLoot + " | " + data.MinimumGPLoot);
            if(loot.getItem().isStackable() && price != null && price > data.MinimumGPStackableLoot)
            {
                return true;
            }
            if(price != null && price > data.MinimumGPLoot)
            {
                return true;
            }
            return false;
        });

        cycle.AddNode(task);
    }
}
