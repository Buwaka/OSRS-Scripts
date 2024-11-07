package Utilities.Patterns.Delegates;

import Utilities.Patterns.Runables.Runnable1;

import java.util.WeakHashMap;

public class Delegate1<A>
{
    WeakHashMap<Object, Runnable1<A>> Subscribers = new WeakHashMap<>();

    public void Fire(A var1)
    {
        for(var func : Subscribers.entrySet())
        {
            if(func.getValue() != null)
            {
                func.getValue().Run(var1);
            }
        }
    }

    public void Subscribe(Object caller, Runnable1<A> function)
    {
        Subscribers.put(caller, function);
    }

    public int SubscribeCount()
    {
        return Subscribers.size();
    }
}