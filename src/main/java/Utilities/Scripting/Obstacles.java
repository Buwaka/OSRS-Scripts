package Utilities.Scripting;

import org.dreambot.api.methods.walking.pathfinding.impl.local.LocalPathFinder;
import org.dreambot.api.methods.walking.pathfinding.impl.obstacle.impl.PassableObstacle;
import org.dreambot.api.utilities.Logger;

public class Obstacles
{
    public static void InitCustomObstacles()
    {
        Logger.log("Adding extra Obstacles");
        LocalPathFinder.getLocalPathFinder().addObstacle(new PassableObstacle("Web", "Slash"));
    }
}
