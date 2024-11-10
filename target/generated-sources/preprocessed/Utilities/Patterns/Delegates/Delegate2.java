package Utilities.Patterns.Delegates;

import Utilities.Patterns.Runables.Runable2;
import Utilities.Scripting.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

public class Delegate2<A, B>
{
    WeakHashMap<Object, List<Runable2<A, B>>> Subscribers = new WeakHashMap<>();

    public void Fire(A var1, B var2)
    {
        for(var funcs : Subscribers.entrySet())
        {
            if(funcs.getValue() != null)
            {
                for(var func : funcs.getValue())
                {
                    Logger.log("Delegate2: Fire: " + func);
                    func.Run(var1, var2);
                }
            }
        }
    }

    public void Subscribe(Object caller, Runable2<A, B> function)
    {
        Subscribers.putIfAbsent(caller, new ArrayList<>());
        Subscribers.get(caller).add(function);
    }

    public int SubscribeCount()
    {
        return Subscribers.size();
    }
}
