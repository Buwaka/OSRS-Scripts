package Scripts.CommissionScripts;

import Cycles.Combat.SlayerCycle;
import Cycles.Tasks.SimpleTasks.Combat.LootAreaTask;
import Cycles.Tasks.SimpleTasks.ItemProcessing.InteractInventoryTask;
import OSRSDatabase.MonsterDB;
import OSRSDatabase.OSRSPrices;
import Utilities.OSRSUtilities;
import Utilities.Requirement.IRequirement;
import Utilities.Requirement.LevelRequirement;
import Utilities.Scripting.ICycle;
import Utilities.Scripting.IFScript;
import Utilities.Scripting.Logger;
import Utilities.Scripting.SimpleCycle;
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


@ScriptManifest(category = Category.COMBAT, name = "Melee Trainer", description = "Kill NPCs", author = "Semanresu", version = 0.1)
public class MeleeTrainer extends IFScript
{
    int                ATTGoal                = 25;
    int                STRGoal                = 23;
    int                DEFGoal                = 26;
    List<IRequirement> Goals                  = new ArrayList<>();
    int                MinimumGPLoot          = 100;
    int                MinimumGPStackableLoot = 1;


    @Override
    public void onStart()
    {
        //        var results = ItemDB.GetAllItemKeywordMatch("2h sword", false);
        //        Logger.log(Arrays.toString(Arrays.stream(results).map((t) -> t.weapon).filter((t) -> t != null).map((t) -> t.weapon_type).toArray()));
        Goals.add(new LevelRequirement(Skill.ATTACK, ATTGoal));
        Goals.add(new LevelRequirement(Skill.STRENGTH, STRGoal));
        Goals.add(new LevelRequirement(Skill.DEFENCE, DEFGoal));

        Area  KillingArea = new Area(new Tile(3183, 3300, 0), new Tile(3171, 3290, 0));
        int[] TargetIDs   = MonsterDB.GetMonsterIDsByName("Chicken", false);

        var cycle = new SlayerCycle("Kill Chickens", KillingArea, TargetIDs);
        cycle.AddGoal(Goals);
        cycle.SetCycleType(ICycle.CycleType.byGoal);
        //        cycle.onLevelUp.Subscribe(this, this::ChooseCombatStyle);
        //        cycle.onEXPGained.Subscribe(this, this::ChooseCombatStyle);
        cycle.onKill.Subscribe(this, this::ChooseCombatStyle);
        cycle.onKill.Subscribe(this, this::LootKills);
        cycle.onKill.Subscribe(this, (c, npc) -> {
            Logger.log("MeleeTrainer: Pray");
            if(OSRSUtilities.InventoryContainsPrayables())
            {
                addNodes(InteractInventoryTask.PrayBonesAndAshes());
            }
        });
        AddCycle(() -> new SlayerCycle[]{cycle});

        super.onStart();
    }

    void LootKills(SimpleCycle cycle, NPC npc)
    {
        Logger.log("MeleeTrainer: LootKills");
        var task = new LootAreaTask("Loot from " + npc.getName(), npc.getTile().getArea(3));
        task.setLootChecker((loot) -> {
            Integer price = OSRSPrices.GetLatestPrice(loot.getID());
            Logger.info("LootKills: prices: " + OSRSPrices.GetLatestPrice(loot.getID()) + " > " +
                        MinimumGPStackableLoot + " | " + MinimumGPLoot);
            if(loot.getItem().isStackable() && price != null && price > MinimumGPStackableLoot)
            {
                return true;
            }
            if(price != null && price > MinimumGPLoot)
            {
                return true;
            }
            return false;
        });
        this.addNodes(task);
    }

    void ChooseCombatStyle(SimpleCycle cycle, NPC npc)
    {
        Logger.log("MeleeTrainer: ChooseCombatStyle");
        if(OSRSUtilities.rand.nextInt(100) < 95)
        {
            Logger.info("ChooseCombatStyle: decided not to change");
            return;
        }

        SortedMap<Integer, CombatStyle> NextChoice = new TreeMap();
        var                             att        = Skills.getRealLevel(Skill.ATTACK);
        if(att < ATTGoal)
        {
            NextChoice.put(att + OSRSUtilities.rand.nextInt(0, ATTGoal), CombatStyle.ATTACK);
        }

        var str = Skills.getRealLevel(Skill.STRENGTH);
        if(str < STRGoal)
        {
            NextChoice.put(str + OSRSUtilities.rand.nextInt(0, STRGoal), CombatStyle.STRENGTH);
        }

        var def = Skills.getRealLevel(Skill.DEFENCE);
        if(def < DEFGoal)
        {
            NextChoice.put(def + OSRSUtilities.rand.nextInt(0, DEFGoal), CombatStyle.DEFENCE);
        }

        if(cycle != null && cycle instanceof SlayerCycle SLcycle)
        {
            var skill = NextChoice.firstEntry().getValue();
            Logger.warn("Current: " + Combat.getCombatStyle() + " vs " + skill);
            SLcycle.setCombatStyle(skill);
        }
    }
}
