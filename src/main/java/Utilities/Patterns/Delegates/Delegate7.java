package Utilities.Patterns.Delegates;


import Utilities.Patterns.Runables.Runable7;

import java.util.WeakHashMap;

public class Delegate7<A, B, C, D, E, F, G>
{
    WeakHashMap<Object, Runable7<A, B, C, D, E, F, G>> Subscribers = new WeakHashMap<>();

    public void Fire(A var1, B var2, C var3, D var4, E var5, F var6, G var7)
    {
        for(var func : Subscribers.entrySet())
        {
            if(func.getValue() != null)
            {
                func.getValue().Run(var1, var2, var3, var4, var5, var6, var7);
            }
        }
    }

    public void Subscribe(Object caller, Runable7<A, B, C, D, E, F, G> function)
    {
        Subscribers.put(caller, function);
    }

    public int SubscribeCount()
    {
        return Subscribers.size();
    }
}