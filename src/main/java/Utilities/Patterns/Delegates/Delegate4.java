package Utilities.Patterns.Delegates;

import io.vavr.Function4;

import java.util.WeakHashMap;

public class Delegate4<A, B, C, D>
{
    WeakHashMap<Object, Function4<A, B, C, D, Boolean>> Subscribers = new WeakHashMap<>();

    public void Fire(A var1, B var2, C var3, D var4)
    {
        for(var func : Subscribers.entrySet())
        {
            if(func.getValue() != null)
            {
                func.getValue().apply(var1, var2, var3, var4);
            }
        }
    }

    public void Subscribe(Object caller, Function4<A, B, C, D, Boolean> function)
    {
        Subscribers.put(caller, function);
    }

    public int SubscribeCount()
    {
        return Subscribers.size();
    }
}