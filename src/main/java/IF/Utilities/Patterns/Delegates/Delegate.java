package IF.Utilities.Patterns.Delegates;


import IF.Utilities.Scripting.Logger;
import org.dreambot.api.utilities.Sleep;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class Delegate
{
    WeakHashMap<Object, List<Runnable>> Subscribers = new WeakHashMap<>();
    AtomicBoolean                       WaitForFire = new AtomicBoolean(false);

    public void Fire()
    {
        WaitForFire.set(false);
        for(var pair : Subscribers.values())
        {
            for(var valueList : pair)
            {
                valueList.run();
            }
        }
    }

    public void Subscribe(Object caller, Runnable function)
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
