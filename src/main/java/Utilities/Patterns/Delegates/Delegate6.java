package Utilities.Patterns.Delegates;

import Utilities.Patterns.Runables.Runnable6;

import java.util.WeakHashMap;

public class Delegate6<A, B, C, D, E, F>
{
    WeakHashMap<Object, Runnable6<A, B, C, D, E, F>> Subscribers = new WeakHashMap<>();

    public void Fire(A var1, B var2, C var3, D var4, E var5, F var6)
    {
        for(var func : Subscribers.entrySet())
        {
            if(func.getValue() != null)
            {
                func.getValue().Run(var1, var2, var3, var4, var5, var6);
            }
        }
    }

    public void Subscribe(Object caller, Runnable6<A, B, C, D, E, F> function)
    {
        Subscribers.put(caller, function);
    }

    public int SubscribeCount()
    {
        return Subscribers.size();
    }
}