package Utilities.Combat;

import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.Character;
import org.dreambot.api.wrappers.interactive.Player;

import java.util.HashMap;
import java.util.Map;

public class CombatManager
{
    private static final Map<Player, CombatManager> Managers     = new HashMap<>();
    public final         String                     AttackAction = "Attack";

    public static CombatManager GetInstance(Player player)
    {
        if(Managers.containsKey(player))
        {
            return Managers.get(player);
        }

        var manager = new CombatManager();
        Managers.put(player, manager);
        return manager;
    }

    public Character Fight(Character Foe)
    {
        // TODO cheeck for hitsplash instead of is interacting with
        if(Foe == null)
        {
            Logger.log("No Foe given");
            return null;
        }

        if(!Foe.interact(AttackAction))
        {
            Logger.log("Failed to attack: " + Foe);
            return null;
        }

        Sleep.sleepTicks(3);
        Logger.log("Attacking: " + Foe);

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

    public enum CombatStyle
    {
        Melee,
        Ranged,
        Magic
    }


}
