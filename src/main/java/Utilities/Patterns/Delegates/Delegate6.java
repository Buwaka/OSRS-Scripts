package Utilities.Patterns.Delegates;

import io.vavr.Function6;

import java.util.WeakHashMap;

public class Delegate6<A, B, C, D, E, F>
{
    WeakHashMap<Object, Function6<A, B, C, D, E, F, Boolean>> Subscribers = new WeakHashMap<>();

    public void Fire(A var1, B var2, C var3, D var4, E var5, F var6)
    {
        for(var func : Subscribers.entrySet())
        {
            if(func.getValue() != null)
            {
                func.getValue().apply(var1, var2, var3, var4, var5, var6);
            }
        }
    }

    public void Subscribe(Object caller, Function6<A, B, C, D, E, F, Boolean> function)
    {
        Subscribers.put(caller, function);
    }

    public int SubscribeCount()
    {
        return Subscribers.size();
    }
}