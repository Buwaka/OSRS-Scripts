package Utilities.Patterns.Delegates;

import io.vavr.Function3;
import org.dreambot.api.utilities.Sleep;

import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class Delegate3<A, B, C>
{
    WeakHashMap<Object, Function3<A, B, C, Boolean>> Subscribers = new WeakHashMap<>();

    AtomicBoolean WaitForFire = new AtomicBoolean(false);

    public void Fire(A var1, B var2, C var3)
    {
        WaitForFire.set(false);
        for(var func : Subscribers.entrySet())
        {
            if(func.getValue() != null)
            {
                func.getValue().apply(var1, var2, var3);
            }
        }
    }

    public void Subscribe(Object caller, Function3<A, B, C, Boolean> function)
    {
        Subscribers.put(caller, function);
    }

    public int SubscribeCount()
    {
        return Subscribers.size();
    }

    public boolean WaitForChange(int timeout)
    {
        WaitForFire.set(true);
        return Sleep.sleepUntil(() -> !WaitForFire.get(), timeout);
    }
}

