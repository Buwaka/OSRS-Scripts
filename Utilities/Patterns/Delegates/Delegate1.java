package Utilities.Patterns.Delegates;

import io.vavr.Function1;

import java.util.ArrayList;
import java.util.List;

public class Delegate1<A>
{
    List<Function1<A, Void>> Subscribers = new ArrayList<Function1<A, Void>>();

    public boolean Subscribe(Function1<A, Void> function)
    {
        return Subscribers.add(function);
    }

    public void Fire(A var1)
    {
        for(var func : Subscribers)
        {
            func.apply(var1);
        }
    }
}