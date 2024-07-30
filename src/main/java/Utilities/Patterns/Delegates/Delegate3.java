package Utilities.Patterns.Delegates;

import io.vavr.Function4;
import org.dreambot.api.utilities.Logger;

import java.util.WeakHashMap;

public class Delegate3<A, B, C>
{
    WeakHashMap<Object, Function4<Object, A, B, C, Boolean>> Subscribers = new WeakHashMap<>();

    public void Fire(A var1, B var2, C var3)
    {
        Logger.log(Subscribers.entrySet());
        for(var pair : Subscribers.entrySet())
        {
            pair.getValue().apply(pair.getKey(), var1, var2, var3);
        }
    }

    public void Subscribe(Object caller, Function4<Object /*context*/, A, B, C, Boolean> function)
    {
        Logger.log(caller + " Current Size " + Subscribers.size());
        Subscribers.put(caller, function);
    }
}

