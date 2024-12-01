package IF.Utilities.Scripting.Listeners;

import IF.Utilities.Patterns.Delegates.Delegate1;
import IF.Utilities.Scripting.Logger;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.script.listener.HitSplatListener;
import org.dreambot.api.wrappers.interactive.Entity;
import org.dreambot.core.Instance;

public class GraveStoneListener implements HitSplatListener
{

    //public static final  int    GraveStoneNPCID = 10046;
    public Delegate1<Tile> onDeath = new Delegate1<>();

    public GraveStoneListener()
    {
        Logger.log("GraveStoneListener: registered!");
        Instance.getInstance().addEventListener(this);
        onDeath.Subscribe(this, this::GraveStoneLocation);
    }

    private boolean GraveStoneLocation(Tile tile)
    {
        Logger.log("GraveStoneListener: GraveStoneLocation: Gravestone is on tile " + tile);
        return false;
    }

    @Override
    public void onHitSplatAdded(Entity entity, int type, int damage, int id, int special, int gameCycle)
    {
        //        Logger.log("GraveStoneListener: onHitSplatAdded: " + entity.getName() + " " + damage + " " + id + " " + special + " " + gameCycle);
        //        Logger.log("GraveStoneListener: onHitSplatAdded: HPP " + Skills.getBoostedLevel(Skill.HITPOINTS));
        //        Logger.log("GraveStoneListener: onHitSplatAdded: isPlayer: " + isPlayer(entity));
        if(isPlayer(entity) && isDeath(damage))
        {
            Logger.log("GraveStoneListener: onHitSplatAdded: isDeath: " + isDeath(damage));
            onDeath.Fire(Players.getLocal().getTile());
        }
    }

    private boolean isPlayer(Entity entity)
    {
        return entity.hashCode() == Players.getLocal().hashCode();
    }

    private boolean isDeath(int damage)
    {
        return Skills.getBoostedLevel(Skill.HITPOINTS) - damage <= 0;
    }
}
