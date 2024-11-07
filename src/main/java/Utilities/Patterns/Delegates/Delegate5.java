package Utilities.Patterns.Delegates;

import Utilities.Patterns.Runables.Runnable5;

import java.util.WeakHashMap;

public class Delegate5<A, B, C, D, E>
{
    WeakHashMap<Object, Runnable5<A, B, C, D, E>> Subscribers = new WeakHashMap<>();

    public void Fire(A var1, B var2, C var3, D var4, E var5)
    {
        for(var func : Subscribers.entrySet())
        {
            if(func.getValue() != null)
            {
                func.getValue().Run(var1, var2, var3, var4, var5);
            }
        }
    }

    public void Subscribe(Object caller, Runnable5<A, B, C, D, E> function)
    {
        Subscribers.put(caller, function);
    }

    public int SubscribeCount()
    {
        return Subscribers.size();
    }
}