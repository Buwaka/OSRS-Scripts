package Utilities.Combat;

import OSRSDatabase.MonsterDB;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.magic.Spell;
import org.dreambot.api.methods.magic.cost.Rune;
import org.dreambot.api.methods.prayer.Prayer;
import org.dreambot.api.methods.prayer.Prayers;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.Character;
import org.dreambot.api.wrappers.interactive.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CombatManager
{
    private static final Map<Integer, CombatManager> Managers                 = new HashMap<>();
    private final        String                      AttackAction             = "Attack";
    public               int                         MinimumHealth            = 5;
    public               int                         MinimumArrows            = 2000;
    public               int                         MinimumSpellCount        = 200;
    public               boolean                     FleeWhenOutOfRunesArrows = false; // TODO
    private              MonsterDB.MonsterData[]     Targets                  = null;
    private              CombatStyle                 Style                    = CombatStyle.Melee;
    private              List<Prayer>                PrayersToUse             = new ArrayList<>();
    private              List<Spell>                 SpellsToUse              = new ArrayList<>();
    private              Runnable                    FightLoop                = null; // Are we going to do a loop inside here? honestly should probably be inside the task and this as layer inbetween

    //public EquipmentManager MeleeManager = new EquipmentManager();

    //TODO use potions
    //TODO function that gets requirements, such as arrows (attach lambda to oncomplete to wield arrows), food, potions


    public enum CombatStyle
    {
        Melee,
        Ranged,
        Magic
    }

    public void AddPrayers(Prayer... prayers)
    {
        if(prayers.length > 0)
        {
            PrayersToUse.addAll(List.of(prayers));
        }
    }

    public void AddSpells(Spell... spells)
    {
        if(spells.length > 0)
        {
            SpellsToUse.addAll(List.of(spells));
        }
    }

    public void End()
    {
        for(var prayer : PrayersToUse)
        {
            Prayers.toggle(false, prayer);
        }
    }

    public Character Fight(Character Foe)
    {
        // TODO cheeck for hitsplash instead of is interacting with
        if(Foe == null)
        {
            Logger.log("CombatManager: Fight: No Foe given");
            return null;
        }

        if(Foe.isInteracting(Players.getLocal()))
        {
            if(!Players.getLocal().isHealthBarVisible() && !Players.getLocal().isMoving())
            {
                Logger.log(
                        "CombatManager: Fight: Interacting without healthbar or movement fallback");
                Walking.walk(Foe.getTile());
                return null;
            }
            Logger.log("CombatManager: Fight: Already Interacting with this character");
            return null;
        }

        if(!Foe.interact(AttackAction))
        {
            Logger.log("CombatManager: Fight:Failed to attack: " + Foe);
            return null;
        }

        Logger.log("CombatManager: Fight: Attacking: " + Foe);

        return Foe;
    }

    public boolean FightInteractingCharacter(int Timeout)
    {
        Character Foe = Players.getLocal().getInteractingCharacter();
        if(Foe == null)
        {
            return false;
        }
        if(!Players.getLocal().isInCombat())
        {
            Foe.interact(AttackAction);
        }
        return Sleep.sleepUntil(() -> Players.getLocal().isInCombat(), Timeout);
    }

    public void Init(CombatStyle style, MonsterDB.MonsterData... targets)
    {
        Style = style;
        if(targets != null && targets.length > 0)
        {
            Targets = targets;
        }
    }

    public void Start()
    {
        for(var prayer : PrayersToUse)
        {
            Prayers.toggle(true, prayer);
        }
    }

    public boolean isReady()
    {
        if(!PrayersToUse.isEmpty() && !isFullPrayer())
        {
            return false;
        }

        if(Style == CombatStyle.Ranged && !isArrowsLoaded())
        {
            return false;
        }

        if(Style == CombatStyle.Magic && !isRunesLoaded())
        {
            return false;
        }

        if(!isFullHealth())
        {
            return false;
        }

        return true;
    }

    public boolean isArrowsLoaded()
    {
        var item = Equipment.getItemInSlot(EquipmentSlot.ARROWS);
        return item != null && item.getAmount() > MinimumArrows;
    }

    public boolean isFullHealth()
    {
        return Skills.getBoostedLevel(Skill.HITPOINTS) >= Skills.getRealLevel(Skill.HITPOINTS);
    }

    public boolean isFullPrayer()
    {
        return Skills.getBoostedLevel(Skill.PRAYER) >= Skills.getRealLevel(Skill.PRAYER);
    }

    public boolean isRunesLoaded()
    {
        for(var spell : SpellsToUse)
        {
            for(Rune rune : spell.getCost())
            {
                if(Inventory.count(rune.getName()) < rune.getAmount() * MinimumSpellCount)
                {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isSafe()
    {
        //var foes = NPCs.all(Arrays.stream(Targets).map(t -> t.id).toArray(Integer[]::new));
        for(var target : Targets)
        {
            if(NPCs.closest(target.id) != null &&
               target.max_hit >= Skills.getBoostedLevel(Skill.HITPOINTS))
            {
                return false;
            }
        }
        return true;
    }

    public static CombatManager GetInstance(Player player)
    {
        if(Managers.containsKey(player.hashCode()))
        {
            return Managers.get(player.hashCode());
        }

        var manager = new CombatManager();
        Managers.put(player.hashCode(), manager);
        return manager;
    }
}
