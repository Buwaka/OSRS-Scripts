package Utilities.Patterns.Delegates;

import io.vavr.Function1;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

public class Delegate1<A>
{
    WeakHashMap<Object, Function1<A, Boolean>>          Subscribers = new WeakHashMap<>();

    public void Fire(A var1)
    {
        for(var func : Subscribers.entrySet())
        {
            if(func.getValue() != null)
            {
                func.getValue().apply(var1);
            }
        }
    }

    public void Subscribe(Object caller, Function1<A, Boolean> function)
    {
        Subscribers.put(caller, function);
    }

    public int SubscribeCount()
    {
        return Subscribers.size();
    }
}