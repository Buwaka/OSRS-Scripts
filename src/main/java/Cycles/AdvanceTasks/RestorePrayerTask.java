package Cycles.AdvanceTasks;

import Cycles.SimpleTasks.TravelTask;
import OSRSDatabase.PrayerDB;
import Utilities.Scripting.SimpleTask;
import org.dreambot.api.Client;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.walking.pathfinding.impl.local.LocalPathFinder;
import org.dreambot.api.utilities.Logger;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class RestorePrayerTask extends SimpleTask
{
    private TravelTask TravelToAltar = null;

    public RestorePrayerTask(String Name)
    {
        super(Name);
    }


    public PrayerDB.Altar GetChurch()
    {
        var Altars = PrayerDB.GetAltars(Client.isMembers());

        if(Altars.length == 0)
        {
            Logger.log("RestorePrayerTask: GetChurch: No Altars found");
            return null;
        }

        //        for(var altar : Altars)
        //        {
        // TODO do something to calculate the distance of the path including teleports and shit
        // hand it over to traveltask
        //WebPathQuery.builder().from(Players.getLocal().getTile()).to(altar.Location).build().calculate()
        //        }

        var playerpos = Players.getLocal().getTile();
        return Arrays.stream(Altars)
                     .min((x, y) -> (int) ((LocalPathFinder.getLocalPathFinder()
                                                           .getWalkingDistance(playerpos,
                                                                               y.Location) -
                                            LocalPathFinder.getLocalPathFinder()
                                                           .getWalkingDistance(playerpos,
                                                                               x.Location)) * 100))
                     .get();
    }

    @Nonnull
    @Override
    public TaskType GetTaskType()
    {
        return TaskType.RestorePrayer;
    }

    /**
     * @return
     */
    @Override
    public boolean Ready()
    {
        boolean NeedJesus =
                Skills.getBoostedLevel(Skill.PRAYER) < Skills.getRealLevel(Skill.PRAYER);
        return NeedJesus && super.Ready();
    }

    /**
     * @return
     */
    @Override
    protected int Loop()
    {
        return super.Loop();
    }
}
