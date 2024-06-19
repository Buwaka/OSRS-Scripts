package Utilities.Patterns.Delegates;


import org.dreambot.api.utilities.Logger;

import java.util.WeakHashMap;

public class Delegate
{
    WeakHashMap<Object, Runnable> Subscribers = new WeakHashMap<>();

    public void Subscribe(Object caller, Runnable function)
    {
        Logger.log(caller + " Current Size " + Subscribers.size());
        Subscribers.put(caller, function);
    }

    public void Fire()
    {
        for(var pair : Subscribers.entrySet())
        {
            pair.getValue().run();
        }
    }

    public int SubscribeCount()
    {
        return Subscribers.size();
    }
}
