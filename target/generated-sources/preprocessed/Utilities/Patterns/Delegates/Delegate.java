package Utilities.Patterns.Delegates;


import Utilities.Scripting.Logger;

import java.util.WeakHashMap;

public class Delegate
{
    WeakHashMap<Object, Runnable> Subscribers = new WeakHashMap<>();

    public void Fire()
    {
        for(var pair : Subscribers.entrySet())
        {
            pair.getValue().run();
        }
    }

    public void Subscribe(Object caller, Runnable function)
    {
        Logger.log(caller + " Current Size " + Subscribers.size());
        Subscribers.put(caller, function);
    }

    public int SubscribeCount()
    {
        return Subscribers.size();
    }
}
