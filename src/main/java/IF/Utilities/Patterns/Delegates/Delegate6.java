package IF.Utilities.Patterns.Delegates;

import IF.Utilities.Patterns.Runnables.Runnable5;
import IF.Utilities.Patterns.Runnables.Runnable6;
import org.dreambot.api.utilities.Sleep;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class Delegate6<A, B, C, D, E, F>
{
    WeakHashMap<Object, List<Runnable6<A, B,C, D, E,F>>> Subscribers = new WeakHashMap<>();
    AtomicBoolean                                        WaitForFire = new AtomicBoolean(false);
    public void Fire(A var1, B var2, C var3, D var4, E var5, F var6)
    {
        WaitForFire.set(false);
        for(var funclist : Subscribers.values())
        {
            for(var func : funclist)
            {
                if(func != null)
                {
                    func.Run(var1, var2, var3, var4, var5, var6);
                }
            }
        }
    }

    public void Subscribe(Object caller, Runnable6<A, B, C, D, E, F> function)
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