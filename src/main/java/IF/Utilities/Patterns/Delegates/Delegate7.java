package IF.Utilities.Patterns.Delegates;


import IF.Utilities.Patterns.Runnables.Runnable6;
import IF.Utilities.Patterns.Runnables.Runnable7;
import org.dreambot.api.utilities.Sleep;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class Delegate7<A, B, C, D, E, F, G>
{
    WeakHashMap<Object, List<Runnable7<A, B,C, D, E,F, G>>> Subscribers = new WeakHashMap<>();
    AtomicBoolean                                           WaitForFire = new AtomicBoolean(false);
    public void Fire(A var1, B var2, C var3, D var4, E var5, F var6, G var7)
    {
        WaitForFire.set(false);
        for(var funclist : Subscribers.values())
        {
            for(var func : funclist)
            {
                if(func != null)
                {
                    func.Run(var1, var2, var3, var4, var5, var6, var7);
                }
            }
        }
    }

    public void Subscribe(Object caller, Runnable7<A, B, C, D, E, F, G> function)
    {
        Subscribers.putIfAbsent(caller, new ArrayList<>());
        Subscribers.get(caller).add(function);
    }

    public int SubscribeCount()
    {
        return Subscribers.size();
    }

    public boolean WaitForChange(int timeout)
    {
        if(!WaitForFire.get())
        {
            WaitForFire.set(true);
        }
        return Sleep.sleepUntil(() -> !WaitForFire.get(), timeout);
    }
}