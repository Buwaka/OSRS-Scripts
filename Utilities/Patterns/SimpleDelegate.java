package Utilities.Patterns;


import org.dreambot.api.utilities.Logger;

import java.util.ArrayList;
import java.util.List;

public class SimpleDelegate
{
    List<Runnable> Subscribers = new ArrayList<>();

    public boolean Subscribe(Runnable function)
    {
        Logger.log("Adding sub: " + function);
        return Subscribers.add(function);
    }

    public void Fire()
    {
        for(var func : Subscribers)
        {
            func.run();
        }
    }

    public int SubscribeCount() {
        return Subscribers.size();
    }
}
