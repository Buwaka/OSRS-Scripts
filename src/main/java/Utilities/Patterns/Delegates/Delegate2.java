package Utilities.Patterns.Delegates;

import io.vavr.Function2;

import java.util.WeakHashMap;

public class Delegate2<A, B>
{
    WeakHashMap<Object, Function2<A, B, Boolean>> Subscribers = new WeakHashMap<>();

    public void Fire(A var1, B var2)
    {
        for(var func : Subscribers.entrySet())
        {
            if(func.getValue() != null)
            {
                func.getValue().apply(var1, var2);
            }
        }
    }

    public void Subscribe(Object caller, Function2<A, B, Boolean> function)
    {
        Subscribers.put(caller, function);
    }

    public int SubscribeCount()
    {
        return Subscribers.size();
    }
}